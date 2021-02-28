package team.cappcraft.icheme.common.tileentity;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class TileHeatExchanger extends TileEntityIEBase implements ITickable, IDirectionalTile {
    public static final int MAX_CONTENTS = 5000;
    public FluidTank[] tank = {new FluidTank(MAX_CONTENTS), new FluidTank(MAX_CONTENTS), new FluidTank(MAX_CONTENTS), new FluidTank(MAX_CONTENTS)};

    public FluidStack[] liquidInFilter = new FluidStack[2];
    public FluidStack[] liquidOutFilter = new FluidStack[2];

    private boolean isRunning = false;

    // PUR TESTING PURPOSE
    public TileHeatExchanger() {
    }

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
    public void receiveMessageFromClient(NBTTagCompound message) {
        if (message.hasKey("filter_slot")) {
            int slot = message.getInteger("filter_slot");
            this.liquidInFilter[slot] = FluidStack.loadFluidStackFromNBT(message.getCompoundTag("filter_content"));
        }
        this.markDirty();
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    // status check
    public boolean isRunning() {
        return this.isRunning;
    }

    public boolean isHalt() {
        return false;
    }

    public boolean isError() {
        return false;
    }

    public boolean isIdle() {
        return this.liquidInFilter[0] == null && this.liquidInFilter[1] == null;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        NBTTagList filterInList = nbt.getTagList("filtersIn", 10);
        NBTTagList filterOutList = nbt.getTagList("filtersOut", 10);
        for (int i = 0; i < filterInList.tagCount(); i++) {
            liquidInFilter[i] = FluidStack.loadFluidStackFromNBT(filterInList.getCompoundTagAt(i));
        }
        for (int i = 0; i < filterOutList.tagCount(); i++) {
            liquidOutFilter[i] = FluidStack.loadFluidStackFromNBT(filterOutList.getCompoundTagAt(i));
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        NBTTagList filterInList = new NBTTagList();
        NBTTagList filterOutList = new NBTTagList();
        for (int i = 0; i < liquidInFilter.length; i++) {
            NBTTagCompound tag = new NBTTagCompound();
            if (liquidInFilter[i] != null) liquidInFilter[i].writeToNBT(tag);
            filterInList.appendTag(tag);
        }
        for (int i = 0; i < liquidOutFilter.length; i++) {
            NBTTagCompound tag = new NBTTagCompound();
            if (liquidOutFilter[i] != null) liquidOutFilter[i].writeToNBT(tag);
            filterOutList.appendTag(tag);
        }
        nbt.setTag("filtersIn", filterInList);
        nbt.setTag("filtersOut", filterOutList);
    }
    

}
