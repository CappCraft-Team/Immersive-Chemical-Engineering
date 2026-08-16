package team.cappcraft.immersivechemical.common.recipe;

import java.util.Locale;

public enum ConvertDirection {
    COOL_DOWN,
    HEAT_UP,
    TWO_WAY;

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ConvertDirection fromSerializedName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        for (ConvertDirection direction : values()) {
            if (direction.getSerializedName().equals(normalized)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown conversion direction: " + name);
    }

    public ConvertDirection getOpposite() {
        return switch (this) {
            case COOL_DOWN -> HEAT_UP;
            case HEAT_UP -> COOL_DOWN;
            case TWO_WAY -> throw new IllegalArgumentException("No opposite for: " + this);
        };
    }
}
