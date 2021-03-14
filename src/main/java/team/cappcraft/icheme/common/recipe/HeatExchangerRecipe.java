package team.cappcraft.icheme.common.recipe;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class HeatExchangerRecipe {
    private final FluidStack inputA;
    private final FluidStack outputA;
    private final FluidStack inputB;
    private final FluidStack outputB;
    private final int heatValueA;
    private final int heatValueB;

    public HeatExchangerRecipe(HeatExchangerRecipeEntry entryA, HeatExchangerRecipeEntry entryB) {
        this.inputA = new FluidStack(entryA.getInput(), 0);
        this.inputB = new FluidStack(entryB.getInput(), 0);
        this.outputA = new FluidStack(entryA.getOutput(), 0);
        this.outputB = new FluidStack(entryB.getOutput(), 0);
        this.heatValueA = entryA.getHeatValue();
        this.heatValueB = entryB.getHeatValue();
    }

    public HeatExchangerRecipe(FluidStack inA, FluidStack inB, FluidStack outA, FluidStack outB, int heatValueA, int heatValueB) {
        this.inputA = inA;
        this.inputB = inB;
        this.outputA = outA;
        this.outputB = outB;
        this.heatValueA = heatValueA;
        this.heatValueB = heatValueB;
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

    @Override
    public String toString() {
        return "HeatExchangerRecipe{" +
                "inputA=" + inputA +
                ", outputA=" + outputA +
                ", inputB=" + inputB +
                ", outputB=" + outputB +
                ", heatValue=" + heatValueA +
                '}';
    }

    // END OF CLASS
    public static ArrayList<HeatExchangerRecipeEntry> exchangeList = new ArrayList<>();

//    static {
//        exchangeList.add(new HeatExchangerRecipeEntry(FluidRegistry.WATER, FluidRegistry.LAVA, 500));
//        exchangeList.add(new HeatExchangerRecipeEntry(FluidRegistry.LAVA, FluidRegistry.WATER, 500));
//    }

    public static HeatExchangerRecipeEntry addFluid(Fluid in, Fluid out, int heatValue) {
        HeatExchangerRecipeEntry e = new HeatExchangerRecipeEntry(in, out, heatValue);
        exchangeList.add(e);
        return e;
    }

    public static HeatExchangerRecipeEntry addFluid(FluidStack in, FluidStack out, int heatValue) {
        HeatExchangerRecipeEntry e = new HeatExchangerRecipeEntry(in.getFluid(), out.getFluid(), heatValue);
        exchangeList.add(e);
        return e;
    }

    public static HeatExchangerRecipe findHeatExchangerRecipe(FluidStack fluidA, FluidStack fluidB) {
        return new HeatExchangerRecipe(new HeatExchangerRecipeEntry(FluidRegistry.WATER, FluidRegistry.LAVA, 500),
                new HeatExchangerRecipeEntry(FluidRegistry.LAVA, FluidRegistry.WATER, 500));
    }


}
