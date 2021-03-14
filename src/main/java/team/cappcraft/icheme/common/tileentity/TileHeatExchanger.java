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
import team.cappcraft.icheme.common.recipe.HeatExchangerRecipe;

public class TileHeatExchanger extends TileEntityIEBase implements ITickable, IDirectionalTile {
    public static final int MAX_CONTENTS = 5000;
    public FluidTank[] tank = {new FluidTank(MAX_CONTENTS), new FluidTank(MAX_CONTENTS), new FluidTank(MAX_CONTENTS), new FluidTank(MAX_CONTENTS)};

    public FluidStack[] liquidInFilter = new FluidStack[2];
    public FluidStack[] liquidOutFilter = new FluidStack[2];

    private HeatExchangerRecipe currentRecipe = null;
    private boolean isRunning = false;

    // PUR TESTING PURPOSE
    public TileHeatExchanger() {
    }

    @Override
    public void update() {
        if (liquidInFilter[0] == null) {
            this.clearFilter(0);
            this.emptyTank(0);
            this.currentRecipe = null;
            this.clearRecipe();
        }
        if (liquidInFilter[1] == null) {
            this.clearFilter(1);
            this.emptyTank(1);
            this.currentRecipe = null;
            this.clearRecipe();
        }
        if (liquidOutFilter[0] == null && liquidOutFilter[1] == null && currentRecipe == null) {
            this.currentRecipe = HeatExchangerRecipe.findHeatExchangerRecipe(liquidInFilter[0], liquidInFilter[1]);
            this.liquidOutFilter[0] = this.currentRecipe.getOutputA();
            this.liquidOutFilter[1] = this.currentRecipe.getOutputB();
            this.tank[0].setFluid(this.currentRecipe.getInputA());
            this.tank[1].setFluid(this.currentRecipe.getOutputA());
            this.tank[2].setFluid(this.currentRecipe.getInputB());
            this.tank[3].setFluid(this.currentRecipe.getOutputB());
        }
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
        return this.isRunning && this.currentRecipe != null;
    }

    public boolean isHalt() {
        return !this.isRunning && this.currentRecipe != null;
    }

    public boolean isError() {
        return this.currentRecipe == null;
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
        for (FluidStack stack : liquidInFilter) {
            NBTTagCompound tag = new NBTTagCompound();
            if (stack != null) stack.writeToNBT(tag);
            filterInList.appendTag(tag);
        }
        for (FluidStack stack : liquidOutFilter) {
            NBTTagCompound tag = new NBTTagCompound();
            if (stack != null) stack.writeToNBT(tag);
            filterOutList.appendTag(tag);
        }
        nbt.setTag("filtersIn", filterInList);
        nbt.setTag("filtersOut", filterOutList);
    }

    private void emptyTank(int position) {
        if (position != 0 && position != 1) return;
        this.tank[position] = new FluidTank(MAX_CONTENTS);
        this.tank[position + 2] = new FluidTank(MAX_CONTENTS);
    }

    private void clearFilter(int position) {
        if (position != 0 && position != 1) return;
        this.liquidInFilter[position] = null;
        this.liquidOutFilter[position] = null;
    }

    private void clearRecipe() {
        this.currentRecipe = null;
    }

}
