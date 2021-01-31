package team.cappcraft.icheme.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import team.cappcraft.icheme.ImmersiveChemicalEngineering;
import team.cappcraft.icheme.common.util.GenericBlock;

public class BlockHeatExchanger extends GenericBlock {

    public static final ResourceLocation HEAT_EXCHANGER= new ResourceLocation(ImmersiveChemicalEngineering.MODID, "heatexchanger");

    public BlockHeatExchanger(){
        super(Material.IRON);
        setRegistryName(HEAT_EXCHANGER);// icheme:heatexchanger
        setUnlocalizedName(ImmersiveChemicalEngineering.MODID+ ".heatexchanger");// icheme.smallheatexchanger
        setHarvestLevel("pickaxe",1);
        setCreativeTab(ImmersiveChemicalEngineering.creativeTab);
    }
}
