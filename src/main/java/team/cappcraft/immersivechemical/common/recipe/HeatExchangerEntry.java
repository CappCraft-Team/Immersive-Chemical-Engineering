package team.cappcraft.immersivechemical.common.recipe;

import blusunrize.immersiveengineering.api.crafting.IJEIRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class HeatExchangerEntry implements IJEIRecipe {
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
        if (fluidA.getFluid().getTemperature() > fluidB.getFluid().getTemperature()) {
            this.FluidHot = fluidA;
            this.FluidCold = fluidB;
        } else {
            this.FluidHot = fluidB;
            this.FluidCold = fluidA;
        }
        this.HeatValue = heatValue;
        this.Direction = direction;
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
        return fluidStack != null && isInputMatches(fluidStack.getFluid(), direction);
    }

    public boolean isInputMatches(@Nonnull Fluid fluid, ConvertDirection direction) {
        return fluid == getInput(direction).getFluid();
    }

    public boolean isOutputMatches(@Nonnull FluidTank fluidTank, ConvertDirection direction) {
        return isOutputMatches(fluidTank.getFluid(), direction);
    }

    public boolean isOutputMatches(@Nullable FluidStack fluidStack, ConvertDirection direction) {
        return fluidStack != null && isOutputMatches(fluidStack.getFluid(), direction);
    }

    public boolean isOutputMatches(@Nonnull Fluid fluid, ConvertDirection direction) {
        return fluid == getOutput(direction).getFluid();
    }

    public FluidStack getInput(ConvertDirection direction) {
        switch (direction) {
            case COOL_DOWN:
                return FluidHot;
            case HEAT_UP:
                return FluidCold;
        }
        throw new IllegalArgumentException("Invalid direction:" + direction);
    }

    public FluidStack getOutput(ConvertDirection direction) {
        switch (direction) {
            case COOL_DOWN:
                return FluidCold;
            case HEAT_UP:
                return FluidHot;
        }
        throw new IllegalArgumentException("Invalid direction:" + direction);
    }

    @Override
    public List<ItemStack> getJEITotalItemInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<ItemStack> getJEITotalItemOutputs() {
        return Collections.emptyList();
    }

    @Override
    public List<FluidStack> getJEITotalFluidInputs() {
        return Collections.singletonList(FluidHot);
    }

    @Override
    public List<FluidStack> getJEITotalFluidOutputs() {
        return Collections.singletonList(FluidCold);
    }
}

