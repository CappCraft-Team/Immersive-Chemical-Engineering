package team.cappcraft.immersivechemical.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.container.ContainerHeatExchanger;
import team.cappcraft.immersivechemical.common.network.MessageBlockEvent;
import team.cappcraft.immersivechemical.common.tileentity.AbstractTileHeatExchanger;

import java.io.IOException;
import java.util.ArrayList;

public class GuiHeatExchanger extends GuiIEContainerBase {
    public static final int GUI_WIDTH = 175;
    public static final int GUI_HEIGHT = 207;
    public static final int LEFT_SLOT_X = 35;
    public static final int TOP_SLOT_Y = 34;
    public static final int BOTTOM_SLOT_Y = 79;
    public static final int RIGHT_SLOT_X = 125;
    public static final int STATE_X = 81;
    public static final int STATE_Y = 55;
    public static final int ICON_OFFSET_X = 177;
    private static final String BACKGROUND = ImmersiveChemicalEngineering.MODID + ":textures/gui/heatexchanger_common.png";
    public AbstractTileHeatExchanger tile;
    public InventoryPlayer playerInventory;

    public GuiHeatExchanger(AbstractTileHeatExchanger tileEntity, InventoryPlayer playerInventory) {
        super(new ContainerHeatExchanger(playerInventory, tileEntity));
        this.tile = tileEntity;
        this.playerInventory = playerInventory;
        xSize = GUI_WIDTH;
        ySize = GUI_HEIGHT;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseX >= guiLeft + LEFT_SLOT_X && mouseX <= guiLeft + RIGHT_SLOT_X + 16
                && mouseY >= guiTop + TOP_SLOT_Y + 16 && mouseY <= guiTop + BOTTOM_SLOT_Y) {
            if (mouseX <= guiLeft + LEFT_SLOT_X + 16) {
                //Left Side
                ImmersiveChemicalEngineering.packetHandler.sendToServer(new MessageBlockEvent(tile.getPos(), 1, 0));
            } else if (mouseX >= guiLeft + RIGHT_SLOT_X) {
                //Right Side
                ImmersiveChemicalEngineering.packetHandler.sendToServer(new MessageBlockEvent(tile.getPos(), 2, 0));
            }
        } else if (mouseX >= guiLeft + 80 && mouseX <= guiLeft + 80 + 16
                && mouseY >= guiTop + 79 && mouseY <= guiTop + 79 + 16)
            //Clear all
            ImmersiveChemicalEngineering.packetHandler.sendToServer(new MessageBlockEvent(tile.getPos(), 3, 0));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ArrayList<String> tooltip = new ArrayList<String>();

        handleFluidAmount(mouseX, mouseY, tooltip);
        if (isShiftKeyDown()) {
            tooltip.add(String.format("Mouse X:%d, Y:%d", mouseX, mouseY));
            tooltip.add(String.format("Current State:%s", tile.currentState));
        }
        if (!tooltip.isEmpty()) {
            ClientUtils.drawHoveringText(tooltip, mouseX, mouseY, fontRenderer, guiLeft + xSize, -1);
            RenderHelper.enableGUIStandardItemLighting();
        }
    }

    private void handleFluidAmount(int mouseX, int mouseY, ArrayList<String> tooltip) {
        if (mouseX >= guiLeft + LEFT_SLOT_X && mouseX <= guiLeft + RIGHT_SLOT_X + 16
                && mouseY >= guiTop + TOP_SLOT_Y && mouseY <= guiTop + BOTTOM_SLOT_Y + 16) {
            if (mouseX <= guiLeft + LEFT_SLOT_X + 16 && mouseY <= guiTop + TOP_SLOT_Y + 16) {
                //InputA
                ClientUtils.addFluidTooltip(tile.Tanks[0].getFluid(), tooltip, tile.Capacity);
            } else if (mouseX >= guiLeft + RIGHT_SLOT_X && mouseY <= guiTop + TOP_SLOT_Y + 16) {
                //InputB
                ClientUtils.addFluidTooltip(tile.Tanks[2].getFluid(), tooltip, tile.Capacity);
            } else if (mouseX <= guiLeft + LEFT_SLOT_X + 16 && mouseY >= guiTop + BOTTOM_SLOT_Y) {
                //OutputA
                ClientUtils.addFluidTooltip(tile.Tanks[1].getFluid(), tooltip, tile.Capacity);
            } else if (mouseX >= guiLeft + RIGHT_SLOT_X && mouseY >= guiTop + BOTTOM_SLOT_Y) {
                //OutputB
                ClientUtils.addFluidTooltip(tile.Tanks[3].getFluid(), tooltip, tile.Capacity);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) { // draw the content
        ClientUtils.bindTexture(BACKGROUND);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw RESET Buttons
        //Left
        this.drawTexturedModalRect(guiLeft + LEFT_SLOT_X, guiTop + 56, ICON_OFFSET_X, 0, 16, 16);
        //Right
        this.drawTexturedModalRect(guiLeft + RIGHT_SLOT_X, guiTop + 56, ICON_OFFSET_X, 0, 16, 16);
        //Center
        this.drawTexturedModalRect(guiLeft + 80, guiTop + 79, ICON_OFFSET_X, 16, 16, 16);

        //State
        if (tile.currentState instanceof AbstractTileHeatExchanger.TileHeatExchangerTickAction.Processing)
            //Running
            this.drawTexturedModalRect(guiLeft + STATE_X, guiTop + STATE_Y, ICON_OFFSET_X, 32, 16, 15);
        else//Idle
            this.drawTexturedModalRect(guiLeft + STATE_X, guiTop + STATE_Y, ICON_OFFSET_X, 77, 16, 15);

        // Draw liquid in Filter
        ClientUtils.bindAtlas();
        if (tile.Tanks[0].getFluid() != null) {
            TextureAtlasSprite sprite = ClientUtils.getSprite(this.tile.Tanks[0].getFluid().getFluid().getStill());
            ClientUtils.drawTexturedRect(guiLeft + LEFT_SLOT_X, guiTop + TOP_SLOT_Y, 16, 16, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
        }
        if (tile.Tanks[1].getFluid() != null) {
            TextureAtlasSprite sprite = ClientUtils.getSprite(this.tile.Tanks[1].getFluid().getFluid().getStill());
            ClientUtils.drawTexturedRect(guiLeft + LEFT_SLOT_X, guiTop + BOTTOM_SLOT_Y, 16, 16, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
        }
        if (tile.Tanks[2].getFluid() != null) {
            TextureAtlasSprite sprite = ClientUtils.getSprite(this.tile.Tanks[2].getFluid().getFluid().getStill());
            ClientUtils.drawTexturedRect(guiLeft + RIGHT_SLOT_X, guiTop + TOP_SLOT_Y, 16, 16, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
        }
        if (tile.Tanks[3].getFluid() != null) {
            TextureAtlasSprite sprite = ClientUtils.getSprite(this.tile.Tanks[3].getFluid().getFluid().getStill());
            ClientUtils.drawTexturedRect(guiLeft + RIGHT_SLOT_X, guiTop + BOTTOM_SLOT_Y, 16, 16, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
        }
        ClientUtils.bindTexture(BACKGROUND);
    }
}
