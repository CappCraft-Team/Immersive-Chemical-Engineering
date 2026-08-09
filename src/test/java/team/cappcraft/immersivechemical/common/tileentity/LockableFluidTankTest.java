package team.cappcraft.immersivechemical.common.tileentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LockableFluidTankTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void emptyTankAcceptsFluidAndNeverReturnsNullFromDrain() {
        LockableFluidTank tank = new LockableFluidTank(1_000, ignored -> { });

        assertEquals(400, tank.fill(new FluidStack(Fluids.WATER, 400), FluidAction.EXECUTE));
        assertEquals(400, tank.getFluidAmount());
        assertTrue(tank.drain(new FluidStack(Fluids.LAVA, 100), FluidAction.EXECUTE).isEmpty());

        assertEquals(400, tank.drain(1_000, FluidAction.EXECUTE).getAmount());
        assertTrue(tank.drain(1, FluidAction.EXECUTE).isEmpty());
    }

    @Test
    void contentLockBlocksBothFillAndDrain() {
        LockableFluidTank tank = new LockableFluidTank(
                new FluidStack(Fluids.WATER, 400), 1_000, ignored -> { });
        tank.setLocked(true);

        assertEquals(0, tank.fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE));
        assertTrue(tank.drain(100, FluidAction.EXECUTE).isEmpty());
        assertEquals(400, tank.getFluidAmount());
        assertFalse(tank.isFluidValid(new FluidStack(Fluids.WATER, 100)));
    }

    @Test
    void typeLockSurvivesEmptyingAndNbtRoundTrip() {
        LockableFluidTank tank = new LockableFluidTank(
                new FluidStack(Fluids.WATER, 400), 1_000, ignored -> { });
        tank.setLockedType(true);
        tank.drain(1_000, FluidAction.EXECUTE);

        assertEquals(0, tank.fill(new FluidStack(Fluids.LAVA, 100), FluidAction.EXECUTE));
        assertEquals(100, tank.fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE));
        tank.drain(1_000, FluidAction.EXECUTE);

        CompoundTag saved = tank.writeToNBT(new CompoundTag());
        LockableFluidTank loaded = new LockableFluidTank(1_000, ignored -> { });
        loaded.readFromNBT(saved);

        assertTrue(loaded.isLockedType());
        assertEquals(0, loaded.fill(new FluidStack(Fluids.LAVA, 100), FluidAction.EXECUTE));
        assertEquals(100, loaded.fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE));
    }

    @Test
    void simulationDoesNotMutateOrNotify() {
        AtomicInteger notifications = new AtomicInteger();
        LockableFluidTank tank = new LockableFluidTank(1_000, ignored -> notifications.incrementAndGet());

        assertEquals(250, tank.fill(new FluidStack(Fluids.WATER, 250), FluidAction.SIMULATE));
        assertEquals(0, tank.getFluidAmount());
        assertEquals(0, notifications.get());

        tank.fill(new FluidStack(Fluids.WATER, 250), FluidAction.EXECUTE);
        assertEquals(1, notifications.get());
    }
}
