package team.cappcraft.immersivechemical.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

/**
 * JSON/network serializer for {@link HeatExchangerDataRecipe}.
 *
 * <p>Datapack shape:</p>
 * <pre>{@code
 * {
 *   "type": "immersivechemical:heat_exchanger",
 *   "fluid_a": "example:hot_fluid",
 *   "amount_a": 1000,
 *   "fluid_b": "example:cold_fluid",
 *   "amount_b": 1000,
 *   "heat": 500,
 *   "direction": "two_way"
 * }
 * }</pre>
 */
public final class HeatExchangerRecipeSerializer implements RecipeSerializer<HeatExchangerDataRecipe> {
    static final String FLUID_A = "fluid_a";
    static final String AMOUNT_A = "amount_a";
    static final String FLUID_B = "fluid_b";
    static final String AMOUNT_B = "amount_b";
    static final String HEAT = "heat";
    static final String DIRECTION = "direction";

    @Override
    public HeatExchangerDataRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Objects.requireNonNull(recipeId, "recipeId");
        Objects.requireNonNull(json, "json");
        try {
            ResourceLocation fluidAId = parseResourceLocation(getRequiredString(json, FLUID_A), FLUID_A);
            int amountA = getPositiveInt(json, AMOUNT_A);
            ResourceLocation fluidBId = parseResourceLocation(getRequiredString(json, FLUID_B), FLUID_B);
            int amountB = getPositiveInt(json, AMOUNT_B);
            int heat = getPositiveInt(json, HEAT);
            ConvertDirection direction = ConvertDirection.fromSerializedName(
                    getRequiredString(json, DIRECTION));
            return createRecipe(recipeId, fluidAId, amountA, fluidBId, amountB, heat, direction);
        } catch (RuntimeException exception) {
            throw new JsonSyntaxException(
                    "Invalid heat exchanger recipe " + recipeId + ": " + exception.getMessage(), exception);
        }
    }

    @Override
    public HeatExchangerDataRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Objects.requireNonNull(recipeId, "recipeId");
        Objects.requireNonNull(buffer, "buffer");
        try {
            ResourceLocation fluidAId = buffer.readResourceLocation();
            int amountA = requirePositive(buffer.readVarInt(), AMOUNT_A);
            ResourceLocation fluidBId = buffer.readResourceLocation();
            int amountB = requirePositive(buffer.readVarInt(), AMOUNT_B);
            int heat = requirePositive(buffer.readVarInt(), HEAT);
            int directionOrdinal = buffer.readVarInt();
            ConvertDirection[] directions = ConvertDirection.values();
            if (directionOrdinal < 0 || directionOrdinal >= directions.length) {
                throw new DecoderException("Invalid conversion direction ordinal: " + directionOrdinal);
            }
            return createRecipe(
                    recipeId, fluidAId, amountA, fluidBId, amountB, heat, directions[directionOrdinal]);
        } catch (DecoderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new DecoderException(
                    "Invalid network heat exchanger recipe " + recipeId + ": " + exception.getMessage(),
                    exception);
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, HeatExchangerDataRecipe recipe) {
        Objects.requireNonNull(buffer, "buffer");
        Objects.requireNonNull(recipe, "recipe");
        HeatExchangerEntry entry = recipe.getEntry();
        ResourceLocation hotId = getRegisteredFluidId(entry.FluidHot.getFluid());
        ResourceLocation coldId = getRegisteredFluidId(entry.FluidCold.getFluid());

        buffer.writeResourceLocation(hotId);
        buffer.writeVarInt(entry.FluidHot.getAmount());
        buffer.writeResourceLocation(coldId);
        buffer.writeVarInt(entry.FluidCold.getAmount());
        buffer.writeVarInt(entry.HeatValue);
        buffer.writeVarInt(entry.Direction.ordinal());
    }

    private static HeatExchangerDataRecipe createRecipe(
            ResourceLocation recipeId,
            ResourceLocation fluidAId,
            int amountA,
            ResourceLocation fluidBId,
            int amountB,
            int heat,
            ConvertDirection direction
    ) {
        Fluid fluidA = getRegisteredFluid(fluidAId, FLUID_A);
        Fluid fluidB = getRegisteredFluid(fluidBId, FLUID_B);
        HeatExchangerEntry entry = new HeatExchangerEntry(
                new FluidStack(fluidA, amountA),
                new FluidStack(fluidB, amountB),
                heat,
                direction);
        return new HeatExchangerDataRecipe(recipeId, entry);
    }

    private static Fluid getRegisteredFluid(ResourceLocation id, String field) {
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown fluid ID in '" + field + "': " + id));
        if (fluid == Fluids.EMPTY) {
            throw new IllegalArgumentException("'" + field + "' may not use minecraft:empty");
        }
        return fluid;
    }

    private static ResourceLocation getRegisteredFluidId(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new IllegalArgumentException("Cannot serialize an empty fluid");
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id == null || BuiltInRegistries.FLUID.getOptional(id).orElse(null) != fluid) {
            throw new IllegalArgumentException("Cannot serialize an unregistered fluid");
        }
        return id;
    }

    private static ResourceLocation parseResourceLocation(String value, String field) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            throw new IllegalArgumentException("Invalid resource location in '" + field + "': " + value);
        }
        return id;
    }

    private static int getPositiveInt(JsonObject json, String field) {
        if (!json.has(field) || !json.get(field).isJsonPrimitive()) {
            throw new IllegalArgumentException("Missing integer field '" + field + "'");
        }
        JsonPrimitive primitive = json.getAsJsonPrimitive(field);
        if (!primitive.isNumber()) {
            throw new IllegalArgumentException("'" + field + "' must be an integer");
        }
        try {
            return requirePositive(primitive.getAsBigDecimal().intValueExact(), field);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("'" + field + "' must be a 32-bit integer", exception);
        }
    }

    private static String getRequiredString(JsonObject json, String field) {
        if (!json.has(field) || !json.get(field).isJsonPrimitive()) {
            throw new IllegalArgumentException("Missing string field '" + field + "'");
        }
        JsonPrimitive primitive = json.getAsJsonPrimitive(field);
        if (!primitive.isString()) {
            throw new IllegalArgumentException("'" + field + "' must be a string");
        }
        return primitive.getAsString();
    }

    private static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException("'" + field + "' must be positive");
        }
        return value;
    }
}
