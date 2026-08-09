package team.cappcraft.immersivechemical.common.recipe;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class HeatExchangerEntry {
    @Nonnull
    public final FluidStack FluidHot;
    @Nonnull
    public final FluidStack FluidCold;
    public final int HeatValue;
    public final ConvertDirection Direction;

    /**
     * Create a HeatExchangeRecipeEntry instance
     *
     * @param fluidA    need to be registered in FluidRegistry
     * @param fluidB    need to be registered in FluidRegistry
     * @param heatValue how much heat to release/consume from Input to Output,
     *                  with their correspond amount of fluid to be consumed or produced,
     *                  this always positive
     */
    public HeatExchangerEntry(@Nonnull FluidStack fluidA, @Nonnull FluidStack fluidB, int heatValue) {
        this(fluidA, fluidB, heatValue, ConvertDirection.TWO_WAY);
    }

    /**
     * Create a HeatExchangeRecipeEntry instance
     *
     * @param fluidA    need to be registered in FluidRegistry
     * @param fluidB    need to be registered in FluidRegistry
     * @param heatValue how much heat to release/consume from Input to Output,
     *                  with their correspond amount of fluid to be consumed or produced,
     *                  this always positive
     * @param direction set if this entry can just convert from one to another
     */
    public HeatExchangerEntry(@Nonnull FluidStack fluidA, @Nonnull FluidStack fluidB, int heatValue, ConvertDirection direction) {
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
            throw new IllegalArgumentException("A heat exchanger conversion must use two different fluids");
        }

        int temperatureA = fluidA.getFluid().getFluidType().getTemperature();
        int temperatureB = fluidB.getFluid().getFluidType().getTemperature();
        if (temperatureA == temperatureB) {
            throw new IllegalArgumentException("Heat exchanger fluids must have different temperatures");
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

    @Override
    public String toString() {
        return "HeatExchangerRecipeEntry{" +
                "FluidHot=" + FluidHot +
                ", FluidCold=" + FluidCold +
                ", HeatValue=" + HeatValue +
                ", Direction=" + Direction +
                '}';
    }

    public boolean isInputMatches(@Nonnull FluidTank fluidTank, ConvertDirection direction) {
        return isInputMatches(fluidTank.getFluid(), direction);
    }

    public boolean isInputMatches(@Nullable FluidStack fluidStack, ConvertDirection direction) {
        return fluidStack != null && !fluidStack.isEmpty() && isInputMatches(fluidStack.getFluid(), direction);
    }

    public boolean isInputMatches(@Nonnull Fluid fluid, ConvertDirection direction) {
        return fluid == getInput(direction).getFluid();
    }

    public boolean isOutputMatches(@Nonnull FluidTank fluidTank, ConvertDirection direction) {
        return isOutputMatches(fluidTank.getFluid(), direction);
    }

    public boolean isOutputMatches(@Nullable FluidStack fluidStack, ConvertDirection direction) {
        return fluidStack != null && !fluidStack.isEmpty() && isOutputMatches(fluidStack.getFluid(), direction);
    }

    public boolean isOutputMatches(@Nonnull Fluid fluid, ConvertDirection direction) {
        return fluid == getOutput(direction).getFluid();
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
