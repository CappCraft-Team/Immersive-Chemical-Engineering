package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeatExchangerStructureLayoutTest {

    @Test
    void mediumMatchesLegacyTopologyAndOffsets() {
        assertEquals(new BlockPos(5, 2, 2), HeatExchangerMediumMultiblock.SIZE);
        assertEquals(
                new BlockPos(2, 0, 1),
                HeatExchangerMediumMultiblock.MASTER_FROM_ORIGIN
        );
        assertEquals(
                new BlockPos(0, 1, 1),
                HeatExchangerMediumMultiblock.TRIGGER_FROM_ORIGIN
        );
        assertEquals(4, countMatching(
                HeatExchangerMediumMultiblock.SIZE,
                HeatExchangerMediumMultiblock::isRadiator
        ));
        assertFalse(HeatExchangerMediumMultiblock.isRadiator(
                HeatExchangerMediumMultiblock.MASTER_FROM_ORIGIN
        ));
        assertTrue(HeatExchangerMediumMultiblock.isRadiator(
                HeatExchangerMediumMultiblock.TRIGGER_FROM_ORIGIN
        ));
    }

    @Test
    void largeUsesActualSixtyThreeBlockPalette() {
        assertEquals(new BlockPos(3, 3, 7), HeatExchangerLargeMultiblock.SIZE);
        assertEquals(
                new BlockPos(1, 0, 3),
                HeatExchangerLargeMultiblock.MASTER_FROM_ORIGIN
        );
        assertEquals(
                new BlockPos(1, 1, 6),
                HeatExchangerLargeMultiblock.TRIGGER_FROM_ORIGIN
        );
        assertEquals(14, countMatching(
                HeatExchangerLargeMultiblock.SIZE,
                HeatExchangerLargeMultiblock::isRadiator
        ));
        assertFalse(HeatExchangerLargeMultiblock.isRadiator(
                HeatExchangerLargeMultiblock.MASTER_FROM_ORIGIN
        ));
        assertTrue(HeatExchangerLargeMultiblock.isRadiator(
                HeatExchangerLargeMultiblock.TRIGGER_FROM_ORIGIN
        ));
    }

    private static int countMatching(BlockPos size, Predicate<BlockPos> predicate) {
        int count = 0;
        for (int x = 0; x < size.getX(); ++x) {
            for (int y = 0; y < size.getY(); ++y) {
                for (int z = 0; z < size.getZ(); ++z) {
                    if (predicate.test(new BlockPos(x, y, z))) {
                        ++count;
                    }
                }
            }
        }
        return count;
    }
}
