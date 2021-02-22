package team.cappcraft.icheme.common.recipe;

import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class HeatExchangerRecipe {
    private final FluidStack inputA;
    private final FluidStack outputA;
    private final FluidStack inputB;
    private final FluidStack outputB;
    private final int heatValueA;
    private final int heatValueB;
    private final int temperatureA;
    private final int temperatureB;

    public HeatExchangerRecipe(FluidStack inA, FluidStack inB, FluidStack outA, FluidStack outB, int heatValueA, int heatValueB) {
        this.inputA = inA;
        this.inputB = inB;
        this.outputA = outA;
        this.outputB = outB;
        this.heatValueA = heatValueA;
        this.heatValueB = heatValueB;
        this.temperatureA = inA.getFluid().getTemperature();
        this.temperatureB = inB.getFluid().getTemperature();
    }

    public HeatExchangerRecipe(HeatExchangerRecipeEntry entryA, HeatExchangerRecipeEntry entryB) {
        this.inputA = entryA.getInput();
        this.inputB = entryB.getInput();
        this.outputA = entryA.getOutput();
        this.outputB = entryB.getOutput();
        this.heatValueA = entryA.getHeatValue();
        this.heatValueB = entryB.getHeatValue();
        this.temperatureA = entryA.getInput().getFluid().getTemperature();
        this.temperatureB = entryB.getInput().getFluid().getTemperature();
    }

    public HeatExchangerRecipe(FluidStack inA, FluidStack inB, FluidStack outA, FluidStack outB, int heatValueA, int heatValueB, int temperatureA, int temperatureB) {
        this.inputA = inA;
        this.inputB = inB;
        this.outputA = outA;
        this.outputB = outB;
        this.heatValueA = heatValueA;
        this.heatValueB = heatValueB;
        this.temperatureA = temperatureA;
        this.temperatureB = temperatureB;
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

    public int getHeatValueA() {
        return heatValueA;
    }

    public int getHeatValueB() {
        return heatValueB;
    }

    public int getTemperatureA() {
        return temperatureA;
    }

    public int getTemperatureB() {
        return temperatureB;
    }

    @Override
    public String toString() {
        return "HeatExchangerRecipe{" +
                "inputA=" + inputA +
                ", outputA=" + outputA +
                ", inputB=" + inputB +
                ", outputB=" + outputB +
                ", heatValue=" + heatValueA +
                ", temperatureA=" + temperatureA +
                ", temperatureB=" + temperatureB +
                '}';
    }

    // END OF CLASS
    public static ArrayList<HeatExchangerRecipeEntry> recipeList = new ArrayList<>();
    public static ArrayList<HeatExchangerRecipe> specialOverwriteList = new ArrayList<>();

    public static HeatExchangerRecipeEntry addFluid(FluidStack in, FluidStack out, int heatValue) {
        HeatExchangerRecipeEntry e = new HeatExchangerRecipeEntry(in, out, heatValue);
        recipeList.add(e);
        return e;
    }

    public static HeatExchangerRecipe addFluidOverwrite(FluidStack inA, FluidStack outA, FluidStack inB, FluidStack outB, int heatValueA, int heatValueB, int temperatureA, int temperatureB) {
        HeatExchangerRecipe r = new HeatExchangerRecipe(inA, outA, inB, outB, heatValueA, heatValueB, temperatureA, temperatureB);
        specialOverwriteList.add(r);
        return r;
    }

    public static HeatExchangerRecipe findHeatExchangerRecipe(FluidStack fluidA, FluidStack fluidB) {
        for (HeatExchangerRecipe r : specialOverwriteList) {
            if (r.getInputA().isFluidEqual(fluidA) && r.getInputB().isFluidEqual(fluidB)) {
                if (r.getTemperatureA() == fluidA.getFluid().getTemperature() && r.getTemperatureB() == fluidB.getFluid().getTemperature()) {
                    return r;
                }
            }
        }
        ArrayList<HeatExchangerRecipeEntry> inputAList = new ArrayList<>();
        ArrayList<HeatExchangerRecipeEntry> inputBList = new ArrayList<>();
        for (HeatExchangerRecipeEntry e : recipeList) {
            if (e.getInput().isFluidEqual(fluidA)) {
                inputAList.add(e);
            } else if (e.getInput().isFluidEqual(fluidB)) {
                inputBList.add(e);
            }
        }
        if (inputAList.size() == 0 || inputBList.size() == 0) return null;
        for (HeatExchangerRecipeEntry a : inputAList) {
            for (HeatExchangerRecipeEntry b : inputBList) {
                int tempA = a.getInput().getFluid().getTemperature();
                int tempB = b.getInput().getFluid().getTemperature();
                int heatA = a.getHeatValue();
                int heatB = b.getHeatValue();
                if (tempA > tempB && heatA < 0 && heatB > 0) {
                    return new HeatExchangerRecipe(a, b);
                } else if (tempA < tempB && heatA > 0 && heatB < 0) {
                    return new HeatExchangerRecipe(a, b);
                }
            }
        }
        return null;
    }


}
