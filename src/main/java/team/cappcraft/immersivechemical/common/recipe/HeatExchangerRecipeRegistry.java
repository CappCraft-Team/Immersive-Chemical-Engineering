package team.cappcraft.immersivechemical.common.recipe;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.registry.IRegistry;
import team.cappcraft.immersivechemical.common.recipe.constant.HeatExchangerSize;
import team.cappcraft.immersivechemical.common.tileentity.LockableFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class HeatExchangerRecipeRegistry implements IRegistry<String, HeatExchangerRecipe> {
    public static final String POSTFIX_REVERSE = "$reverse";
    private final BiMap<String, HeatExchangerRecipe> recipeBiMap = HashBiMap.create();

    @Nullable
    @Override
    public HeatExchangerRecipe getObject(@Nonnull String name) {
        return recipeBiMap.get(name);
    }

    public String getKey(@Nonnull HeatExchangerRecipe recipe) {
        return recipeBiMap.inverse().get(recipe);
    }

    /**
     * Register a HeatExchangerRecipe, will automatically put an reverse one, whose registry name with a '-reverse' postfix
     *
     * @param key   registry name
     * @param value recipe
     */
    @Override
    public void putObject(@Nonnull String key, @Nonnull HeatExchangerRecipe value) {
        recipeBiMap.put(key, value);
        recipeBiMap.put(key + POSTFIX_REVERSE, new HeatExchangerRecipe(value.ExchangeB, value.ExchangeA, value.Size));
    }

    @Nonnull
    @Override
    public Set<String> getKeys() {
        return recipeBiMap.keySet();
    }

    @Nonnull
    @Override
    public Iterator<HeatExchangerRecipe> iterator() {
        return recipeBiMap.values().iterator();
    }

    public Optional<HeatExchangerRecipe> findRecipe(HeatExchangerSize size, LockableFluidTank tankA, LockableFluidTank tankB) {
        return recipeBiMap.values().parallelStream().filter(recipe ->
                (recipe.Size == size || recipe.Size == HeatExchangerSize.GENERAL)
                        && recipe.ExchangeA.isInputMatches(tankA)
                        && recipe.ExchangeB.isInputMatches(tankB)).findAny();
    }
}
