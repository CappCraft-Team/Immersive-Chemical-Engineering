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
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipeEntry;
import team.cappcraft.immersivechemical.common.recipe.constant.HeatExchangerSize;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.concurrent.Executors;

public abstract class AbstractTileHeatExchanger extends TileEntityMultiblockPart<AbstractTileHeatExchanger>
        implements IEBlockInterfaces.IGuiTile, IEBlockInterfaces.IProcessTile, IEBlockInterfaces.IMirrorAble {
    public static final String TAG_TANKS = "Tanks";
    public static final String TAG_PROCESSING = "Processing";
    public static final String TAG_STATE = "State";
    public static final String TAG_IDLE_TIME = "IdleTime";
    public final int CoolDownBase = 20 * 10;
    @Nonnull
    public final HeatExchangerSize Size;
    public final int Capacity;
    /**
     * Tick required to exchange one heat
     */
    public final float TickRequiredToExchange;
    /**
     * Input/Output Tanks
     * InputA = 0
     * OutputA = 1
     * InputB = 2
     * OutputB = 3
     */
    public final LockableFluidTank[] Tanks;
    /**
     * When restore, we need to refresh the recipe by current input
     */
    @Nonnull
    public ITickableStateMachine<AbstractTileHeatExchanger> currentState = TileHeatExchangerTickAction.DecideRecipe.IS_INPUT_READY_FOR_MATCH;
    /**
     * Wont save to NBT, rely on the InputSlot
     * available recipes may changing
     */
    @Nullable
    protected HeatExchangerRecipe cachedRecipe;
    /**
     * Use to decide cool down time
     * increase when not exchange heat, otherwise decrease
     */
    public int idleTime = 0;
    protected int exchangeTimes;
    protected int coolDown;
    protected int tickSinceLastExchange;
    protected int tickRequired;


    public AbstractTileHeatExchanger(int[] structureDimensions, int capacity, @Nonnull HeatExchangerSize size, float tickRequiredToExchange) {
        super(structureDimensions);
        //region Apply Props
        Capacity = capacity;
        Size = size;
        TickRequiredToExchange = tickRequiredToExchange;
        //endregion
        //region Setup FluidTanks
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
        if (isDummy()) return;
        if (world.isRemote) {
            //TODO: handle client side logic
        } else {
            final ITickableStateMachine<AbstractTileHeatExchanger> newState = this.currentState.nextState(this);
            if (currentState != newState) markContainingBlockForUpdate(null);
            this.currentState = newState;
        }
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        final NBTTagList tanks = nbt.getTagList(TAG_TANKS, 10);
        for (int i = 0; i < tanks.tagList.size(); i++) {
            LockableFluidTank tank = Tanks[i];
            tank.readFromNBT((NBTTagCompound) tanks.tagList.get(i));
        }
        idleTime = nbt.getInteger(TAG_IDLE_TIME);
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
        final NBTTagList tanks = new NBTTagList();
        for (LockableFluidTank tank : Tanks) {
            tanks.appendTag(tank.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag(TAG_TANKS, tanks);
        nbt.setInteger(TAG_IDLE_TIME, idleTime);
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
        return new int[]{tickRequired};
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
        if (currentState == TileHeatExchangerTickAction.DecideRecipe.WAITING_INPUT_SLOT_CHANGE) {
            coolDown = 5 * 20; //Wait 5 seconds, for clear Fluid action
            currentState = TileHeatExchangerTickAction.DecideRecipe.MATCH_COOL_DOWN;
        }
    }

    protected void onOutputSlotChanged(FluidEvent event) {
    }

    private void setInputSlotLockType(boolean locked) {
        for (int i = 0; i < Tanks.length; i++) {
            if (i % 2 == 0) Tanks[i].setLockedType(locked);
        }
        markDirty();
    }

    private void setOutputSlotLockType(boolean locked) {
        for (int i = 0; i < Tanks.length; i++) {
            if (i % 2 != 0) Tanks[i].setLockedType(locked);
        }
        markDirty();
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
     * Wont action if in WAITING_FOR_RECIPE_MATCH state
     */
    private void clearFluidSlot(int slot) {
        if (currentState == TileHeatExchangerTickAction.DecideRecipe.WAITING_FOR_RECIPE_MATCH) return;
        currentState = TileHeatExchangerTickAction.DecideRecipe.WAITING_INPUT_SLOT_CHANGE;
        final LockableFluidTank tank = Tanks[slot];
        tank.setLockedType(false);
        tank.setFluid(null);
    }

    private void deltaIdle(int delta) {
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
                        Executors.newSingleThreadExecutor().submit(() -> {
                            tickAble.cachedRecipe =
                                    ImmersiveChemicalEngineering.proxy.heatExchangerRecipeRegistry.
                                            findRecipe(tickAble.Size, tickAble.Tanks[0], tickAble.Tanks[2]).orElse(null);
                            if (tickAble.cachedRecipe != null) {
                                tickAble.currentState = Processing.ASSERT_IO_FLUID_TYPE;
                                tickAble.tickRequired = (int) (tickAble.cachedRecipe.UnitHeatValue * tickAble.TickRequiredToExchange);
                            } else {
                                tickAble.currentState = WAITING_INPUT_SLOT_CHANGE;
                                tickAble.tickRequired = 0;
                            }
                        });
                        return WAITING_FOR_RECIPE_MATCH;
                    }
                    return WAITING_INPUT_SLOT_CHANGE;
                }
            },
            /**
             * When in this state, means a async task is running
             * we should not change this state nor change the slot
             */
            WAITING_FOR_RECIPE_MATCH
        }

        /**
         * When in processing state, cachedRecipe should not be null
         */
        @SuppressWarnings("ConstantConditions")
        public enum Processing implements ITickableStateMachine<AbstractTileHeatExchanger> {
            COOL_DOWN {
                @Override
                public ITickableStateMachine<AbstractTileHeatExchanger> nextState(AbstractTileHeatExchanger tickAble) {
                    if (--tickAble.coolDown <= 0) return ASSERT_IO_FLUID_TYPE;
                    return this;
                }
            },
            ASSERT_IO_FLUID_TYPE {
                @Override
                public ITickableStateMachine<AbstractTileHeatExchanger> nextState(AbstractTileHeatExchanger tickAble) {
                    final HeatExchangerRecipeEntry RecipeA = tickAble.cachedRecipe.ExchangeA;
                    final HeatExchangerRecipeEntry RecipeB = tickAble.cachedRecipe.ExchangeB;
                    if (RecipeA.isInputMatches(tickAble.Tanks[0])
                            && RecipeA.isOutputMatches(tickAble.Tanks[1])
                            && RecipeB.isInputMatches(tickAble.Tanks[2])
                            && RecipeB.isOutputMatches(tickAble.Tanks[3])) {
                        //Lock fluid type here, we wont check type in the following states
                        tickAble.setInputSlotLockType(true);
                        return ASSERT_IO_FLUID_AMOUNT;
                    }
                    return DecideRecipe.IS_INPUT_READY_FOR_MATCH;
                }
            },
            ASSERT_IO_FLUID_AMOUNT {
                @Override
                public ITickableStateMachine<AbstractTileHeatExchanger> nextState(AbstractTileHeatExchanger tickAble) {
                    tickAble.deltaIdle(1);

                    final HeatExchangerRecipe cachedRecipe = tickAble.cachedRecipe;
                    tickAble.exchangeTimes = Arrays.stream(new int[]{
                            tickAble.Tanks[0].getFluidAmount() / cachedRecipe.UnitInputA,
                            tickAble.Tanks[1].getSpareVolume() / cachedRecipe.UnitOutputA,
                            tickAble.Tanks[2].getFluidAmount() / cachedRecipe.UnitInputB,
                            tickAble.Tanks[3].getSpareVolume() / cachedRecipe.UnitOutputB
                    }).min().getAsInt();

                    //nothing to exchange, just wait for the fluid
                    if (tickAble.exchangeTimes == 0) {
                        tickAble.coolDown = tickAble.CoolDownBase + tickAble.idleTime;
                        //Unlock to allow new type in
                        tickAble.setInputSlotLockType(false);
                        return COOL_DOWN;
                    }
                    tickAble.deltaIdle(-1);
                    return DO_EXCHANGE;
                }
            },
            DO_EXCHANGE {
                @Override
                public ITickableStateMachine<AbstractTileHeatExchanger> nextState(AbstractTileHeatExchanger tickAble) {
                    if (++tickAble.tickSinceLastExchange >= tickAble.tickRequired) {
                        tickAble.tickSinceLastExchange = 0;

                        final HeatExchangerRecipe cachedRecipe = tickAble.cachedRecipe;

                        tickAble.Tanks[0].drainInternal(cachedRecipe.UnitInputA, true);
                        tickAble.Tanks[1].fillInternal(new FluidStack(cachedRecipe.ExchangeA.Output, cachedRecipe.UnitOutputA), true);
                        tickAble.Tanks[2].drainInternal(cachedRecipe.UnitInputB, true);
                        tickAble.Tanks[3].fillInternal(new FluidStack(cachedRecipe.ExchangeB.Output, cachedRecipe.UnitOutputB), true);

                        tickAble.exchangeTimes--;
                    }
                    return tickAble.exchangeTimes > 0 ? DO_EXCHANGE : ASSERT_IO_FLUID_AMOUNT;
                }
            }
        }
    }
}
