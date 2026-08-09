package team.cappcraft.immersivechemical.common.recipe;

import java.util.Locale;
import java.util.Objects;

public enum ConvertDirection {
    COOL_DOWN,
    HEAT_UP,
    TWO_WAY;

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ConvertDirection fromSerializedName(String name) {
        Objects.requireNonNull(name, "name");
        String normalized = name.toLowerCase(Locale.ROOT);
        for (ConvertDirection direction : values()) {
            if (direction.getSerializedName().equals(normalized)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown conversion direction: " + name);
    }

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
