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
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.BlockTypes_HeatExchanger;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;

import javax.annotation.Nonnull;
import java.util.List;

public class HeatExchangerRecipeCategory extends IERecipeCategory<HeatExchangerRecipe, HeatExchangerRecipeWrapper> {

    public HeatExchangerRecipeCategory(IGuiHelper guiHelper) {
        super("heatExchanger",
                "category.heat_exchanger.name",
                guiHelper.createDrawable(
                        new ResourceLocation(ImmersiveChemicalEngineering.MODID, "textures/gui/heatexchanger_category.png"),
                        0, 0,
                        135, 140),
                HeatExchangerRecipe.class,
                new ItemStack(ICHEME_Contents.blockHeatExchanger, 1, BlockTypes_HeatExchanger.HEAT_EXCHANGER_SMALL.getMeta()),
                new ItemStack(ICHEME_Contents.blockHeatExchanger, 1, BlockTypes_HeatExchanger.HEAT_EXCHANGER_MEDIUM.getMeta()),
                new ItemStack(ICHEME_Contents.blockHeatExchanger, 1, BlockTypes_HeatExchanger.HEAT_EXCHANGER_LARGE.getMeta()));
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull HeatExchangerRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        final IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        int x = 31;
        int y = 18;
        int i = 0;
        for (List<FluidStack> input : ingredients.getInputs(VanillaTypes.FLUID)) {
            fluidStacks.init(i, true, x, y, 16, 16,
                    i == 0 ? recipeWrapper.MinCapacityA : recipeWrapper.MinCapacityB,
                    true, null);
            fluidStacks.set(i++, input);
            x += 57;
        }
        x = 31;
        y += 23;
        for (List<FluidStack> output : ingredients.getOutputs(VanillaTypes.FLUID)) {
            fluidStacks.init(i, false, x, y, 16, 16,
                    i == 2 ? recipeWrapper.MinCapacityA : recipeWrapper.MinCapacityB,
                    true, null);
            fluidStacks.set(i++, output);
            x += 57;
        }
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull HeatExchangerRecipe recipe) {
        return new HeatExchangerRecipeWrapper(recipe);
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
