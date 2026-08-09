package team.cappcraft.immersivechemical.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeatExchangerRecipeSerializerTest {
    private static final ResourceLocation RECIPE_ID = new ResourceLocation(
            "immersivechemical", "serializer_test");
    private final HeatExchangerRecipeSerializer serializer = new HeatExchangerRecipeSerializer();

    @BeforeAll
    static void bootstrapMinecraftRegistries() throws ReflectiveOperationException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // A plain JUnit worker does not construct ForgeMod, so its vanilla FluidType registry
        // objects are unavailable even though the vanilla fluid registry itself is bootstrapped.
        // Supply the same relevant temperatures directly to exercise a successful codec path.
        Field forgeFluidType = Fluid.class.getDeclaredField("forgeFluidType");
        forgeFluidType.setAccessible(true);
        forgeFluidType.set(Fluids.WATER,
                new FluidType(FluidType.Properties.create().temperature(300)));
        forgeFluidType.set(Fluids.LAVA,
                new FluidType(FluidType.Properties.create().temperature(1_300)));
    }

    @Test
    void jsonRejectsMissingMalformedAndUnknownFields() {
        JsonObject missingAmount = validShape();
        missingAmount.remove(HeatExchangerRecipeSerializer.AMOUNT_A);
        assertThrows(JsonSyntaxException.class,
                () -> serializer.fromJson(RECIPE_ID, missingAmount));

        JsonObject malformedId = validShape();
        malformedId.addProperty(HeatExchangerRecipeSerializer.FLUID_A, "not a valid:id:again");
        assertThrows(JsonSyntaxException.class,
                () -> serializer.fromJson(RECIPE_ID, malformedId));

        JsonObject unknownFluid = validShape();
        unknownFluid.addProperty(HeatExchangerRecipeSerializer.FLUID_A, "missing:not_registered");
        assertThrows(JsonSyntaxException.class,
                () -> serializer.fromJson(RECIPE_ID, unknownFluid));

        JsonObject unknownDirection = validShape();
        unknownDirection.addProperty(HeatExchangerRecipeSerializer.DIRECTION, "sideways");
        assertThrows(JsonSyntaxException.class,
                () -> serializer.fromJson(RECIPE_ID, unknownDirection));
    }

    @Test
    void jsonRejectsNonPositiveValuesAndSelfConversions() {
        JsonObject zeroAmount = validShape();
        zeroAmount.addProperty(HeatExchangerRecipeSerializer.AMOUNT_A, 0);
        assertThrows(JsonSyntaxException.class,
                () -> serializer.fromJson(RECIPE_ID, zeroAmount));

        JsonObject negativeHeat = validShape();
        negativeHeat.addProperty(HeatExchangerRecipeSerializer.HEAT, -1);
        assertThrows(JsonSyntaxException.class,
                () -> serializer.fromJson(RECIPE_ID, negativeHeat));

        JsonObject fractionalAmount = validShape();
        fractionalAmount.addProperty(HeatExchangerRecipeSerializer.AMOUNT_A, 1.5);
        assertThrows(JsonSyntaxException.class,
                () -> serializer.fromJson(RECIPE_ID, fractionalAmount));

        JsonObject stringAmount = validShape();
        stringAmount.addProperty(HeatExchangerRecipeSerializer.AMOUNT_A, "1000");
        assertThrows(JsonSyntaxException.class,
                () -> serializer.fromJson(RECIPE_ID, stringAmount));

        JsonObject sameFluid = validShape();
        sameFluid.addProperty(HeatExchangerRecipeSerializer.FLUID_B, "minecraft:water");
        assertThrows(JsonSyntaxException.class,
                () -> serializer.fromJson(RECIPE_ID, sameFluid));
    }

    @Test
    void networkRejectsNonPositiveAmountsBeforeConstructingRecipe() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeResourceLocation(new ResourceLocation("minecraft", "water"));
        buffer.writeVarInt(0);

        assertThrows(DecoderException.class,
                () -> serializer.fromNetwork(RECIPE_ID, buffer));
    }

    @Test
    void networkRejectsUnknownDirectionOrdinal() {
        FriendlyByteBuf buffer = completeNetworkShape();
        buffer.writeVarInt(ConvertDirection.values().length);

        assertThrows(DecoderException.class,
                () -> serializer.fromNetwork(RECIPE_ID, buffer));
    }

    @Test
    void validRecipeRoundTripsThroughNetworkEncoding() {
        HeatExchangerDataRecipe original = serializer.fromJson(RECIPE_ID, validShape());
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        serializer.toNetwork(buffer, original);

        HeatExchangerDataRecipe decoded = serializer.fromNetwork(RECIPE_ID, buffer);
        HeatExchangerEntry entry = decoded.getEntry();
        assertEquals(RECIPE_ID, decoded.getId());
        assertSame(Fluids.LAVA, entry.FluidHot.getFluid());
        assertEquals(1_000, entry.FluidHot.getAmount());
        assertSame(Fluids.WATER, entry.FluidCold.getFluid());
        assertEquals(1_000, entry.FluidCold.getAmount());
        assertEquals(500, entry.HeatValue);
        assertEquals(ConvertDirection.TWO_WAY, entry.Direction);
    }

    private static JsonObject validShape() {
        JsonObject json = new JsonObject();
        json.addProperty(HeatExchangerRecipeSerializer.FLUID_A, "minecraft:water");
        json.addProperty(HeatExchangerRecipeSerializer.AMOUNT_A, 1000);
        json.addProperty(HeatExchangerRecipeSerializer.FLUID_B, "minecraft:lava");
        json.addProperty(HeatExchangerRecipeSerializer.AMOUNT_B, 1000);
        json.addProperty(HeatExchangerRecipeSerializer.HEAT, 500);
        json.addProperty(HeatExchangerRecipeSerializer.DIRECTION, "two_way");
        return json;
    }

    private static FriendlyByteBuf completeNetworkShape() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeResourceLocation(new ResourceLocation("minecraft", "water"));
        buffer.writeVarInt(1000);
        buffer.writeResourceLocation(new ResourceLocation("minecraft", "lava"));
        buffer.writeVarInt(1000);
        buffer.writeVarInt(500);
        return buffer;
    }
}
