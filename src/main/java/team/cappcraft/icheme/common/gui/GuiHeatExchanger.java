package team.cappcraft.icheme.common.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import team.cappcraft.icheme.ImmersiveChemicalEngineering;
import team.cappcraft.icheme.common.container.ContainerHeatExchanger;
import team.cappcraft.icheme.common.tileentity.TileHeatExchanger;

public class GuiHeatExchanger extends GuiContainer {
    public static final int WIDTH = 175;
    public static final int HEIGHT = 207;

    private static final ResourceLocation background = new ResourceLocation(ImmersiveChemicalEngineering.MODID, "textures/gui/heatexchanger_common.png");

    public GuiHeatExchanger(TileHeatExchanger tileEntity, ContainerHeatExchanger container) {
        super(container);

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
