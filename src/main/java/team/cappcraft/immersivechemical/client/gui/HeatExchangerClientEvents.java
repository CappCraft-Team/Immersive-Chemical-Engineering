package team.cappcraft.immersivechemical.client.gui;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerLargeMenu;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerMediumMenu;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerSmallMenu;


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
                    HeatExchangerScreen<HeatExchangerSmallMenu>::new
            );
            MenuScreens.register(
                    ICHEME_Contents.HEAT_EXCHANGER_MEDIUM_MENU.get(),
                    HeatExchangerScreen<HeatExchangerMediumMenu>::new
            );
            MenuScreens.register(
                    ICHEME_Contents.HEAT_EXCHANGER_LARGE_MENU.get(),
                    HeatExchangerScreen<HeatExchangerLargeMenu>::new
            );
        });
    }
}
