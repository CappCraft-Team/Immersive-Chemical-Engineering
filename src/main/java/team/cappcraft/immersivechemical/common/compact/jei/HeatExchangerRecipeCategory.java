package team.cappcraft.immersivechemical.common.compact.jei;

import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import com.google.common.collect.Lists;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
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

        List<FluidStack> left = Lists.newArrayList(recipeWrapper.Entry.FluidHot);
        fluidStacks.init(i,
                recipeWrapper.Entry.Direction == ConvertDirection.COOL_DOWN
                        || recipeWrapper.Entry.Direction == ConvertDirection.TWO_WAY,
                x, y, 16, 16, recipeWrapper.MinCapacity, true, null);
        fluidStacks.set(i++, left);

        x += 53;
        List<FluidStack> right = Lists.newArrayList(recipeWrapper.Entry.FluidCold);
        fluidStacks.init(i,
                recipeWrapper.Entry.Direction == ConvertDirection.HEAT_UP
                        || recipeWrapper.Entry.Direction == ConvertDirection.TWO_WAY,
                x, y, 16, 16, recipeWrapper.MinCapacity, true, null);
        fluidStacks.set(i, right);
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
