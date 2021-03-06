package team.cappcraft.immersivechemical.common.tileentity;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

import javax.annotation.Nonnull;

public class TileHeatExchangerSmall extends AbstractTileHeatExchanger {
    /**
     * Fluid IO block's pos
     */
    protected static final int FluidAccessPointA = 0;
    protected static final int FluidAccessPointB = 3;

    public TileHeatExchangerSmall() {
        super(new int[]{1, 1, 4}, HeatExchangerSizeVariants.SMALL);
    }

    @Nonnull
    @Override
    protected IFluidTank[] getAccessibleFluidTanks(@Nonnull EnumFacing side) {
        AbstractTileHeatExchanger master;
        if (side.getAxis() == EnumFacing.Axis.Y && (master = master()) != null)
            switch (pos) {
                case FluidAccessPointA:
                    return new IFluidTank[]{side == EnumFacing.UP ? master.Tanks[0] : master.Tanks[1]};
                case FluidAccessPointB:
                    return new IFluidTank[]{side == EnumFacing.UP ? master.Tanks[2] : master.Tanks[3]};
            }
        return new IFluidTank[0];
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource) {
        if (side.getAxis() == EnumFacing.Axis.Y)
            switch (pos) {
                case FluidAccessPointA:
                case FluidAccessPointB:
                    return side == EnumFacing.UP;
            }
        return false;
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side) {
        if (side.getAxis() == EnumFacing.Axis.Y)
            switch (pos) {
                case FluidAccessPointA:
                case FluidAccessPointB:
                    return side == EnumFacing.DOWN;
            }
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getOriginalBlock() {
        switch (pos) {
            case FluidAccessPointA:
            case FluidAccessPointB:
                return new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta());
        }
        return new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());
    }

    @Nonnull
    @Override
    public float[] getBlockBounds() {
        switch (pos) {
            case FluidAccessPointA:
            case FluidAccessPointB:
                return new float[]{0, 0, 0, 1, 1, 1};
        }
        if (facing.getAxis() == EnumFacing.Axis.X)
            return new float[]{
                    0.1f, 0, 0,
                    0.9f, 1, 1};
        else
            return new float[]{
                    0, 0, 0.1f,
                    1, 1, 0.9f};
    }
}
