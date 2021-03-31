package team.cappcraft.immersivechemical.common.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class LockableFluidTank extends FluidTank implements ILockableSlot {
    public static final String TAG_LOCKED = "Locked";
    public static final String TAG_LOCKED_TYPE = "LockedType";
    private final Consumer<FluidEvent> onContentChangedConsumer;
    private boolean locked = false;
    private boolean lockedType = false;

    public LockableFluidTank(int capacity, Consumer<FluidEvent> onContentChanged) {
        super(capacity);
        onContentChangedConsumer = onContentChanged;
    }

    public LockableFluidTank(@Nullable FluidStack fluidStack, int capacity, Consumer<FluidEvent> onContentChanged) {
        super(fluidStack, capacity);
        onContentChangedConsumer = onContentChanged;
    }

    public LockableFluidTank(Fluid fluid, int amount, int capacity, Consumer<FluidEvent> onContentChanged) {
        super(fluid, amount, capacity);
        onContentChangedConsumer = onContentChanged;
    }

    /**
     * @return the volume the tank left
     */
    public int getSpareVolume() {
        if (fluid != null) {
            return capacity - fluid.amount;
        }
        return capacity;
    }

    @Override
    public boolean canDrain() {
        return !isLocked() && super.canDrain();
    }

    @Override
    public boolean canFill() {
        return !isLocked() && super.canFill();
    }

    @Override
    public FluidTank readFromNBT(NBTTagCompound nbt) {
        locked = nbt.getBoolean(TAG_LOCKED);
        lockedType = nbt.getBoolean(TAG_LOCKED_TYPE);
        return super.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean(TAG_LOCKED, locked);
        nbt.setBoolean(TAG_LOCKED_TYPE, lockedType);
        return super.writeToNBT(nbt);
    }

    @Override
    public int fillInternal(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0 || (fluid == null && isLockedType())) {
            return 0;
        }

        if (!doFill) {
            if (fluid == null) {
                return Math.min(capacity, resource.amount);
            }

            if (!fluid.isFluidEqual(resource)) {
                return 0;
            }

            return Math.min(capacity - fluid.amount, resource.amount);
        }

        if (fluid == null) {
            fluid = new FluidStack(resource, Math.min(capacity, resource.amount));

            onContentsChanged();

            if (tile != null) {
                FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, tile.getWorld(), tile.getPos(), this, fluid.amount));
            }
            return fluid.amount;
        }

        if (!fluid.isFluidEqual(resource)) {
            return 0;
        }
        int filled = capacity - fluid.amount;

        if (resource.amount < filled) {
            fluid.amount += resource.amount;
            filled = resource.amount;
        } else {
            fluid.amount = capacity;
        }

        onContentsChanged();

        if (tile != null) {
            final FluidEvent.FluidFillingEvent fluidEvent = new FluidEvent.FluidFillingEvent(fluid, tile.getWorld(), tile.getPos(), this, filled);
            onContentChangedConsumer.accept(fluidEvent);
            FluidEvent.fireEvent(fluidEvent);
        }
        return filled;
    }

    @Nullable
    @Override
    public FluidStack drainInternal(int maxDrain, boolean doDrain) {
        if (fluid == null || maxDrain <= 0) {
            return null;
        }

        int drained = maxDrain;
        if (fluid.amount < drained) {
            drained = fluid.amount;
        }

        FluidStack stack = new FluidStack(fluid, drained);
        if (doDrain) {
            fluid.amount -= drained;
            if (fluid.amount <= 0 && !isLockedType()) {
                fluid = null;
            }

            onContentsChanged();

            if (tile != null) {
                final FluidEvent.FluidDrainingEvent fluidEvent = new FluidEvent.FluidDrainingEvent(fluid, tile.getWorld(), tile.getPos(), this, drained);
                onContentChangedConsumer.accept(fluidEvent);
                FluidEvent.fireEvent(fluidEvent);
            }
        }
        return stack;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
        if (!locked && fluid != null && fluid.amount <= 0)
            fluid = null;
        if (tile != null) tile.markDirty();
    }

    @Override
    public boolean isLockedType() {
        return lockedType;
    }

    @Override
    public void setLockedType(boolean locked) {
        lockedType = locked;
        if (!locked && fluid != null && fluid.amount <= 0)
            fluid = null;
        if (tile != null) tile.markDirty();
    }
}
