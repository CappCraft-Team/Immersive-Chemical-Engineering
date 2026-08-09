package team.cappcraft.immersivechemical.common.compact.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.client.gui.HeatExchangerLargeScreen;
import team.cappcraft.immersivechemical.client.gui.HeatExchangerMediumScreen;
import team.cappcraft.immersivechemical.client.gui.HeatExchangerSmallScreen;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerDataRecipe;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipeRegistration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(
            ImmersiveChemicalEngineering.MODID, "jei_plugin");

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

        List<HeatExchangerEntry> entries = connection.getRecipeManager()
                .getAllRecipesFor(HeatExchangerRecipeRegistration.HEAT_EXCHANGER_TYPE.get())
                .stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .map(HeatExchangerDataRecipe::getEntry)
                .flatMap(JEIPlugin::directionalDisplays)
                .toList();

        if (!entries.isEmpty()) {
            registration.addRecipes(IChemERecipeTypes.HEAT_EXCHANGER, entries);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ICHEME_Contents.HEAT_EXCHANGER_SMALL.blockItem().get()),
                IChemERecipeTypes.HEAT_EXCHANGER
        );
        registration.addRecipeCatalyst(
                new ItemStack(ICHEME_Contents.HEAT_EXCHANGER_MEDIUM.blockItem().get()),
                IChemERecipeTypes.HEAT_EXCHANGER
        );
        registration.addRecipeCatalyst(
                new ItemStack(ICHEME_Contents.HEAT_EXCHANGER_LARGE.blockItem().get()),
                IChemERecipeTypes.HEAT_EXCHANGER
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                HeatExchangerSmallScreen.class,
                81, 55, 16, 15,
                IChemERecipeTypes.HEAT_EXCHANGER
        );
        registration.addRecipeClickArea(
                HeatExchangerMediumScreen.class,
                81, 55, 16, 15,
                IChemERecipeTypes.HEAT_EXCHANGER
        );
        registration.addRecipeClickArea(
                HeatExchangerLargeScreen.class,
                81, 55, 16, 15,
                IChemERecipeTypes.HEAT_EXCHANGER
        );
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
                        ConvertDirection.COOL_DOWN
                ),
                new HeatExchangerEntry(
                        entry.FluidHot,
                        entry.FluidCold,
                        entry.HeatValue,
                        ConvertDirection.HEAT_UP
                )
        );
    }
}
