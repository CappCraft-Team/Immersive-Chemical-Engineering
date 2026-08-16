package team.cappcraft.immersivechemical.common.recipe;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class HeatExchangerRegistry {
    public static final HeatExchangerRegistry REGISTRY = new HeatExchangerRegistry();

    private static final BigInteger BIG_INTEGER_MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

    private final Set<HeatExchangerEntry> registeredEntries = new LinkedHashSet<>();
    private final Set<HeatExchangerEntry> generatedEntries = new LinkedHashSet<>();
    private final DirectedConversionGraph<Fluid, HeatExchangerEntry> coolDownFluidGraph =
            new DirectedConversionGraph<>();
    private final DirectedConversionGraph<Fluid, HeatExchangerEntry> heatUpFluidGraph =
            new DirectedConversionGraph<>();

    public synchronized void replaceRegisteredEntries(Collection<HeatExchangerEntry> entries) {
        List<HeatExchangerEntry> snapshot = new ArrayList<>(entries.size());
        for (HeatExchangerEntry entry : entries) {
            snapshot.add(copyEntry(entry));
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

    public synchronized Optional<HeatExchangerRecipe> findRecipe(
            IFluidTank inputA, IFluidTank inputB, IFluidTank outputA, IFluidTank outputB) {
        Fluid fluidIA = getPresentFluid(inputA);
        Fluid fluidIB = getPresentFluid(inputB);
        Fluid fluidOA = getPresentFluid(outputA);
        Fluid fluidOB = getPresentFluid(outputB);

        if (fluidIA == null || fluidIB == null || temperature(fluidIA) == temperature(fluidIB)) {
            return Optional.empty();
        }

        if (fluidOA != null
                && fluidOB != null
                && ((temperature(fluidIA) > temperature(fluidIB)
                                && temperature(fluidOA) < temperature(fluidOB))
                        || (temperature(fluidIA) < temperature(fluidIB)
                                && temperature(fluidOA) > temperature(fluidOB)))) {
            return Optional.empty();
        }

        ConvertDirection directionA =
                temperature(fluidIA) > temperature(fluidIB)
                        ? ConvertDirection.COOL_DOWN
                        : ConvertDirection.HEAT_UP;
        ConvertDirection directionB = directionA.getOpposite();

        if (!getFluidGraph(directionA).containsVertex(fluidIA)
                || !getFluidGraph(directionB).containsVertex(fluidIB)) {
            return Optional.empty();
        }

        if (fluidOA != null && fluidOB != null) {
            return getHeatExchangerRecipe(
                    fluidIA, fluidIB, fluidOA, fluidOB, directionA, directionB);
        }

        if (fluidOA != null) {
            return findFarthest(fluidIB, directionB, temperature(fluidOA))
                    .flatMap(
                            farthestOB ->
                                    getHeatExchangerRecipe(
                                            fluidIA,
                                            fluidIB,
                                            fluidOA,
                                            farthestOB,
                                            directionA,
                                            directionB));
        }

        if (fluidOB != null) {
            return findFarthest(fluidIA, directionA, temperature(fluidOB))
                    .flatMap(
                            farthestOA ->
                                    getHeatExchangerRecipe(
                                            fluidIA,
                                            fluidIB,
                                            farthestOA,
                                            fluidOB,
                                            directionA,
                                            directionB));
        }

        Optional<Fluid> nextOA = findClosestReachable(fluidIA, directionA);
        if (nextOA.isEmpty()) {
            return Optional.empty();
        }
        return findFarthest(fluidIB, directionB, temperature(nextOA.get()))
                .flatMap(
                        farthestOB ->
                                getHeatExchangerRecipe(
                                        fluidIA,
                                        fluidIB,
                                        nextOA.get(),
                                        farthestOB,
                                        directionA,
                                        directionB));
    }

    public synchronized Optional<HeatExchangerRecipe> getHeatExchangerRecipe(
            Fluid fluidIA,
            Fluid fluidIB,
            Fluid fluidOA,
            Fluid fluidOB,
            ConvertDirection directionA,
            ConvertDirection directionB) {
        Optional<HeatExchangerEntry> entryA = findOrGenerateEntry(fluidIA, fluidOA, directionA);
        Optional<HeatExchangerEntry> entryB = findOrGenerateEntry(fluidIB, fluidOB, directionB);
        if (entryA.isEmpty() || entryB.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                new HeatExchangerRecipe(entryA.get(), entryB.get(), directionA, directionB));
    }

    public synchronized Optional<Fluid> findFarthest(
            Fluid input, ConvertDirection direction, int tempLimit) {
        List<Fluid> reachable = findReachable(input, direction);
        return switch (direction) {
            case COOL_DOWN ->
                    reachable.stream()
                            .filter(fluid -> temperature(fluid) >= tempLimit)
                            .min(Comparator.comparingInt(HeatExchangerRegistry::temperature));
            case HEAT_UP ->
                    reachable.stream()
                            .filter(fluid -> temperature(fluid) <= tempLimit)
                            .max(Comparator.comparingInt(HeatExchangerRegistry::temperature));
            case TWO_WAY ->
                    throw new IllegalArgumentException(
                            "A concrete conversion direction is required");
        };
    }

    public synchronized List<Fluid> findReachable(Fluid input, ConvertDirection direction) {
        return getFluidGraph(direction).reachableDepthFirst(input);
    }

    public synchronized Optional<HeatExchangerEntry> findOrGenerateEntry(
            Fluid input, Fluid output, ConvertDirection direction) {
        DirectedConversionGraph<Fluid, HeatExchangerEntry> graph = getFluidGraph(direction);

        if (input == output) {
            return Optional.empty();
        }

        Optional<HeatExchangerEntry> direct = graph.getEdgeValue(input, output);
        if (direct.isPresent()) {
            return direct;
        }

        Optional<DirectedConversionGraph.Path<HeatExchangerEntry>> pathResult =
                graph.shortestPath(input, output);
        if (pathResult.isEmpty() || pathResult.get().edges().isEmpty()) {
            return Optional.empty();
        }

        DirectedConversionGraph.Path<HeatExchangerEntry> path = pathResult.get();
        List<HeatExchangerEntry> pathEntries = path.edges();
        List<ConversionStep> conversionSteps = new ArrayList<>(pathEntries.size());
        for (HeatExchangerEntry entry : pathEntries) {
            FluidStack inputStack = entry.getInput(direction);
            FluidStack outputStack = entry.getOutput(direction);
            conversionSteps.add(
                    new ConversionStep(
                            inputStack.getAmount(), outputStack.getAmount(), entry.HeatValue));
        }

        Optional<ComposedConversion> composedResult = composeConversionAmounts(conversionSteps);
        if (composedResult.isEmpty()) {
            return Optional.empty();
        }
        ComposedConversion composed = composedResult.get();

        FluidStack firstInput = pathEntries.get(0).getInput(direction);
        FluidStack finalOutput = pathEntries.get(pathEntries.size() - 1).getOutput(direction);
        HeatExchangerEntry generatedEntry =
                new HeatExchangerEntry(
                        new FluidStack(firstInput, composed.inputAmount()),
                        new FluidStack(finalOutput, composed.outputAmount()),
                        composed.heat(),
                        direction);

        long totalWeight = path.totalWeight();
        if (totalWeight <= 0 || totalWeight > Integer.MAX_VALUE) {
            return Optional.empty();
        }
        insertEntryAsEdge(generatedEntry, (int) totalWeight, true);
        return Optional.of(generatedEntry);
    }

    static Optional<ComposedConversion> composeConversionAmounts(List<ConversionStep> steps) {
        if (steps.isEmpty()) {
            return Optional.empty();
        }

        List<Fraction> relativeRepetitions = new ArrayList<>(steps.size());
        BigInteger numerator = BigInteger.ONE;
        BigInteger denominator = BigInteger.ONE;
        BigInteger commonDenominator = BigInteger.ONE;
        relativeRepetitions.add(new Fraction(numerator, denominator));

        for (int index = 1; index < steps.size(); ++index) {
            ConversionStep previous = steps.get(index - 1);
            ConversionStep current = steps.get(index);

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
            BigInteger repetitionsForStep =
                    relative.numerator().multiply(commonDenominator.divide(relative.denominator()));
            if (exceedsInt(repetitionsForStep)) {
                return Optional.empty();
            }
            repetitions.add(repetitionsForStep);
        }

        BigInteger inputAmount =
                BigInteger.valueOf(steps.get(0).inputAmount()).multiply(repetitions.get(0));
        BigInteger outputAmount =
                BigInteger.valueOf(steps.get(steps.size() - 1).outputAmount())
                        .multiply(repetitions.get(repetitions.size() - 1));
        BigInteger totalHeat = BigInteger.ZERO;
        for (int index = 0; index < steps.size(); ++index) {
            totalHeat =
                    totalHeat.add(
                            BigInteger.valueOf(steps.get(index).heat())
                                    .multiply(repetitions.get(index)));
            if (exceedsInt(totalHeat)) {
                return Optional.empty();
            }
        }

        if (exceedsInt(inputAmount) || exceedsInt(outputAmount)) {
            return Optional.empty();
        }
        return Optional.of(
                new ComposedConversion(
                        inputAmount.intValueExact(),
                        outputAmount.intValueExact(),
                        totalHeat.intValueExact()));
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

    static record ComposedConversion(int inputAmount, int outputAmount, int heat) {}

    private record Fraction(BigInteger numerator, BigInteger denominator) {}

    public synchronized void insertEntryAsEdge(
            HeatExchangerEntry entry, int weight, boolean generated) {
        Fluid hot = entry.FluidHot.getFluid();
        Fluid cold = entry.FluidCold.getFluid();
        boolean inserted = false;

        if (entry.Direction == ConvertDirection.COOL_DOWN
                || entry.Direction == ConvertDirection.TWO_WAY) {
            inserted |= coolDownFluidGraph.addEdge(hot, cold, entry, weight);
        }
        if (entry.Direction == ConvertDirection.HEAT_UP
                || entry.Direction == ConvertDirection.TWO_WAY) {
            inserted |= heatUpFluidGraph.addEdge(cold, hot, entry, weight);
        }

        if (generated && inserted) {
            generatedEntries.add(entry);
        }
    }

    private Optional<Fluid> findClosestReachable(Fluid input, ConvertDirection direction) {
        return switch (direction) {
            case COOL_DOWN ->
                    findReachable(input, direction).stream()
                            .max(Comparator.comparingInt(HeatExchangerRegistry::temperature));
            case HEAT_UP ->
                    findReachable(input, direction).stream()
                            .min(Comparator.comparingInt(HeatExchangerRegistry::temperature));
            case TWO_WAY ->
                    throw new IllegalArgumentException(
                            "A concrete conversion direction is required");
        };
    }

    private DirectedConversionGraph<Fluid, HeatExchangerEntry> getFluidGraph(
            ConvertDirection direction) {
        return switch (direction) {
            case COOL_DOWN -> coolDownFluidGraph;
            case HEAT_UP -> heatUpFluidGraph;
            case TWO_WAY ->
                    throw new IllegalArgumentException(
                            "A concrete conversion direction is required");
        };
    }

    private static Fluid getPresentFluid(IFluidTank tank) {
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
