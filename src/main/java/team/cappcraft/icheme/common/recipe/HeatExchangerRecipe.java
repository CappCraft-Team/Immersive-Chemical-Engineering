package team.cappcraft.icheme.common.recipe;

import net.minecraftforge.fluids.FluidStack;

import java.util.Hashtable;

public class HeatExchangerRecipe {
    private final FluidStack inputA;
    private final FluidStack outputA;
    private final FluidStack inputB;
    private final FluidStack outputB;

    public HeatExchangerRecipe(FluidStack inA, FluidStack inB, FluidStack outA, FluidStack outB) {
        this.inputA = inA;
        this.inputB = inB;
        this.outputA = outA;
        this.outputB = outB;
    }

    public FluidStack getInputA() {
        return inputA;
    }

    public FluidStack getOutputA() {
        return outputA;
    }

    public FluidStack getInputB() {
        return inputB;
    }

    public FluidStack getOutputB() {
        return outputB;
    }

    // END OF CLASS
    public static Hashtable<FluidStack, HeatExchangerRecipeEntry> recipeTable = new Hashtable<>();
    public static Hashtable<FluidStack, Integer> temperatureOverwrite = new Hashtable<>();

    public static HeatExchangerRecipeEntry addFluid(FluidStack in, FluidStack out, int temperature) {
        HeatExchangerRecipeEntry r = new HeatExchangerRecipeEntry(in, out, temperature);
        recipeTable.put(in, r);
        return r;
    }

    public static HeatExchangerRecipe findHeatExchangerRecipe(FluidStack fluidA, FluidStack fluidB) {
        int fluidAtemp = temperatureOverwrite.get(fluidA) == null ? fluidA.getFluid().getTemperature() : temperatureOverwrite.get(fluidA);
        int fluidBtemp = temperatureOverwrite.get(fluidB) == null ? fluidB.getFluid().getTemperature() : temperatureOverwrite.get(fluidB);

        return null; // TODO
    }


}
