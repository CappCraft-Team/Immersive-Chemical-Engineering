package team.cappcraft.immersivechemical.common.tileentity;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractTileHeatExchanger extends TileEntityMultiblockPart<AbstractTileHeatExchanger>
        implements IEBlockInterfaces.IGuiTile, IEBlockInterfaces.IProcessTile, IEBlockInterfaces.IMirrorAble, IEBlockInterfaces.IComparatorOverride {
    public static final String TAG_TANKS = "Tanks";
    public static final String TAG_PROCESSING = "Processing";
    public static final String TAG_STATE = "State";
    public static final String TAG_IDLE_TIME = "IdleTime";
    public static final String TAG_CACHED_HEAT = "Cached_Heat";
    public final int CoolDownBase = 20 * 10;
    public final IHeatExchangerProperties Properties;
    /**
     * Input/Output Tanks
     * InputA = 0
     * OutputA = 1
     * InputB = 2
     * OutputB = 3
     */
    public final LockableFluidTank[] Tanks;
    /**
     * When restored, we need to refresh the recipe by current input
     */
    @Nonnull
    public ITickableStateMachine<AbstractTileHeatExchanger> currentState = TileHeatExchangerTickAction.DecideRecipe.IS_INPUT_READY_FOR_MATCH;
    /**
     * Use for decide cool down,
     * increase when not exchanging heat, otherwise decrease
     */
    public int idleTime = 0;
    /**
     * Won't save to NBT, rely on the InputSlot,
     * as available recipes may change
     */
    protected HeatExchangerRecipe cachedRecipe;
    protected int coolDown;
    protected int cachedHeat;
    protected int tickSinceLastExchange;
    protected int tickRequired;

    public AbstractTileHeatExchanger(int[] structureDimensions, IHeatExchangerProperties properties) {
        super(structureDimensions);
        //region Apply Props
        Properties = properties;
        //endregion
        //region Setup FluidTanks
        final int Capacity = Properties.getCapacity();
        Tanks = new LockableFluidTank[]{
                new LockableFluidTank(Capacity, this::onInputSlotChanged),
                new LockableFluidTank(Capacity, this::onOutputSlotChanged),
                new LockableFluidTank(Capacity, this::onInputSlotChanged),
                new LockableFluidTank(Capacity, this::onOutputSlotChanged),
        };
        for (int i = 0; i < Tanks.length; i++) {
            LockableFluidTank tank = Tanks[i];
            tank.setTileEntity(this);
            tank.setCanFill(i % 2 == 0);
            tank.setCanDrain(i % 2 != 0);
        }
        //endregion
    }

    @Nonnull
    @Override
    protected abstract IFluidTank[] getAccessibleFluidTanks(@Nonnull EnumFacing side);

    @Override
    protected abstract boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource);

    @Override
    protected abstract boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side);

    @Nonnull
    @Override
    public abstract ItemStack getOriginalBlock();

    @Nonnull
    @Override
    public abstract float[] getBlockBounds();

    @Override
    public void update() {
        ApiUtils.checkForNeedlessTicking(this);
        if (isDummy() || world.isRemote) return;

        final ITickableStateMachine<AbstractTileHeatExchanger> newState = this.currentState.nextState(this);
        if (currentState != newState) markContainingBlockForUpdate(null);
        this.currentState = newState;
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        if (isDummy()) return;
        final NBTTagList tanks = nbt.getTagList(TAG_TANKS, 10);
        for (int i = 0; i < tanks.tagCount(); i++) {
            LockableFluidTank tank = Tanks[i];
            tank.readFromNBT(tanks.getCompoundTagAt(i));
        }
        idleTime = nbt.getInteger(TAG_IDLE_TIME);
        cachedHeat = nbt.getInteger(TAG_CACHED_HEAT);
        if (descPacket) {
            currentState = nbt.getBoolean(TAG_PROCESSING) ?
                    TileHeatExchangerTickAction.Processing.values()[nbt.getInteger(TAG_STATE)]
                    : TileHeatExchangerTickAction.DecideRecipe.values()[nbt.getInteger(TAG_STATE)];
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        if (isDummy()) return;
        final NBTTagList tanks = new NBTTagList();
        for (LockableFluidTank tank : Tanks) {
            tanks.appendTag(tank.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag(TAG_TANKS, tanks);
        nbt.setInteger(TAG_IDLE_TIME, idleTime);
        nbt.setInteger(TAG_CACHED_HEAT, cachedHeat);
        if (descPacket) {
            nbt.setBoolean(TAG_PROCESSING, currentState instanceof TileHeatExchangerTickAction.Processing);
            nbt.setInteger(TAG_STATE, ((Enum) currentState).ordinal());
        }
    }

    @Nonnull
    @Override
    public IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IEBlockInterfaces.IUsesBooleanProperty> inf) {
        return IEProperties.BOOLEANS[0];
    }

    @Override
    public boolean getIsMirrored() {
        return mirrored;
    }

    @Nonnull
    @Override
    public int[] getCurrentProcessesStep() {
        AbstractTileHeatExchanger master = master();
        if (master != this && master != null)
            return master.getCurrentProcessesStep();
        return new int[]{tickSinceLastExchange};
    }

    @Nonnull
    @Override
    public int[] getCurrentProcessesMax() {
        AbstractTileHeatExchanger master = master();
        if (master != this && master != null)
            return master.getCurrentProcessesMax();
        return new int[]{currentState instanceof TileHeatExchangerTickAction.Processing ? tickRequired : 0};
    }

    @Override
    public boolean canOpenGui() {
        return formed;
    }

    @Override
    public int getGuiID() {
        return 0;
    }

    @Nullable
    @Override
    public TileEntity getGuiMaster() {
        return master();
    }

    protected void onInputSlotChanged(FluidEvent event) {
        if (event instanceof FluidEvent.FluidFillingEvent) {
            if (currentState == TileHeatExchangerTickAction.DecideRecipe.WAITING_INPUT_SLOT_CHANGE) {
                coolDown = 5 * 20; //Wait 5 seconds, for clear Fluid action
                currentState = TileHeatExchangerTickAction.DecideRecipe.MATCH_COOL_DOWN;
            }
            ((ILockableSlot) ((FluidEvent.FluidFillingEvent) event).getTank()).setLockedType(true);
        }
    }

    protected void onOutputSlotChanged(FluidEvent event) {
        if (event instanceof FluidEvent.FluidFillingEvent)
            ((ILockableSlot) ((FluidEvent.FluidFillingEvent) event).getTank()).setLockedType(true);
    }

    /**
     * Clear fluid events, use block event to ensure thread safe
     *
     * @param id 1 -> Left Slot
     *           2 -> Right Slot
     *           3 -> All SLot
     */
    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (!super.receiveClientEvent(id, type)) {
            switch (id) {
                case 1:
                    clearFluidSlot(0);
                    clearFluidSlot(1);
                    return true;
                case 2:
                    clearFluidSlot(2);
                    clearFluidSlot(3);
                    return true;
                case 3:
                    clearFluidSlot(0);
                    clearFluidSlot(1);
                    clearFluidSlot(2);
                    clearFluidSlot(3);
                    return true;
            }
        }
        return false;
    }

    /**
     * Clear the fluid in the specified slot and unlock it
     * Won't action if in WAITING_FOR_RECIPE_MATCH state
     */
    protected void clearFluidSlot(int slot) {
        if (currentState == TileHeatExchangerTickAction.DecideRecipe.WAITING_FOR_RECIPE_MATCH) return;
        currentState = TileHeatExchangerTickAction.DecideRecipe.WAITING_INPUT_SLOT_CHANGE;
        final LockableFluidTank tank = Tanks[slot];
        tank.setLockedType(false);
        tank.setFluid(null);
    }

    /**
     * Won't action if in WAITING_FOR_RECIPE_MATCH state
     */
    protected void setFluidFilter(int slot, FluidStack fluidStack) {
        if (currentState == TileHeatExchangerTickAction.DecideRecipe.WAITING_FOR_RECIPE_MATCH) return;
        currentState = TileHeatExchangerTickAction.DecideRecipe.MATCH_COOL_DOWN;
        final LockableFluidTank tank = Tanks[slot];
        tank.setLockedType(true);
        tank.setFluid(new FluidStack(fluidStack, 0));
    }

    private int exchange(HeatExchangerEntry entry, ConvertDirection direction, LockableFluidTank input, LockableFluidTank output) {
        final FluidStack In = entry.getInput(direction);
        final FluidStack Out = entry.getOutput(direction);

        if (input.getFluidAmount() >= In.amount && output.getSpareVolume() >= Out.amount) {
            input.drainInternal(In, true);
            output.fillInternal(Out, true);
            return entry.HeatValue;
        }
        return 0;
    }

    /**
     * Delta the idle time of the machine, to reduce useless tick
     *
     * @param delta delta value
     */
    protected void deltaIdle(int delta) {
        idleTime += delta;
        idleTime = net.minecraft.util.math.MathHelper.clamp(idleTime, -CoolDownBase, CoolDownBase * 3);
    }

    public static class TileHeatExchangerTickAction {
        public enum DecideRecipe implements ITickableStateMachine<AbstractTileHeatExchanger> {
            WAITING_INPUT_SLOT_CHANGE,
            MATCH_COOL_DOWN {
                @Override
                public ITickableStateMachine<AbstractTileHeatExchanger> nextState(AbstractTileHeatExchanger tickAble) {
                    if (--tickAble.coolDown <= 0) return IS_INPUT_READY_FOR_MATCH;
                    return this;
                }
            },
            IS_INPUT_READY_FOR_MATCH {
                @Override
                public ITickableStateMachine<AbstractTileHeatExchanger> nextState(AbstractTileHeatExchanger tickAble) {
                    if (tickAble.Tanks[0].getFluid() != null && tickAble.Tanks[2].getFluid() != null) {
                        try {
                            HeatExchangerRegistry.REGISTRY.RecipeFinder.submit(() -> {
                                tickAble.cachedRecipe =
                                        HeatExchangerRegistry.REGISTRY.findRecipe(
                                                tickAble.Tanks[0],
                                                tickAble.Tanks[2],
                                                tickAble.Tanks[1],
                                                tickAble.Tanks[3]
                                        ).orElse(null);
                            }).get(20, TimeUnit.SECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            ImmersiveChemicalEngineering.logger.error("Exception occurred while searching recipes", e);
                            tickAble.cachedRecipe = null;
                        } finally {
                            //Ensure state is changed after this tick
                            ApiUtils.addFutureServerTask(tickAble.world, () -> {
                                if (tickAble.cachedRecipe != null) {
                                    tickAble.currentState = Processing.DO_EXCHANGE;
                                } else {
                                    tickAble.currentState = WAITING_INPUT_SLOT_CHANGE;
                                }
                            });
                        }
                        return WAITING_FOR_RECIPE_MATCH;
                    }
                    return WAITING_INPUT_SLOT_CHANGE;
                }
            },
            /**
             * When in this state, means an async task is running
             * we should not change this state nor change the slot
             */
            WAITING_FOR_RECIPE_MATCH
        }

        /**
         * When in processing state, cachedRecipe should not be null
         */
        public enum Processing implements ITickableStateMachine<AbstractTileHeatExchanger> {
            COOL_DOWN {
                @Override
                public ITickableStateMachine<AbstractTileHeatExchanger> nextState(AbstractTileHeatExchanger tickAble) {
                    if (--tickAble.coolDown <= 0) return DO_EXCHANGE;
                    return this;
                }
            },
            DO_EXCHANGE {
                @Override
                public ITickableStateMachine<AbstractTileHeatExchanger> nextState(AbstractTileHeatExchanger tickAble) {
                    if (++tickAble.tickSinceLastExchange > tickAble.tickRequired) {
                        tickAble.tickSinceLastExchange = 0;
                        tickAble.tickRequired = 0;
                        tickAble.deltaIdle(1);

                        HeatExchangerEntry ExchangeA = tickAble.cachedRecipe.ExchangeA;
                        HeatExchangerEntry ExchangeB = tickAble.cachedRecipe.ExchangeB;

                        ConvertDirection DirectionA = tickAble.cachedRecipe.DirectionA;
                        final int oldHeat = tickAble.cachedHeat;
                        //Heat A->B
                        if (DirectionA == ConvertDirection.COOL_DOWN) {
                            if (tickAble.cachedHeat < ExchangeB.HeatValue) {
                                tickAble.cachedHeat += exchangeA(tickAble);
                            } else {
                                final int doExchanged = exchangeB(tickAble);
                                tickAble.tickRequired = (int) (doExchanged * tickAble.Properties.getTickMultiplier());
                                tickAble.cachedHeat -= doExchanged;
                            }
                        } else {
                            if (tickAble.cachedHeat < ExchangeA.HeatValue) {
                                tickAble.cachedHeat += exchangeB(tickAble);
                            } else {
                                final int doExchanged = exchangeA(tickAble);
                                tickAble.tickRequired = (int) (doExchanged * tickAble.Properties.getTickMultiplier());
                                tickAble.cachedHeat -= doExchanged;
                            }
                        }

                        if (oldHeat != tickAble.cachedHeat) {
                            tickAble.deltaIdle(-2);
                            return this;
                        }

                        //exchanged nothing, just wait for the fluid
                        tickAble.coolDown = tickAble.CoolDownBase + tickAble.idleTime;
                        return COOL_DOWN;
                    }
                    return this;
                }

                private int exchangeA(AbstractTileHeatExchanger tickAble) {
                    return tickAble.exchange(tickAble.cachedRecipe.ExchangeA, tickAble.cachedRecipe.DirectionA, tickAble.Tanks[0], tickAble.Tanks[1]);
                }

                private int exchangeB(AbstractTileHeatExchanger tickAble) {
                    return tickAble.exchange(tickAble.cachedRecipe.ExchangeB, tickAble.cachedRecipe.DirectionB, tickAble.Tanks[2], tickAble.Tanks[3]);
                }
            }
        }
    }
}
