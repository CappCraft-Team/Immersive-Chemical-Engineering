package team.cappcraft.immersivechemical.common.recipe;

import blusunrize.immersiveengineering.api.crafting.IJEIRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.recipe.constant.HeatExchangerSize;
import team.cappcraft.immersivechemical.common.util.MathHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HeatExchangerRecipe implements IJEIRecipe {
    @Nonnull
    public final HeatExchangerRecipeEntry ExchangeA;
    @Nonnull
    public final HeatExchangerRecipeEntry ExchangeB;
    /**
     * the minimum heat value per exchange
     */
    public final int UnitHeatValue;
    /**
     * the minimum fluid amount per exchange
     */
    public final int UnitAmountExchangeA;
    public final int UnitAmountExchangeB;

    public final HeatExchangerSize Size;

    public HeatExchangerRecipe(@Nonnull HeatExchangerRecipeEntry exchangeA, @Nonnull HeatExchangerRecipeEntry exchangeB, HeatExchangerSize size) {
        ExchangeA = exchangeA;
        ExchangeB = exchangeB;
        UnitHeatValue = MathHelper.LeastCommonMultiple(ExchangeA.HeatValue, ExchangeB.HeatValue);
        UnitAmountExchangeA = UnitHeatValue / ExchangeA.HeatValue;
        UnitAmountExchangeB = UnitHeatValue / ExchangeB.HeatValue;

        this.Size = size;
    }

    @Override
    public String toString() {
        return "HeatExchangerRecipe{" +
                "ExchangeA=" + ExchangeA +
                ", ExchangeB=" + ExchangeB +
                ", UnitHeatValue=" + UnitHeatValue +
                ", UnitAmountExchangeA=" + UnitAmountExchangeA +
                ", UnitAmountExchangeB=" + UnitAmountExchangeB +
                ", Size=" + Size +
                '}';
    }

    @Override
    public List<ItemStack> getJEITotalItemInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<ItemStack> getJEITotalItemOutputs() {
        return Collections.emptyList();
    }

    @Override
    public List<FluidStack> getJEITotalFluidInputs() {
        return ImmersiveChemicalEngineering.proxy.heatExchangerRecipeRegistry.getKey(this).endsWith(HeatExchangerRecipeRegistry.POSTFIX_REVERSE) ?
                Collections.emptyList() : Arrays.asList(
                new FluidStack(ExchangeA.Input, UnitAmountExchangeA),
                new FluidStack(ExchangeB.Input, UnitAmountExchangeB));
    }

    @Override
    public List<FluidStack> getJEITotalFluidOutputs() {
        return ImmersiveChemicalEngineering.proxy.heatExchangerRecipeRegistry.getKey(this).endsWith(HeatExchangerRecipeRegistry.POSTFIX_REVERSE) ?
                Collections.emptyList() : Arrays.asList(
                new FluidStack(ExchangeA.Output, UnitAmountExchangeA),
                new FluidStack(ExchangeB.Output, UnitAmountExchangeB));
    }
}
