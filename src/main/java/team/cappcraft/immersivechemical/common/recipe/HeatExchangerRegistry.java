package team.cappcraft.immersivechemical.common.recipe;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Registry and deterministic path finder for heat exchanger fluid conversions.
 */
public class HeatExchangerRegistry {
    public static final HeatExchangerRegistry REGISTRY = new HeatExchangerRegistry();

    private static final BigInteger BIG_INTEGER_MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

    private final Set<HeatExchangerEntry> registeredEntries = new LinkedHashSet<>();
    private final Set<HeatExchangerEntry> generatedEntries = new LinkedHashSet<>();
    private final DirectedConversionGraph<Fluid, HeatExchangerEntry> coolDownFluidGraph =
            new DirectedConversionGraph<>();
    private final DirectedConversionGraph<Fluid, HeatExchangerEntry> heatUpFluidGraph =
            new DirectedConversionGraph<>();

    public synchronized void registerFluid(HeatExchangerEntry entry) {
        Objects.requireNonNull(entry, "entry");
        HeatExchangerEntry snapshot = copyEntry(entry);
        insertEntryAsEdge(snapshot, 1, false);
        registeredEntries.add(snapshot);
    }

    /**
     * Atomically replaces all datapack-provided conversions.
     *
     * <p>Every entry is validated and copied before the live graphs are cleared,
     * so a malformed reload cannot leave a partially rebuilt registry.</p>
     */
    public synchronized void replaceRegisteredEntries(Collection<HeatExchangerEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        List<HeatExchangerEntry> snapshot = new ArrayList<>(entries.size());
        for (HeatExchangerEntry entry : entries) {
            snapshot.add(copyEntry(Objects.requireNonNull(entry, "entry")));
        }

        registeredEntries.clear();
        generatedEntries.clear();
        coolDownFluidGraph.clear();
        heatUpFluidGraph.clear();
        for (HeatExchangerEntry entry : snapshot) {
            insertEntryAsEdge(entry, 1, false);
            registeredEntries.add(entry);
        }
    }

    public synchronized Set<HeatExchangerEntry> getRegisteredEntries() {
        Set<HeatExchangerEntry> copy = new LinkedHashSet<>();
        registeredEntries.forEach(entry -> copy.add(copyEntry(entry)));
        return Collections.unmodifiableSet(copy);
    }

    public synchronized Optional<HeatExchangerRecipe> findRecipe(
            IFluidTank inputA, IFluidTank inputB, IFluidTank outputA, IFluidTank outputB
    ) {
        Fluid fluidIA = getPresentFluid(inputA);
        Fluid fluidIB = getPresentFluid(inputB);
        Fluid fluidOA = getPresentFluid(outputA);
        Fluid fluidOB = getPresentFluid(outputB);

        if (fluidIA == null || fluidIB == null || temperature(fluidIA) == temperature(fluidIB)) {
            return Optional.empty();
        }

        if (fluidOA != null && fluidOB != null
                && ((temperature(fluidIA) > temperature(fluidIB) && temperature(fluidOA) < temperature(fluidOB))
                || (temperature(fluidIA) < temperature(fluidIB) && temperature(fluidOA) > temperature(fluidOB)))) {
            return Optional.empty();
        }

        ConvertDirection directionA = temperature(fluidIA) > temperature(fluidIB)
                ? ConvertDirection.COOL_DOWN
                : ConvertDirection.HEAT_UP;
        ConvertDirection directionB = directionA.getOpposite();

        if (!getFluidGraph(directionA).containsVertex(fluidIA)
                || !getFluidGraph(directionB).containsVertex(fluidIB)) {
            return Optional.empty();
        }

        if (fluidOA != null && fluidOB != null) {
            return getHeatExchangerRecipe(fluidIA, fluidIB, fluidOA, fluidOB, directionA, directionB);
        }

        if (fluidOA != null) {
            return findFarthest(fluidIB, directionB, temperature(fluidOA))
                    .flatMap(farthestOB -> getHeatExchangerRecipe(
                            fluidIA, fluidIB, fluidOA, farthestOB, directionA, directionB));
        }

        if (fluidOB != null) {
            return findFarthest(fluidIA, directionA, temperature(fluidOB))
                    .flatMap(farthestOA -> getHeatExchangerRecipe(
                            fluidIA, fluidIB, farthestOA, fluidOB, directionA, directionB));
        }

        Optional<Fluid> nextOA = findClosestReachable(fluidIA, directionA);
        if (nextOA.isEmpty()) {
            return Optional.empty();
        }
        return findFarthest(fluidIB, directionB, temperature(nextOA.get()))
                .flatMap(farthestOB -> getHeatExchangerRecipe(
                        fluidIA, fluidIB, nextOA.get(), farthestOB, directionA, directionB));
    }

    public synchronized Optional<HeatExchangerRecipe> getHeatExchangerRecipe(
            Fluid fluidIA, Fluid fluidIB, Fluid fluidOA, Fluid fluidOB,
            ConvertDirection directionA, ConvertDirection directionB
    ) {
        Optional<HeatExchangerEntry> entryA = findOrGenerateEntry(fluidIA, fluidOA, directionA);
        Optional<HeatExchangerEntry> entryB = findOrGenerateEntry(fluidIB, fluidOB, directionB);
        if (entryA.isEmpty() || entryB.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new HeatExchangerRecipe(entryA.get(), entryB.get(), directionA, directionB));
    }

    /**
     * Finds the reachable fluid farthest from the input without crossing the
     * temperature available from the other side of the exchanger.
     */
    public synchronized Optional<Fluid> findFarthest(
            @Nonnull Fluid input, ConvertDirection direction, int tempLimit
    ) {
        Objects.requireNonNull(input, "input");
        List<Fluid> reachable = findReachable(input, direction);
        Comparator<Fluid> temperatureComparator = Comparator.comparingInt(HeatExchangerRegistry::temperature);

        if (direction == ConvertDirection.COOL_DOWN) {
            return reachable.stream()
                    .filter(fluid -> temperature(fluid) >= tempLimit)
                    .min(temperatureComparator);
        }
        if (direction == ConvertDirection.HEAT_UP) {
            return reachable.stream()
                    .filter(fluid -> temperature(fluid) <= tempLimit)
                    .max(temperatureComparator);
        }
        throw new IllegalArgumentException("A concrete conversion direction is required");
    }

    /**
     * Returns all fluids reachable from {@code input} in deterministic depth-first order.
     */
    public synchronized List<Fluid> findReachable(@Nonnull Fluid input, ConvertDirection direction) {
        Objects.requireNonNull(input, "input");
        return getFluidGraph(direction).reachableDepthFirst(input);
    }

    /**
     * Finds a direct entry or creates a cached aggregate entry for the cheapest path.
     */
    public synchronized Optional<HeatExchangerEntry> findOrGenerateEntry(
            @Nonnull Fluid input, @Nonnull Fluid output, ConvertDirection direction
    ) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        DirectedConversionGraph<Fluid, HeatExchangerEntry> graph = getFluidGraph(direction);

        if (input == output) {
            return Optional.empty();
        }

        Optional<HeatExchangerEntry> direct = graph.getEdgeValue(input, output);
        if (direct.isPresent()) {
            return direct;
        }

        Optional<DirectedConversionGraph.Path<HeatExchangerEntry>> pathResult = graph.shortestPath(input, output);
        if (pathResult.isEmpty() || pathResult.get().edges().isEmpty()) {
            return Optional.empty();
        }

        DirectedConversionGraph.Path<HeatExchangerEntry> path = pathResult.get();
        List<HeatExchangerEntry> pathEntries = path.edges();
        List<ConversionStep> conversionSteps = new ArrayList<>(pathEntries.size());
        for (HeatExchangerEntry entry : pathEntries) {
            FluidStack inputStack = entry.getInput(direction);
            FluidStack outputStack = entry.getOutput(direction);
            conversionSteps.add(new ConversionStep(
                    inputStack.getAmount(),
                    outputStack.getAmount(),
                    entry.HeatValue
            ));
        }

        Optional<ComposedConversion> composedResult = composeConversionAmounts(conversionSteps);
        if (composedResult.isEmpty()) {
            return Optional.empty();
        }
        ComposedConversion composed = composedResult.get();

        FluidStack firstInput = pathEntries.get(0).getInput(direction);
        FluidStack finalOutput = pathEntries.get(pathEntries.size() - 1).getOutput(direction);
        HeatExchangerEntry generatedEntry = new HeatExchangerEntry(
                new FluidStack(firstInput, composed.inputAmount()),
                new FluidStack(finalOutput, composed.outputAmount()),
                composed.heat(),
                direction
        );

        long totalWeight = path.totalWeight();
        if (totalWeight <= 0 || totalWeight > Integer.MAX_VALUE) {
            return Optional.empty();
        }
        insertEntryAsEdge(generatedEntry, (int) totalWeight, true);
        return Optional.of(generatedEntry);
    }

    /**
     * Composes a path of integer fluid conversions into the smallest whole-number transaction.
     *
     * <p>For every adjacent pair, the repetitions are scaled until the first conversion's
     * intermediate output exactly equals the second conversion's intermediate input. Relative
     * repetition counts are kept as reduced fractions, then integerized with the least common
     * multiple of their denominators. This preserves both fluid and heat across unequal recipe
     * ratios without accumulating rounding error.</p>
     *
     * <p>The runtime recipe representation uses signed 32-bit amounts and heat. A path whose
     * minimal exact transaction cannot fit that representation is rejected instead of wrapping
     * or caching a lossy shortcut.</p>
     */
    static Optional<ComposedConversion> composeConversionAmounts(List<ConversionStep> steps) {
        Objects.requireNonNull(steps, "steps");
        if (steps.isEmpty()) {
            return Optional.empty();
        }

        List<Fraction> relativeRepetitions = new ArrayList<>(steps.size());
        BigInteger numerator = BigInteger.ONE;
        BigInteger denominator = BigInteger.ONE;
        BigInteger commonDenominator = BigInteger.ONE;
        relativeRepetitions.add(new Fraction(numerator, denominator));

        for (int index = 1; index < steps.size(); ++index) {
            ConversionStep previous = Objects.requireNonNull(steps.get(index - 1), "step");
            ConversionStep current = Objects.requireNonNull(steps.get(index), "step");

            numerator = numerator.multiply(BigInteger.valueOf(previous.outputAmount()));
            denominator = denominator.multiply(BigInteger.valueOf(current.inputAmount()));
            BigInteger commonFactor = numerator.gcd(denominator);
            numerator = numerator.divide(commonFactor);
            denominator = denominator.divide(commonFactor);

            if (exceedsInt(numerator) || exceedsInt(denominator)) {
                return Optional.empty();
            }

            commonDenominator = leastCommonMultiple(commonDenominator, denominator);
            if (exceedsInt(commonDenominator)) {
                return Optional.empty();
            }
            relativeRepetitions.add(new Fraction(numerator, denominator));
        }

        List<BigInteger> repetitions = new ArrayList<>(steps.size());
        for (Fraction relative : relativeRepetitions) {
            BigInteger repetitionsForStep = relative.numerator()
                    .multiply(commonDenominator.divide(relative.denominator()));
            if (exceedsInt(repetitionsForStep)) {
                return Optional.empty();
            }
            repetitions.add(repetitionsForStep);
        }

        BigInteger inputAmount = BigInteger.valueOf(steps.get(0).inputAmount())
                .multiply(repetitions.get(0));
        BigInteger outputAmount = BigInteger.valueOf(steps.get(steps.size() - 1).outputAmount())
                .multiply(repetitions.get(repetitions.size() - 1));
        BigInteger totalHeat = BigInteger.ZERO;
        for (int index = 0; index < steps.size(); ++index) {
            totalHeat = totalHeat.add(
                    BigInteger.valueOf(steps.get(index).heat()).multiply(repetitions.get(index))
            );
            if (exceedsInt(totalHeat)) {
                return Optional.empty();
            }
        }

        if (exceedsInt(inputAmount) || exceedsInt(outputAmount)) {
            return Optional.empty();
        }
        return Optional.of(new ComposedConversion(
                inputAmount.intValueExact(),
                outputAmount.intValueExact(),
                totalHeat.intValueExact()
        ));
    }

    private static BigInteger leastCommonMultiple(BigInteger first, BigInteger second) {
        return first.divide(first.gcd(second)).multiply(second);
    }

    private static boolean exceedsInt(BigInteger value) {
        return value.signum() <= 0 || value.compareTo(BIG_INTEGER_MAX_INT) > 0;
    }

    static record ConversionStep(int inputAmount, int outputAmount, int heat) {
        ConversionStep {
            if (inputAmount <= 0 || outputAmount <= 0 || heat <= 0) {
                throw new IllegalArgumentException("Conversion amounts and heat must be positive");
            }
        }
    }

    static record ComposedConversion(int inputAmount, int outputAmount, int heat) {
    }

    private record Fraction(BigInteger numerator, BigInteger denominator) {
    }

    /**
     * Inserts an entry into the graph(s) allowed by its declared direction.
     */
    public synchronized void insertEntryAsEdge(HeatExchangerEntry entry, int weight, boolean generated) {
        Objects.requireNonNull(entry, "entry");
        if (weight <= 0) {
            throw new IllegalArgumentException("Conversion weight must be positive");
        }

        Fluid hot = entry.FluidHot.getFluid();
        Fluid cold = entry.FluidCold.getFluid();
        boolean inserted = false;

        if (entry.Direction == ConvertDirection.COOL_DOWN || entry.Direction == ConvertDirection.TWO_WAY) {
            inserted |= coolDownFluidGraph.addEdge(hot, cold, entry, weight);
        }
        if (entry.Direction == ConvertDirection.HEAT_UP || entry.Direction == ConvertDirection.TWO_WAY) {
            inserted |= heatUpFluidGraph.addEdge(cold, hot, entry, weight);
        }

        if (generated && inserted) {
            generatedEntries.add(entry);
        }
    }

    /**
     * Retained as a compatibility accessor. Generated entries are now held strongly,
     * so there are no weak references to clean.
     */
    public synchronized List<HeatExchangerEntry> cleanAndRefEntry() {
        List<HeatExchangerEntry> copy = new ArrayList<>(generatedEntries.size());
        generatedEntries.forEach(entry -> copy.add(copyEntry(entry)));
        return copy;
    }

    private Optional<Fluid> findClosestReachable(Fluid input, ConvertDirection direction) {
        Comparator<Fluid> temperatureComparator = Comparator.comparingInt(HeatExchangerRegistry::temperature);
        if (direction == ConvertDirection.COOL_DOWN) {
            return findReachable(input, direction).stream().max(temperatureComparator);
        }
        if (direction == ConvertDirection.HEAT_UP) {
            return findReachable(input, direction).stream().min(temperatureComparator);
        }
        throw new IllegalArgumentException("A concrete conversion direction is required");
    }

    private DirectedConversionGraph<Fluid, HeatExchangerEntry> getFluidGraph(ConvertDirection direction) {
        Objects.requireNonNull(direction, "direction");
        if (direction == ConvertDirection.COOL_DOWN) {
            return coolDownFluidGraph;
        }
        if (direction == ConvertDirection.HEAT_UP) {
            return heatUpFluidGraph;
        }
        throw new IllegalArgumentException("A concrete conversion direction is required");
    }

    private static Fluid getPresentFluid(IFluidTank tank) {
        Objects.requireNonNull(tank, "tank");
        FluidStack stack = tank.getFluid();
        return stack == null || stack.isEmpty() ? null : stack.getFluid();
    }

    private static int temperature(Fluid fluid) {
        return fluid.getFluidType().getTemperature();
    }

    private static HeatExchangerEntry copyEntry(HeatExchangerEntry entry) {
        return new HeatExchangerEntry(
                entry.FluidHot, entry.FluidCold, entry.HeatValue, entry.Direction);
    }
}
