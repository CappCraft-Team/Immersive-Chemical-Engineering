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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The formation template for the small heat exchanger.
 *
 * <p>The structure is intentionally code-backed for this small 1x1x4 shape. It still uses IE's
 * public {@link TemplateMultiblock} formation and disassembly machinery, while avoiding a binary
 * structure file for four simple blocks.</p>
 */
public final class HeatExchangerSmallMultiblock extends TemplateMultiblock {

    static final BlockPos MASTER_FROM_ORIGIN = BlockPos.ZERO;
    private static final BlockPos TRIGGER_FROM_ORIGIN = BlockPos.ZERO;
    private static final BlockPos SIZE = new BlockPos(1, 1, 4);

    private final Supplier<MultiblockRegistration<HeatExchangerSmallState>> registration;
    private volatile TemplateData generatedTemplate;

    public HeatExchangerSmallMultiblock(
            Supplier<MultiblockRegistration<HeatExchangerSmallState>> registration
    ) {
        super(
                ICHEME_Contents.HEAT_EXCHANGER_SMALL_ID,
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
        return List.of(
                new StructureBlockInfo(new BlockPos(0, 0, 0), radiator, null),
                new StructureBlockInfo(new BlockPos(0, 0, 1), sheetmetal, null),
                new StructureBlockInfo(new BlockPos(0, 0, 2), sheetmetal, null),
                new StructureBlockInfo(new BlockPos(0, 0, 3), radiator, null)
        );
    }

    /**
     * IE synchronizes every registered template to clients at login. Supplying a populated
     * template here keeps the code-backed structure compatible with that synchronization path.
     */
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

                ListTag palette = new ListTag();
                palette.add(NbtUtils.writeBlockState(blocks.get(0).state()));
                palette.add(NbtUtils.writeBlockState(blocks.get(1).state()));
                serialized.put("palette", palette);

                ListTag serializedBlocks = new ListTag();
                for (int index = 0; index < blocks.size(); ++index) {
                    StructureBlockInfo block = blocks.get(index);
                    CompoundTag blockTag = new CompoundTag();
                    blockTag.put("pos", integerList(
                            block.pos().getX(),
                            block.pos().getY(),
                            block.pos().getZ()
                    ));
                    blockTag.putInt("state", index == 0 || index == 3 ? 0 : 1);
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
                current = new TemplateData(template, blocks, blocks.get(0).state());
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
        return false;
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
        MultiblockRegistration<HeatExchangerSmallState> multiblock = registration.get();
        MultiblockPartBlock<HeatExchangerSmallState> formedBlock = multiblock.block().get();
        BlockState formedState = formedBlock.defaultBlockState()
                .setValue(IEProperties.MULTIBLOCKSLAVE, !Vec3i.ZERO.equals(offsetFromMaster));
        if (formedState.hasProperty(IEProperties.FACING_HORIZONTAL)) {
            formedState = formedState.setValue(IEProperties.FACING_HORIZONTAL, clickDirection.getOpposite());
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
        return 16.0F;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) {
        consumer.accept(HeatExchangerManualData.of(2, 2));
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
        return ICHEME_Contents.HEAT_EXCHANGER_SMALL_ID;
    }
}
