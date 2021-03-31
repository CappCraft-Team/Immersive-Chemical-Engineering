package team.cappcraft.immersivechemical.common.container;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import team.cappcraft.immersivechemical.common.tileentity.AbstractTileHeatExchanger;

import javax.annotation.Nonnull;

public class ContainerHeatExchanger extends ContainerIEBase<AbstractTileHeatExchanger> {
    public ContainerHeatExchanger(InventoryPlayer inventoryPlayer, AbstractTileHeatExchanger tile) {
        super(inventoryPlayer, tile);

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 126 + i * 18));
        for (int i = 0; i < 9; i++)
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 58 + 126));
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int id, int button, ClickType clickType, EntityPlayer player) {
        return super.slotClick(id, button, clickType, player);
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer player) {
        return true;
    }
}
