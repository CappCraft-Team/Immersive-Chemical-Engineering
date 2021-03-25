package team.cappcraft.immersivechemical.common.compact.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipeEntry;
import team.cappcraft.immersivechemical.common.recipe.constant.HeatExchangerSize;

import java.util.Arrays;

import static team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipeRegistry.POSTFIX_REVERSE;

@SuppressWarnings("unused")
@ZenClass("team.cappcraft.icheme.HeatExchanger")
public class HeatExchangerCTRecipe {
    /**
     * Add recipe to all type of HeatExchanger
     *
     * @param registryName Unique name of the recipe
     * @param exchangeA    One of exchange fluid
     * @param exchangeB    One of exchange fluid
     * @apiNote the two fluid to exchange heat is shapeless
     * @see HeatExchangerSize
     * @see HeatExchangerRecipeEntry
     */
    @ZenMethod
    public static void addRecipe(String registryName, HeatExchangerRecipeEntry exchangeA, HeatExchangerRecipeEntry exchangeB) {
        addRecipe(registryName, exchangeA, exchangeB, "GENERAL");
    }

    /**
     * Add recipe to HeatExchanger
     *
     * @param registryName Unique name of the recipe
     * @param exchangeA    One of exchange fluid
     * @param exchangeB    One of exchange fluid
     * @param size         need to be one of team.cappcraft.icheme.common.recipe.constant.HeatExchangerSize
     * @apiNote the two fluid to exchange heat is shapeless
     * @see team.cappcraft.immersivechemical.common.recipe.constant.HeatExchangerSize
     * @see team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipeEntry
     */
    @ZenMethod
    public static void addRecipe(String registryName, HeatExchangerRecipeEntry exchangeA, HeatExchangerRecipeEntry exchangeB, String size) {
        CraftTweakerAPI.apply(new AddHeatExchangerRecipeAction(registryName, exchangeA, exchangeB, size));
    }

    private static class AddHeatExchangerRecipeAction implements IAction {
        private final String RegistryName;
        private final HeatExchangerRecipeEntry ExchangeA;
        private final HeatExchangerRecipeEntry ExchangeB;
        private final String Size;
        private HeatExchangerRecipe recipe;
        private String describeInvalid;

        public AddHeatExchangerRecipeAction(String registryName, HeatExchangerRecipeEntry exchangeA, HeatExchangerRecipeEntry exchangeB, String size) {
            RegistryName = registryName;
            ExchangeA = exchangeA;
            ExchangeB = exchangeB;
            Size = size;
        }

        @Override
        public void apply() {
            recipe = new HeatExchangerRecipe(ExchangeA, ExchangeB, HeatExchangerSize.valueOf(Size));
            ImmersiveChemicalEngineering.proxy.heatExchangerRecipeRegistry.putObject(RegistryName, recipe);
        }

        @Override
        public String describe() {
            return String.format("Added %s", recipe);
        }

        @Override
        public boolean validate() {
            if (RegistryName == null
                    || !ImmersiveChemicalEngineering.proxy.heatExchangerRecipeRegistry.getKeys().contains(RegistryName))
                describeInvalid = String.format("RegistryName null or duplicate: %s", RegistryName);
            else if (RegistryName.endsWith(POSTFIX_REVERSE))
                describeInvalid = String.format("RegistryName contains invalid POSTFIX: %s", POSTFIX_REVERSE);
            if (ExchangeA == null || ExchangeB == null)
                describeInvalid = String.format("ExchangeEntry could not be null: ExchangeA:%s, ExchangeB:%s", ExchangeA, ExchangeB);
            try {
                HeatExchangerSize.valueOf(Size);
            } catch (Exception e) {
                describeInvalid = String.format("Size:'%s' isn't any valueOf: %s", Size,
                        Arrays.toString(HeatExchangerSize.values()));
            }
            return describeInvalid == null;
        }

        @Override
        public String describeInvalid() {
            return describeInvalid;
        }
    }


}
