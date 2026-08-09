package team.cappcraft.immersivechemical.common.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Datapack representation of one heat-exchanger fluid conversion.
 *
 * <p>This is a data-only recipe and is never matched by a vanilla container.</p>
 */
public final class HeatExchangerDataRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HeatExchangerEntry entry;

    public HeatExchangerDataRecipe(ResourceLocation id, HeatExchangerEntry entry) {
        this.id = Objects.requireNonNull(id, "id");
        this.entry = copyEntry(Objects.requireNonNull(entry, "entry"));
    }

    public HeatExchangerEntry getEntry() {
        return copyEntry(entry);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HeatExchangerRecipeRegistration.HEAT_EXCHANGER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HeatExchangerRecipeRegistration.HEAT_EXCHANGER_TYPE.get();
    }

    private static HeatExchangerEntry copyEntry(HeatExchangerEntry entry) {
        return new HeatExchangerEntry(
                entry.FluidHot, entry.FluidCold, entry.HeatValue, entry.Direction);
    }
}
