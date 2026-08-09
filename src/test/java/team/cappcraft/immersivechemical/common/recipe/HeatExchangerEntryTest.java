package team.cappcraft.immersivechemical.common.recipe;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HeatExchangerEntryTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void invalidEntriesFailFast() {
        FluidStack water = new FluidStack(Fluids.WATER, 100);
        FluidStack lava = new FluidStack(Fluids.LAVA, 100);

        assertThrows(IllegalArgumentException.class,
                () -> new HeatExchangerEntry(water, water, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new HeatExchangerEntry(water, lava, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new HeatExchangerEntry(FluidStack.EMPTY, lava, 100));
    }

    @Test
    void conversionDirectionsHaveStableDatapackNames() {
        for (ConvertDirection direction : ConvertDirection.values()) {
            assertEquals(direction,
                    ConvertDirection.fromSerializedName(direction.getSerializedName()));
        }
        assertEquals(ConvertDirection.TWO_WAY,
                ConvertDirection.fromSerializedName("TWO_WAY"));
        assertThrows(IllegalArgumentException.class,
                () -> ConvertDirection.fromSerializedName("sideways"));
    }
}
