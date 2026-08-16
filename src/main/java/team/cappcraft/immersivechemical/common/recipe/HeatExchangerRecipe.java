package team.cappcraft.immersivechemical.common.recipe;

import javax.annotation.Nonnull;

public class HeatExchangerRecipe {
    @Nonnull public final HeatExchangerEntry ExchangeA;
    @Nonnull public final HeatExchangerEntry ExchangeB;

    public final ConvertDirection DirectionA;
    public final ConvertDirection DirectionB;

    public HeatExchangerRecipe(
            HeatExchangerEntry exchangeA,
            HeatExchangerEntry exchangeB,
            ConvertDirection directionA,
            ConvertDirection directionB) {
        ExchangeA = exchangeA;
        ExchangeB = exchangeB;
        DirectionA = directionA;
        DirectionB = directionB;
        if (DirectionA == DirectionB) {
            throw new IllegalArgumentException(
                    "A heat exchanger recipe must heat one side and cool the other");
        }
    }
}
