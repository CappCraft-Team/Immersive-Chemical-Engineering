package team.cappcraft.immersivechemical.common.recipe;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@ZenClass("team.cappcraft.icheme.HeatExchangerRecipeEntry")
@ZenRegister
public class HeatExchangerRecipeEntry {
    @Nonnull
    public final FluidStack Input;
    @Nonnull
    public final FluidStack Output;
    public final int HeatValue;

    /**
     * Create a HeatExchangeRecipeEntry instance
     *
     * @param in        need to be registered in FluidRegistry
     * @param out       need to be registered in FluidRegistry
     * @param heatValue how much heat to release/consume from Input to Output,
     *                  with their correspond amount of fluid to be consumed or produced,
     *                  this always positive
     */
    @SuppressWarnings("unused")
    @ZenConstructor
    public HeatExchangerRecipeEntry(ILiquidStack in, ILiquidStack out, int heatValue) {
        this(
                new FluidStack(Objects.requireNonNull(FluidRegistry.getFluid(in.getName())), in.getAmount()),
                new FluidStack(Objects.requireNonNull(FluidRegistry.getFluid(out.getName())), out.getAmount()),
                heatValue);
    }

    public HeatExchangerRecipeEntry(@Nonnull FluidStack in, @Nonnull FluidStack out, int heatValue) {
        this.Input = in;
        this.Output = out;
        this.HeatValue = heatValue;
    }

    public boolean isInputMatches(@Nonnull FluidTank fluidTank) {
        return isInputMatches(fluidTank.getFluid());
    }

    public boolean isInputMatches(@Nullable FluidStack fluidStack) {
        return fluidStack != null && fluidStack.getFluid() == Input.getFluid();
    }

    public boolean isOutputMatches(@Nonnull FluidTank fluidTank) {
        return isOutputMatches(fluidTank.getFluid());
    }

    public boolean isOutputMatches(@Nullable FluidStack fluidStack) {
        return fluidStack == null || fluidStack.getFluid() == Input.getFluid();
    }

    @Override
    public String toString() {
        return "HeatExchangerRecipeEntry{" +
                "Input=" + Input +
                ", Output=" + Output +
                ", HeatValue=" + HeatValue +
                '}';
    }
}

