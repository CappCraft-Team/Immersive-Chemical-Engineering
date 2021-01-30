package team.cappcraft.icheme;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {
    //@GameRegistry.ObjectHolder("heatexchanger:smallheatexchanger")
    //public static BlockSmallHeatExchanger blockSmallHeatExchanger;

    @SideOnly(Side.CLIENT)
    public static void initModels(){
        //blockSmallHeatExchanger.initModel();
    }
}
