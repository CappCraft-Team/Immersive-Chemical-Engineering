package team.cappcraft.immersivechemical;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class Config {

    public static final ForgeConfigSpec CONFIG_SPEC;

    private static final IntValue SMALL_CAPACITY;
    private static final IntValue MEDIUM_CAPACITY;
    private static final IntValue LARGE_CAPACITY;

    private static final IntValue SMALL_HEAT_TRANSFER_RATE;
    private static final IntValue MEDIUM_HEAT_TRANSFER_RATE;
    private static final IntValue LARGE_HEAT_TRANSFER_RATE;

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

        builder.comment("Heat transfer rate for each heat exchanger size (heat per tick)")
                .push("heat_transfer_rate");
        SMALL_HEAT_TRANSFER_RATE =
                builder.comment("Heat transfer rate for Small Heat Exchanger")
                        .defineInRange("small", 100, 1, Integer.MAX_VALUE);
        MEDIUM_HEAT_TRANSFER_RATE =
                builder.comment("Heat transfer rate for Medium Heat Exchanger")
                        .defineInRange("medium", 200, 1, Integer.MAX_VALUE);
        LARGE_HEAT_TRANSFER_RATE =
                builder.comment("Heat transfer rate for Large Heat Exchanger")
                        .defineInRange("large", 400, 1, Integer.MAX_VALUE);
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

    public static int getSmallHeatTransferRate() {
        return SMALL_HEAT_TRANSFER_RATE.get();
    }

    public static int getMediumHeatTransferRate() {
        return MEDIUM_HEAT_TRANSFER_RATE.get();
    }

    public static int getLargeHeatTransferRate() {
        return LARGE_HEAT_TRANSFER_RATE.get();
    }
}
