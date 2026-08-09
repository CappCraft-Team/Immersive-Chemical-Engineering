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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.client.manual.HeatExchangerManualData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Code-backed 5x2x2 legacy medium heat-exchanger formation template. */
public final class HeatExchangerMediumMultiblock extends TemplateMultiblock {

    /** Legacy offset-zero master: center-width, lower layer, rear depth row. */
    static final BlockPos MASTER_FROM_ORIGIN = new BlockPos(2, 0, 1);
    /** The hammer targets the upper radiator at one end of the rear row. */
    static final BlockPos TRIGGER_FROM_ORIGIN = new BlockPos(0, 1, 1);
    static final BlockPos SIZE = new BlockPos(5, 2, 2);

    private final Supplier<MultiblockRegistration<HeatExchangerMediumState>> registration;
    private volatile TemplateData generatedTemplate;

    public HeatExchangerMediumMultiblock(
            Supplier<MultiblockRegistration<HeatExchangerMediumState>> registration
    ) {
        super(
                ICHEME_Contents.HEAT_EXCHANGER_MEDIUM_ID,
                MASTER_FROM_ORIGIN,
                TRIGGER_FROM_ORIGIN,
                SIZE
        );
        this.registration = registration;
    }

    @Nonnull
    @Override
    public List<StructureBlockInfo> getStructure(@Nonnull Level level) {
        BlockState radiator = IEBlocks.MetalDecoration.RADIATOR.get().defaultBlockState();
        BlockState sheetmetal = IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON).defaultBlockState();
        List<StructureBlockInfo> structure = new ArrayList<>(20);
        for (int y = 0; y < SIZE.getY(); ++y) {
            for (int z = 0; z < SIZE.getZ(); ++z) {
                for (int x = 0; x < SIZE.getX(); ++x) {
                    structure.add(new StructureBlockInfo(
                            new BlockPos(x, y, z),
                            isRadiator(new BlockPos(x, y, z)) ? radiator : sheetmetal,
                            null
                    ));
                }
            }
        }
        return List.copyOf(structure);
    }

    static boolean isRadiator(BlockPos position) {
        return position.getX() == 0;
    }

    /** Supplies the real template data IE synchronizes to clients during login. */
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
                List<StructureBlockInfo> blocks = getStructure(level);
                CompoundTag serialized = new CompoundTag();
                serialized.put("size", integerList(SIZE.getX(), SIZE.getY(), SIZE.getZ()));

                BlockState radiator = blocks.get(0).state();
                BlockState sheetmetal = blocks.stream()
                        .filter(block -> !isRadiator(block.pos()))
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
                    blockTag.putInt("state", isRadiator(block.pos()) ? 0 : 1);
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
        return SIZE;
    }

    @Override
    public boolean canBeMirrored() {
        return true;
    }

    /** Preserve legacy top/bottom hammer clicks by using the player's horizontal direction. */
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
        MultiblockRegistration<HeatExchangerMediumState> multiblock = registration.get();
        MultiblockPartBlock<HeatExchangerMediumState> formedBlock = multiblock.block().get();
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
                    "Expected a medium heat exchanger multiblock block entity at {} during formation",
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
        return 16.0F;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) {
        consumer.accept(HeatExchangerManualData.of(16, 4));
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
        return ICHEME_Contents.HEAT_EXCHANGER_MEDIUM_ID;
    }
}
