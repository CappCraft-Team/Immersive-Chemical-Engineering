package team.cappcraft.immersivechemical.common.tileentity.constant;

import team.cappcraft.immersivechemical.Config;
import team.cappcraft.immersivechemical.common.tileentity.IHeatExchangerProperties;

public enum HeatExchangerSizeVariants implements IHeatExchangerProperties {
    SMALL(Config.HeatExchangerCapacity.Small,
            Config.HeatExchangerTickMultiplier.Small),
    MEDIUM(Config.HeatExchangerCapacity.Medium,
            Config.HeatExchangerTickMultiplier.Medium),
    LARGE(Config.HeatExchangerCapacity.Large,
            Config.HeatExchangerTickMultiplier.Large);

    private final Integer Capacity;
    private final Float TickMultiplier;

    HeatExchangerSizeVariants(Integer capacity, Float tickMultiplier) {
        Capacity = capacity;
        TickMultiplier = tickMultiplier;
    }

    @Override
    public int getCapacity() {
        return Capacity;
    }

    @Override
    public float getTickMultiplier() {
        return TickMultiplier;
    }
}
