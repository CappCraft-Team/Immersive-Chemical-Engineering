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

public class TileHeatExchangerLarge extends AbstractTileHeatExchanger {
    /**
     * Fluid IO block's pos
     */
    protected static final int FluidAccessPointInputA = 43;
    protected static final int FluidAccessPointOutputA = 1;
    protected static final int FluidAccessPointInputB = 7;
    protected static final int FluidAccessPointOutputB = 58;

    /**
     * Redstone Comparator Access Point
     */
    protected static final int ComparatorAccessPointInputA = 0;
    protected static final int ComparatorAccessPointOutputA = 3;
    protected static final int ComparatorAccessPointInputB = 2;
    protected static final int ComparatorAccessPointOutputB = 5;

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

    public TileHeatExchangerLarge() {
        super(new int[]{3, 7, 3}, HeatExchangerSizeVariants.LARGE);
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
        final int h = pos / 21;
        final int l = pos % 21 / 3;
        final int w = pos % 3;
        if (l == 0 || (w == 1 && l < 6 && h == 1))
            return new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta());
        return new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta());
    }

    @Nonnull
    @Override
    public float[] getBlockBounds() {
        return new float[]{0, 0, 0, 1, 1, 1};
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
