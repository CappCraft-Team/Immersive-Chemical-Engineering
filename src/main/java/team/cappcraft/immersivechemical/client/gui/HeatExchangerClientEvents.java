package team.cappcraft.immersivechemical.client.gui;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;

/** Client-only menu registration kept separate from common class loading. */
@Mod.EventBusSubscriber(
        modid = ImmersiveChemicalEngineering.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class HeatExchangerClientEvents {
    private HeatExchangerClientEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(
                    ICHEME_Contents.HEAT_EXCHANGER_SMALL_MENU.get(),
                    HeatExchangerSmallScreen::new
            );
            MenuScreens.register(
                    ICHEME_Contents.HEAT_EXCHANGER_MEDIUM_MENU.get(),
                    HeatExchangerMediumScreen::new
            );
            MenuScreens.register(
                    ICHEME_Contents.HEAT_EXCHANGER_LARGE_MENU.get(),
                    HeatExchangerLargeScreen::new
            );
        });
    }
}
