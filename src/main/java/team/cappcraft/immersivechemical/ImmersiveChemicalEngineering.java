package team.cappcraft.immersivechemical;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Logger;
import team.cappcraft.immersivechemical.common.proxy.CommonProxy;


@Mod(modid = ImmersiveChemicalEngineering.MODID,
        name = ImmersiveChemicalEngineering.NAME,
        version = ImmersiveChemicalEngineering.VERSION,
        dependencies = "required-after:immersiveengineering",
        acceptedMinecraftVersions = "[1.12.2]")
public class ImmersiveChemicalEngineering {
    //insert an 'immersive' to get around IE's registry
    public static final String MODID = "immersivechemical";
    public static final String NAME = "Immersive Chemical Engineering";
    public static final String VERSION = "${version}";

    public static Logger logger;

    public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @Mod.Instance
    public static ImmersiveChemicalEngineering instance;

    @SidedProxy(clientSide = "team.cappcraft.immersivechemical.common.proxy.ClientProxy",
            serverSide = "team.cappcraft.immersivechemical.common.proxy.ServerProxy")
    public static CommonProxy proxy;


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
