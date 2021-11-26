package team.cappcraft.immersivechemical.common.tileentity;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

import javax.annotation.Nonnull;

public class TileHeatExchangerMedium extends AbstractTileHeatExchanger {
    /**
     * Fluid IO block's pos
     */
    protected static final int FluidAccessPointInputA = 15;
    protected static final int FluidAccessPointOutputA = 0;
    protected static final int FluidAccessPointInputB = 6;
    protected static final int FluidAccessPointOutputB = 11;

    /**
     * Redstone Comparator Access Point
     */
    protected static final int ComparatorAccessPointInputA = 5;
    protected static final int ComparatorAccessPointOutputA = 6;
    protected static final int ComparatorAccessPointInputB = 0;
    protected static final int ComparatorAccessPointOutputB = 1;

    public TileHeatExchangerMedium() {
        super(new int[]{2, 2, 5}, HeatExchangerSizeVariants.MEDIUM);
    }

    @Override
    protected void onInputSlotChanged(FluidEvent event) {
        super.onInputSlotChanged(event);
        markBlockForUpdate(getBlockPosForPos(ComparatorAccessPointInputA), null);
        markBlockForUpdate(getBlockPosForPos(ComparatorAccessPointInputB), null);
    }

    @Override
    protected void onOutputSlotChanged(FluidEvent event) {
        super.onOutputSlotChanged(event);
        markBlockForUpdate(getBlockPosForPos(ComparatorAccessPointOutputA), null);
        markBlockForUpdate(getBlockPosForPos(ComparatorAccessPointOutputB), null);
    }

    @Override
    protected void clearFluidSlot(int slot) {
        super.clearFluidSlot(slot);
        switch (slot) {
            case 0:
                markBlockForUpdate(getBlockPosForPos(ComparatorAccessPointInputA), null);
                break;
            case 1:
                markBlockForUpdate(getBlockPosForPos(ComparatorAccessPointOutputA), null);
                break;
            case 2:
                markBlockForUpdate(getBlockPosForPos(ComparatorAccessPointInputB), null);
                break;
            case 3:
                markBlockForUpdate(getBlockPosForPos(ComparatorAccessPointOutputB), null);
                break;
        }
    }

    @Nonnull
    @Override
    protected IFluidTank[] getAccessibleFluidTanks(@Nonnull EnumFacing side) {
        AbstractTileHeatExchanger master;
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

    @Override
    public int getComparatorInputOverride() {
        final AbstractTileHeatExchanger master = master();
        if (master == null) return 0;
        final LockableFluidTank[] Tanks = master.Tanks;
        switch (pos) {
            case ComparatorAccessPointInputA:
                return 15 * Tanks[0].getFluidAmount() / Tanks[0].getCapacity();
            case ComparatorAccessPointOutputA:
                return 15 * Tanks[1].getFluidAmount() / Tanks[1].getCapacity();
            case ComparatorAccessPointInputB:
                return 15 * Tanks[2].getFluidAmount() / Tanks[2].getCapacity();
            case ComparatorAccessPointOutputB:
                return 15 * Tanks[3].getFluidAmount() / Tanks[3].getCapacity();
        }
        return 0;
    }
}
