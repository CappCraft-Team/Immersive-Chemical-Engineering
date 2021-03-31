package team.cappcraft.immersivechemical.common.proxy;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.network.MessageBlockEvent;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class CommonProxy {
    public static <T extends TileEntity & IEBlockInterfaces.IGuiTile> void openGuiForTile(@Nonnull EntityPlayer player, @Nonnull T tile) {
        player.openGui(ImmersiveChemicalEngineering.instance, tile.getGuiID(), tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
    }

    public void preInit(FMLPreInitializationEvent e) {
        ICHEME_Contents.preInit();
    }

    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ImmersiveChemicalEngineering.instance, new GuiHandler());
        ImmersiveChemicalEngineering.packetHandler.registerMessage(MessageBlockEvent.HandlerServer.class, MessageBlockEvent.class, 0, Side.SERVER);
        ICHEME_Contents.init();
    }

    public void postInit(FMLPostInitializationEvent e) {
    }
}
