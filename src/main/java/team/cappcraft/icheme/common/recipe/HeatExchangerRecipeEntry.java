package team.cappcraft.icheme.common.recipe;

import net.minecraftforge.fluids.FluidStack;

public class HeatExchangerRecipeEntry {
    private final FluidStack input;
    private final FluidStack output;
    private final int heatValue;

    public HeatExchangerRecipeEntry(FluidStack in, FluidStack out, int heatValue) {
        this.input = in;
        this.output = out;
        this.heatValue = heatValue;
    }

    public FluidStack getInput() {
        return input;
    }

    public FluidStack getOutput() {
        return output;
    }

    public int getHeatValue() {
        return heatValue;
    }

    @Override
    public String toString() {
        return "HeatExchangerRecipeEntry{" +
                "input=" + input +
                ", output=" + output +
                ", heatValue=" + heatValue +
                '}';
    }
}
