package team.cappcraft.immersivechemical.common.recipe;

import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraftforge.fluids.FluidStack;

public class HeatExchangerEntry {
    @Nonnull public final FluidStack FluidHot;
    @Nonnull public final FluidStack FluidCold;
    public final int HeatValue;
    public final ConvertDirection Direction;

    public HeatExchangerEntry(
            @Nonnull FluidStack fluidA,
            @Nonnull FluidStack fluidB,
            int heatValue,
            ConvertDirection direction) {
        Objects.requireNonNull(fluidA, "fluidA");
        Objects.requireNonNull(fluidB, "fluidB");
        this.Direction = Objects.requireNonNull(direction, "direction");

        if (fluidA.isEmpty() || fluidB.isEmpty()) {
            throw new IllegalArgumentException("Heat exchanger fluids must be non-empty");
        }
        if (fluidA.getAmount() <= 0 || fluidB.getAmount() <= 0) {
            throw new IllegalArgumentException("Heat exchanger fluid amounts must be positive");
        }
        if (heatValue <= 0) {
            throw new IllegalArgumentException("Heat value must be positive");
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

        if (temperatureA > temperatureB) {
            this.FluidHot = fluidA.copy();
            this.FluidCold = fluidB.copy();
        } else {
            this.FluidHot = fluidB.copy();
            this.FluidCold = fluidA.copy();
        }
        this.HeatValue = heatValue;
    }

    public FluidStack getInput(ConvertDirection direction) {
        switch (direction) {
            case COOL_DOWN:
                return FluidHot.copy();
            case HEAT_UP:
                return FluidCold.copy();
        }
        throw new IllegalArgumentException("Invalid direction:" + direction);
    }

    public FluidStack getOutput(ConvertDirection direction) {
        switch (direction) {
            case COOL_DOWN:
                return FluidCold.copy();
            case HEAT_UP:
                return FluidHot.copy();
        }
        throw new IllegalArgumentException("Invalid direction:" + direction);
    }
}
