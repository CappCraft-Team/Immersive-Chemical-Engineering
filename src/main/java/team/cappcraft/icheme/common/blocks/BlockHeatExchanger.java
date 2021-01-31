package team.cappcraft.icheme.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState blockState) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }
}
