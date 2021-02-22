package team.cappcraft.icheme.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import team.cappcraft.icheme.ImmersiveChemicalEngineering;
import team.cappcraft.icheme.common.container.ContainerHeatExchanger;
import team.cappcraft.icheme.common.network.MessageTileSync;
import team.cappcraft.icheme.common.tileentity.TileHeatExchanger;

import java.io.IOException;

public class GuiHeatExchanger extends GuiIEContainerBase {
    public static final int WIDTH = 175;
    public static final int HEIGHT = 207;

    public TileHeatExchanger tile;
    public InventoryPlayer playerInventory;


    private static final String background = ImmersiveChemicalEngineering.MODID + ":textures/gui/heatexchanger_common.png";

    public GuiHeatExchanger(TileHeatExchanger tileEntity, InventoryPlayer playerInventory) {
        super(new ContainerHeatExchanger(playerInventory, tileEntity));
        this.tile = tileEntity;
        this.playerInventory = playerInventory;
        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.checkClickArea(mouseX, mouseY, 35, 34)) {//input A, top left slot
            FluidStack fs = FluidUtil.getFluidContained(playerInventory.getItemStack());
            this.setFluidInSlot(0, fs);
            return;
        } else if (this.checkClickArea(mouseX, mouseY, 125, 34)) {//input B, top right slot
            FluidStack fs = FluidUtil.getFluidContained(playerInventory.getItemStack());
            this.setFluidInSlot(1, fs);
            return;
        } else if (this.checkClickArea(mouseX, mouseY, 35, 79)) { // click to clear
            this.setFluidOutSlot(0, null);
            return;
        } else if (this.checkClickArea(mouseX, mouseY, 125, 79)) {
            this.setFluidOutSlot(1, null);
            return;
        }
        return;
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) { // draw the content
        ClientUtils.bindTexture(background);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        ClientUtils.bindAtlas();
        if (this.tile.liquidInFilter[0] != null) {
            TextureAtlasSprite spriteInputA = ClientUtils.getSprite(this.tile.liquidInFilter[0].getFluid().getStill());
            ClientUtils.drawTexturedRect(guiLeft + 35, guiTop + 34, 16, 16, spriteInputA.getMinU(), spriteInputA.getMaxU(), spriteInputA.getMinV(), spriteInputA.getMaxV());
        }
        if (this.tile.liquidInFilter[1] != null) {
            TextureAtlasSprite spriteInputB = ClientUtils.getSprite(this.tile.liquidInFilter[1].getFluid().getStill());
            ClientUtils.drawTexturedRect(guiLeft + 125, guiTop + 34, 16, 16, spriteInputB.getMinU(), spriteInputB.getMaxU(), spriteInputB.getMinV(), spriteInputB.getMaxV());
        }
        if (this.tile.liquidOutFilter[0] != null) {
            TextureAtlasSprite spriteOutputA = ClientUtils.getSprite(this.tile.liquidOutFilter[0].getFluid().getStill());
            ClientUtils.drawTexturedRect(guiLeft + 35, guiTop + 79, 16, 16, spriteOutputA.getMinU(), spriteOutputA.getMaxU(), spriteOutputA.getMinV(), spriteOutputA.getMaxV());
        }
        if (this.tile.liquidOutFilter[1] != null) {
            TextureAtlasSprite spriteOutputB = ClientUtils.getSprite(this.tile.liquidOutFilter[1].getFluid().getStill());
            ClientUtils.drawTexturedRect(guiLeft + 125, guiTop + 79, 16, 16, spriteOutputB.getMinU(), spriteOutputB.getMaxU(), spriteOutputB.getMinV(), spriteOutputB.getMaxV());
        }
        ClientUtils.bindTexture(background);
    }

    private boolean checkClickArea(double mouseX, double mouseY, int x, int y) {
        int tempX = guiLeft + x;
        int tempY = guiTop + y;
        return mouseX > tempX && mouseX < tempX + 16 && mouseY > tempY && mouseY < tempY + 16;
    }

    public void setFluidInSlot(int slot, FluidStack fluid) {
        tile.liquidInFilter[slot] = fluid;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("filter_slot_in", slot);
        if (fluid != null) tag.setTag("filter_content", fluid.writeToNBT(new NBTTagCompound()));
        ImmersiveChemicalEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
    }

    public void setFluidOutSlot(int slot, FluidStack fluid) {
        tile.liquidInFilter[slot] = fluid;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("filter_slot_out", slot);
        if (fluid != null) tag.setTag("filter_content", fluid.writeToNBT(new NBTTagCompound()));
        ImmersiveChemicalEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
    }

}
