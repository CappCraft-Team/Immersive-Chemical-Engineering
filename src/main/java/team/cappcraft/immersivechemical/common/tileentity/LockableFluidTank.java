package team.cappcraft.immersivechemical.common.tileentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;




public class LockableFluidTank extends FluidTank {
    public static final String TAG_LOCKED = "Locked";
    public static final String TAG_LOCKED_TYPE = "LockedType";
    public static final String TAG_LOCKED_FLUID = "LockedFluid";

    private final Consumer<FluidStack> onContentChangedConsumer;
    private boolean locked;
    private boolean lockedType;
    private FluidStack lockedFluid = FluidStack.EMPTY;

    public LockableFluidTank(int capacity, Consumer<FluidStack> onContentChanged) {
        super(requirePositiveCapacity(capacity));
        this.onContentChangedConsumer = Objects.requireNonNull(onContentChanged, "onContentChanged");
    }

    public LockableFluidTank(
            @Nullable FluidStack fluidStack, int capacity, Consumer<FluidStack> onContentChanged
    ) {
        this(capacity, onContentChanged);
        setFluid(fluidStack);
    }

    public LockableFluidTank(
            @Nullable Fluid fluid, int amount, int capacity, Consumer<FluidStack> onContentChanged
    ) {
        this(capacity, onContentChanged);
        if (fluid != null && amount > 0) {
            setFluid(new FluidStack(fluid, amount));
        }
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        if (locked || stack == null || stack.isEmpty()) {
            return false;
        }
        if (lockedType && (lockedFluid.isEmpty() || !lockedFluid.isFluidEqual(stack))) {
            return false;
        }
        return super.isFluidValid(stack);
    }

    @Override
    public FluidTank readFromNBT(CompoundTag nbt) {
        Objects.requireNonNull(nbt, "nbt");
        locked = nbt.getBoolean(TAG_LOCKED);
        lockedType = nbt.getBoolean(TAG_LOCKED_TYPE);
        lockedFluid = nbt.contains(TAG_LOCKED_FLUID, Tag.TAG_COMPOUND)
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
        Objects.requireNonNull(nbt, "nbt");
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
        Objects.requireNonNull(action, "action");
        if (locked || resource == null || resource.isEmpty()) {
            return 0;
        }
        return super.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        Objects.requireNonNull(action, "action");
        if (locked || resource == null || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack drained = super.drain(resource, action);
        return drained == null ? FluidStack.EMPTY : drained;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Objects.requireNonNull(action, "action");
        if (locked || maxDrain <= 0 || fluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack drained = super.drain(maxDrain, action);
        return drained == null ? FluidStack.EMPTY : drained;
    }

    @Override
    public void setFluid(@Nullable FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
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

    private static int requirePositiveCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Tank capacity must be positive");
        }
        return capacity;
    }
}
