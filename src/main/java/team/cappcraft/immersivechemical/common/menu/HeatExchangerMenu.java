package team.cappcraft.immersivechemical.common.menu;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerState;
import team.cappcraft.immersivechemical.common.network.HeatExchangerMenuAction;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

public final class HeatExchangerMenu extends IEContainerMenu {
    public static final int TANK_COUNT = HeatExchangerMenuAction.TANK_COUNT;
    private static final int PLAYER_INVENTORY_START = 0;
    private static final int PLAYER_INVENTORY_END = 27;
    private static final int HOTBAR_START = 27;
    private static final int HOTBAR_END = 36;
    private static final double MAX_DISTANCE_SQUARED = 64.0D;

    private final FluidTank[] syncedTanks = new FluidTank[TANK_COUNT];
    private final boolean[] syncedTypeLocks = new boolean[TANK_COUNT];
    private final BlockPos masterPos;
    private final BlockPos interactionPos;

    @Nullable private final HeatExchangerState serverState;
    @Nullable private final IMultiblockContext<HeatExchangerState> serverContext;
    @Nullable private final Level serverLevel;
    @Nullable private final Block expectedFormedBlock;

    private int syncedCapacity = 1;
    private int syncedProgress;
    private int syncedDuration = 1;
    private boolean syncedProcessing;

    public HeatExchangerMenu(
            HeatExchangerSizeVariants size,
            int containerId,
            Inventory inventory,
            HeatExchangerState state,
            IMultiblockContext<HeatExchangerState> context,
            BlockPos masterPos,
            BlockPos interactionPos) {
        super(
                multiblockCtx(
                        ICHEME_Contents.menuType(size),
                        containerId,
                        new MultiblockMenuContext<>(context, masterPos)));
        this.serverState = state;
        this.serverContext = context;
        this.serverLevel = context.getLevel().getRawLevel();
        this.masterPos = masterPos.immutable();
        this.interactionPos = interactionPos.immutable();
        this.expectedFormedBlock = ICHEME_Contents.registration(size).block().get();

        for (int tankIndex = 0; tankIndex < TANK_COUNT; ++tankIndex) {
            syncedTanks[tankIndex] = state.Tanks[tankIndex];
        }
        registerSyncedData();
        addPlayerInventory(inventory);
    }

    private HeatExchangerMenu(
            HeatExchangerSizeVariants size,
            int containerId,
            Inventory inventory,
            BlockPos masterPos) {
        super(clientCtx(ICHEME_Contents.menuType(size), containerId));
        this.serverState = null;
        this.serverContext = null;
        this.serverLevel = null;
        this.masterPos = masterPos.immutable();
        this.interactionPos = masterPos.immutable();
        this.expectedFormedBlock = null;

        for (int tankIndex = 0; tankIndex < TANK_COUNT; ++tankIndex) {
            syncedTanks[tankIndex] = new FluidTank(syncedCapacity);
        }
        registerSyncedData();
        addPlayerInventory(inventory);
    }

    public static HeatExchangerMenu fromNetwork(
            HeatExchangerSizeVariants size,
            int containerId,
            Inventory inventory,
            FriendlyByteBuf extraData) {
        BlockPos masterPos = extraData.readBlockPos();
        return new HeatExchangerMenu(size, containerId, inventory, masterPos);
    }

    private void registerSyncedData() {
        addGenericData(
                GenericContainerData.int32(
                        () ->
                                serverState == null
                                        ? syncedCapacity
                                        : serverState.getTankCapacity(HeatExchangerState.INPUT_A),
                        value -> {
                            syncedCapacity = Math.max(1, value);
                            for (FluidTank tank : syncedTanks) {
                                tank.setCapacity(syncedCapacity);
                            }
                        }));

        for (FluidTank tank : syncedTanks) {
            addGenericData(GenericContainerData.fluid(tank));
        }

        for (int tankIndex = 0; tankIndex < TANK_COUNT; ++tankIndex) {
            int syncedIndex = tankIndex;
            addGenericData(
                    GenericContainerData.bool(
                            () ->
                                    serverState == null
                                            ? syncedTypeLocks[syncedIndex]
                                            : serverState.isTankTypeLocked(syncedIndex),
                            value -> syncedTypeLocks[syncedIndex] = value));
        }

        addGenericData(
                GenericContainerData.int32(
                        () ->
                                serverState == null
                                        ? syncedProgress
                                        : serverState.getExchangeProgress(),
                        value -> syncedProgress = Math.max(0, value)));
        addGenericData(
                GenericContainerData.int32(
                        () ->
                                serverState == null
                                        ? syncedDuration
                                        : serverState.getExchangeDuration(),
                        value -> syncedDuration = Math.max(1, value)));
        addGenericData(
                GenericContainerData.bool(
                        () -> serverState == null ? syncedProcessing : serverState.isProcessing(),
                        value -> syncedProcessing = value));
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 126 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 184));
        }
    }

    public FluidStack getTankFluid(int tankIndex) {
        if (!isTankIndex(tankIndex)) {
            return FluidStack.EMPTY;
        }
        FluidStack fluid = syncedTanks[tankIndex].getFluid();
        return fluid.isEmpty() ? FluidStack.EMPTY : fluid.copy();
    }

    public int getTankCapacity(int tankIndex) {
        return isTankIndex(tankIndex) ? Math.max(1, syncedTanks[tankIndex].getCapacity()) : 0;
    }

    public boolean isTankTypeLocked(int tankIndex) {
        if (!isTankIndex(tankIndex)) {
            return false;
        }
        return serverState == null
                ? syncedTypeLocks[tankIndex]
                : serverState.isTankTypeLocked(tankIndex);
    }

    public int getExchangeProgress() {
        return serverState == null ? syncedProgress : serverState.getExchangeProgress();
    }

    public int getExchangeDuration() {
        return serverState == null ? syncedDuration : serverState.getExchangeDuration();
    }

    public boolean isProcessing() {
        return serverState == null ? syncedProcessing : serverState.isProcessing();
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public boolean stillValid(Player player) {
        if (serverState == null
                || serverContext == null
                || serverLevel == null
                || expectedFormedBlock == null) {
            return true;
        }
        return player.isAlive()
                && !player.isSpectator()
                && player.level() == serverLevel
                && serverContext.isValid().getAsBoolean()
                && serverLevel.getBlockState(masterPos).is(expectedFormedBlock)
                && player.distanceToSqr(
                                interactionPos.getX() + 0.5D,
                                interactionPos.getY() + 0.5D,
                                interactionPos.getZ() + 0.5D)
                        <= MAX_DISTANCE_SQUARED;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player instanceof ServerPlayer)
                || serverState == null
                || serverContext == null
                || !stillValid(player)) {
            return false;
        }

        Optional<HeatExchangerMenuAction.Command> decoded =
                HeatExchangerMenuAction.decodeButton(buttonId);
        if (decoded.isEmpty()) {
            return false;
        }

        HeatExchangerMenuAction.Command command = decoded.get();
        boolean changed =
                switch (command.action()) {
                    case CLEAR_TANK -> serverState.clearTank(command.tankIndex());
                    case LOCK_FILTER -> serverState.setTankTypeLocked(command.tankIndex(), true);
                    case UNLOCK_FILTER -> serverState.setTankTypeLocked(command.tankIndex(), false);
                    case CLEAR_SIDE -> serverState.clearTankSide(command.tankIndex());
                    case CLEAR_ALL -> serverState.clearAllTanks();
                };
        if (changed) {
            serverContext.markDirtyAndSync();
        }
        return changed;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack current = slot.getItem();
        ItemStack original = current.copy();
        boolean moved;
        if (slotIndex < PLAYER_INVENTORY_END) {
            moved = moveItemStackTo(current, HOTBAR_START, HOTBAR_END, false);
        } else {
            moved = moveItemStackTo(current, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (current.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private static boolean isTankIndex(int tankIndex) {
        return tankIndex >= 0 && tankIndex < TANK_COUNT;
    }
}
