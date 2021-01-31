package team.cappcraft.icheme.common.util;

import net.minecraft.nbt.NBTTagCompound;

public interface IRestorableTileEntity {
    void readRestorableFromNBT(NBTTagCompound compound);
    void writeRestorableToNBT(NBTTagCompound compound);
}
