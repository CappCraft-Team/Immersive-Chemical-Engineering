package team.cappcraft.icheme.common.proxy;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import team.cappcraft.icheme.ImmersiveChemicalEngineering;
import team.cappcraft.icheme.ModBlocks;
import team.cappcraft.icheme.common.network.MessageTileSync;

import static team.cappcraft.icheme.ImmersiveChemicalEngineering.MODID;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        OBJLoader.INSTANCE.addDomain(MODID);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModBlocks.initModels();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        ImmersiveChemicalEngineering.packetHandler.registerMessage(MessageTileSync.HandlerClient.class, MessageTileSync.class, 0, Side.CLIENT);
        ImmersiveChemicalEngineering.packetHandler.registerMessage(MessageTileSync.HandlerServer.class, MessageTileSync.class, 0, Side.SERVER);
    }


}
