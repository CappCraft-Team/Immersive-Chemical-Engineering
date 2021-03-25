package team.cappcraft.immersivechemical.common.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import team.cappcraft.immersivechemical.client.gui.GuiHeatExchanger;
import team.cappcraft.immersivechemical.common.container.ContainerHeatExchanger;
import team.cappcraft.immersivechemical.common.tileentity.AbstractTileHeatExchanger;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof AbstractTileHeatExchanger) {
            return new ContainerHeatExchanger(player.inventory, (AbstractTileHeatExchanger) te);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof AbstractTileHeatExchanger) {
            AbstractTileHeatExchanger containerTileEntity = (AbstractTileHeatExchanger) te;
            return new GuiHeatExchanger(containerTileEntity, player.inventory);
        }
        return null;
    }
}
