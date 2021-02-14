package team.cappcraft.icheme.common.util.compact.crafttweaker;

import blusunrize.immersiveengineering.common.util.compat.crafttweaker.CraftTweakerHelper;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Logger;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import team.cappcraft.icheme.ImmersiveChemicalEngineering;
import team.cappcraft.icheme.common.recipe.HeatExchangerRecipe;
import team.cappcraft.icheme.common.recipe.HeatExchangerRecipeEntry;

@ZenClass("team.cappcraft.icheme.SmallHeatExchanger")
public class SmallHeatExchangerCTRecipe {
    private static Logger logger = ImmersiveChemicalEngineering.logger;

    @ZenMethod
    public static void addHXHeating(ILiquidStack input, ILiquidStack output, int heatValue) {
        if (integrityCheck(input, output, heatValue)) return;
        FluidStack fluidin = CraftTweakerHelper.toFluidStack(input);
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(output);

    }

    @ZenMethod
    public static void addHXOHeating(ILiquidStack input, ILiquidStack output, int heatValue, int inputTemperature, int outputTemperature) {
        if (integrityCheck(input, output, heatValue) || inputTemperature <= 0 || outputTemperature <= 0) return;
        FluidStack fluidin = CraftTweakerHelper.toFluidStack(input);
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(output);
    }

    @ZenMethod
    public static void addHXCooling(ILiquidStack input, ILiquidStack output, int heatValue) {
        if (integrityCheck(input, output, heatValue)) return;
        FluidStack fluidin = CraftTweakerHelper.toFluidStack(input);
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(output);
    }

    @ZenMethod
    public static void addHXOCooling(ILiquidStack input, ILiquidStack output, int heatValue, int inputTemperature, int outputTemperature) {
        if (integrityCheck(input, output, heatValue) || inputTemperature <= 0 || outputTemperature <= 0) return;
        FluidStack fluidin = CraftTweakerHelper.toFluidStack(input);
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(output);

    }

    private static boolean integrityCheck(ILiquidStack input, ILiquidStack output, int heatValue) {
        if (input == null || output == null || heatValue <= 0) {
            logger.error("Failed to add Heat Exchange Entry FluidIn=" + input + " FluidOut=" + input + " HeatValue=" + heatValue);
            return false;
        }
        if (CraftTweakerHelper.toFluidStack(input) == null || CraftTweakerHelper.toFluidStack(output) == null) {
            logger.error("Unable Convert to FluidStack FluidIn=" + input + " FluidOut=" + output);
            return false;
        }
        return true;
    }


    // this is an example
    @ZenMethod
    public static void addHeatExchangeEntry(ILiquidStack input, ILiquidStack output, int heatValue) {
        FluidStack fluidin = CraftTweakerHelper.toFluidStack(input);
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(output);

        if (input == null || output == null || heatValue <= 0) {
            logger.error("Failed to add Heat Exchange Entry FluidIn=" + fluidin + " FluidOut=" + fluidOut + " HeatValue=" + heatValue);
            return;
        }
        if (fluidin == null || fluidOut == null) {
            logger.error("Unable Convert to FluidStack FluidIn=" + fluidin + " FluidOut=" + fluidOut);
            return;
        }

        HeatExchangerRecipeEntry r = new HeatExchangerRecipeEntry(CraftTweakerHelper.toFluidStack(input), CraftTweakerHelper.toFluidStack(output), heatValue);
        CraftTweakerAPI.apply(new SmallHeatExchangerCTRecipe.AddSmallHeatExchangeRecipe(fluidin, fluidOut, heatValue));
    }

    // this is an example
    private static class AddSmallHeatExchangeRecipe implements IAction {
        private FluidStack fluidin;
        private FluidStack fluidOut;
        private int heatValue;

        public AddSmallHeatExchangeRecipe(FluidStack input, FluidStack output, int heatValue) {
            this.fluidin = input;
            this.fluidOut = output;
            this.heatValue = heatValue;
        }

        @Override
        public void apply() {
            HeatExchangerRecipe.addFluid(fluidin, fluidOut, heatValue);
        }

        @Override
        public String describe() {
            return "Added Recipe for FluidIn=" + fluidin + " FluidOut=" + fluidOut + " HeatValue=" + heatValue;
        }

        @Override
        public boolean validate() {
            return false;
        }

        @Override
        public String describeInvalid() {
            return null;
        }
    }


}
