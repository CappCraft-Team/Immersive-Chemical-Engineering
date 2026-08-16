package team.cappcraft.immersivechemical;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipeRegistration;

@Mod(ImmersiveChemicalEngineering.MODID)
public class ImmersiveChemicalEngineering {

    public static final String MODID = "immersivechemical";
    public static final String NAME = "Immersive Chemical Engineering";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public ImmersiveChemicalEngineering() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ICHEME_Contents.BLOCKS.register(modEventBus);
        ICHEME_Contents.ITEMS.register(modEventBus);
        ICHEME_Contents.BLOCK_ENTITIES.register(modEventBus);
        ICHEME_Contents.MENUS.register(modEventBus);
        HeatExchangerRecipeRegistration.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(HeatExchangerRecipeRegistration::onAddReloadListeners);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Immersive Chemical Engineering initializing...");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(
                () -> {
                    MultiblockHandler.registerMultiblock(
                            ICHEME_Contents.HEAT_EXCHANGER_SMALL_TEMPLATE);
                    MultiblockHandler.registerMultiblock(
                            ICHEME_Contents.HEAT_EXCHANGER_MEDIUM_TEMPLATE);
                    MultiblockHandler.registerMultiblock(
                            ICHEME_Contents.HEAT_EXCHANGER_LARGE_TEMPLATE);
                    LOGGER.info("Immersive Chemical Engineering multiblocks registered.");
                });
    }
}
