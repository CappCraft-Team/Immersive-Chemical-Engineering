package team.cappcraft.immersivechemical.common.recipe;

public enum ConvertDirection {
    COOL_DOWN,
    HEAT_UP,
    TWO_WAY;

    public ConvertDirection getOpposite() {
        switch (this) {
            case COOL_DOWN:
                return HEAT_UP;
            case HEAT_UP:
                return COOL_DOWN;
        }
        throw new IllegalArgumentException(String.format("No opposite for:%s", this));
    }
}
