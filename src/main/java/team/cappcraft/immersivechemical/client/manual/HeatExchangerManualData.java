package team.cappcraft.immersivechemical.client.manual;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only manual metadata for an IChemE heat exchanger.
 *
 * <p>The manual's normal structure preview is built from the template synchronized by IE. The
 * formed machines use split OBJ models, so the optional formed preview is deliberately disabled
 * instead of attempting to render those world models in the manual.</p>
 */
public final class HeatExchangerManualData
        implements ClientMultiblocks.MultiblockManualData {
    private final int sheetmetalCount;
    private final int radiatorCount;

    private HeatExchangerManualData(int sheetmetalCount, int radiatorCount) {
        if (sheetmetalCount <= 0 || radiatorCount <= 0) {
            throw new IllegalArgumentException("Manual material counts must be positive");
        }
        this.sheetmetalCount = sheetmetalCount;
        this.radiatorCount = radiatorCount;
    }

    public static HeatExchangerManualData of(int sheetmetalCount, int radiatorCount) {
        return new HeatExchangerManualData(sheetmetalCount, radiatorCount);
    }

    @Override
    public NonNullList<ItemStack> getTotalMaterials() {
        NonNullList<ItemStack> materials = NonNullList.create();
        materials.add(new ItemStack(
                IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON),
                sheetmetalCount
        ));
        materials.add(new ItemStack(
                IEBlocks.MetalDecoration.RADIATOR.get(),
                radiatorCount
        ));
        return materials;
    }

    @Override
    public boolean canRenderFormedStructure() {
        return false;
    }

    @Override
    public void renderFormedStructure(PoseStack poseStack, MultiBufferSource buffers) {
        // Not called while canRenderFormedStructure() is false.
    }
}
