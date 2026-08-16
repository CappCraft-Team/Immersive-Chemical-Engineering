package team.cappcraft.immersivechemical.common.network;

import java.util.Optional;

public enum HeatExchangerMenuAction {
    CLEAR_TANK(true),
    LOCK_FILTER(true),
    UNLOCK_FILTER(true),
    CLEAR_SIDE(true),
    CLEAR_ALL(false);

    public static final int TANK_COUNT = 4;
    private static final int ACTION_STRIDE = TANK_COUNT;

    private final boolean usesTankIndex;

    HeatExchangerMenuAction(boolean usesTankIndex) {
        this.usesTankIndex = usesTankIndex;
    }

    public int encodeButton(int tankIndex) {
        if (usesTankIndex) {
            if (!isTankIndex(tankIndex)) {
                throw new IllegalArgumentException(
                        "Invalid heat exchanger tank index: " + tankIndex);
            }
            if (this == CLEAR_SIDE && tankIndex >= 2) {
                throw new IllegalArgumentException(
                        "Invalid heat exchanger side index: " + tankIndex);
            }
        } else if (tankIndex != 0) {
            throw new IllegalArgumentException("Non-tank actions require index zero");
        }
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
        if ((!action.usesTankIndex && tankIndex != 0)
                || (action.usesTankIndex && !isTankIndex(tankIndex))
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
