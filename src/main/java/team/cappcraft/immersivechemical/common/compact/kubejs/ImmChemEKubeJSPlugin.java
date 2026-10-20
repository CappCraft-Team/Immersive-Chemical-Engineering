package team.cappcraft.immersivechemical.common.compact.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.EnumComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RegistryComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.world.level.material.Fluid;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipeRegistration;

public final class ImmChemEKubeJSPlugin extends KubeJSPlugin {
    private static final RegistryComponent<Fluid> FLUID =
            new RegistryComponent<>(RegistryInfo.FLUID);
    private static final EnumComponent<ConvertDirection> DIRECTION =
            new EnumComponent<>(ConvertDirection.class);

    private static final RecipeSchema HEAT_EXCHANGER =
            new RecipeSchema(
                    FLUID.key("fluid_a"),
                    NumberComponent.ANY_INT.key("amount_a"),
                    FLUID.key("fluid_b"),
                    NumberComponent.ANY_INT.key("amount_b"),
                    NumberComponent.ANY_INT.key("heat"),
                    DIRECTION.key("direction"));

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(HeatExchangerRecipeRegistration.ID, HEAT_EXCHANGER);
    }
}
