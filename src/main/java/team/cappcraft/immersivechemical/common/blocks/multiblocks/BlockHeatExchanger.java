package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.blocks.BlockIEMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.proxy.CommonProxy;
import team.cappcraft.immersivechemical.common.tileentity.TileHeatExchangerLarge;
import team.cappcraft.immersivechemical.common.tileentity.TileHeatExchangerMedium;
import team.cappcraft.immersivechemical.common.tileentity.TileHeatExchangerSmall;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHeatExchanger extends BlockIEMultiblock<BlockTypes_HeatExchanger> {
    public BlockHeatExchanger() {
        //TODO:check additionalProperties
        super("multiblock_heat_exchanger",
                Material.IRON,
                PropertyEnum.create("type", BlockTypes_HeatExchanger.class),
                ItemBlockIEBase.class);
        setHardness(3.0F);
        setResistance(15.0F);
        setAllNotNormalBlock();
        setCreativeTab(ImmersiveChemicalEngineering.creativeTab);
        //TODO:check meta setting
        lightOpacity = 0;
    }

    @Nonnull
    @Override
    public String createRegistryName() {
        return ImmersiveChemicalEngineering.MODID + ":" + name;
    }

    @Override
    public boolean onBlockActivated(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IEBlockInterfaces.IGuiTile && hand == EnumHand.MAIN_HAND && !player.isSneaking()) {
            TileEntity master = ((IEBlockInterfaces.IGuiTile) tile).getGuiMaster();
            if (!world.isRemote && master != null && ((IEBlockInterfaces.IGuiTile) master).canOpenGui(player))
                CommonProxy.openGuiForTile(player, (TileEntity & IEBlockInterfaces.IGuiTile) master);
            return true;
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @Nullable
    @Override
    public TileEntity createBasicTE(@Nonnull World worldIn, @Nonnull BlockTypes_HeatExchanger type) {
        switch (type) {
            case HEAT_EXCHANGER_SMALL:
                return new TileHeatExchangerSmall();
            case HEAT_EXCHANGER_MEDIUM:
                return new TileHeatExchangerMedium();
            case HEAT_EXCHANGER_LARGE:
                return new TileHeatExchangerLarge();
        }
        return null;
    }

    @Nonnull
    @Override
    public EnumPushReaction getMobilityFlag(@Nonnull IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess world, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return super.getBlockFaceShape(world, state, pos, side);
    }

    @Override
    public boolean allowHammerHarvest(@Nonnull IBlockState blockState) {
        return true;
    }
}
