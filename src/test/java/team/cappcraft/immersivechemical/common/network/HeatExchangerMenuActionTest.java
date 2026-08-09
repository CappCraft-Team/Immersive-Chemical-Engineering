package team.cappcraft.immersivechemical.common.network;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeatExchangerMenuActionTest {
    @Test
    void tankActionsRoundTripEveryValidIndex() {
        HeatExchangerMenuAction[] tankActions = {
                HeatExchangerMenuAction.CLEAR_TANK,
                HeatExchangerMenuAction.LOCK_FILTER,
                HeatExchangerMenuAction.UNLOCK_FILTER
        };

        for (HeatExchangerMenuAction action : tankActions) {
            for (int tankIndex = 0; tankIndex < HeatExchangerMenuAction.TANK_COUNT; ++tankIndex) {
                Optional<HeatExchangerMenuAction.Command> decoded =
                        HeatExchangerMenuAction.decodeButton(action.encodeButton(tankIndex));
                assertTrue(decoded.isPresent());
                assertEquals(action, decoded.orElseThrow().action());
                assertEquals(tankIndex, decoded.orElseThrow().tankIndex());
            }
        }
    }

    @Test
    void clearAllOnlyAcceptsCanonicalZeroIndex() {
        int encoded = HeatExchangerMenuAction.CLEAR_ALL.encodeButton(0);
        HeatExchangerMenuAction.Command decoded =
                HeatExchangerMenuAction.decodeButton(encoded).orElseThrow();
        assertEquals(HeatExchangerMenuAction.CLEAR_ALL, decoded.action());
        assertEquals(0, decoded.tankIndex());

        assertThrows(
                IllegalArgumentException.class,
                () -> HeatExchangerMenuAction.CLEAR_ALL.encodeButton(1)
        );
        assertTrue(HeatExchangerMenuAction.decodeButton(encoded + 1).isEmpty());
    }

    @Test
    void sideClearOnlyAcceptsTheTwoMachineSides() {
        for (int sideIndex = 0; sideIndex < 2; ++sideIndex) {
            HeatExchangerMenuAction.Command decoded = HeatExchangerMenuAction.decodeButton(
                    HeatExchangerMenuAction.CLEAR_SIDE.encodeButton(sideIndex)
            ).orElseThrow();
            assertEquals(HeatExchangerMenuAction.CLEAR_SIDE, decoded.action());
            assertEquals(sideIndex, decoded.tankIndex());
        }

        int invalidEncodedId = HeatExchangerMenuAction.CLEAR_SIDE.ordinal()
                * HeatExchangerMenuAction.TANK_COUNT + 2;
        assertTrue(HeatExchangerMenuAction.decodeButton(invalidEncodedId).isEmpty());
        assertThrows(
                IllegalArgumentException.class,
                () -> HeatExchangerMenuAction.CLEAR_SIDE.encodeButton(2)
        );
    }

    @Test
    void malformedButtonIdsAreRejected() {
        assertTrue(HeatExchangerMenuAction.decodeButton(-1).isEmpty());
        assertTrue(HeatExchangerMenuAction.decodeButton(Integer.MAX_VALUE).isEmpty());
        assertThrows(
                IllegalArgumentException.class,
                () -> HeatExchangerMenuAction.CLEAR_TANK.encodeButton(-1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> HeatExchangerMenuAction.LOCK_FILTER.encodeButton(
                        HeatExchangerMenuAction.TANK_COUNT
                )
        );
    }
}
