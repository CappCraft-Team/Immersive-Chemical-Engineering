package team.cappcraft.icheme;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import team.cappcraft.icheme.common.proxy.CommonProxy;

@Mod(modid = ImmersiveChemicalEngineering.MODID, name = ImmersiveChemicalEngineering.NAME, version = ImmersiveChemicalEngineering.VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class ImmersiveChemicalEngineering {
    public static final String MODID = "immersive-chemical-engineering";
    public static final String NAME = "Immersive Chemical Engineering";
    public static final String VERSION = "1.0";

    private static Logger logger;

    @Mod.Instance
    public static ImmersiveChemicalEngineering instance;

    @SidedProxy(clientSide = "team.cappcraft.icheme.common.proxy.ClientProxy", serverSide = "team.cappcraft.icheme.common.proxy.ServerProxy")
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
