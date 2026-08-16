package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.network.NetworkHooks;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerMenu;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRegistry;
import team.cappcraft.immersivechemical.common.tileentity.LockableFluidTank;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

public final class HeatExchangerLogic
        implements IMultiblockLogic<HeatExchangerState>,
                IServerTickableComponent<HeatExchangerState> {

    private static final int RETRY_DELAY_TICKS = 20;
    private static final int AUTOMATIC_OUTPUT_RATE = 1000;

    private final HeatExchangerSizeVariants size;
    private final BlockPos masterPosition;
    private final CapabilityPosition inputA;
    private final CapabilityPosition outputA;
    private final CapabilityPosition inputB;
    private final CapabilityPosition outputB;
    private final boolean exposeUnsidedPorts;

    public HeatExchangerLogic(HeatExchangerSizeVariants size) {
        this.size = size;
        this.masterPosition = size.getMasterPos().immutable();
        this.inputA = size.getInputA();
        this.outputA = size.getOutputA();
        this.inputB = size.getInputB();
        this.outputB = size.getOutputB();
        this.exposeUnsidedPorts = size.isExposeUnsidedPorts();
    }

    @Override
    public HeatExchangerState createInitialState(
            IInitialMultiblockContext<HeatExchangerState> context) {
        return new HeatExchangerState(
                size.getCapacity(),
                context,
                context.getCapabilityAt(
                        ForgeCapabilities.FLUID_HANDLER, getAdjacentTargetFace(outputA)),
                context.getCapabilityAt(
                        ForgeCapabilities.FLUID_HANDLER, getAdjacentTargetFace(outputB)));
    }

    static MultiblockFace getAdjacentTargetFace(CapabilityPosition outputPort) {
        RelativeBlockFace outwardFace = outputPort.side();
        return new MultiblockFace(
                outwardFace.getOpposite(),
                outwardFace.offsetRelative(outputPort.posInMultiblock(), 1));
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType type) {
        return ignored -> Shapes.block();
    }

    @Override
    public InteractionResult click(
            IMultiblockContext<HeatExchangerState> context,
            BlockPos posInMultiblock,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            boolean isClient) {
        if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown() || player.isSpectator()) {
            return InteractionResult.PASS;
        }
        if (isClient) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !context.isValid().getAsBoolean()) {
            return InteractionResult.PASS;
        }

        openMenu(
                context,
                serverPlayer,
                context.getLevel().toAbsolute(masterPosition),
                context.getLevel().toAbsolute(posInMultiblock));
        return InteractionResult.CONSUME;
    }

    private void openMenu(
            IMultiblockContext<HeatExchangerState> context,
            ServerPlayer player,
            BlockPos masterPos,
            BlockPos interactionPos) {
        HeatExchangerState state = context.getState();
        SimpleMenuProvider provider =
                new SimpleMenuProvider(
                        (containerId, inventory, ignoredPlayer) ->
                                new HeatExchangerMenu(
                                        size,
                                        containerId,
                                        inventory,
                                        state,
                                        context,
                                        masterPos,
                                        interactionPos),
                        Component.translatable(size.getContainerKey()));
        NetworkHooks.openScreen(player, provider, masterPos);
    }

    @Override
    public void tickServer(IMultiblockContext<HeatExchangerState> context) {
        HeatExchangerState state = context.getState();
        pushOutputFluids(state);

        if (state.cooldown > 0) {
            --state.cooldown;
            context.markMasterDirty();
            return;
        }

        if (state.recipeDirty) {
            state.recipeDirty = false;
            state.cachedRecipe =
                    HeatExchangerRegistry.REGISTRY
                            .findRecipe(
                                    state.Tanks[HeatExchangerState.INPUT_A],
                                    state.Tanks[HeatExchangerState.INPUT_B],
                                    state.Tanks[HeatExchangerState.OUTPUT_A],
                                    state.Tanks[HeatExchangerState.OUTPUT_B])
                            .orElse(null);
        }

        HeatExchangerRecipe recipe = state.cachedRecipe;
        if (recipe == null) {
            state.cooldown = RETRY_DELAY_TICKS;
            setProcessing(context, state, false);
            context.markMasterDirty();
            return;
        }
        int rate = size.getHeatTransferRate();

        boolean sideAAbsorbs = recipe.DirectionA == ConvertDirection.HEAT_UP;
        HeatExchangerEntry absorbingEntry = sideAAbsorbs ? recipe.ExchangeA : recipe.ExchangeB;
        HeatExchangerEntry releasingEntry = sideAAbsorbs ? recipe.ExchangeB : recipe.ExchangeA;
        ConvertDirection absorbingDirection = sideAAbsorbs ? recipe.DirectionA : recipe.DirectionB;
        ConvertDirection releasingDirection = sideAAbsorbs ? recipe.DirectionB : recipe.DirectionA;
        int absorbingInput = sideAAbsorbs ? HeatExchangerState.INPUT_A : HeatExchangerState.INPUT_B;
        int absorbingOutput =
                sideAAbsorbs ? HeatExchangerState.OUTPUT_A : HeatExchangerState.OUTPUT_B;
        int releasingInput = sideAAbsorbs ? HeatExchangerState.INPUT_B : HeatExchangerState.INPUT_A;
        int releasingOutput =
                sideAAbsorbs ? HeatExchangerState.OUTPUT_B : HeatExchangerState.OUTPUT_A;

        boolean processed = false;
        long heatCost = Math.min(rate, absorbingEntry.HeatValue);
        if (state.heatBuffer >= heatCost) {
            int heatMoved =
                    exchangeSide(
                            state.Tanks[absorbingInput],
                            state.Tanks[absorbingOutput],
                            absorbingEntry,
                            absorbingDirection,
                            rate);
            if (heatMoved > 0) {
                state.heatBuffer -= heatMoved;
                processed = true;
            }
        }

        if (!processed) {
            int heatMoved =
                    exchangeSide(
                            state.Tanks[releasingInput],
                            state.Tanks[releasingOutput],
                            releasingEntry,
                            releasingDirection,
                            rate);
            if (heatMoved > 0) {
                state.heatBuffer += heatMoved;
                processed = true;
            }
        }

        if (!processed) {
            state.cooldown = RETRY_DELAY_TICKS;
            setProcessing(context, state, false);
            context.markMasterDirty();
            return;
        }

        setProcessing(context, state, true);
        context.markDirtyAndSync();
    }

    private static void pushOutputFluids(HeatExchangerState state) {
        LockableFluidTank outputA = state.Tanks[HeatExchangerState.OUTPUT_A];
        if (!outputA.getFluid().isEmpty() && !outputA.isLocked()) {
            pushOutputFluid(state.automaticOutputA.getNullable(), outputA);
        }

        LockableFluidTank outputB = state.Tanks[HeatExchangerState.OUTPUT_B];
        if (!outputB.getFluid().isEmpty() && !outputB.isLocked()) {
            pushOutputFluid(state.automaticOutputB.getNullable(), outputB);
        }
    }

    static int pushOutputFluid(IFluidHandler target, LockableFluidTank source) {
        if (target == null || source.getFluid().isEmpty() || source.isLocked()) {
            return 0;
        }
        return FluidUtil.tryFluidTransfer(target, source, AUTOMATIC_OUTPUT_RATE, true).getAmount();
    }

    private static int exchangeSide(
            LockableFluidTank input,
            LockableFluidTank output,
            HeatExchangerEntry entry,
            ConvertDirection direction,
            int maxHeat) {
        int heatValue = entry.HeatValue;
        int heatToMove = Math.min(maxHeat, heatValue);
        if (heatToMove <= 0) {
            return 0;
        }

        FluidStack fullInput = entry.getInput(direction);
        FluidStack fullOutput = entry.getOutput(direction);
        long inputAmount = (long) fullInput.getAmount() * heatToMove / heatValue;
        long outputAmount = (long) fullOutput.getAmount() * heatToMove / heatValue;
        if (inputAmount <= 0 || outputAmount <= 0) {
            return 0;
        }

        FluidStack inputStack = new FluidStack(fullInput, (int) inputAmount);
        FluidStack outputStack = new FluidStack(fullOutput, (int) outputAmount);
        if (input.drain(inputStack, FluidAction.SIMULATE).getAmount() != inputAmount
                || output.fill(outputStack, FluidAction.SIMULATE) != outputAmount) {
            return 0;
        }

        FluidStack drained = input.drain(inputStack, FluidAction.EXECUTE);
        if (drained.getAmount() != inputAmount) {
            input.fill(drained, FluidAction.EXECUTE);
            return 0;
        }
        int filled = output.fill(outputStack, FluidAction.EXECUTE);
        if (filled != outputAmount) {
            output.drain(new FluidStack(outputStack, filled), FluidAction.EXECUTE);
            input.fill(drained, FluidAction.EXECUTE);
            return 0;
        }
        return heatToMove;
    }

    private static void setProcessing(
            IMultiblockContext<HeatExchangerState> context,
            HeatExchangerState state,
            boolean processing) {
        if (state.processing != processing) {
            state.processing = processing;
            context.markDirtyAndSync();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(
            IMultiblockContext<HeatExchangerState> context,
            CapabilityPosition position,
            Capability<T> capability) {
        if (capability != ForgeCapabilities.FLUID_HANDLER) {
            return LazyOptional.empty();
        }

        StoredCapability<IFluidHandler> selected =
                selectFluidCapability(context.getState(), position);
        return selected == null ? LazyOptional.empty() : selected.cast(context);
    }

    private StoredCapability<IFluidHandler> selectFluidCapability(
            HeatExchangerState state, CapabilityPosition position) {
        if (inputA.equals(position)) {
            return state.inputACapability;
        }
        if (outputA.equals(position)) {
            return state.outputACapability;
        }
        if (inputB.equals(position)) {
            return state.inputBCapability;
        }
        if (outputB.equals(position)) {
            return state.outputBCapability;
        }
        if (position.side() != null || !exposeUnsidedPorts) {
            return null;
        }

        BlockPos requested = position.posInMultiblock();
        StoredCapability<IFluidHandler> sideA =
                unsidedCapability(
                        state.inputACapability,
                        state.outputACapability,
                        state.portACapability,
                        inputA.posInMultiblock(),
                        outputA.posInMultiblock(),
                        requested);
        if (sideA != null) {
            return sideA;
        }
        return unsidedCapability(
                state.inputBCapability,
                state.outputBCapability,
                state.portBCapability,
                inputB.posInMultiblock(),
                outputB.posInMultiblock(),
                requested);
    }

    private static StoredCapability<IFluidHandler> unsidedCapability(
            StoredCapability<IFluidHandler> input,
            StoredCapability<IFluidHandler> output,
            StoredCapability<IFluidHandler> combined,
            BlockPos inputPosition,
            BlockPos outputPosition,
            BlockPos requested) {
        boolean isInput = inputPosition.equals(requested);
        boolean isOutput = outputPosition.equals(requested);
        if (isInput && isOutput) {
            return combined;
        }
        if (isInput) {
            return input;
        }
        return isOutput ? output : null;
    }
}
