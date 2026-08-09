package team.cappcraft.immersivechemical.common.compact.jei;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;

public class IChemERecipeTypes {
    public static final RecipeType<HeatExchangerEntry> HEAT_EXCHANGER =
            RecipeType.create(ImmersiveChemicalEngineering.MODID, "heat_exchanger", HeatExchangerEntry.class);
}
