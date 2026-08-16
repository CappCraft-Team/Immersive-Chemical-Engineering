package team.cappcraft.immersivechemical.common.network;

import java.util.Optional;

public enum HeatExchangerMenuAction {
    CLEAR_TANK,
    LOCK_FILTER,
    UNLOCK_FILTER,
    CLEAR_SIDE,
    CLEAR_ALL;

    public static final int TANK_COUNT = 4;
    private static final int ACTION_STRIDE = TANK_COUNT;

    public int encodeButton(int tankIndex) {
        return ordinal() * ACTION_STRIDE + tankIndex;
    }

    public static Optional<Command> decodeButton(int buttonId) {
        if (buttonId < 0) {
            return Optional.empty();
        }

        int actionIndex = buttonId / ACTION_STRIDE;
        int tankIndex = buttonId % ACTION_STRIDE;
        HeatExchangerMenuAction[] actions = values();
        if (actionIndex >= actions.length) {
            return Optional.empty();
        }

        HeatExchangerMenuAction action = actions[actionIndex];
        boolean usesTankIndex = action != CLEAR_ALL;
        if ((!usesTankIndex && tankIndex != 0)
                || (usesTankIndex && !isTankIndex(tankIndex))
                || (action == CLEAR_SIDE && tankIndex >= 2)) {
            return Optional.empty();
        }
        return Optional.of(new Command(action, tankIndex));
    }

    private static boolean isTankIndex(int tankIndex) {
        return tankIndex >= 0 && tankIndex < TANK_COUNT;
    }

    public record Command(HeatExchangerMenuAction action, int tankIndex) {}
}
