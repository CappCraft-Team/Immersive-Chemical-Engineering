package team.cappcraft.immersivechemical.common.tileentity;

import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class LockableFluidTank extends FluidTank {
    public static final String TAG_LOCKED = "Locked";
    public static final String TAG_LOCKED_TYPE = "LockedType";
    public static final String TAG_LOCKED_FLUID = "LockedFluid";

    private final Consumer<FluidStack> onContentChangedConsumer;
    private boolean locked;
    private boolean lockedType;
    private FluidStack lockedFluid = FluidStack.EMPTY;

    public LockableFluidTank(int capacity, Consumer<FluidStack> onContentChanged) {
        super(capacity);
        this.onContentChangedConsumer = onContentChanged;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        if (locked) {
            return false;
        }
        if (lockedType && (lockedFluid.isEmpty() || !lockedFluid.isFluidEqual(stack))) {
            return false;
        }
        return super.isFluidValid(stack);
    }

    @Override
    public FluidTank readFromNBT(CompoundTag nbt) {
        locked = nbt.getBoolean(TAG_LOCKED);
        lockedType = nbt.getBoolean(TAG_LOCKED_TYPE);
        lockedFluid =
                nbt.contains(TAG_LOCKED_FLUID, Tag.TAG_COMPOUND)
                        ? FluidStack.loadFluidStackFromNBT(nbt.getCompound(TAG_LOCKED_FLUID))
                        : FluidStack.EMPTY;

        super.readFromNBT(nbt);
        if (lockedType && lockedFluid.isEmpty() && !fluid.isEmpty()) {
            lockedFluid = copyType(fluid);
        }
        if (!lockedType) {
            lockedFluid = FluidStack.EMPTY;
        }
        return this;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        super.writeToNBT(nbt);
        nbt.putBoolean(TAG_LOCKED, locked);
        nbt.putBoolean(TAG_LOCKED_TYPE, lockedType);
        if (lockedType && !lockedFluid.isEmpty()) {
            nbt.put(TAG_LOCKED_FLUID, lockedFluid.writeToNBT(new CompoundTag()));
        } else {
            nbt.remove(TAG_LOCKED_FLUID);
        }
        return nbt;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (locked) {
            return 0;
        }
        return super.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (locked) {
            return FluidStack.EMPTY;
        }
        return super.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (locked) {
            return FluidStack.EMPTY;
        }
        return super.drain(maxDrain, action);
    }

    @Override
    public void setFluid(FluidStack stack) {
        if (stack.isEmpty()) {
            super.setFluid(FluidStack.EMPTY);
            return;
        }

        int amount = Math.min(capacity, stack.getAmount());
        super.setFluid(new FluidStack(stack, amount));
        if (lockedType) {
            lockedFluid = copyType(stack);
        }
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        onContentChangedConsumer.accept(fluid.isEmpty() ? FluidStack.EMPTY : fluid.copy());
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (this.locked != locked) {
            this.locked = locked;
            onContentsChanged();
        }
    }

    public boolean isLockedType() {
        return lockedType;
    }

    public void setLockedType(boolean locked) {
        if (lockedType == locked) {
            return;
        }

        lockedType = locked;
        lockedFluid = locked && !fluid.isEmpty() ? copyType(fluid) : FluidStack.EMPTY;
        onContentsChanged();
    }

    private static FluidStack copyType(FluidStack stack) {
        return new FluidStack(stack, 1);
    }
}
