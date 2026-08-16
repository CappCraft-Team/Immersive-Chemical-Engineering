package team.cappcraft.immersivechemical.common.compact.jei;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.client.gui.HeatExchangerScreen;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerDataRecipe;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipeRegistration;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static final RecipeType<HeatExchangerEntry> HEAT_EXCHANGER_RECIPE_TYPE =
            RecipeType.create(
                    ImmersiveChemicalEngineering.MODID, "heat_exchanger", HeatExchangerEntry.class);

    private static final ResourceLocation PLUGIN_ID =
            ResourceLocation.fromNamespaceAndPath(ImmersiveChemicalEngineering.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new HeatExchangerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return;
        }

        List<HeatExchangerEntry> entries =
                connection
                        .getRecipeManager()
                        .getAllRecipesFor(HeatExchangerRecipeRegistration.HEAT_EXCHANGER_TYPE.get())
                        .stream()
                        .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                        .map(HeatExchangerDataRecipe::getEntry)
                        .flatMap(JEIPlugin::directionalDisplays)
                        .toList();

        if (!entries.isEmpty()) {
            registration.addRecipes(JEIPlugin.HEAT_EXCHANGER_RECIPE_TYPE, entries);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ICHEME_Contents.HEAT_EXCHANGER_SMALL.blockItem().get()),
                JEIPlugin.HEAT_EXCHANGER_RECIPE_TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(ICHEME_Contents.HEAT_EXCHANGER_MEDIUM.blockItem().get()),
                JEIPlugin.HEAT_EXCHANGER_RECIPE_TYPE);
        registration.addRecipeCatalyst(
                new ItemStack(ICHEME_Contents.HEAT_EXCHANGER_LARGE.blockItem().get()),
                JEIPlugin.HEAT_EXCHANGER_RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                HeatExchangerScreen.class, 81, 55, 16, 15, JEIPlugin.HEAT_EXCHANGER_RECIPE_TYPE);
    }

    private static Stream<HeatExchangerEntry> directionalDisplays(HeatExchangerEntry entry) {
        if (entry.Direction != ConvertDirection.TWO_WAY) {
            return Stream.of(entry);
        }
        return Stream.of(
                new HeatExchangerEntry(
                        entry.FluidHot,
                        entry.FluidCold,
                        entry.HeatValue,
                        ConvertDirection.COOL_DOWN),
                new HeatExchangerEntry(
                        entry.FluidHot,
                        entry.FluidCold,
                        entry.HeatValue,
                        ConvertDirection.HEAT_UP));
    }
}
