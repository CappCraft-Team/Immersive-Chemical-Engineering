package team.cappcraft.immersivechemical;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class Config {

    public static final ForgeConfigSpec CONFIG_SPEC;

    private static final IntValue SMALL_CAPACITY;
    private static final IntValue MEDIUM_CAPACITY;
    private static final IntValue LARGE_CAPACITY;

    private static final DoubleValue SMALL_TICK_MULTIPLIER;
    private static final DoubleValue MEDIUM_TICK_MULTIPLIER;
    private static final DoubleValue LARGE_TICK_MULTIPLIER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Heat Exchanger Settings").push("heat_exchanger");

        builder.comment("Fluid capacity for each heat exchanger size (mB)").push("capacity");
        SMALL_CAPACITY =
                builder.comment("Capacity of the Small Heat Exchanger")
                        .defineInRange("small", 5000, 1, Integer.MAX_VALUE);
        MEDIUM_CAPACITY =
                builder.comment("Capacity of the Medium Heat Exchanger")
                        .defineInRange("medium", 10000, 1, Integer.MAX_VALUE);
        LARGE_CAPACITY =
                builder.comment("Capacity of the Large Heat Exchanger")
                        .defineInRange("large", 30000, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("Tick multiplier for each heat exchanger size. Lower = faster.")
                .push("tick_multiplier");
        SMALL_TICK_MULTIPLIER =
                builder.comment("Tick multiplier for Small Heat Exchanger")
                        .defineInRange("small", 0.8, 1e-7, 1e7);
        MEDIUM_TICK_MULTIPLIER =
                builder.comment("Tick multiplier for Medium Heat Exchanger")
                        .defineInRange("medium", 0.4, 1e-7, 1e7);
        LARGE_TICK_MULTIPLIER =
                builder.comment("Tick multiplier for Large Heat Exchanger")
                        .defineInRange("large", 0.2, 1e-7, 1e7);
        builder.pop();

        builder.pop();

        CONFIG_SPEC = builder.build();
    }

    public static int getSmallCapacity() {
        return SMALL_CAPACITY.get();
    }

    public static int getMediumCapacity() {
        return MEDIUM_CAPACITY.get();
    }

    public static int getLargeCapacity() {
        return LARGE_CAPACITY.get();
    }

    public static double getSmallTickMultiplier() {
        return SMALL_TICK_MULTIPLIER.get();
    }

    public static double getMediumTickMultiplier() {
        return MEDIUM_TICK_MULTIPLIER.get();
    }

    public static double getLargeTickMultiplier() {
        return LARGE_TICK_MULTIPLIER.get();
    }
}
