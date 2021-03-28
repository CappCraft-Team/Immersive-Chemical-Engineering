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

public class TileHeatExchangerLarge extends AbstractTileHeatExchanger {
    /**
     * Fluid IO block's pos
     */
    protected static final int FluidAccessPointInputA = 43;
    protected static final int FluidAccessPointOutputA = 1;
    protected static final int FluidAccessPointInputB = 7;
    protected static final int FluidAccessPointOutputB = 58;

    public TileHeatExchangerLarge() {
        super(new int[]{3, 7, 3},
                Config.HeatExchangerCapacity.Large,
                HeatExchangerSize.LARGE,
                Config.HeatExchangerTickMultiplier.Large);
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
}
