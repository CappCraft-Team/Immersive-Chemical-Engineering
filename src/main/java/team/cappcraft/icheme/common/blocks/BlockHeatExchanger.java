package team.cappcraft.icheme.common.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import team.cappcraft.icheme.ImmersiveChemicalEngineering;
import team.cappcraft.icheme.common.util.GenericBlock;

import javax.annotation.Nullable;

public class BlockHeatExchanger extends GenericBlock implements ITileEntityProvider {

    public static final ResourceLocation HEAT_EXCHANGER = new ResourceLocation(ImmersiveChemicalEngineering.MODID, "heatexchanger");

    public BlockHeatExchanger() {
        super(Material.IRON);
        setRegistryName(HEAT_EXCHANGER);// icheme:heatexchanger
        setUnlocalizedName(ImmersiveChemicalEngineering.MODID + ".heatexchanger");// icheme.smallheatexchanger
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(ImmersiveChemicalEngineering.creativeTab);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }
}
