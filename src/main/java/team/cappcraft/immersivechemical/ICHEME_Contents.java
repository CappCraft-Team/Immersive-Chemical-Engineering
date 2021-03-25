package team.cappcraft.immersivechemical;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.BlockHeatExchanger;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.MultiBlockHeatExchangerLarge;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.MultiBlockHeatExchangerMedium;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.MultiBlockHeatExchangerSmall;
import team.cappcraft.immersivechemical.common.tileentity.TileHeatExchangerLarge;
import team.cappcraft.immersivechemical.common.tileentity.TileHeatExchangerMedium;
import team.cappcraft.immersivechemical.common.tileentity.TileHeatExchangerSmall;

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
    }

    public static void registerTile(Class<? extends TileEntity> tile) {
        String s = tile.getSimpleName();
        s = s.substring(s.indexOf("Tile") + "Tile".length());
        GameRegistry.registerTileEntity(tile, ImmersiveChemicalEngineering.MODID + ":" + s);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
    }
}
