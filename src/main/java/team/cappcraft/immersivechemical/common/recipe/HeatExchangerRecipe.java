package team.cappcraft.immersivechemical.common.recipe;

import javax.annotation.Nonnull;

public class HeatExchangerRecipe {
    @Nonnull
    public final HeatExchangerEntry ExchangeA;
    @Nonnull
    public final HeatExchangerEntry ExchangeB;

    public final ConvertDirection DirectionA;
    public final ConvertDirection DirectionB;

    public HeatExchangerRecipe(@Nonnull HeatExchangerEntry exchangeA, @Nonnull HeatExchangerEntry exchangeB,
                               ConvertDirection directionA, ConvertDirection directionB) {
        ExchangeA = exchangeA;
        ExchangeB = exchangeB;
        DirectionA = directionA;
        DirectionB = directionB;
    }

    @Override
    public String toString() {
        return "HeatExchangerRecipe{" +
                "ExchangeA=" + ExchangeA +
                ", ExchangeB=" + ExchangeB +
                ", DirectionA=" + DirectionA +
                ", DirectionB=" + DirectionB +
                '}';
    }

}
