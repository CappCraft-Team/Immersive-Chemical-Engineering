package team.cappcraft.icheme.common.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.cappcraft.icheme.ImmersiveChemicalEngineering;
import team.cappcraft.icheme.common.tileentity.TileHeatExchanger;
import team.cappcraft.icheme.common.util.GenericBlock;

import javax.annotation.Nullable;

public class BlockHeatExchanger extends GenericBlock implements ITileEntityProvider {

    public static final ResourceLocation HEAT_EXCHANGER = new ResourceLocation(ImmersiveChemicalEngineering.MODID, "heatexchanger");
    public static final int GUI_ID = 1;

    public BlockHeatExchanger() {
        super(Material.IRON);
        setRegistryName(HEAT_EXCHANGER);// icheme:heatexchanger
        setUnlocalizedName(ImmersiveChemicalEngineering.MODID + ".heatexchanger");// icheme.smallheatexchanger
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(ImmersiveChemicalEngineering.creativeTab);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileHeatExchanger)) return false;
        player.openGui(ImmersiveChemicalEngineering.instance, GUI_ID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileHeatExchanger();
    }
}
