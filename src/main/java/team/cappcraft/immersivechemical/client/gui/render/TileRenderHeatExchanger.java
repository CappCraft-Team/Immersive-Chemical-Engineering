package team.cappcraft.immersivechemical.client.gui.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.common.tileentity.AbstractTileHeatExchanger;

import javax.annotation.Nonnull;

public class TileRenderHeatExchanger extends TileEntitySpecialRenderer<AbstractTileHeatExchanger> {
    @Override
    public boolean isGlobalRenderer(@Nonnull AbstractTileHeatExchanger te) {
        //Ensure the model not disappear when the master block out of viewport
        return true;
    }

    @Override
    public void render(@Nonnull AbstractTileHeatExchanger te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.isDummy()) return;

        BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

        BlockPos pos = te.getPos();
        IBlockState state = getWorld().getBlockState(pos);
        if (state.getBlock() != ICHEME_Contents.blockHeatExchanger) return;

        state = state.getActualState(getWorld(), pos);
        IBakedModel model = renderer.getModelForState(state);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();

        ClientUtils.bindAtlas();

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y + 3, z);

        GlStateManager.translate(te.facing.getFrontOffsetX() * .5, .5, te.facing.getFrontOffsetZ() * .5);
        GlStateManager.translate(te.facing.rotateYCCW().getFrontOffsetX() * .5, 0, te.facing.rotateY().getFrontOffsetZ() * .5);

        RenderHelper.disableStandardItemLighting();

        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled())
            GlStateManager.shadeModel(7425);
        else
            GlStateManager.shadeModel(7424);

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        worldRenderer.color(255, 255, 255, 255);
        worldRenderer.setTranslation(-.5 - pos.getX(), -.5 - pos.getY(), -.5 - pos.getZ());
        renderer.getBlockModelRenderer().renderModelSmooth(te.getWorld(), model, state, pos, worldRenderer, true, MathHelper.getPositionRandom(pos));
        worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();

        GlStateManager.popMatrix();
    }
}
