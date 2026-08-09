package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.cappcraft.immersivechemical.common.tileentity.LockableFluidTank;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeatExchangerAutomaticOutputTest {

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void everyOutputPortTargetsTheCorrectAdjacentFace() {
        assertTarget(
                HeatExchangerSmallLogic.OUTPUT_A,
                new BlockPos(0, -1, 0),
                RelativeBlockFace.UP
        );
        assertTarget(
                HeatExchangerSmallLogic.OUTPUT_B,
                new BlockPos(0, -1, 3),
                RelativeBlockFace.UP
        );
        assertTarget(
                HeatExchangerMediumLogic.OUTPUT_A,
                new BlockPos(0, -1, 1),
                RelativeBlockFace.UP
        );
        assertTarget(
                HeatExchangerMediumLogic.OUTPUT_B,
                new BlockPos(1, 2, 1),
                RelativeBlockFace.DOWN
        );
        assertTarget(
                HeatExchangerLargeLogic.OUTPUT_A,
                new BlockPos(1, -1, 6),
                RelativeBlockFace.UP
        );
        assertTarget(
                HeatExchangerLargeLogic.OUTPUT_B,
                new BlockPos(1, 3, 1),
                RelativeBlockFace.DOWN
        );
    }

    @Test
    void pushesAtMostOneBucketPerTick() {
        AtomicInteger changes = new AtomicInteger();
        LockableFluidTank source = sourceTank(1500, changes);
        FluidTank target = new FluidTank(5000);
        changes.set(0);

        int moved = AbstractHeatExchangerLogic.pushOutputFluid(target, source);

        assertEquals(AbstractHeatExchangerLogic.AUTOMATIC_OUTPUT_RATE, moved);
        assertEquals(500, source.getFluidAmount());
        assertEquals(1000, target.getFluidAmount());
        assertEquals(1, changes.get());
    }

    @Test
    void respectsPartialCapacityAndRejectsIncompatibleFluid() {
        LockableFluidTank source = sourceTank(2000, new AtomicInteger());
        FluidTank partialTarget = new FluidTank(600);

        assertEquals(600, AbstractHeatExchangerLogic.pushOutputFluid(partialTarget, source));
        assertEquals(1400, source.getFluidAmount());
        assertEquals(600, partialTarget.getFluidAmount());

        FluidTank incompatibleTarget = new FluidTank(2000);
        incompatibleTarget.setFluid(new FluidStack(Fluids.LAVA, 500));
        assertEquals(0, AbstractHeatExchangerLogic.pushOutputFluid(incompatibleTarget, source));
        assertEquals(1400, source.getFluidAmount());
        assertEquals(500, incompatibleTarget.getFluidAmount());
        assertTrue(incompatibleTarget.getFluid().isFluidEqual(new FluidStack(Fluids.LAVA, 1)));
    }

    @Test
    void hardLockAndMissingTargetCannotMoveOrDuplicateFluid() {
        AtomicInteger changes = new AtomicInteger();
        LockableFluidTank source = sourceTank(1000, changes);
        FluidTank target = new FluidTank(5000);
        source.setLocked(true);
        changes.set(0);

        assertEquals(0, AbstractHeatExchangerLogic.pushOutputFluid(target, source));
        assertEquals(0, AbstractHeatExchangerLogic.pushOutputFluid(null, source));
        assertEquals(1000, source.getFluidAmount());
        assertEquals(0, target.getFluidAmount());
        assertEquals(0, changes.get());
    }

    @Test
    void typeLockSurvivesAutomaticDrain() {
        AtomicInteger changes = new AtomicInteger();
        LockableFluidTank source = sourceTank(1000, changes);
        FluidTank target = new FluidTank(5000);
        source.setLockedType(true);
        changes.set(0);

        assertEquals(1000, AbstractHeatExchangerLogic.pushOutputFluid(target, source));
        assertEquals(0, source.getFluidAmount());
        assertTrue(source.isLockedType());
        assertEquals(1000, target.getFluidAmount());
        assertEquals(1, changes.get());
    }

    private static LockableFluidTank sourceTank(int amount, AtomicInteger changes) {
        LockableFluidTank source = new LockableFluidTank(5000, ignored -> changes.incrementAndGet());
        source.setFluid(new FluidStack(Fluids.WATER, amount));
        return source;
    }

    private static void assertTarget(
            CapabilityPosition outputPort,
            BlockPos expectedPosition,
            RelativeBlockFace expectedFace
    ) {
        MultiblockFace target = AbstractHeatExchangerLogic.getAdjacentTargetFace(outputPort);
        assertEquals(expectedPosition, target.posInMultiblock());
        assertEquals(expectedFace, target.face());
        assertEquals(outputPort, CapabilityPosition.opposing(target));
    }
}
