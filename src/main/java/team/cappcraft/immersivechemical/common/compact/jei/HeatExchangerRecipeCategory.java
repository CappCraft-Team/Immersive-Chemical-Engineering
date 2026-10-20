package team.cappcraft.immersivechemical.common.compact.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import team.cappcraft.immersivechemical.IMMCHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;

public class HeatExchangerRecipeCategory implements IRecipeCategory<HeatExchangerEntry> {

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    ImmersiveChemicalEngineering.MODID, "textures/gui/heatexchanger.png");

    private final IDrawable background;
    private final IDrawable icon;

    public HeatExchangerRecipeCategory(IGuiHelper guiHelper) {

        this.background = guiHelper.createDrawable(TEXTURE, 15, 20, 145, 45);
        this.icon =
                guiHelper.createDrawableItemStack(
                        new ItemStack(IMMCHEME_Contents.HEAT_EXCHANGER_SMALL.blockItem().get()));
    }

    @Override
    public RecipeType<HeatExchangerEntry> getRecipeType() {
        return JEIPlugin.HEAT_EXCHANGER_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("category.immersivechemical.heat_exchanger");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder, HeatExchangerEntry recipe, IFocusGroup focuses) {
        boolean heatUp = recipe.Direction == ConvertDirection.HEAT_UP;
        FluidStack input = heatUp ? recipe.FluidCold : recipe.FluidHot;
        FluidStack output = heatUp ? recipe.FluidHot : recipe.FluidCold;

        builder.addSlot(RecipeIngredientRole.INPUT, 20, 14)
                .setFluidRenderer(input.getAmount(), false, 16, 16)
                .addFluidStack(input.getFluid(), input.getAmount());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 14)
                .setFluidRenderer(output.getAmount(), false, 16, 16)
                .addFluidStack(output.getFluid(), output.getAmount());
    }
}
