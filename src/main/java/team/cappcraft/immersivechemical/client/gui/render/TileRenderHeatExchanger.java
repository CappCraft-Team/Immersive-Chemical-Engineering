package team.cappcraft.immersivechemical.client.gui.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
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
    }
}
