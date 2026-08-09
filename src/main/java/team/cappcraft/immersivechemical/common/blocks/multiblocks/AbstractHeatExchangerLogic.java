package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRegistry;
import team.cappcraft.immersivechemical.common.tileentity.LockableFluidTank;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Shared tick, transaction, interaction, and capability behavior for all exchanger sizes. */
public abstract class AbstractHeatExchangerLogic<S extends HeatExchangerState> implements
        IMultiblockLogic<S>,
        IServerTickableComponent<S> {

    private static final int RETRY_DELAY_TICKS = 20;
    static final int AUTOMATIC_OUTPUT_RATE = 1000;

    private final HeatExchangerSizeVariants size;
    private final BlockPos masterPosition;
    private final CapabilityPosition inputA;
    private final CapabilityPosition outputA;
    private final CapabilityPosition inputB;
    private final CapabilityPosition outputB;
    private final boolean exposeUnsidedPorts;

    protected AbstractHeatExchangerLogic(
            HeatExchangerSizeVariants size,
            BlockPos masterPosition,
            CapabilityPosition inputA,
            CapabilityPosition outputA,
            CapabilityPosition inputB,
            CapabilityPosition outputB,
            boolean exposeUnsidedPorts
    ) {
        this.size = size;
        this.masterPosition = masterPosition.immutable();
        this.inputA = inputA;
        this.outputA = outputA;
        this.inputB = inputB;
        this.outputB = outputB;
        this.exposeUnsidedPorts = exposeUnsidedPorts;
    }

    @Override
    public final S createInitialState(IInitialMultiblockContext<S> context) {
        return createState(
                size.getCapacity(),
                context,
                context.getCapabilityAt(
                        ForgeCapabilities.FLUID_HANDLER,
                        getAdjacentTargetFace(outputA)
                ),
                context.getCapabilityAt(
                        ForgeCapabilities.FLUID_HANDLER,
                        getAdjacentTargetFace(outputB)
                )
        );
    }

    protected abstract S createState(
            int tankCapacity,
            IInitialMultiblockContext<S> context,
            CapabilityReference<IFluidHandler> automaticOutputA,
            CapabilityReference<IFluidHandler> automaticOutputB
    );

    /**
     * Converts a capability exposed on a formed part into the face of the adjacent block.
     * IE's {@link CapabilityPosition#opposing(MultiblockFace)} is the inverse operation.
     */
    static MultiblockFace getAdjacentTargetFace(CapabilityPosition outputPort) {
        var outwardFace = Objects.requireNonNull(outputPort.side(), "Output port face");
        return new MultiblockFace(
                outwardFace.getOpposite(),
                outwardFace.offsetRelative(outputPort.posInMultiblock(), 1)
        );
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType type) {
        return ignored -> Shapes.block();
    }

    @Override
    public InteractionResult click(
            IMultiblockContext<S> context,
            BlockPos posInMultiblock,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            boolean isClient
    ) {
        if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown() || player.isSpectator()) {
            return InteractionResult.PASS;
        }
        if (isClient) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)
                || !context.isValid().getAsBoolean()) {
            return InteractionResult.PASS;
        }

        openMenu(
                context,
                serverPlayer,
                context.getLevel().toAbsolute(masterPosition),
                context.getLevel().toAbsolute(posInMultiblock)
        );
        return InteractionResult.CONSUME;
    }

    protected abstract void openMenu(
            IMultiblockContext<S> context,
            ServerPlayer player,
            BlockPos masterPos,
            BlockPos interactionPos
    );

    @Override
    public void tickServer(IMultiblockContext<S> context) {
        S state = context.getState();
        pushOutputFluids(state);

        if (state.cooldown > 0) {
            --state.cooldown;
            context.markMasterDirty();
            return;
        }

        Optional<HeatExchangerRecipe> foundRecipe = HeatExchangerRegistry.REGISTRY.findRecipe(
                state.Tanks[HeatExchangerState.INPUT_A],
                state.Tanks[HeatExchangerState.INPUT_B],
                state.Tanks[HeatExchangerState.OUTPUT_A],
                state.Tanks[HeatExchangerState.OUTPUT_B]
        );
        if (foundRecipe.isEmpty()) {
            state.cooldown = RETRY_DELAY_TICKS;
            state.tickSinceLastExchange = 0;
            setProcessing(context, state, false);
            context.markMasterDirty();
            return;
        }

        if (++state.tickSinceLastExchange < Math.max(1, state.tickRequired)) {
            setProcessing(context, state, true);
            context.markMasterDirty();
            return;
        }

        state.tickSinceLastExchange = 0;
        int transferredHeat = exchangeAtomically(state, foundRecipe.get());
        if (transferredHeat <= 0) {
            state.cooldown = RETRY_DELAY_TICKS;
            setProcessing(context, state, false);
            context.markMasterDirty();
            return;
        }

        state.tickRequired = Math.max(1, (int)Math.ceil(
                transferredHeat * size.getTickMultiplier()
        ));
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

    /**
     * Pushes at most one bucket into an adjacent handler using Forge's simulate-first transfer.
     * The source tank is drained only after the target reports that it can accept the fluid.
     */
    static int pushOutputFluid(IFluidHandler target, LockableFluidTank source) {
        if (target == null || source.getFluid().isEmpty() || source.isLocked()) {
            return 0;
        }
        return FluidUtil.tryFluidTransfer(target, source, AUTOMATIC_OUTPUT_RATE, true).getAmount();
    }

    /**
     * Converts both sides as one energy-balanced transaction. When recipe sides use different
     * heat quanta, each side is scaled by the other side's value divided by their GCD.
     */
    private int exchangeAtomically(S state, HeatExchangerRecipe recipe) {
        int heatGcd = greatestCommonDivisor(recipe.ExchangeA.HeatValue, recipe.ExchangeB.HeatValue);
        int repetitionsA = recipe.ExchangeB.HeatValue / heatGcd;
        int repetitionsB = recipe.ExchangeA.HeatValue / heatGcd;

        ExchangeBatch batchA = createBatch(recipe.ExchangeA, recipe.DirectionA, repetitionsA);
        ExchangeBatch batchB = createBatch(recipe.ExchangeB, recipe.DirectionB, repetitionsB);
        if (batchA == null || batchB == null || batchA.heat() != batchB.heat()) {
            return 0;
        }

        LockableFluidTank inputTankA = state.Tanks[HeatExchangerState.INPUT_A];
        LockableFluidTank outputTankA = state.Tanks[HeatExchangerState.OUTPUT_A];
        LockableFluidTank inputTankB = state.Tanks[HeatExchangerState.INPUT_B];
        LockableFluidTank outputTankB = state.Tanks[HeatExchangerState.OUTPUT_B];
        if (!canExchange(batchA, inputTankA, outputTankA)
                || !canExchange(batchB, inputTankB, outputTankB)) {
            return 0;
        }

        FluidStack drainedA = inputTankA.drain(batchA.input(), FluidAction.EXECUTE);
        if (!sameFluidAndAmount(drainedA, batchA.input())) {
            rollbackInputs(inputTankA, drainedA, inputTankB, FluidStack.EMPTY);
            logTransactionFailure("first input drain");
            return 0;
        }

        FluidStack drainedB = inputTankB.drain(batchB.input(), FluidAction.EXECUTE);
        if (!sameFluidAndAmount(drainedB, batchB.input())) {
            rollbackInputs(inputTankA, drainedA, inputTankB, drainedB);
            logTransactionFailure("second input drain");
            return 0;
        }

        int filledA = outputTankA.fill(batchA.output(), FluidAction.EXECUTE);
        if (filledA != batchA.output().getAmount()) {
            rollbackTransaction(
                    inputTankA, drainedA, outputTankA, batchA.output(), filledA,
                    inputTankB, drainedB, outputTankB, batchB.output(), 0
            );
            logTransactionFailure("first output fill");
            return 0;
        }

        int filledB = outputTankB.fill(batchB.output(), FluidAction.EXECUTE);
        if (filledB != batchB.output().getAmount()) {
            rollbackTransaction(
                    inputTankA, drainedA, outputTankA, batchA.output(), filledA,
                    inputTankB, drainedB, outputTankB, batchB.output(), filledB
            );
            logTransactionFailure("second output fill");
            return 0;
        }
        return batchA.heat();
    }

    private static ExchangeBatch createBatch(
            HeatExchangerEntry entry,
            ConvertDirection direction,
            int repetitions
    ) {
        FluidStack input = scale(entry.getInput(direction), repetitions);
        FluidStack output = scale(entry.getOutput(direction), repetitions);
        long heat = (long)entry.HeatValue * repetitions;
        if (input.isEmpty() || output.isEmpty() || heat > Integer.MAX_VALUE) {
            return null;
        }
        return new ExchangeBatch(input, output, (int)heat);
    }

    private static FluidStack scale(FluidStack stack, int repetitions) {
        long amount = (long)stack.getAmount() * repetitions;
        if (stack.isEmpty() || repetitions <= 0 || amount > Integer.MAX_VALUE) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(stack, (int)amount);
    }

    private static boolean canExchange(
            ExchangeBatch batch,
            LockableFluidTank input,
            LockableFluidTank output
    ) {
        FluidStack simulatedDrain = input.drain(batch.input(), FluidAction.SIMULATE);
        return sameFluidAndAmount(simulatedDrain, batch.input())
                && output.fill(batch.output(), FluidAction.SIMULATE) == batch.output().getAmount();
    }

    private static void rollbackTransaction(
            LockableFluidTank inputA,
            FluidStack drainedA,
            LockableFluidTank outputA,
            FluidStack producedA,
            int filledA,
            LockableFluidTank inputB,
            FluidStack drainedB,
            LockableFluidTank outputB,
            FluidStack producedB,
            int filledB
    ) {
        rollbackOutput(outputB, producedB, filledB);
        rollbackOutput(outputA, producedA, filledA);
        rollbackInputs(inputA, drainedA, inputB, drainedB);
    }

    private static void rollbackOutput(
            LockableFluidTank output,
            FluidStack produced,
            int filled
    ) {
        if (filled <= 0) {
            return;
        }
        FluidStack removed = output.drain(new FluidStack(produced, filled), FluidAction.EXECUTE);
        if (removed.getAmount() != filled) {
            ImmersiveChemicalEngineering.LOGGER.error(
                    "Heat exchanger rollback removed {}/{} mB from an output tank",
                    removed.getAmount(),
                    filled
            );
        }
    }

    private static void rollbackInputs(
            LockableFluidTank inputA,
            FluidStack drainedA,
            LockableFluidTank inputB,
            FluidStack drainedB
    ) {
        restoreInput(inputB, drainedB);
        restoreInput(inputA, drainedA);
    }

    private static void restoreInput(LockableFluidTank input, FluidStack drained) {
        if (drained.isEmpty()) {
            return;
        }
        int restored = input.fill(drained, FluidAction.EXECUTE);
        if (restored != drained.getAmount()) {
            ImmersiveChemicalEngineering.LOGGER.error(
                    "Heat exchanger rollback restored {}/{} mB to an input tank",
                    restored,
                    drained.getAmount()
            );
        }
    }

    private void logTransactionFailure(String stage) {
        ImmersiveChemicalEngineering.LOGGER.error(
                "{} heat exchanger changed during atomic exchange at {}; rolled back",
                size.name().toLowerCase(),
                stage
        );
    }

    private static int greatestCommonDivisor(int a, int b) {
        while (b != 0) {
            int remainder = a % b;
            a = b;
            b = remainder;
        }
        return a;
    }

    private record ExchangeBatch(FluidStack input, FluidStack output, int heat) {
    }

    private static boolean sameFluidAndAmount(FluidStack actual, FluidStack expected) {
        return !actual.isEmpty()
                && actual.getAmount() == expected.getAmount()
                && actual.isFluidEqual(expected);
    }

    private static <S extends HeatExchangerState> void setProcessing(
            IMultiblockContext<S> context,
            S state,
            boolean processing
    ) {
        if (state.processing != processing) {
            state.processing = processing;
            context.markDirtyAndSync();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(
            IMultiblockContext<S> context,
            CapabilityPosition position,
            Capability<T> capability
    ) {
        if (capability != ForgeCapabilities.FLUID_HANDLER) {
            return LazyOptional.empty();
        }

        StoredCapability<IFluidHandler> selected = selectFluidCapability(
                context.getState(),
                position
        );
        return selected == null ? LazyOptional.empty() : selected.cast(context);
    }

    private StoredCapability<IFluidHandler> selectFluidCapability(
            S state,
            CapabilityPosition position
    ) {
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
        StoredCapability<IFluidHandler> sideA = unsidedCapability(
                state.inputACapability,
                state.outputACapability,
                state.portACapability,
                inputA.posInMultiblock(),
                outputA.posInMultiblock(),
                requested
        );
        if (sideA != null) {
            return sideA;
        }
        return unsidedCapability(
                state.inputBCapability,
                state.outputBCapability,
                state.portBCapability,
                inputB.posInMultiblock(),
                outputB.posInMultiblock(),
                requested
        );
    }

    private static StoredCapability<IFluidHandler> unsidedCapability(
            StoredCapability<IFluidHandler> input,
            StoredCapability<IFluidHandler> output,
            StoredCapability<IFluidHandler> combined,
            BlockPos inputPosition,
            BlockPos outputPosition,
            BlockPos requested
    ) {
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
