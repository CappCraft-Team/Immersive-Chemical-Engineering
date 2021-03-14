package team.cappcraft.icheme.common.recipe;

import net.minecraftforge.fluids.Fluid;

public class HeatExchangerRecipeEntry {
    private final Fluid input;
    private final Fluid output;
    private final int heatValue;

    public HeatExchangerRecipeEntry(Fluid in, Fluid out, int heatValue) {
        this.input = in;
        this.output = out;
        this.heatValue = heatValue;
    }

    public Fluid getInput() {
        return input;
    }

    public Fluid getOutput() {
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
