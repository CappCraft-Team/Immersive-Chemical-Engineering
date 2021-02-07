package team.cappcraft.icheme.common.recipe;

import net.minecraftforge.fluids.FluidStack;

public class HeatExchangerRecipeEntry {
    private final FluidStack input;
    private final FluidStack output;
    private final int temperature;

    public HeatExchangerRecipeEntry(FluidStack in, FluidStack out, int temperatureDifference) {
        this.input = in;
        this.output = out;
        this.temperature = temperatureDifference;
    }

    public FluidStack getInput() {
        return input;
    }

    public FluidStack getOutput() {
        return output;
    }

    public int getTemperature() {
        return temperature;
    }

    @Override
    public String toString() {
        return "HeatExchangerRecipeEntry{" +
                "input=" + input +
                ", output=" + output +
                ", temperature=" + temperature +
                '}';
    }
}
