package team.cappcraft.immersivechemical.common.tileentity.constant;

import team.cappcraft.immersivechemical.Config;





public enum HeatExchangerSizeVariants {
    SMALL,
    MEDIUM,
    LARGE;

    public int getCapacity() {
        switch (this) {
            case SMALL: return Config.getSmallCapacity();
            case MEDIUM: return Config.getMediumCapacity();
            case LARGE: return Config.getLargeCapacity();
        }
        throw new IllegalStateException("Unknown size: " + this);
    }

    public float getTickMultiplier() {
        switch (this) {
            case SMALL: return (float) Config.getSmallTickMultiplier();
            case MEDIUM: return (float) Config.getMediumTickMultiplier();
            case LARGE: return (float) Config.getLargeTickMultiplier();
        }
        throw new IllegalStateException("Unknown size: " + this);
    }
}
