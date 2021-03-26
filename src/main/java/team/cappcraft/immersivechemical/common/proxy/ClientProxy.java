package team.cappcraft.immersivechemical.common.proxy;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.lib.manual.ManualPages;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.MultiBlockHeatExchangerLarge;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.MultiBlockHeatExchangerMedium;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.MultiBlockHeatExchangerSmall;
import team.cappcraft.immersivechemical.common.network.MessageTileSync;

import static team.cappcraft.immersivechemical.ImmersiveChemicalEngineering.MODID;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ICHEME_Contents.initModels();
    }

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        OBJLoader.INSTANCE.addDomain(MODID);
        IEOBJLoader.instance.addDomain(MODID);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);

        ManualHelper.addEntry("heatExchanger", "heatExchanger",
                new ManualPages.Text(ManualHelper.getManual(), "heatExchanger0"),
                new ManualPages.Crafting(ManualHelper.getManual(), "heatExchangerBlock0",
                        new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.IRON.getMeta()),
                        new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta())),
                new ManualPages.Text(ManualHelper.getManual(), "heatExchangerBlock1"),
                new ManualPageMultiblock(ManualHelper.getManual(), "heatExchangerSmall", MultiBlockHeatExchangerSmall.INSTANCE),
                new ManualPageMultiblock(ManualHelper.getManual(), "heatExchangerMedium", MultiBlockHeatExchangerMedium.INSTANCE),
                new ManualPageMultiblock(ManualHelper.getManual(), "heatExchangerLarge", MultiBlockHeatExchangerLarge.INSTANCE));
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        ImmersiveChemicalEngineering.packetHandler.registerMessage(MessageTileSync.HandlerClient.class, MessageTileSync.class, 0, Side.CLIENT);
        ImmersiveChemicalEngineering.packetHandler.registerMessage(MessageTileSync.HandlerServer.class, MessageTileSync.class, 0, Side.SERVER);
    }
}
