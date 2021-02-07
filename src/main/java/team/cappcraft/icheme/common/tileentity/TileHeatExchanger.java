package team.cappcraft.icheme.common.tileentity;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidTank;

public class TileHeatExchanger extends TileEntityIEBase implements ITickable, IDirectionalTile {
    public static final int MAX_CONTENTS = 5000;
    public FluidTank tankInputA = new FluidTank(MAX_CONTENTS);
    public FluidTank tankInputB = new FluidTank(MAX_CONTENTS);
    public FluidTank tankOunputA = new FluidTank(MAX_CONTENTS);
    public FluidTank tankOunputB = new FluidTank(MAX_CONTENTS);
    

    @Override
    public void update() {

    }


    @Override
    public EnumFacing getFacing() {
        return null;
    }

    @Override
    public void setFacing(EnumFacing facing) {

    }

    @Override
    public int getFacingLimitation() {
        return 0;
    }

    @Override
    public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
        return false;
    }

    @Override
    public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity) {
        return false;
    }

    @Override
    public boolean canRotate(EnumFacing axis) {
        return false;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {

    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {

    }
}
