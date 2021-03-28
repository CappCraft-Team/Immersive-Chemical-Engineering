package team.cappcraft.immersivechemical.common.compact.jei;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import team.cappcraft.immersivechemical.Config;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;
import team.cappcraft.immersivechemical.common.recipe.constant.HeatExchangerSize;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class HeatExchangerRecipeWrapper implements IRecipeWrapper {
    private static final int TEXT_COLOR = 0xffffff;
    public final int MinDeviceCapacity;
    public final int MinCapacityA;
    public final int MinCapacityB;
    private final HeatExchangerRecipe recipe;

    public HeatExchangerRecipeWrapper(HeatExchangerRecipe recipe) {
        this.recipe = recipe;
        MinCapacityA = Math.max(recipe.UnitInputA, recipe.UnitOutputA);
        MinCapacityB = Math.max(recipe.UnitInputB, recipe.UnitOutputB);
        MinDeviceCapacity = Math.max(MinCapacityA, MinCapacityB);
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID,
                recipe.getJEITotalFluidInputs().stream().map(Arrays::asList).collect(Collectors.toList()));
        ingredients.setOutputLists(VanillaTypes.FLUID,
                recipe.getJEITotalFluidOutputs().stream().map(Arrays::asList).collect(Collectors.toList()));
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        final int x = 7;
        int y = 85;
        minecraft.fontRenderer.drawString(String.format("Heat: %d", recipe.UnitHeatValue),
                x, y, TEXT_COLOR);
        if (recipe.Size != HeatExchangerSize.GENERAL) {
            y += minecraft.fontRenderer.FONT_HEIGHT;
            minecraft.fontRenderer.drawString(String.format("Require: %s",
                    I18n.format("tile.immersivechemical.multiblock_heat_exchanger.heat_exchanger_" + recipe.Size.toString().toLowerCase(Locale.ENGLISH) + ".name")),
                    x, y, TEXT_COLOR);
            drawTickString(minecraft, recipe.Size, x, y);
        } else {
            y = drawTickString(minecraft, HeatExchangerSize.SMALL, x, y);
            y = drawTickString(minecraft, HeatExchangerSize.MEDIUM, x, y);
            drawTickString(minecraft, HeatExchangerSize.LARGE, x, y);
        }
    }

    private int drawTickString(@Nonnull Minecraft minecraft, HeatExchangerSize size, int x, int y) {
        if (getCapacity(size) >= MinDeviceCapacity) {
            y += minecraft.fontRenderer.FONT_HEIGHT;
            minecraft.fontRenderer.drawString(String.format("%s Tick: %d",
                    I18n.format("tile.immersivechemical.multiblock_heat_exchanger.heat_exchanger_" + size.toString().toLowerCase(Locale.ENGLISH) + ".name"),
                    getTick(recipe.UnitHeatValue, size)),
                    x, y, TEXT_COLOR);
        }
        return y;
    }

    private int getTick(int heatValue, HeatExchangerSize size) {
        switch (size) {
            case SMALL:
                return (int) (heatValue * Config.HeatExchangerTickMultiplier.Small);
            case MEDIUM:
                return (int) (heatValue * Config.HeatExchangerTickMultiplier.Medium);
            case LARGE:
                return (int) (heatValue * Config.HeatExchangerTickMultiplier.Large);
        }
        return 0;
    }

    private int getCapacity(HeatExchangerSize size) {
        switch (size) {
            case SMALL:
                return Config.HeatExchangerCapacity.Small;
            case MEDIUM:
                return Config.HeatExchangerCapacity.Medium;
            case LARGE:
                return Config.HeatExchangerCapacity.Large;
        }
        return 0;
    }
}
