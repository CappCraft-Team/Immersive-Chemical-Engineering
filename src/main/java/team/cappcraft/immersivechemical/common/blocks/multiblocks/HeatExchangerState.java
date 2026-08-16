package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import team.cappcraft.immersivechemical.common.tileentity.LockableFluidTank;

import javax.annotation.Nullable;
import java.util.Objects;









public class HeatExchangerState implements IMultiblockState {

    public static final int INPUT_A = 0;
    public static final int OUTPUT_A = 1;
    public static final int INPUT_B = 2;
    public static final int OUTPUT_B = 3;
    public static final int TANK_COUNT = 4;

    private static final String TAG_TANKS = "Tanks";
    private static final String TAG_COOLDOWN = "Cooldown";
    private static final String TAG_TICK_SINCE_EXCHANGE = "TickSinceExchange";
    private static final String TAG_TICK_REQUIRED = "TickRequired";
    private static final String TAG_PROCESSING = "Processing";

    public final LockableFluidTank[] Tanks;
    final StoredCapability<IFluidHandler> inputACapability;
    final StoredCapability<IFluidHandler> outputACapability;
    final StoredCapability<IFluidHandler> portACapability;
    final StoredCapability<IFluidHandler> inputBCapability;
    final StoredCapability<IFluidHandler> outputBCapability;
    final StoredCapability<IFluidHandler> portBCapability;
    final CapabilityReference<IFluidHandler> automaticOutputA;
    final CapabilityReference<IFluidHandler> automaticOutputB;

    int cooldown;
    int tickSinceLastExchange;
    int tickRequired = 1;
    boolean processing;

    protected HeatExchangerState(
            int tankCapacity,
            IInitialMultiblockContext<?> context,
            CapabilityReference<IFluidHandler> automaticOutputA,
            CapabilityReference<IFluidHandler> automaticOutputB
    ) {
        Runnable markDirty = context.getMarkDirtyRunnable();
        Runnable sync = context.getSyncRunnable();
        this.Tanks = new LockableFluidTank[]{
                createTank(tankCapacity, markDirty, sync),
                createTank(tankCapacity, markDirty, sync),
                createTank(tankCapacity, markDirty, sync),
                createTank(tankCapacity, markDirty, sync)
        };

        this.inputACapability = new StoredCapability<>(
                new PortFluidHandler(Tanks[INPUT_A], null));
        this.outputACapability = new StoredCapability<>(
                new PortFluidHandler(null, Tanks[OUTPUT_A]));
        this.portACapability = new StoredCapability<>(
                new PortFluidHandler(Tanks[INPUT_A], Tanks[OUTPUT_A]));
        this.inputBCapability = new StoredCapability<>(
                new PortFluidHandler(Tanks[INPUT_B], null));
        this.outputBCapability = new StoredCapability<>(
                new PortFluidHandler(null, Tanks[OUTPUT_B]));
        this.portBCapability = new StoredCapability<>(
                new PortFluidHandler(Tanks[INPUT_B], Tanks[OUTPUT_B]));
        this.automaticOutputA = Objects.requireNonNull(automaticOutputA, "automaticOutputA");
        this.automaticOutputB = Objects.requireNonNull(automaticOutputB, "automaticOutputB");
    }

    private static LockableFluidTank createTank(int capacity, Runnable markDirty, Runnable sync) {
        return new LockableFluidTank(capacity, ignored -> {
            markDirty.run();
            sync.run();
        });
    }

    @Override
    public void writeSaveNBT(CompoundTag nbt) {
        writeCommonNBT(nbt);
    }

    @Override
    public void readSaveNBT(CompoundTag nbt) {
        readCommonNBT(nbt);
    }

    @Override
    public void writeSyncNBT(CompoundTag nbt) {
        writeCommonNBT(nbt);
    }

    @Override
    public void readSyncNBT(CompoundTag nbt) {
        readCommonNBT(nbt);
    }

    private void writeCommonNBT(CompoundTag nbt) {
        ListTag tanks = new ListTag();
        for (LockableFluidTank tank : Tanks) {
            tanks.add(tank.writeToNBT(new CompoundTag()));
        }
        nbt.put(TAG_TANKS, tanks);
        nbt.putInt(TAG_COOLDOWN, cooldown);
        nbt.putInt(TAG_TICK_SINCE_EXCHANGE, tickSinceLastExchange);
        nbt.putInt(TAG_TICK_REQUIRED, tickRequired);
        nbt.putBoolean(TAG_PROCESSING, processing);
    }

    private void readCommonNBT(CompoundTag nbt) {
        ListTag tanks = nbt.getList(TAG_TANKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < tanks.size() && i < Tanks.length; ++i) {
            Tanks[i].readFromNBT(tanks.getCompound(i));
        }
        cooldown = Math.max(0, nbt.getInt(TAG_COOLDOWN));
        tickSinceLastExchange = Math.max(0, nbt.getInt(TAG_TICK_SINCE_EXCHANGE));
        tickRequired = Math.max(1, nbt.getInt(TAG_TICK_REQUIRED));
        processing = nbt.getBoolean(TAG_PROCESSING);
    }

    public FluidStack getTankFluid(int tankIndex) {
        if (!isTankIndex(tankIndex)) {
            return FluidStack.EMPTY;
        }
        FluidStack fluid = Tanks[tankIndex].getFluid();
        return fluid.isEmpty() ? FluidStack.EMPTY : fluid.copy();
    }

    public int getTankCapacity(int tankIndex) {
        return isTankIndex(tankIndex) ? Tanks[tankIndex].getCapacity() : 0;
    }

    public boolean isTankTypeLocked(int tankIndex) {
        return isTankIndex(tankIndex) && Tanks[tankIndex].isLockedType();
    }

    public int getExchangeProgress() {
        return tickSinceLastExchange;
    }

    public int getExchangeDuration() {
        return tickRequired;
    }

    public boolean isProcessing() {
        return processing;
    }

    
    public int getComparatorOutput(int tankIndex) {
        if (!isTankIndex(tankIndex)) {
            return 0;
        }
        LockableFluidTank tank = Tanks[tankIndex];
        return (int)(15L * tank.getFluidAmount() / Math.max(1, tank.getCapacity()));
    }

    
    public boolean clearTank(int tankIndex) {
        if (!isTankIndex(tankIndex)) {
            return false;
        }

        LockableFluidTank tank = Tanks[tankIndex];
        boolean changed = tank.isLocked() || tank.isLockedType() || !tank.getFluid().isEmpty();
        if (!changed) {
            return false;
        }

        tank.setLocked(false);
        tank.setLockedType(false);
        tank.setFluid(FluidStack.EMPTY);
        resetAfterManualTankChange();
        return true;
    }

    public boolean clearAllTanks() {
        boolean changed = false;
        for (int tankIndex = 0; tankIndex < Tanks.length; ++tankIndex) {
            changed |= clearTank(tankIndex);
        }
        return changed;
    }

    
    public boolean clearTankSide(int sideIndex) {
        if (sideIndex < 0 || sideIndex > 1) {
            return false;
        }
        int firstTank = sideIndex * 2;
        return clearTank(firstTank) | clearTank(firstTank + 1);
    }

    



    public boolean setTankTypeLocked(int tankIndex, boolean locked) {
        if (!isTankIndex(tankIndex)) {
            return false;
        }

        LockableFluidTank tank = Tanks[tankIndex];
        if (tank.isLockedType() == locked || (locked && tank.getFluid().isEmpty())) {
            return false;
        }

        tank.setLockedType(locked);
        resetAfterManualTankChange();
        return true;
    }

    private void resetAfterManualTankChange() {
        cooldown = 0;
        tickSinceLastExchange = 0;
        tickRequired = Math.max(1, tickRequired);
        processing = false;
    }

    private static boolean isTankIndex(int tankIndex) {
        return tankIndex >= 0 && tankIndex < TANK_COUNT;
    }

    private static final class PortFluidHandler implements IFluidHandler {
        @Nullable
        private final LockableFluidTank input;
        @Nullable
        private final LockableFluidTank output;

        private PortFluidHandler(@Nullable LockableFluidTank input, @Nullable LockableFluidTank output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public int getTanks() {
            return input != null && output != null ? 2 : 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            LockableFluidTank selected = getTank(tank);
            return selected == null ? FluidStack.EMPTY : selected.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            LockableFluidTank selected = getTank(tank);
            return selected == null ? 0 : selected.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && input != null && input.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return input == null ? 0 : input.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return output == null ? FluidStack.EMPTY : output.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return output == null ? FluidStack.EMPTY : output.drain(maxDrain, action);
        }

        @Nullable
        private LockableFluidTank getTank(int index) {
            if (input != null && output != null) {
                if (index == 0) {
                    return input;
                }
                return index == 1 ? output : null;
            }
            return index == 0 ? (input != null ? input : output) : null;
        }
    }
}
