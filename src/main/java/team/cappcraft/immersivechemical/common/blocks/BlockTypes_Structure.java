package team.cappcraft.immersivechemical.common.blocks;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_Structure implements IStringSerializable, BlockIEBase.IBlockEnum {
    SHELL,
    TUBE;

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
        return true;
    }
}
