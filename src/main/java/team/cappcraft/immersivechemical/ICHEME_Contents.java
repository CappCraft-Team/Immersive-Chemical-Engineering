package team.cappcraft.immersivechemical;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.*;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRegistry;
import team.cappcraft.immersivechemical.common.tileentity.TileHeatExchangerLarge;
import team.cappcraft.immersivechemical.common.tileentity.TileHeatExchangerMedium;
import team.cappcraft.immersivechemical.common.tileentity.TileHeatExchangerSmall;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

@Mod.EventBusSubscriber
public class ICHEME_Contents {
    public static BlockHeatExchanger blockHeatExchanger;

    public static void preInit() {
        blockHeatExchanger = new BlockHeatExchanger();
    }

    public static void init() {
        registerTile(TileHeatExchangerSmall.class);
        registerTile(TileHeatExchangerMedium.class);
        registerTile(TileHeatExchangerLarge.class);

        MultiblockHandler.registerMultiblock(MultiBlockHeatExchangerSmall.INSTANCE);
        MultiblockHandler.registerMultiblock(MultiBlockHeatExchangerMedium.INSTANCE);
        MultiblockHandler.registerMultiblock(MultiBlockHeatExchangerLarge.INSTANCE);

        HeatExchangerRegistry.REGISTRY.registerHeatExchanger(HeatExchangerSizeVariants.SMALL,
                new ItemStack(ICHEME_Contents.blockHeatExchanger, 1, BlockTypes_HeatExchanger.HEAT_EXCHANGER_SMALL.getMeta()));
        HeatExchangerRegistry.REGISTRY.registerHeatExchanger(HeatExchangerSizeVariants.MEDIUM,
                new ItemStack(ICHEME_Contents.blockHeatExchanger, 1, BlockTypes_HeatExchanger.HEAT_EXCHANGER_MEDIUM.getMeta()));
        HeatExchangerRegistry.REGISTRY.registerHeatExchanger(HeatExchangerSizeVariants.LARGE,
                new ItemStack(ICHEME_Contents.blockHeatExchanger, 1, BlockTypes_HeatExchanger.HEAT_EXCHANGER_LARGE.getMeta()));
    }

    @SuppressWarnings("deprecation")
    public static void registerTile(Class<? extends TileEntity> tile) {
        String s = tile.getSimpleName();
        s = s.substring(s.indexOf("Tile") + "Tile".length());
        GameRegistry.registerTileEntity(tile, ImmersiveChemicalEngineering.MODID + ":" + s);
    }
}
