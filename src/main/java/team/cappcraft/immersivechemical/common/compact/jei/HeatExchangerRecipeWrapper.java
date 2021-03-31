package team.cappcraft.immersivechemical.common.compact.jei;

import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;

import javax.annotation.Nonnull;

public class HeatExchangerRecipeWrapper implements IRecipeWrapper {
    private static final int TEXT_COLOR = 0xffffff;
    private static final int ICON_X = 135;
    private static final int ICON_WIDTH = 15;
    private static final int ICON_HEIGHT = 5;
    private static final int ICON_HOT_Y = 6;
    private static final int ICON_DRAW_X = 59;
    private static final int ICON_DRAW_Y_COLD = 25;
    private static final int ICON_DRAW_Y_HOT = ICON_DRAW_Y_COLD + ICON_HEIGHT + 4;
    private static final IDrawableStatic COLD_ICON =
            JEIPlugin.guiHelper.createDrawable(HeatExchangerRecipeCategory.resourceLocation, ICON_X, 0, ICON_WIDTH, ICON_HEIGHT);
    private static final IDrawableStatic HOT_ICON =
            JEIPlugin.guiHelper.createDrawable(HeatExchangerRecipeCategory.resourceLocation, ICON_X, ICON_HOT_Y, ICON_WIDTH, ICON_HEIGHT);
    public final int MinCapacity;
    private final HeatExchangerEntry Entry;

    public HeatExchangerRecipeWrapper(HeatExchangerEntry Entry) {
        this.Entry = Entry;
        MinCapacity = Math.max(Entry.FluidHot.amount, Entry.FluidCold.amount);
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, Entry.FluidHot);
        ingredients.setOutput(VanillaTypes.FLUID, Entry.FluidCold);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString(String.format("Heat: %d", Entry.HeatValue), 20, 56, TEXT_COLOR);

        if (Entry.Direction == ConvertDirection.COOL_DOWN || Entry.Direction == ConvertDirection.TWO_WAY) {
            COLD_ICON.draw(minecraft, ICON_DRAW_X, ICON_DRAW_Y_COLD);
        }
        if (Entry.Direction == ConvertDirection.HEAT_UP || Entry.Direction == ConvertDirection.TWO_WAY) {
            HOT_ICON.draw(minecraft, ICON_DRAW_X + 1, ICON_DRAW_Y_HOT);
        }
    }
}
