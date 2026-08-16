package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;
import team.cappcraft.immersivechemical.common.tileentity.LockableFluidTank;

public class HeatExchangerState implements IMultiblockState {

    public static final int INPUT_A = 0;
    public static final int OUTPUT_A = 1;
    public static final int INPUT_B = 2;
    public static final int OUTPUT_B = 3;
    public static final int TANK_COUNT = 4;

    private static final String TAG_TANKS = "Tanks";
    private static final String TAG_COOLDOWN = "Cooldown";
    private static final String TAG_HEAT_BUFFER = "HeatBuffer";
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
    long heatBuffer;
    boolean processing;
    @Nullable HeatExchangerRecipe cachedRecipe;
    boolean recipeDirty = true;

    protected HeatExchangerState(
            int tankCapacity,
            IInitialMultiblockContext<?> context,
            CapabilityReference<IFluidHandler> automaticOutputA,
            CapabilityReference<IFluidHandler> automaticOutputB) {
        Runnable markDirty = context.getMarkDirtyRunnable();
        Runnable sync = context.getSyncRunnable();
        this.Tanks =
                new LockableFluidTank[] {
                    createTank(tankCapacity, true, markDirty, sync),
                    createTank(tankCapacity, true, markDirty, sync),
                    createTank(tankCapacity, true, markDirty, sync),
                    createTank(tankCapacity, true, markDirty, sync)
                };

        this.inputACapability = new StoredCapability<>(new PortFluidHandler(Tanks[INPUT_A], null));
        this.outputACapability =
                new StoredCapability<>(new PortFluidHandler(null, Tanks[OUTPUT_A]));
        this.portACapability =
                new StoredCapability<>(new PortFluidHandler(Tanks[INPUT_A], Tanks[OUTPUT_A]));
        this.inputBCapability = new StoredCapability<>(new PortFluidHandler(Tanks[INPUT_B], null));
        this.outputBCapability =
                new StoredCapability<>(new PortFluidHandler(null, Tanks[OUTPUT_B]));
        this.portBCapability =
                new StoredCapability<>(new PortFluidHandler(Tanks[INPUT_B], Tanks[OUTPUT_B]));
        this.automaticOutputA = automaticOutputA;
        this.automaticOutputB = automaticOutputB;
    }

    private LockableFluidTank createTank(
            int capacity, boolean autoLock, Runnable markDirty, Runnable sync) {
        return new LockableFluidTank(
                capacity,
                ignored -> {
                    recipeDirty = true;
                    markDirty.run();
                    sync.run();
                },
                autoLock);
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
        nbt.putLong(TAG_HEAT_BUFFER, heatBuffer);
        nbt.putBoolean(TAG_PROCESSING, processing);
    }

    private void readCommonNBT(CompoundTag nbt) {
        ListTag tanks = nbt.getList(TAG_TANKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < tanks.size() && i < Tanks.length; ++i) {
            Tanks[i].readFromNBT(tanks.getCompound(i));
        }
        cooldown = nbt.getInt(TAG_COOLDOWN);
        heatBuffer = nbt.getLong(TAG_HEAT_BUFFER);
        processing = nbt.getBoolean(TAG_PROCESSING);
    }

    public FluidStack getTankFluid(int tankIndex) {
        FluidStack fluid = Tanks[tankIndex].getFluid();
        return fluid.isEmpty() ? FluidStack.EMPTY : fluid.copy();
    }

    public int getTankCapacity(int tankIndex) {
        return Tanks[tankIndex].getCapacity();
    }

    public boolean isTankTypeLocked(int tankIndex) {
        return Tanks[tankIndex].isLockedType();
    }

    public long getHeatBuffer() {
        return heatBuffer;
    }

    public boolean isProcessing() {
        return processing;
    }

    public int getComparatorOutput(int tankIndex) {
        LockableFluidTank tank = Tanks[tankIndex];
        return (int) (15L * tank.getFluidAmount() / tank.getCapacity());
    }

    public boolean clearTank(int tankIndex) {
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
        int firstTank = sideIndex * 2;
        return clearTank(firstTank) | clearTank(firstTank + 1);
    }

    public boolean setTankTypeLocked(int tankIndex, boolean locked) {
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
        heatBuffer = 0;
        processing = false;
    }

    private static final class PortFluidHandler implements IFluidHandler {
        @Nullable private final LockableFluidTank input;
        @Nullable private final LockableFluidTank output;

        private PortFluidHandler(
                @Nullable LockableFluidTank input, @Nullable LockableFluidTank output) {
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
