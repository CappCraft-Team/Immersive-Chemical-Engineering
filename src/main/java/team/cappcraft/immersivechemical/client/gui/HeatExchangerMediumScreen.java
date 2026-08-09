package team.cappcraft.immersivechemical.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerMediumMenu;

/** Medium registration wrapper for the shared four-tank exchanger screen. */
public final class HeatExchangerMediumScreen
        extends HeatExchangerScreen<HeatExchangerMediumMenu> {
    public HeatExchangerMediumScreen(
            HeatExchangerMediumMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
    }
}
