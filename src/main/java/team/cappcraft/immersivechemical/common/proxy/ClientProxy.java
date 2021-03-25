package team.cappcraft.immersivechemical.common.proxy;

import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.network.MessageTileSync;

import static team.cappcraft.immersivechemical.ImmersiveChemicalEngineering.MODID;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ICHEME_Contents.initModels();
    }

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        OBJLoader.INSTANCE.addDomain(MODID);
        IEOBJLoader.instance.addDomain(MODID);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        ImmersiveChemicalEngineering.packetHandler.registerMessage(MessageTileSync.HandlerClient.class, MessageTileSync.class, 0, Side.CLIENT);
        ImmersiveChemicalEngineering.packetHandler.registerMessage(MessageTileSync.HandlerServer.class, MessageTileSync.class, 0, Side.SERVER);
    }


}
