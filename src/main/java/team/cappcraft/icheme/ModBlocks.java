package team.cappcraft.icheme;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.cappcraft.icheme.common.blocks.BlockHeatExchanger;

public class ModBlocks {
    @GameRegistry.ObjectHolder("icheme:heatexchanger")
    public static BlockHeatExchanger blockHeatExchanger;

    @SideOnly(Side.CLIENT)
    public static void initModels(){
    }
}
