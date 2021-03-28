package team.cappcraft.immersivechemical;

import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class Config {
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent event) {
        if (event.getModID().equals(ImmersiveChemicalEngineering.MODID)) {
            ConfigManager.sync(ImmersiveChemicalEngineering.MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);
        }
    }

    @net.minecraftforge.common.config.Config(modid = ImmersiveChemicalEngineering.MODID, category = "Heat_Exchanger_Tick_Multiplier")
    public static class HeatExchangerTickMultiplier {
        @RangeDouble(min = 1e-7, max = 1e7)
        public static float Small = 0.8f;

        @RangeDouble(min = 1e-7, max = 1e7)
        public static float Medium = 0.4f;

        @RangeDouble(min = 1e-7, max = 1e7)
        public static float Large = 0.2f;
    }

    @net.minecraftforge.common.config.Config(modid = ImmersiveChemicalEngineering.MODID, category = "Heat_Exchanger_Capacity")
    public static class HeatExchangerCapacity {
        @RangeInt(min = 1)
        public static int Small = 5000;
        @RangeInt(min = 1)
        public static int Medium = 5000 * 2;
        @RangeInt(min = 1)
        public static int Large = 5000 * 6;
    }
}
