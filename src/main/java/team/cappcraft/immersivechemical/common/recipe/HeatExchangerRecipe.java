package team.cappcraft.immersivechemical.common.recipe;

import java.util.Objects;
import javax.annotation.Nonnull;

public class HeatExchangerRecipe {
    @Nonnull public final HeatExchangerEntry ExchangeA;
    @Nonnull public final HeatExchangerEntry ExchangeB;

    public final ConvertDirection DirectionA;
    public final ConvertDirection DirectionB;

    public HeatExchangerRecipe(
            @Nonnull HeatExchangerEntry exchangeA,
            @Nonnull HeatExchangerEntry exchangeB,
            ConvertDirection directionA,
            ConvertDirection directionB) {
        ExchangeA = Objects.requireNonNull(exchangeA, "exchangeA");
        ExchangeB = Objects.requireNonNull(exchangeB, "exchangeB");
        DirectionA = requireSpecificDirection(directionA, "directionA");
        DirectionB = requireSpecificDirection(directionB, "directionB");
        if (DirectionA == DirectionB) {
            throw new IllegalArgumentException(
                    "A heat exchanger recipe must heat one side and cool the other");
        }
    }

    private static ConvertDirection requireSpecificDirection(
            ConvertDirection direction, String name) {
        Objects.requireNonNull(direction, name);
        if (direction == ConvertDirection.TWO_WAY) {
            throw new IllegalArgumentException(name + " must be COOL_DOWN or HEAT_UP");
        }
        return direction;
    }
}
