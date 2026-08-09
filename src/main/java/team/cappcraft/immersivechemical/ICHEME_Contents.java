package team.cappcraft.immersivechemical;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.extensions.IForgeMenuType;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerSmallLogic;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerSmallMultiblock;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerSmallState;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerMediumLogic;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerMediumMultiblock;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerMediumState;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerState;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerLargeLogic;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerLargeMultiblock;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerLargeState;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerLargeMenu;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerMediumMenu;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerSmallMenu;

public final class ICHEME_Contents {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ImmersiveChemicalEngineering.MODID);

    public static final ResourceLocation HEAT_EXCHANGER_SMALL_ID =
            new ResourceLocation(ImmersiveChemicalEngineering.MODID, "heat_exchanger_small");
    public static final HeatExchangerSmallLogic HEAT_EXCHANGER_SMALL_LOGIC = new HeatExchangerSmallLogic();
    public static final HeatExchangerSmallMultiblock HEAT_EXCHANGER_SMALL_TEMPLATE =
            new HeatExchangerSmallMultiblock(ICHEME_Contents::getHeatExchangerSmallRegistration);

    public static final MultiblockRegistration<HeatExchangerSmallState> HEAT_EXCHANGER_SMALL =
            MultiblockRegistration.builder(HEAT_EXCHANGER_SMALL_LOGIC, HEAT_EXCHANGER_SMALL_ID)
                    .withComparator()
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.INPUT_A),
                            new net.minecraft.core.BlockPos(0, 0, 0)
                    ))
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.OUTPUT_A),
                            new net.minecraft.core.BlockPos(0, 0, 1)
                    ))
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.INPUT_B),
                            new net.minecraft.core.BlockPos(0, 0, 3)
                    ))
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.OUTPUT_B),
                            new net.minecraft.core.BlockPos(0, 0, 2)
                    ))
                    .notMirrored()
                    .defaultBEs(BLOCK_ENTITIES)
                    .defaultBlock(
                            BLOCKS,
                            ITEMS,
                            formedBlockProperties()
                    )
                    .structure(() -> HEAT_EXCHANGER_SMALL_TEMPLATE)
                    .build();

    public static final ResourceLocation HEAT_EXCHANGER_MEDIUM_ID =
            new ResourceLocation(ImmersiveChemicalEngineering.MODID, "heat_exchanger_medium");
    public static final HeatExchangerMediumLogic HEAT_EXCHANGER_MEDIUM_LOGIC =
            new HeatExchangerMediumLogic();
    public static final HeatExchangerMediumMultiblock HEAT_EXCHANGER_MEDIUM_TEMPLATE =
            new HeatExchangerMediumMultiblock(ICHEME_Contents::getHeatExchangerMediumRegistration);

    public static final MultiblockRegistration<HeatExchangerMediumState> HEAT_EXCHANGER_MEDIUM =
            MultiblockRegistration.builder(HEAT_EXCHANGER_MEDIUM_LOGIC, HEAT_EXCHANGER_MEDIUM_ID)
                    .withComparator()
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.INPUT_A),
                            new net.minecraft.core.BlockPos(0, 0, 0)
                    ))
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.OUTPUT_A),
                            new net.minecraft.core.BlockPos(1, 0, 0)
                    ))
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.INPUT_B),
                            new net.minecraft.core.BlockPos(0, 0, 1)
                    ))
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.OUTPUT_B),
                            new net.minecraft.core.BlockPos(1, 0, 1)
                    ))
                    .defaultBEs(BLOCK_ENTITIES)
                    .customBlock(
                            BLOCKS,
                            ITEMS,
                            registration -> new MultiblockPartBlock.WithMirrorState<>(
                                    formedBlockProperties(),
                                    registration
                            ),
                            MultiblockItem::new
                    )
                    .structure(() -> HEAT_EXCHANGER_MEDIUM_TEMPLATE)
                    .build();

    public static final ResourceLocation HEAT_EXCHANGER_LARGE_ID =
            new ResourceLocation(ImmersiveChemicalEngineering.MODID, "heat_exchanger_large");
    public static final HeatExchangerLargeLogic HEAT_EXCHANGER_LARGE_LOGIC =
            new HeatExchangerLargeLogic();
    public static final HeatExchangerLargeMultiblock HEAT_EXCHANGER_LARGE_TEMPLATE =
            new HeatExchangerLargeMultiblock(ICHEME_Contents::getHeatExchangerLargeRegistration);

    public static final MultiblockRegistration<HeatExchangerLargeState> HEAT_EXCHANGER_LARGE =
            MultiblockRegistration.builder(HEAT_EXCHANGER_LARGE_LOGIC, HEAT_EXCHANGER_LARGE_ID)
                    .withComparator()
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.INPUT_A),
                            new net.minecraft.core.BlockPos(0, 0, 6)
                    ))
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.OUTPUT_A),
                            new net.minecraft.core.BlockPos(0, 0, 5)
                    ))
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.INPUT_B),
                            new net.minecraft.core.BlockPos(2, 0, 6)
                    ))
                    .selfWrappingComponent(ComparatorManager.makeSimple(
                            state -> state.getComparatorOutput(HeatExchangerState.OUTPUT_B),
                            new net.minecraft.core.BlockPos(2, 0, 5)
                    ))
                    .notMirrored()
                    .defaultBEs(BLOCK_ENTITIES)
                    .defaultBlock(
                            BLOCKS,
                            ITEMS,
                            formedBlockProperties()
                    )
                    .structure(() -> HEAT_EXCHANGER_LARGE_TEMPLATE)
                    .build();

    public static final RegistryObject<MenuType<HeatExchangerSmallMenu>> HEAT_EXCHANGER_SMALL_MENU =
            MENUS.register(
                    "heat_exchanger_small",
                    () -> IForgeMenuType.create(HeatExchangerSmallMenu::fromNetwork)
            );

    public static final RegistryObject<MenuType<HeatExchangerMediumMenu>> HEAT_EXCHANGER_MEDIUM_MENU =
            MENUS.register(
                    "heat_exchanger_medium",
                    () -> IForgeMenuType.create(HeatExchangerMediumMenu::fromNetwork)
            );

    public static final RegistryObject<MenuType<HeatExchangerLargeMenu>> HEAT_EXCHANGER_LARGE_MENU =
            MENUS.register(
                    "heat_exchanger_large",
                    () -> IForgeMenuType.create(HeatExchangerLargeMenu::fromNetwork)
            );

    private ICHEME_Contents() {
    }

    private static MultiblockRegistration<HeatExchangerSmallState> getHeatExchangerSmallRegistration() {
        return HEAT_EXCHANGER_SMALL;
    }

    private static MultiblockRegistration<HeatExchangerMediumState> getHeatExchangerMediumRegistration() {
        return HEAT_EXCHANGER_MEDIUM;
    }

    private static MultiblockRegistration<HeatExchangerLargeState> getHeatExchangerLargeRegistration() {
        return HEAT_EXCHANGER_LARGE;
    }

    private static BlockBehaviour.Properties formedBlockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 15.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                // IE split multiblocks keep full collision while excluding their
                // formed parts from ambient-occlusion neighbor tests.
                .isViewBlocking((state, level, position) -> false)
                .noOcclusion();
    }
}
