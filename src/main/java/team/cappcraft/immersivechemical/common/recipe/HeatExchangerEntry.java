package team.cappcraft.immersivechemical.common.recipe;

import javax.annotation.Nonnull;
import net.minecraftforge.fluids.FluidStack;

public class HeatExchangerEntry {
    @Nonnull public final FluidStack FluidHot;
    @Nonnull public final FluidStack FluidCold;
    public final int HeatValue;
    public final ConvertDirection Direction;

    public HeatExchangerEntry(
            FluidStack fluidA, FluidStack fluidB, int heatValue, ConvertDirection direction) {
        if (fluidA.isEmpty() || fluidB.isEmpty() || heatValue <= 0) {
            throw new IllegalArgumentException("Heat exchanger fluids and heat must be positive");
        }
        if (fluidA.getFluid() == fluidB.getFluid()) {
            throw new IllegalArgumentException(
                    "A heat exchanger conversion must use two different fluids");
        }

        int temperatureA = fluidA.getFluid().getFluidType().getTemperature();
        int temperatureB = fluidB.getFluid().getFluidType().getTemperature();
        if (temperatureA == temperatureB) {
            throw new IllegalArgumentException(
                    "Heat exchanger fluids must have different temperatures");
        }

        boolean aIsHot = temperatureA > temperatureB;
        this.FluidHot = (aIsHot ? fluidA : fluidB).copy();
        this.FluidCold = (aIsHot ? fluidB : fluidA).copy();
        this.HeatValue = heatValue;
        this.Direction = direction;
    }

    public FluidStack getInput(ConvertDirection direction) {
        return switch (direction) {
            case COOL_DOWN -> FluidHot.copy();
            case HEAT_UP -> FluidCold.copy();
            case TWO_WAY -> throw new IllegalArgumentException("A concrete direction is required");
        };
    }

    public FluidStack getOutput(ConvertDirection direction) {
        return switch (direction) {
            case COOL_DOWN -> FluidCold.copy();
            case HEAT_UP -> FluidHot.copy();
            case TWO_WAY -> throw new IllegalArgumentException("A concrete direction is required");
        };
    }
}
