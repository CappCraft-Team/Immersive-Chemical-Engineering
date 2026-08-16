package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class HeatExchangerMultiblock extends TemplateMultiblock {

    private final HeatExchangerSizeVariants variant;
    private final Supplier<MultiblockRegistration<HeatExchangerState>> registration;
    private volatile TemplateData generatedTemplate;

    public HeatExchangerMultiblock(
            HeatExchangerSizeVariants variant,
            Supplier<MultiblockRegistration<HeatExchangerState>> registration
    ) {
        super(
                variant.getId(),
                variant.getMasterPos(),
                variant.getTriggerPos(),
                variant.getSize()
        );
        this.variant = variant;
        this.registration = registration;
    }

    @Nonnull
    @Override
    public List<StructureBlockInfo> getStructure(@Nonnull Level level) {
        BlockState radiator = IEBlocks.MetalDecoration.RADIATOR.get().defaultBlockState();
        BlockState sheetmetal = IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON).defaultBlockState();
        BlockPos size = variant.getSize();
        List<StructureBlockInfo> structure = new ArrayList<>(size.getX() * size.getY() * size.getZ());
        for (int y = 0; y < size.getY(); ++y) {
            for (int z = 0; z < size.getZ(); ++z) {
                for (int x = 0; x < size.getX(); ++x) {
                    BlockPos position = new BlockPos(x, y, z);
                    structure.add(new StructureBlockInfo(
                            position,
                            variant.isRadiator(position) ? radiator : sheetmetal,
                            null
                    ));
                }
            }
        }
        return List.copyOf(structure);
    }

    @Nonnull
    @Override
    public TemplateData getTemplate(@Nonnull Level level) {
        TemplateData current = generatedTemplate;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            current = generatedTemplate;
            if (current == null) {
                BlockPos size = variant.getSize();
                List<StructureBlockInfo> blocks = getStructure(level);
                CompoundTag serialized = new CompoundTag();
                serialized.put("size", integerList(size.getX(), size.getY(), size.getZ()));

                BlockState radiator = blocks.stream()
                        .filter(block -> variant.isRadiator(block.pos()))
                        .findFirst()
                        .orElseThrow()
                        .state();
                BlockState sheetmetal = blocks.stream()
                        .filter(block -> !variant.isRadiator(block.pos()))
                        .findFirst()
                        .orElseThrow()
                        .state();
                ListTag palette = new ListTag();
                palette.add(NbtUtils.writeBlockState(radiator));
                palette.add(NbtUtils.writeBlockState(sheetmetal));
                serialized.put("palette", palette);

                ListTag serializedBlocks = new ListTag();
                for (StructureBlockInfo block : blocks) {
                    CompoundTag blockTag = new CompoundTag();
                    blockTag.put("pos", integerList(
                            block.pos().getX(),
                            block.pos().getY(),
                            block.pos().getZ()
                    ));
                    blockTag.putInt("state", variant.isRadiator(block.pos()) ? 0 : 1);
                    serializedBlocks.add(blockTag);
                }
                serialized.put("blocks", serializedBlocks);
                serialized.put("entities", new ListTag());
                NbtUtils.addCurrentDataVersion(serialized);

                StructureTemplate template = new StructureTemplate();
                template.load(
                        level.registryAccess().lookup(Registries.BLOCK).orElseThrow(),
                        serialized
                );
                current = new TemplateData(template, blocks, radiator);
                generatedTemplate = current;
            }
        }
        return current;
    }

    private static ListTag integerList(int... values) {
        ListTag result = new ListTag();
        for (int value : values) {
            result.add(IntTag.valueOf(value));
        }
        return result;
    }

    @Nonnull
    @Override
    public Vec3i getSize(@Nonnull Level level) {
        return variant.getSize();
    }

    @Override
    public boolean canBeMirrored() {
        return variant.isMirrorable();
    }

    @Override
    public boolean createStructure(
            Level level,
            BlockPos position,
            Direction side,
            Player player
    ) {
        Direction formationSide = side.getAxis() == Direction.Axis.Y
                ? player.getDirection().getOpposite()
                : side;
        return super.createStructure(level, position, formationSide, player);
    }

    @Override
    protected void replaceStructureBlock(
            StructureBlockInfo originalBlock,
            Level level,
            BlockPos worldPosition,
            boolean mirrored,
            Direction clickDirection,
            Vec3i offsetFromMaster
    ) {
        MultiblockRegistration<HeatExchangerState> multiblock = registration.get();
        MultiblockPartBlock<HeatExchangerState> formedBlock = multiblock.block().get();
        BlockState formedState = formedBlock.defaultBlockState()
                .setValue(IEProperties.MULTIBLOCKSLAVE, !Vec3i.ZERO.equals(offsetFromMaster));
        if (formedState.hasProperty(IEProperties.FACING_HORIZONTAL)) {
            formedState = formedState.setValue(
                    IEProperties.FACING_HORIZONTAL,
                    clickDirection.getOpposite()
            );
        }
        if (formedState.hasProperty(IEProperties.MIRRORED)) {
            formedState = formedState.setValue(IEProperties.MIRRORED, mirrored);
        }

        BlockState oldState = level.getBlockState(worldPosition);
        level.setBlock(worldPosition, formedState, 0);
        if (level.getBlockEntity(worldPosition) instanceof MultiblockBlockEntityDummy<?> dummy) {
            dummy.getHelper().setPositionInMB(originalBlock.pos());
        } else if (!(level.getBlockEntity(worldPosition) instanceof MultiblockBlockEntityMaster<?>)) {
            ImmersiveChemicalEngineering.LOGGER.error(
                    "Expected a heat exchanger multiblock block entity at {} during formation",
                    worldPosition
            );
        }

        LevelChunk chunk = level.getChunkAt(worldPosition);
        level.markAndNotifyBlock(
                worldPosition,
                chunk,
                oldState,
                formedState,
                Block.UPDATE_ALL,
                512
        );
    }

    @Override
    protected void prepareBlockForDisassembly(Level level, BlockPos position) {
        if (level.getBlockEntity(position) instanceof IMultiblockBE<?> multiblockBE) {
            multiblockBE.getHelper().markDisassembling();
        }
    }

    @Override
    public float getManualScale() {
        return variant.getManualScale();
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) {
        BlockPos size = variant.getSize();
        int radiatorCount = 0;
        for (int y = 0; y < size.getY(); ++y) {
            for (int z = 0; z < size.getZ(); ++z) {
                for (int x = 0; x < size.getX(); ++x) {
                    if (variant.isRadiator(new BlockPos(x, y, z))) {
                        ++radiatorCount;
                    }
                }
            }
        }
        int sheetmetalCount = size.getX() * size.getY() * size.getZ() - radiatorCount;
        consumer.accept(new ClientMultiblocks.MultiblockManualData() {
            @Override
            public NonNullList<ItemStack> getTotalMaterials() {
                NonNullList<ItemStack> materials = NonNullList.create();
                materials.add(new ItemStack(
                        IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON),
                        sheetmetalCount
                ));
                materials.add(new ItemStack(
                        IEBlocks.MetalDecoration.RADIATOR.get(),
                        radiatorCount
                ));
                return materials;
            }

            @Override
            public boolean canRenderFormedStructure() {
                return false;
            }

            @Override
            public void renderFormedStructure(PoseStack poseStack, MultiBufferSource buffers) {
            }
        });
    }

    @Override
    public Component getDisplayName() {
        return registration.get().block().get().getName();
    }

    @Override
    public Block getBlock() {
        return registration.get().block().get();
    }

    @Override
    public ResourceLocation getUniqueName() {
        return variant.getId();
    }
}
