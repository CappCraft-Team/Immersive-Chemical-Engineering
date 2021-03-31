package team.cappcraft.immersivechemical.common.compact.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraftforge.fluids.FluidRegistry;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRegistry;

import java.util.Objects;

@SuppressWarnings("unused")
@ZenClass("team.cappcraft.icheme.HeatExchanger")
@ZenRegister
public class HeatExchangerCTRecipe {
    @ZenMethod
    public static void addCoolDownEntry(ILiquidStack in, ILiquidStack out, int heat) {
        CraftTweakerAPI.apply(new AddHeatExchangerEntryAction(in, out, heat, ConvertDirection.COOL_DOWN));
    }

    @ZenMethod
    public static void addHeatUpEntry(ILiquidStack in, ILiquidStack out, int heat) {
        CraftTweakerAPI.apply(new AddHeatExchangerEntryAction(in, out, heat, ConvertDirection.HEAT_UP));
    }

    @ZenMethod
    public static void addBiDirectionEntry(ILiquidStack in, ILiquidStack out, int heat) {
        CraftTweakerAPI.apply(new AddHeatExchangerEntryAction(in, out, heat, ConvertDirection.TWO_WAY));
    }

    private static class AddHeatExchangerEntryAction implements IAction {
        private final ILiquidStack In;
        private final ILiquidStack Out;
        private final int Heat;
        private final ConvertDirection Direction;
        private String describeInvalid;
        private HeatExchangerEntry entry;

        public AddHeatExchangerEntryAction(ILiquidStack in, ILiquidStack out, int heat, ConvertDirection direction) {
            In = in;
            Out = out;
            Heat = heat;
            Direction = direction;
        }

        @Override
        public void apply() {
            entry = new HeatExchangerEntry(
                    Objects.requireNonNull(FluidRegistry.getFluidStack(In.getName(), In.getAmount())),
                    Objects.requireNonNull(FluidRegistry.getFluidStack(Out.getName(), Out.getAmount())),
                    Heat,
                    Direction
            );
            HeatExchangerRegistry.REGISTRY.registerFluid(entry);
        }

        @Override
        public String describe() {
            return String.format("Added %s", entry);
        }

        @Override
        public boolean validate() {
            if (FluidRegistry.getFluid(In.getName()) == null || FluidRegistry.getFluid(Out.getName()) == null)
                describeInvalid = "Input/Output Fluid isn't registered in FluidRegistry";
            if (Heat <= 0) describeInvalid = "Heat value <= 0";
            return describeInvalid == null;
        }

        @Override
        public String describeInvalid() {
            return describeInvalid;
        }
    }


}
