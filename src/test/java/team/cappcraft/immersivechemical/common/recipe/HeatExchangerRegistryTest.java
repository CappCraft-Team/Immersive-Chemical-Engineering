package team.cappcraft.immersivechemical.common.recipe;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeatExchangerRegistryTest {
    @Test
    void composesUnequalIntermediateAmountsWithoutCreatingFluid() {
        HeatExchangerRegistry.ComposedConversion composed =
                HeatExchangerRegistry.composeConversionAmounts(List.of(
                        new HeatExchangerRegistry.ConversionStep(1_000, 500, 100),
                        new HeatExchangerRegistry.ConversionStep(1_000, 1_000, 300)
                )).orElseThrow();

        assertEquals(
                new HeatExchangerRegistry.ComposedConversion(2_000, 1_000, 500),
                composed
        );
    }

    @Test
    void reducesAndIntegerizesRatiosAcrossAnEntirePath() {
        HeatExchangerRegistry.ComposedConversion composed =
                HeatExchangerRegistry.composeConversionAmounts(List.of(
                        new HeatExchangerRegistry.ConversionStep(3, 2, 5),
                        new HeatExchangerRegistry.ConversionStep(6, 4, 7),
                        new HeatExchangerRegistry.ConversionStep(10, 5, 11)
                )).orElseThrow();

        assertEquals(
                new HeatExchangerRegistry.ComposedConversion(45, 10, 132),
                composed
        );
    }

    @Test
    void rejectsPathsWhoseMinimalFluidTransactionOverflowsAnInt() {
        assertTrue(HeatExchangerRegistry.composeConversionAmounts(List.of(
                new HeatExchangerRegistry.ConversionStep(Integer.MAX_VALUE, 1, 1),
                new HeatExchangerRegistry.ConversionStep(2, 1, 1)
        )).isEmpty());
    }

    @Test
    void rejectsPathsWhoseComposedHeatOverflowsAnInt() {
        assertTrue(HeatExchangerRegistry.composeConversionAmounts(List.of(
                new HeatExchangerRegistry.ConversionStep(1, 1, Integer.MAX_VALUE),
                new HeatExchangerRegistry.ConversionStep(1, 1, 1)
        )).isEmpty());
    }
}
