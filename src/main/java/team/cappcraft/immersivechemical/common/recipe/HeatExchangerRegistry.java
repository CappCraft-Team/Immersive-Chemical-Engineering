package team.cappcraft.immersivechemical.common.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import team.cappcraft.immersivechemical.common.tileentity.IHeatExchangerProperties;

import javax.annotation.Nonnull;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HeatExchangerRegistry {
    public final static HeatExchangerRegistry REGISTRY = new HeatExchangerRegistry();
    public final ExecutorService RecipeFinder = Executors.newSingleThreadExecutor();

    private final HashMap<IHeatExchangerProperties, ItemStack> registeredHeatExchanger = new HashMap<>();
    /**
     * Keep the registered entries referenced, just the generated one need to be GC
     */
    private final HashSet<HeatExchangerEntry> registeredEntries = new HashSet<>();
    private final SimpleDirectedWeightedGraph<Fluid, WeakReference<HeatExchangerEntry>> COOL_DOWN_FluidGraph =
            new SimpleDirectedWeightedGraph<>(null, null);
    private final SimpleDirectedWeightedGraph<Fluid, WeakReference<HeatExchangerEntry>> HEAT_UP_FluidGraph =
            new SimpleDirectedWeightedGraph<>(null, null);
    private final HashSet<WeakReference<HeatExchangerEntry>> generatedEntries = new HashSet<>();


    public void registerHeatExchanger(IHeatExchangerProperties properties, ItemStack renderStack) {
        registeredHeatExchanger.put(properties, renderStack);
    }

    public void registerFluid(HeatExchangerEntry entry) {
        registeredEntries.add(entry);
        insertEntryAsEdge(entry, 1, false);
    }

    public Set<HeatExchangerEntry> getRegisteredEntries() {
        return Collections.unmodifiableSet(registeredEntries);
    }

    public Map<IHeatExchangerProperties, ItemStack> getRegisteredHeatExchanger() {
        return Collections.unmodifiableMap(registeredHeatExchanger);
    }

    public Optional<HeatExchangerRecipe> findRecipe(IFluidTank inputA, IFluidTank inputB, IFluidTank outputA, IFluidTank outputB) {
        final Fluid FluidIA = inputA.getFluid() == null ? null : inputA.getFluid().getFluid();
        final Fluid FluidIB = inputB.getFluid() == null ? null : inputB.getFluid().getFluid();

        final Fluid FluidOA = outputA.getFluid() == null ? null : outputA.getFluid().getFluid();
        final Fluid FluidOB = outputB.getFluid() == null ? null : outputB.getFluid().getFluid();

        if (FluidIA == null || FluidIB == null || FluidIA.getTemperature() == FluidIB.getTemperature())
            return Optional.empty();

        if (FluidOA != null && FluidOB != null
                && (
                (FluidIA.getTemperature() > FluidIB.getTemperature() && FluidOA.getTemperature() < FluidOB.getTemperature())
                        || (FluidIA.getTemperature() < FluidIB.getTemperature() && FluidOA.getTemperature() > FluidOB.getTemperature())
        ))
            return Optional.empty();

        ConvertDirection DirectionA = FluidIA.getTemperature() > FluidIB.getTemperature() ?
                ConvertDirection.COOL_DOWN : ConvertDirection.HEAT_UP;
        ConvertDirection DirectionB = DirectionA.getOpposite();

        if (FluidOA != null && FluidOB != null) {
            return getHeatExchangerRecipe(FluidIA, FluidIB, FluidOA, FluidOB, DirectionA, DirectionB);
        }

        if (FluidOA != null) {
            final Optional<Fluid> FarthestOB = findFarthest(FluidIB, DirectionB, FluidOA.getTemperature());
            if (FarthestOB.isPresent()) {
                return getHeatExchangerRecipe(FluidIA, FluidIB, FluidOA, FarthestOB.get(), DirectionA, DirectionB);
            }
        }

        if (FluidOA == null && FluidOB != null) {
            final Optional<Fluid> FarthestOA = findFarthest(FluidIA, DirectionA, FluidOB.getTemperature());
            if (FarthestOA.isPresent())
                return getHeatExchangerRecipe(FluidIA, FluidIB, FarthestOA.get(), FluidOB, DirectionA, DirectionB);
        }

        if (FluidOA == null && FluidOB == null) {
            final Optional<Fluid> FarthestOA = findFarthest(FluidIA, DirectionA, (FluidIA.getTemperature() + FluidIB.getTemperature()) / 2);
            if (FarthestOA.isPresent()) {
                final Optional<Fluid> FarthestOB = findFarthest(FluidIB, DirectionB, FarthestOA.get().getTemperature());
                if (FarthestOB.isPresent())
                    return getHeatExchangerRecipe(FluidIA, FluidIB, FarthestOA.get(), FarthestOB.get(), DirectionA, DirectionB);
            }
        }

        return Optional.empty();
    }

    public Optional<HeatExchangerRecipe> getHeatExchangerRecipe(Fluid FluidIA, Fluid FluidIB, Fluid FluidOA, Fluid FluidOB, ConvertDirection DirectionA, ConvertDirection DirectionB) {
        final Optional<HeatExchangerEntry> EntryA = findOrGenerateEntry(FluidIA, FluidOA, DirectionA);
        final Optional<HeatExchangerEntry> EntryB = findOrGenerateEntry(FluidIB, FluidOB, DirectionB);
        if (EntryA.isPresent() && EntryB.isPresent())
            return Optional.of(new HeatExchangerRecipe(EntryA.get(), EntryB.get(), DirectionA, DirectionB));
        return Optional.empty();
    }

    /**
     * Find the farthest vertex
     *
     * @param input     start vertex
     * @param direction convert direction
     * @param tempLimit the temperature limit of the fluid, depends on direction
     *                  COOL_DOWN -> Greater or equal to limit
     *                  Heat_UP -> Smaller or equal to limit
     * @return farthest vertex
     */
    public Optional<Fluid> findFarthest(@Nonnull Fluid input, ConvertDirection direction, int tempLimit) {
        if (direction == ConvertDirection.COOL_DOWN)
            return findReachable(input, direction)
                    .parallelStream()
                    .filter(fluid -> fluid != input && fluid.getTemperature() >= tempLimit)
                    .min(Comparator.comparingInt(Fluid::getTemperature));
        else
            return findReachable(input, direction)
                    .parallelStream()
                    .filter(fluid -> fluid != input && fluid.getTemperature() <= tempLimit)
                    .max(Comparator.comparingInt(Fluid::getTemperature));
    }

    /**
     * Find reachable fluid from input
     *
     * @param input     start vertex
     * @param direction convert direction
     * @return list of reachable fluids
     */
    public LinkedList<Fluid> findReachable(@Nonnull Fluid input, ConvertDirection direction) {
        final DepthFirstIterator<Fluid, WeakReference<HeatExchangerEntry>> iter
                = new DepthFirstIterator<>(getFluidGraph(direction), input);

        LinkedList<Fluid> fluids = new LinkedList<>();
        while (iter.hasNext()) fluids.add(iter.next());
        return fluids;
    }

    /**
     * Find correspond entry for input and output, create a generated one if these two is connected
     *
     * @param input     fluid input
     * @param output    fluid output
     * @param direction convert direction
     * @return empty if input cant connect to output
     */
    public Optional<HeatExchangerEntry> findOrGenerateEntry(@Nonnull Fluid input, @Nonnull Fluid output, ConvertDirection direction) {
        Graph<Fluid, WeakReference<HeatExchangerEntry>> graph = getFluidGraph(direction);
        final Optional<HeatExchangerEntry> StrongConnected = findEntry(input, output, graph);
        if (!StrongConnected.isPresent()) {
            //noinspection unused
            List<HeatExchangerEntry> refEntry = cleanAndRefEntry();//Keep referencing entry
            //Create entry connecting input and output
            GraphPath<Fluid, WeakReference<HeatExchangerEntry>> path;
            if (graph.containsVertex(input) && graph.containsVertex(output)
                    && (path = new DijkstraShortestPath<>(graph).getPath(input, output)) != null) {
                //Sum up all the heat in the path
                final int TotalHeat = path.getEdgeList().parallelStream().mapToInt(value -> Objects.requireNonNull(value.get()).HeatValue).sum();
                final FluidStack FluidStackInput = Objects.requireNonNull(path.getEdgeList().get(0).get()).getInput(direction);
                final FluidStack FluidStackOutput = Objects.requireNonNull(path.getEdgeList().get(path.getLength() - 1).get()).getOutput(direction);

                final HeatExchangerEntry generatedEntry = new HeatExchangerEntry(FluidStackInput, FluidStackOutput, TotalHeat, direction);
                insertEntryAsEdge(generatedEntry, path.getLength(), true);

                return Optional.of(generatedEntry);
            }
        }
        return StrongConnected;
    }

    public Graph<Fluid, WeakReference<HeatExchangerEntry>> getFluidGraph(ConvertDirection direction) {
        return direction == ConvertDirection.COOL_DOWN ? COOL_DOWN_FluidGraph : HEAT_UP_FluidGraph;
    }

    /**
     * Insert HeatExchangerEntry into graph for searching
     *
     * @param entry     to insert
     * @param weight    the weight of the edge, use to consider the farthest edge
     * @param generated if the entry is generated one
     */
    public void insertEntryAsEdge(HeatExchangerEntry entry, int weight, boolean generated) {
        final Fluid Hot = entry.FluidHot.getFluid();
        final Fluid Cold = entry.FluidCold.getFluid();

        final WeakReference<HeatExchangerEntry> ref = new WeakReference<>(entry);
        if (generated) generatedEntries.add(ref);
        if (entry.Direction == ConvertDirection.COOL_DOWN || entry.Direction == ConvertDirection.TWO_WAY) {
            COOL_DOWN_FluidGraph.addVertex(Hot);
            COOL_DOWN_FluidGraph.addVertex(Cold);
            COOL_DOWN_FluidGraph.addEdge(Hot, Cold, ref);
            COOL_DOWN_FluidGraph.setEdgeWeight(ref, weight);
        }
        if (entry.Direction == ConvertDirection.HEAT_UP || entry.Direction == ConvertDirection.TWO_WAY) {
            HEAT_UP_FluidGraph.addVertex(Hot);
            HEAT_UP_FluidGraph.addVertex(Cold);
            HEAT_UP_FluidGraph.addEdge(Cold, Hot, ref);
            HEAT_UP_FluidGraph.setEdgeWeight(ref, weight);
        }
    }

    /**
     * Clean all the null generated entries, and return a list to keep reference
     *
     * @return reference list
     */
    public List<HeatExchangerEntry> cleanAndRefEntry() {
        final List<HeatExchangerEntry> result = generatedEntries
                .parallelStream()
                .map(Reference::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final Collection<WeakReference<HeatExchangerEntry>> nullRefs = generatedEntries
                .parallelStream()
                .filter(edge -> edge.get() == null)
                .collect(Collectors.toList());
        COOL_DOWN_FluidGraph.removeAllEdges(nullRefs);
        HEAT_UP_FluidGraph.removeAllEdges(nullRefs);
        generatedEntries.removeAll(nullRefs);
        return result;
    }

    /**
     * Find Strong connected edge between input and output
     *
     * @param input  maybe not exist in the graph
     * @param output maybe not exist in the graph
     * @param graph  the graph to find
     * @return empty if no such entry exist
     */
    public Optional<HeatExchangerEntry> findEntry(@Nonnull Fluid input, @Nonnull Fluid output,
                                                  Graph<Fluid, WeakReference<HeatExchangerEntry>> graph) {
        final WeakReference<HeatExchangerEntry> edge = graph.getEdge(input, output);
        if (edge == null) return Optional.empty();
        return Optional.ofNullable(edge.get());
    }
}
