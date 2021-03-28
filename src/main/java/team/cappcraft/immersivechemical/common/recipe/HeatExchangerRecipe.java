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
    public final int UnitInputA;
    public final int UnitOutputA;
    public final int UnitInputB;
    public final int UnitOutputB;

    public final HeatExchangerSize Size;

    public HeatExchangerRecipe(@Nonnull HeatExchangerRecipeEntry exchangeA, @Nonnull HeatExchangerRecipeEntry exchangeB, HeatExchangerSize size) {
        ExchangeA = exchangeA;
        ExchangeB = exchangeB;
        UnitHeatValue = MathHelper.LeastCommonMultiple(ExchangeA.HeatValue, ExchangeB.HeatValue);

        final int UnitA = UnitHeatValue / ExchangeA.HeatValue;
        final int UnitB = UnitHeatValue / ExchangeB.HeatValue;

        UnitInputA = ExchangeA.Input.amount * UnitA;
        UnitOutputA = ExchangeA.Output.amount * UnitA;
        UnitInputB = ExchangeB.Input.amount * UnitB;
        UnitOutputB = ExchangeB.Output.amount * UnitB;

        this.Size = size;
    }

    @Override
    public String toString() {
        return "HeatExchangerRecipe{" +
                "ExchangeA=" + ExchangeA +
                ", ExchangeB=" + ExchangeB +
                ", UnitHeatValue=" + UnitHeatValue +
                ", UnitInputA=" + UnitInputA +
                ", UnitOutputA=" + UnitOutputA +
                ", UnitInputB=" + UnitInputB +
                ", UnitOutputB=" + UnitOutputB +
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
        return Arrays.asList(
                new FluidStack(ExchangeA.Input, UnitInputA),
                new FluidStack(ExchangeB.Input, UnitInputB));
    }

    @Override
    public List<FluidStack> getJEITotalFluidOutputs() {
        return Arrays.asList(
                new FluidStack(ExchangeA.Output, UnitOutputA),
                new FluidStack(ExchangeB.Output, UnitOutputB));
    }

    @Override
    public boolean listInJEI() {
        return ImmersiveChemicalEngineering.proxy.heatExchangerRecipeRegistry.getKey(this)
                .endsWith(HeatExchangerRecipeRegistry.POSTFIX_REVERSE);
    }
}
