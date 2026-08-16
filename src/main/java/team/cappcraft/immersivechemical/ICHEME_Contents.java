package team.cappcraft.immersivechemical;

import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import java.util.function.Supplier;
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

/** 本模组所有注册内容的集中入口：方块、物品、方块实体、菜单类型与多方块结构。 */
public final class ICHEME_Contents {

    // ======== 各类注册表 ========

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ImmersiveChemicalEngineering.MODID);

    // ======== 热交换器：逻辑 / 结构模板 / 多方块注册 ========

    // 每种尺寸由三部分组成：
    //   LOGIC      —— 逻辑实现（按尺寸参数化，三个尺寸共用一个类）
    //   TEMPLATE   —— 结构模板，构造时需要懒引用 Registration（两者循环依赖）
    //   REGISTRATION —— 由 registerHeatExchanger 组装成的最终多方块注册对象
    public static final HeatExchangerLogic HEAT_EXCHANGER_SMALL_LOGIC =
            new HeatExchangerLogic(HeatExchangerSizeVariants.SMALL);
    public static final HeatExchangerMultiblock HEAT_EXCHANGER_SMALL_TEMPLATE =
            new HeatExchangerMultiblock(
                    HeatExchangerSizeVariants.SMALL, () -> ICHEME_Contents.HEAT_EXCHANGER_SMALL);
    public static final MultiblockRegistration<HeatExchangerState> HEAT_EXCHANGER_SMALL =
            registerHeatExchanger(
                    HeatExchangerSizeVariants.SMALL,
                    HEAT_EXCHANGER_SMALL_LOGIC,
                    () -> HEAT_EXCHANGER_SMALL_TEMPLATE);

    public static final HeatExchangerLogic HEAT_EXCHANGER_MEDIUM_LOGIC =
            new HeatExchangerLogic(HeatExchangerSizeVariants.MEDIUM);
    public static final HeatExchangerMultiblock HEAT_EXCHANGER_MEDIUM_TEMPLATE =
            new HeatExchangerMultiblock(
                    HeatExchangerSizeVariants.MEDIUM, () -> ICHEME_Contents.HEAT_EXCHANGER_MEDIUM);
    public static final MultiblockRegistration<HeatExchangerState> HEAT_EXCHANGER_MEDIUM =
            registerHeatExchanger(
                    HeatExchangerSizeVariants.MEDIUM,
                    HEAT_EXCHANGER_MEDIUM_LOGIC,
                    () -> HEAT_EXCHANGER_MEDIUM_TEMPLATE);

    public static final HeatExchangerLogic HEAT_EXCHANGER_LARGE_LOGIC =
            new HeatExchangerLogic(HeatExchangerSizeVariants.LARGE);
    public static final HeatExchangerMultiblock HEAT_EXCHANGER_LARGE_TEMPLATE =
            new HeatExchangerMultiblock(
                    HeatExchangerSizeVariants.LARGE, () -> ICHEME_Contents.HEAT_EXCHANGER_LARGE);
    public static final MultiblockRegistration<HeatExchangerState> HEAT_EXCHANGER_LARGE =
            registerHeatExchanger(
                    HeatExchangerSizeVariants.LARGE,
                    HEAT_EXCHANGER_LARGE_LOGIC,
                    () -> HEAT_EXCHANGER_LARGE_TEMPLATE);

    // ======== 热交换器：容器菜单类型（每种尺寸一个） ========

    public static final RegistryObject<MenuType<HeatExchangerMenu>> HEAT_EXCHANGER_SMALL_MENU =
            MENUS.register(
                    "heat_exchanger_small",
                    () ->
                            IForgeMenuType.create(
                                    (containerId, inventory, extraData) ->
                                            HeatExchangerMenu.fromNetwork(
                                                    HeatExchangerSizeVariants.SMALL,
                                                    containerId,
                                                    inventory,
                                                    extraData)));

    public static final RegistryObject<MenuType<HeatExchangerMenu>> HEAT_EXCHANGER_MEDIUM_MENU =
            MENUS.register(
                    "heat_exchanger_medium",
                    () ->
                            IForgeMenuType.create(
                                    (containerId, inventory, extraData) ->
                                            HeatExchangerMenu.fromNetwork(
                                                    HeatExchangerSizeVariants.MEDIUM,
                                                    containerId,
                                                    inventory,
                                                    extraData)));

    public static final RegistryObject<MenuType<HeatExchangerMenu>> HEAT_EXCHANGER_LARGE_MENU =
            MENUS.register(
                    "heat_exchanger_large",
                    () ->
                            IForgeMenuType.create(
                                    (containerId, inventory, extraData) ->
                                            HeatExchangerMenu.fromNetwork(
                                                    HeatExchangerSizeVariants.LARGE,
                                                    containerId,
                                                    inventory,
                                                    extraData)));

    // ======== 对外查询入口 ========

    /** 按尺寸查询对应菜单类型，供 GUI 打开时使用。 */
    public static MenuType<HeatExchangerMenu> menuType(HeatExchangerSizeVariants variant) {
        return switch (variant) {
            case SMALL -> HEAT_EXCHANGER_SMALL_MENU.get();
            case MEDIUM -> HEAT_EXCHANGER_MEDIUM_MENU.get();
            case LARGE -> HEAT_EXCHANGER_LARGE_MENU.get();
        };
    }

    /** 按尺寸查询对应多方块注册对象。 */
    public static MultiblockRegistration<HeatExchangerState> registration(
            HeatExchangerSizeVariants variant) {
        return switch (variant) {
            case SMALL -> HEAT_EXCHANGER_SMALL;
            case MEDIUM -> HEAT_EXCHANGER_MEDIUM;
            case LARGE -> HEAT_EXCHANGER_LARGE;
        };
    }

    // ======== 私有构造辅助 ========

    /** 组装一个热交换器多方块注册：登记 4 个红石比较器输出位，并按是否可镜像选择方块注册方式。 */
    private static MultiblockRegistration<HeatExchangerState> registerHeatExchanger(
            HeatExchangerSizeVariants variant,
            HeatExchangerLogic logic,
            Supplier<TemplateMultiblock> template) {
        MultiblockRegistrationBuilder<HeatExchangerState, ?> builder =
                MultiblockRegistration.builder(logic, variant.getId())
                        .withComparator()
                        .selfWrappingComponent(
                                ComparatorManager.makeSimple(
                                        state ->
                                                state.getComparatorOutput(
                                                        HeatExchangerState.INPUT_A),
                                        variant.getComparatorInputA()))
                        .selfWrappingComponent(
                                ComparatorManager.makeSimple(
                                        state ->
                                                state.getComparatorOutput(
                                                        HeatExchangerState.OUTPUT_A),
                                        variant.getComparatorOutputA()))
                        .selfWrappingComponent(
                                ComparatorManager.makeSimple(
                                        state ->
                                                state.getComparatorOutput(
                                                        HeatExchangerState.INPUT_B),
                                        variant.getComparatorInputB()))
                        .selfWrappingComponent(
                                ComparatorManager.makeSimple(
                                        state ->
                                                state.getComparatorOutput(
                                                        HeatExchangerState.OUTPUT_B),
                                        variant.getComparatorOutputB()))
                        .defaultBEs(BLOCK_ENTITIES);

        if (variant.isMirrorable()) {
            builder =
                    builder.customBlock(
                            BLOCKS,
                            ITEMS,
                            registration ->
                                    new MultiblockPartBlock.WithMirrorState<>(
                                            formedBlockProperties(), registration),
                            MultiblockItem::new);
        } else {
            builder = builder.notMirrored().defaultBlock(BLOCKS, ITEMS, formedBlockProperties());
        }
        return builder.structure(template).build();
    }

    /** 多方块成形后构成的实体方块属性：金属质感、需正确工具挖掘、无碰撞阻挡。 */
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
