package team.cappcraft.icheme.common.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import team.cappcraft.icheme.ImmersiveChemicalEngineering;
import team.cappcraft.icheme.ModBlocks;
import team.cappcraft.icheme.common.blocks.BlockHeatExchanger;
import team.cappcraft.icheme.common.tileentity.TileHeatExchanger;

@Mod.EventBusSubscriber
public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
    }

    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ImmersiveChemicalEngineering.instance, new GuiHandler());
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockHeatExchanger());
        GameRegistry.registerTileEntity(TileHeatExchanger.class, ImmersiveChemicalEngineering.MODID + "_heatexchanger_tile");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(ModBlocks.blockHeatExchanger).setRegistryName(BlockHeatExchanger.HEAT_EXCHANGER));
    }
}
