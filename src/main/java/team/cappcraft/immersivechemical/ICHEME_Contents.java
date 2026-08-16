package team.cappcraft.immersivechemical;

import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerLogic;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerMultiblock;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerState;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerMenu;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

import java.util.function.Supplier;

public final class ICHEME_Contents {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ImmersiveChemicalEngineering.MODID);

    public static final HeatExchangerLogic HEAT_EXCHANGER_SMALL_LOGIC =
            new HeatExchangerLogic(HeatExchangerSizeVariants.SMALL);
    public static final HeatExchangerMultiblock HEAT_EXCHANGER_SMALL_TEMPLATE =
            new HeatExchangerMultiblock(HeatExchangerSizeVariants.SMALL, ICHEME_Contents::getHeatExchangerSmallRegistration);
    public static final MultiblockRegistration<HeatExchangerState> HEAT_EXCHANGER_SMALL =
            registerHeatExchanger(
                    HeatExchangerSizeVariants.SMALL,
                    HEAT_EXCHANGER_SMALL_LOGIC,
                    () -> HEAT_EXCHANGER_SMALL_TEMPLATE
            );

    public static final HeatExchangerLogic HEAT_EXCHANGER_MEDIUM_LOGIC =
            new HeatExchangerLogic(HeatExchangerSizeVariants.MEDIUM);
    public static final HeatExchangerMultiblock HEAT_EXCHANGER_MEDIUM_TEMPLATE =
            new HeatExchangerMultiblock(HeatExchangerSizeVariants.MEDIUM, ICHEME_Contents::getHeatExchangerMediumRegistration);
    public static final MultiblockRegistration<HeatExchangerState> HEAT_EXCHANGER_MEDIUM =
            registerHeatExchanger(
                    HeatExchangerSizeVariants.MEDIUM,
                    HEAT_EXCHANGER_MEDIUM_LOGIC,
                    () -> HEAT_EXCHANGER_MEDIUM_TEMPLATE
            );

    public static final HeatExchangerLogic HEAT_EXCHANGER_LARGE_LOGIC =
            new HeatExchangerLogic(HeatExchangerSizeVariants.LARGE);
    public static final HeatExchangerMultiblock HEAT_EXCHANGER_LARGE_TEMPLATE =
            new HeatExchangerMultiblock(HeatExchangerSizeVariants.LARGE, ICHEME_Contents::getHeatExchangerLargeRegistration);
    public static final MultiblockRegistration<HeatExchangerState> HEAT_EXCHANGER_LARGE =
            registerHeatExchanger(
                    HeatExchangerSizeVariants.LARGE,
                    HEAT_EXCHANGER_LARGE_LOGIC,
                    () -> HEAT_EXCHANGER_LARGE_TEMPLATE
            );

    public static final RegistryObject<MenuType<HeatExchangerMenu>> HEAT_EXCHANGER_SMALL_MENU =
            MENUS.register(
                    "heat_exchanger_small",
                    () -> IForgeMenuType.create((containerId, inventory, extraData) ->
                            HeatExchangerMenu.fromNetwork(
                                    HeatExchangerSizeVariants.SMALL,
                                    containerId,
                                    inventory,
                                    extraData
                            ))
            );

    public static final RegistryObject<MenuType<HeatExchangerMenu>> HEAT_EXCHANGER_MEDIUM_MENU =
            MENUS.register(
                    "heat_exchanger_medium",
                    () -> IForgeMenuType.create((containerId, inventory, extraData) ->
                            HeatExchangerMenu.fromNetwork(
                                    HeatExchangerSizeVariants.MEDIUM,
                                    containerId,
                                    inventory,
                                    extraData
                            ))
            );

    public static final RegistryObject<MenuType<HeatExchangerMenu>> HEAT_EXCHANGER_LARGE_MENU =
            MENUS.register(
                    "heat_exchanger_large",
                    () -> IForgeMenuType.create((containerId, inventory, extraData) ->
                            HeatExchangerMenu.fromNetwork(
                                    HeatExchangerSizeVariants.LARGE,
                                    containerId,
                                    inventory,
                                    extraData
                            ))
            );

    private ICHEME_Contents() {
    }

    public static MenuType<HeatExchangerMenu> menuType(HeatExchangerSizeVariants variant) {
        return switch (variant) {
            case SMALL -> HEAT_EXCHANGER_SMALL_MENU.get();
            case MEDIUM -> HEAT_EXCHANGER_MEDIUM_MENU.get();
            case LARGE -> HEAT_EXCHANGER_LARGE_MENU.get();
        };
    }

    public static MultiblockRegistration<HeatExchangerState> registration(HeatExchangerSizeVariants variant) {
        return switch (variant) {
            case SMALL -> HEAT_EXCHANGER_SMALL;
            case MEDIUM -> HEAT_EXCHANGER_MEDIUM;
            case LARGE -> HEAT_EXCHANGER_LARGE;
        };
    }

    private static MultiblockRegistration<HeatExchangerState> getHeatExchangerSmallRegistration() {
        return HEAT_EXCHANGER_SMALL;
    }

    private static MultiblockRegistration<HeatExchangerState> getHeatExchangerMediumRegistration() {
        return HEAT_EXCHANGER_MEDIUM;
    }

    private static MultiblockRegistration<HeatExchangerState> getHeatExchangerLargeRegistration() {
        return HEAT_EXCHANGER_LARGE;
    }

    private static MultiblockRegistration<HeatExchangerState> registerHeatExchanger(
            HeatExchangerSizeVariants variant,
            HeatExchangerLogic logic,
            Supplier<TemplateMultiblock> template
    ) {
        MultiblockRegistrationBuilder<HeatExchangerState, ?> builder = MultiblockRegistration
                .builder(logic, variant.getId())
                .withComparator()
                .selfWrappingComponent(ComparatorManager.makeSimple(
                        state -> state.getComparatorOutput(HeatExchangerState.INPUT_A),
                        variant.getComparatorInputA()
                ))
                .selfWrappingComponent(ComparatorManager.makeSimple(
                        state -> state.getComparatorOutput(HeatExchangerState.OUTPUT_A),
                        variant.getComparatorOutputA()
                ))
                .selfWrappingComponent(ComparatorManager.makeSimple(
                        state -> state.getComparatorOutput(HeatExchangerState.INPUT_B),
                        variant.getComparatorInputB()
                ))
                .selfWrappingComponent(ComparatorManager.makeSimple(
                        state -> state.getComparatorOutput(HeatExchangerState.OUTPUT_B),
                        variant.getComparatorOutputB()
                ))
                .defaultBEs(BLOCK_ENTITIES);

        if (variant.isMirrorable()) {
            builder = builder.customBlock(
                    BLOCKS,
                    ITEMS,
                    registration -> new MultiblockPartBlock.WithMirrorState<>(
                            formedBlockProperties(),
                            registration
                    ),
                    MultiblockItem::new
            );
        } else {
            builder = builder.notMirrored().defaultBlock(BLOCKS, ITEMS, formedBlockProperties());
        }
        return builder.structure(template).build();
    }

    private static BlockBehaviour.Properties formedBlockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 15.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .isViewBlocking((state, level, position) -> false)
                .noOcclusion();
    }
}
