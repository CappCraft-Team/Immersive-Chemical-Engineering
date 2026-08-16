package team.cappcraft.immersivechemical.common.recipe;

import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;

public final class HeatExchangerRecipeRegistration {
    public static final ResourceLocation ID =
            new ResourceLocation(ImmersiveChemicalEngineering.MODID, "heat_exchanger");

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(
                    ForgeRegistries.RECIPE_TYPES, ImmersiveChemicalEngineering.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(
                    ForgeRegistries.RECIPE_SERIALIZERS, ImmersiveChemicalEngineering.MODID);

    public static final RegistryObject<RecipeType<HeatExchangerDataRecipe>> HEAT_EXCHANGER_TYPE =
            RECIPE_TYPES.register("heat_exchanger", () -> RecipeType.simple(ID));
    public static final RegistryObject<HeatExchangerRecipeSerializer> HEAT_EXCHANGER_SERIALIZER =
            RECIPE_SERIALIZERS.register("heat_exchanger", HeatExchangerRecipeSerializer::new);

    private HeatExchangerRecipeRegistration() {}

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        RecipeManager recipeManager = event.getServerResources().getRecipeManager();
        event.addListener(
                (ResourceManagerReloadListener) resourceManager -> replaceFrom(recipeManager));
    }

    static void replaceFrom(RecipeManager recipeManager) {
        List<HeatExchangerEntry> snapshot =
                recipeManager.getAllRecipesFor(HEAT_EXCHANGER_TYPE.get()).stream()
                        .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                        .map(HeatExchangerDataRecipe::getEntry)
                        .toList();
        HeatExchangerRegistry.REGISTRY.replaceRegisteredEntries(snapshot);
        ImmersiveChemicalEngineering.LOGGER.info(
                "Loaded {} heat exchanger recipe(s).", snapshot.size());
    }
}
