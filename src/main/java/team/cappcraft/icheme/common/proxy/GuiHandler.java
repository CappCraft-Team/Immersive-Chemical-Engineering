package team.cappcraft.icheme.common.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import team.cappcraft.icheme.client.gui.GuiHeatExchanger;
import team.cappcraft.icheme.common.container.ContainerHeatExchanger;
import team.cappcraft.icheme.common.tileentity.TileHeatExchanger;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHeatExchanger) {
            return new ContainerHeatExchanger(player.inventory, (TileHeatExchanger) te);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileHeatExchanger) {
            TileHeatExchanger containerTileEntity = (TileHeatExchanger) te;
            return new GuiHeatExchanger(containerTileEntity, player.inventory);
        }
        return null;
    }
}
