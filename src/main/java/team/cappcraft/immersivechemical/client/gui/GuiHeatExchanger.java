package team.cappcraft.immersivechemical.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import net.minecraft.entity.player.InventoryPlayer;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.container.ContainerHeatExchanger;
import team.cappcraft.immersivechemical.common.tileentity.AbstractTileHeatExchanger;

import java.io.IOException;

public class GuiHeatExchanger extends GuiIEContainerBase {
    public static final int WIDTH = 175;
    public static final int HEIGHT = 207;
    private static final String background = ImmersiveChemicalEngineering.MODID + ":textures/gui/heatexchanger_common.png";
    public AbstractTileHeatExchanger tile;
    public InventoryPlayer playerInventory;

    public GuiHeatExchanger(AbstractTileHeatExchanger tileEntity, InventoryPlayer playerInventory) {
        super(new ContainerHeatExchanger(playerInventory, tileEntity));
        this.tile = tileEntity;
        this.playerInventory = playerInventory;
        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) { // draw the content
        ClientUtils.bindTexture(background);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw Buttons
        this.drawTexturedModalRect(guiLeft + 35, guiTop + 56, 177, 0, 16, 16); //reset left
        this.drawTexturedModalRect(guiLeft + 125, guiTop + 56, 177, 0, 16, 16); //reset right
        this.drawTexturedModalRect(guiLeft + 80, guiTop + 79, 177, 16, 16, 16);
        // Draw liquid in Filter
        ClientUtils.bindAtlas();
        ClientUtils.bindTexture(background);
    }
}
