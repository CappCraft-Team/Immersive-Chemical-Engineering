package team.cappcraft.immersivechemical.common.tileentity;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import team.cappcraft.immersivechemical.Config;
import team.cappcraft.immersivechemical.common.recipe.constant.HeatExchangerSize;

import javax.annotation.Nonnull;

public class TileHeatExchangerMedium extends AbstractTileHeatExchanger {
    /**
     * Fluid IO block's pos
     */
    protected static final int FluidAccessPointInputA = 15;
    protected static final int FluidAccessPointOutputA = 0;
    protected static final int FluidAccessPointInputB = 6;
    protected static final int FluidAccessPointOutputB = 11;

    public TileHeatExchangerMedium() {
        super(new int[]{2, 2, 5},
                Config.HeatExchangerCapacity.Medium,
                HeatExchangerSize.MEDIUM,
                Config.HeatExchangerTickMultiplier.Medium);
    }

    @Nonnull
    @Override
    protected IFluidTank[] getAccessibleFluidTanks(@Nonnull EnumFacing side) {
        AbstractTileHeatExchanger master = null;
        if (side.getAxis() == EnumFacing.Axis.Y && (master = master()) != null)
            switch (pos) {
                case FluidAccessPointInputA:
                    if (side == EnumFacing.UP)
                        return new IFluidTank[]{master.Tanks[0]};
                case FluidAccessPointOutputA:
                    if (side == EnumFacing.DOWN)
                        return new IFluidTank[]{master.Tanks[1]};
                case FluidAccessPointInputB:
                    if (side == EnumFacing.DOWN)
                        return new IFluidTank[]{master.Tanks[2]};
                case FluidAccessPointOutputB:
                    if (side == EnumFacing.UP)
                        return new IFluidTank[]{master.Tanks[3]};
            }
        return new IFluidTank[0];
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource) {
        if (side.getAxis() == EnumFacing.Axis.Y)
            switch (pos) {
                case FluidAccessPointInputA:
                    return side == EnumFacing.UP;
                case FluidAccessPointInputB:
                    return side == EnumFacing.DOWN;
            }
        return false;
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side) {
        if (side.getAxis() == EnumFacing.Axis.Y)
            switch (pos) {
                case FluidAccessPointOutputA:
                    return side == EnumFacing.DOWN;
                case FluidAccessPointOutputB:
                    return side == EnumFacing.UP;
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
        final int h = pos / 10;
        final int l = pos % 10 / 5;
        final int w = pos % 5;
        if (w == 0)
            return new float[]{0, 0, 0, 1, 1, 1};
        else {
            final boolean Front = l == (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 0 : 1);
            final float X_1 = Front ? 0.09f : 0;
            final float X_2 = !Front ? 0.91f : 1;

            final float Y_1 = 0;
            final float Y_2 = 1;

            final float Z_1 = 0;
            final float Z_2 = 1;

            if (facing.getAxis() == EnumFacing.Axis.X)
                return new float[]{X_1, Y_1, Z_1, X_2, Y_2, Z_2};
            else
                return new float[]{Z_1, Y_1, X_1, Z_2, Y_2, X_2};
        }
    }
}
