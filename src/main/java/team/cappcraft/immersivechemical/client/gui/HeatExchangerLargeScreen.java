package team.cappcraft.immersivechemical.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerLargeMenu;

/** Large registration wrapper for the shared four-tank exchanger screen. */
public final class HeatExchangerLargeScreen
        extends HeatExchangerScreen<HeatExchangerLargeMenu> {
    public HeatExchangerLargeScreen(
            HeatExchangerLargeMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
    }
}
