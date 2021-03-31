package team.cappcraft.immersivechemical.common.compact.jei;

import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRegistry;

import javax.annotation.Nonnull;
import java.util.List;

public class HeatExchangerRecipeCategory extends IERecipeCategory<HeatExchangerEntry, HeatExchangerRecipeWrapper> {
    public static final ResourceLocation resourceLocation =
            new ResourceLocation(ImmersiveChemicalEngineering.MODID, "textures/gui/heatexchanger_category.png");

    public HeatExchangerRecipeCategory(IGuiHelper guiHelper) {
        super("heatExchanger",
                "category.heat_exchanger.name",
                guiHelper.createDrawable(
                        resourceLocation,
                        0, 0,
                        135, 100),
                HeatExchangerEntry.class,
                HeatExchangerRegistry.REGISTRY.getRegisteredHeatExchanger().values().toArray(new ItemStack[0]));
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull HeatExchangerRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        final IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        int x = 33;
        int y = 24;

        int i = 0;

        List<FluidStack> input = ingredients.getInputs(VanillaTypes.FLUID).get(0);
        fluidStacks.init(i, true, x, y, 16, 16, recipeWrapper.MinCapacity, true, null);
        fluidStacks.set(i++, input);

        x += 53;
        List<FluidStack> output = ingredients.getOutputs(VanillaTypes.FLUID).get(0);
        fluidStacks.init(i, false, x, y, 16, 16, recipeWrapper.MinCapacity, true, null);
        fluidStacks.set(i, output);
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull HeatExchangerEntry entry) {
        return new HeatExchangerRecipeWrapper(entry);
    }

    @Nonnull
    @Override
    public String getUid() {
        return "ichme." + uniqueName;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid() {
        return "ichme." + uniqueName;
    }

    @Nonnull
    @Override
    public String getModName() {
        return ImmersiveChemicalEngineering.NAME;
    }
}
