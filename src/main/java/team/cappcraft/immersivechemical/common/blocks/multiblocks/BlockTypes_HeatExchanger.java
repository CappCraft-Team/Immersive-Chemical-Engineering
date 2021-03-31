package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_HeatExchanger implements IStringSerializable, BlockIEBase.IBlockEnum {
    HEAT_EXCHANGER_SMALL,
    HEAT_EXCHANGER_MEDIUM,
    HEAT_EXCHANGER_LARGE;

    @Override
    public String getName() {
        return this.toString().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public int getMeta() {
        return ordinal();
    }

    @Override
    public boolean listForCreative() {
        return false;
    }
}
