package team.cappcraft.immersivechemical.common.tileentity;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import team.cappcraft.immersivechemical.common.recipe.constant.HeatExchangerSize;

import javax.annotation.Nonnull;

public class TileHeatExchangerMedium extends AbstractTileHeatExchanger {
    /**
     * Fluid IO block's pos
     */
    protected static final int FluidAccessPointInputA = 10;
    protected static final int FluidAccessPointOutputA = 5;
    protected static final int FluidAccessPointInputB = 16;
    protected static final int FluidAccessPointOutputB = 1;

    public TileHeatExchangerMedium() {
        super(new int[]{2, 2, 5}, 5000 * 2, HeatExchangerSize.MEDIUM, 0.4f);
    }

    @Nonnull
    @Override
    protected IFluidTank[] getAccessibleFluidTanks(@Nonnull EnumFacing side) {
        if (side.getAxis() == EnumFacing.Axis.Y)
            switch (pos) {
                case FluidAccessPointInputA:
                    if (side == EnumFacing.UP)
                        return new IFluidTank[]{Tanks[0]};
                case FluidAccessPointOutputA:
                    if (side == EnumFacing.DOWN)
                        return new IFluidTank[]{Tanks[1]};
                case FluidAccessPointInputB:
                    if (side == EnumFacing.UP)
                        return new IFluidTank[]{Tanks[2]};
                case FluidAccessPointOutputB:
                    if (side == EnumFacing.DOWN)
                        return new IFluidTank[]{Tanks[3]};
            }
        return new IFluidTank[0];
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource) {
        if (side.getAxis() == EnumFacing.Axis.Y)
            switch (pos) {
                case FluidAccessPointInputA:
                case FluidAccessPointInputB:
                    return side == EnumFacing.UP;
            }
        return false;
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side) {
        if (side.getAxis() == EnumFacing.Axis.Y)
            switch (pos) {
                case FluidAccessPointOutputA:
                case FluidAccessPointOutputB:
                    return side == EnumFacing.DOWN;
            }
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getOriginalBlock() {
        if (pos % 5 == 0)
            return new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta());
        return new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());
    }

    @Nonnull
    @Override
    public float[] getBlockBounds() {
        return new float[]{0, 0, 0, 1, 1, 1};
    }
}
