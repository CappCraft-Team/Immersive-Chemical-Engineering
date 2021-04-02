import net.minecraft.init.Bootstrap;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import team.cappcraft.immersivechemical.common.recipe.ConvertDirection;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerEntry;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRecipe;
import team.cappcraft.immersivechemical.common.recipe.HeatExchangerRegistry;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TestHeatExchangerRegistry {

    private static final LinkedList<Fluid> fluids = new LinkedList<>();

    @BeforeAll
    public static void setupRegistry() {
        Bootstrap.register();
        for (int i = 0; i <= 100; i++) {
            final Fluid fluid = new Fluid("water|" + i, null, null).setTemperature(i);
            fluids.add(fluid);
            FluidRegistry.registerFluid(fluid);
        }
        HeatExchangerEntry[] entries = new HeatExchangerEntry[]{
                new HeatExchangerEntry(
                        new FluidStack(fluids.get(0), 100),
                        new FluidStack(fluids.get(10), 100),
                        100),
                new HeatExchangerEntry(
                        new FluidStack(fluids.get(10), 100),
                        new FluidStack(fluids.get(20), 100),
                        100),
                new HeatExchangerEntry(
                        new FluidStack(fluids.get(20), 100),
                        new FluidStack(fluids.get(30), 100),
                        100),
                new HeatExchangerEntry(
                        new FluidStack(fluids.get(30), 100),
                        new FluidStack(fluids.get(40), 100),
                        100),
                new HeatExchangerEntry(
                        new FluidStack(fluids.get(40), 100),
                        new FluidStack(fluids.get(50), 100),
                        100),
                new HeatExchangerEntry(
                        new FluidStack(fluids.get(50), 100),
                        new FluidStack(fluids.get(60), 100),
                        100),
                new HeatExchangerEntry(
                        new FluidStack(fluids.get(60), 100),
                        new FluidStack(fluids.get(70), 100),
                        100)
        };
        Arrays.stream(entries).forEachOrdered(HeatExchangerRegistry.REGISTRY::registerFluid);
    }

    @Test
    public void testFindEntry() {
        final Graph<Fluid, WeakReference<HeatExchangerEntry>> coolDown
                = HeatExchangerRegistry.REGISTRY.getFluidGraph(ConvertDirection.COOL_DOWN);
        //Cool 10 -> 0
        assert HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(10), fluids.get(0), coolDown).isPresent();
        //Cool 20 -> 10
        assert HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(20), fluids.get(10), coolDown).isPresent();
        //Cool 10 -> 10, not exist
        assert !HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(10), fluids.get(10), coolDown).isPresent();
        //Cool 10 -> 20, not exist
        assert !HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(10), fluids.get(20), coolDown).isPresent();
        //Cool 10 -> 9, not exist
        assert !HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(10), fluids.get(9), coolDown).isPresent();

        final Graph<Fluid, WeakReference<HeatExchangerEntry>> heatUp
                = HeatExchangerRegistry.REGISTRY.getFluidGraph(ConvertDirection.HEAT_UP);
        //Heat 0 -> 10
        assert HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(0), fluids.get(10), heatUp).isPresent();
        //Heat 10 -> 20
        assert HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(10), fluids.get(20), heatUp).isPresent();
        //Heat 10 -> 10, not exist
        assert !HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(10), fluids.get(10), heatUp).isPresent();
        //Heat 10 -> 11, not exist
        assert !HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(10), fluids.get(11), heatUp).isPresent();
        //Heat 20 -> 10, not exist
        assert !HeatExchangerRegistry.REGISTRY.findEntry(fluids.get(20), fluids.get(10), heatUp).isPresent();
    }

    @Test
    public void testGenerateEntry() {
        {
            //Heat 0 -> 10
            assert HeatExchangerRegistry.REGISTRY
                    .findOrGenerateEntry(fluids.get(0), fluids.get(10), ConvertDirection.HEAT_UP).isPresent();
            //Heat 0 -> 10 -> 20
            assert HeatExchangerRegistry.REGISTRY
                    .findOrGenerateEntry(fluids.get(0), fluids.get(20), ConvertDirection.HEAT_UP).isPresent();
            //Heat 0 -> 10 -> ... -> 70
            final Optional<HeatExchangerEntry> Zero2Seventy = HeatExchangerRegistry.REGISTRY
                    .findOrGenerateEntry(fluids.get(0), fluids.get(70), ConvertDirection.HEAT_UP);
            assert Zero2Seventy.isPresent();
            assert Zero2Seventy.get().FluidCold.getFluid() == fluids.get(0);
            assert Zero2Seventy.get().FluidHot.getFluid() == fluids.get(70);
            assert Zero2Seventy.get().HeatValue == 100 * 7;
            assert Zero2Seventy.get().Direction == ConvertDirection.HEAT_UP;
            //Cool 10 -> 0
            assert HeatExchangerRegistry.REGISTRY
                    .findOrGenerateEntry(fluids.get(10), fluids.get(0), ConvertDirection.COOL_DOWN).isPresent();
            //Cool 20 -> 10 -> 0
            assert HeatExchangerRegistry.REGISTRY
                    .findOrGenerateEntry(fluids.get(20), fluids.get(0), ConvertDirection.COOL_DOWN).isPresent();
            //Cool 70 -> ... -> 0
            final Optional<HeatExchangerEntry> Seventy2Zero = HeatExchangerRegistry.REGISTRY
                    .findOrGenerateEntry(fluids.get(70), fluids.get(0), ConvertDirection.COOL_DOWN);
            assert Seventy2Zero.isPresent();
            assert Seventy2Zero.get().FluidCold.getFluid() == fluids.get(0);
            assert Seventy2Zero.get().FluidHot.getFluid() == fluids.get(70);
            assert Seventy2Zero.get().HeatValue == 100 * 7;
            assert Seventy2Zero.get().Direction == ConvertDirection.COOL_DOWN;

            assert HeatExchangerRegistry.REGISTRY.cleanAndRefEntry().size() == 4;
        }
        System.gc();
        final List<HeatExchangerEntry> refEntry = HeatExchangerRegistry.REGISTRY.cleanAndRefEntry();
        assert refEntry.size() == 2;//Two entry assigned to final wont be clean if not exit method
    }

    @Test
    public void testFindFarthest() {
        //0 -> 70
        final Optional<Fluid> Zero2Seventy = HeatExchangerRegistry.REGISTRY
                .findFarthest(fluids.get(0), ConvertDirection.HEAT_UP, 70);
        assert Zero2Seventy.isPresent();
        assert Zero2Seventy.get() == fluids.get(70);
        //0 -> 60
        final Optional<Fluid> Zero2Sixty = HeatExchangerRegistry.REGISTRY
                .findFarthest(fluids.get(0), ConvertDirection.HEAT_UP, 60);
        assert Zero2Sixty.isPresent();
        assert Zero2Sixty.get() == fluids.get(60);
        //20 -> 50
        final Optional<Fluid> Twenty2Fifty = HeatExchangerRegistry.REGISTRY
                .findFarthest(fluids.get(20), ConvertDirection.HEAT_UP, 55);//Smaller than 55
        assert Twenty2Fifty.isPresent();
        assert Twenty2Fifty.get() == fluids.get(50);
        //70 -> 100, not exist
        final Optional<Fluid> OverHeat = HeatExchangerRegistry.REGISTRY
                .findFarthest(fluids.get(70), ConvertDirection.HEAT_UP, 100);//Smaller than 100
        assert !OverHeat.isPresent();
        //70 -> 0
        final Optional<Fluid> Seventy2Zero = HeatExchangerRegistry.REGISTRY
                .findFarthest(fluids.get(70), ConvertDirection.COOL_DOWN, 0);
        assert Seventy2Zero.isPresent();
        assert Seventy2Zero.get() == fluids.get(0);
        //70 -> 20
        final Optional<Fluid> Seventy2Twenty = HeatExchangerRegistry.REGISTRY
                .findFarthest(fluids.get(70), ConvertDirection.COOL_DOWN, 20);
        assert Seventy2Twenty.isPresent();
        assert Seventy2Twenty.get() == fluids.get(20);
        //40 -> 10
        final Optional<Fluid> Forty2Ten = HeatExchangerRegistry.REGISTRY
                .findFarthest(fluids.get(40), ConvertDirection.COOL_DOWN, 9);//Larger than 9
        assert Forty2Ten.isPresent();
        assert Forty2Ten.get() == fluids.get(10);
        //0 -> -100
        final Optional<Fluid> OverCool = HeatExchangerRegistry.REGISTRY
                .findFarthest(fluids.get(0), ConvertDirection.COOL_DOWN, -100);//Larger than -100
        assert !OverCool.isPresent();
    }

    @Test
    public void testGetRecipe() {
        assert HeatExchangerRegistry.REGISTRY
                .getHeatExchangerRecipe(
                        fluids.get(0), fluids.get(30),
                        fluids.get(10), fluids.get(20), ConvertDirection.HEAT_UP, ConvertDirection.COOL_DOWN)
                .isPresent();
        assert HeatExchangerRegistry.REGISTRY
                .getHeatExchangerRecipe(
                        fluids.get(0), fluids.get(30),
                        fluids.get(30), fluids.get(20), ConvertDirection.HEAT_UP, ConvertDirection.COOL_DOWN)
                .isPresent();
        assert HeatExchangerRegistry.REGISTRY
                .getHeatExchangerRecipe(
                        fluids.get(0), fluids.get(40),
                        fluids.get(10), fluids.get(10), ConvertDirection.HEAT_UP, ConvertDirection.COOL_DOWN)
                .isPresent();
        assert HeatExchangerRegistry.REGISTRY
                .getHeatExchangerRecipe(
                        fluids.get(0), fluids.get(70),
                        fluids.get(30), fluids.get(40), ConvertDirection.HEAT_UP, ConvertDirection.COOL_DOWN)
                .isPresent();
        assert HeatExchangerRegistry.REGISTRY
                .getHeatExchangerRecipe(
                        fluids.get(70), fluids.get(10),
                        fluids.get(40), fluids.get(40), ConvertDirection.COOL_DOWN, ConvertDirection.HEAT_UP)
                .isPresent();
    }

    @Test
    public void testFindRecipe() {
        //All Out null
        final FluidTank[] Tanks_1 = new FluidTank[]{
                new FluidTank(new FluidStack(fluids.get(0), 3000), 5000),
                new FluidTank(5000),
                new FluidTank(new FluidStack(fluids.get(70), 3000), 5000),
                new FluidTank(5000),
        };
        findAndAssert(Tanks_1, true);
        //OA null
        final FluidTank[] Tanks_2 = new FluidTank[]{
                new FluidTank(new FluidStack(fluids.get(0), 3000), 5000),
                new FluidTank(5000),
                new FluidTank(new FluidStack(fluids.get(70), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(40), 200), 5000),
        };
        findAndAssert(Tanks_2, true);
        //OB null
        final FluidTank[] Tanks_3 = new FluidTank[]{
                new FluidTank(new FluidStack(fluids.get(0), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(40), 200), 5000),
                new FluidTank(new FluidStack(fluids.get(70), 3000), 5000),
                new FluidTank(5000),
        };
        findAndAssert(Tanks_3, true);
        //non null
        final FluidTank[] Tanks_4 = new FluidTank[]{
                new FluidTank(new FluidStack(fluids.get(0), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(30), 200), 5000),
                new FluidTank(new FluidStack(fluids.get(70), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(40), 200), 5000),
        };
        findAndAssert(Tanks_4, true);
        //IA == IB
        final FluidTank[] Tanks_5 = new FluidTank[]{
                new FluidTank(new FluidStack(fluids.get(70), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(30), 200), 5000),
                new FluidTank(new FluidStack(fluids.get(70), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(40), 200), 5000),
        };
        findAndAssert(Tanks_5, false);
        //Two Heat up
        final FluidTank[] Tanks_6 = new FluidTank[]{
                new FluidTank(new FluidStack(fluids.get(0), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(30), 200), 5000),
                new FluidTank(new FluidStack(fluids.get(40), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(70), 200), 5000),
        };
        findAndAssert(Tanks_6, false);
        //Two Cool down
        final FluidTank[] Tanks_7 = new FluidTank[]{
                new FluidTank(new FluidStack(fluids.get(30), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(0), 200), 5000),
                new FluidTank(new FluidStack(fluids.get(70), 3000), 5000),
                new FluidTank(new FluidStack(fluids.get(40), 200), 5000),
        };
        findAndAssert(Tanks_7, false);
    }

    private void findAndAssert(FluidTank[] Tanks, boolean isPresent) {
        final Optional<HeatExchangerRecipe> recipe = HeatExchangerRegistry.REGISTRY.findRecipe(
                Tanks[0],
                Tanks[2],
                Tanks[1],
                Tanks[3]
        );
        assert recipe.isPresent() == isPresent;
    }
}
