package team.cappcraft.immersivechemical.common.compact.jei;

import blusunrize.immersiveengineering.api.crafting.IJEIRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import com.google.common.collect.Collections2;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.client.gui.GuiHeatExchanger;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {
    public static final String HEAT_EXCHANGER_RECIPE_CATE_ID = "ichme.heatExchanger";
    public static IModRegistry modRegistry;
    public static IJeiHelpers jeiHelpers;
    public static IGuiHelper guiHelper;

    @SuppressWarnings("rawtypes")
    Map<Class, IERecipeCategory> categories = new LinkedHashMap<>();

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        jeiHelpers = registry.getJeiHelpers();
        guiHelper = jeiHelpers.getGuiHelper();

        categories.put(HeatExchangerRecipe.class, new HeatExchangerRecipeCategory(guiHelper));
        registry.addRecipeCategories(categories.values().toArray(new IRecipeCategory[0]));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(@Nonnull IModRegistry registry) {
        modRegistry = registry;

        for (IERecipeCategory<Object, IRecipeWrapper> cat : categories.values()) {
            cat.addCatalysts(registry);
            modRegistry.handleRecipes(cat.getRecipeClass(), cat, cat.getRecipeCategoryUid());
        }

        modRegistry.addRecipes(Collections2.filter(ImmersiveChemicalEngineering.proxy.heatExchangerRecipeRegistry.getRegisteredRecipes(),
                IJEIRecipe::listInJEI),
                HEAT_EXCHANGER_RECIPE_CATE_ID);

        modRegistry.addRecipeClickArea(GuiHeatExchanger.class,
                GuiHeatExchanger.STATE_X, GuiHeatExchanger.STATE_Y,
                16, 16,
                HEAT_EXCHANGER_RECIPE_CATE_ID);
    }
}
