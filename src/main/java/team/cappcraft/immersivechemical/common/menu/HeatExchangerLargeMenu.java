package team.cappcraft.immersivechemical.common.menu;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerLargeState;


public final class HeatExchangerLargeMenu
        extends AbstractHeatExchangerMenu<HeatExchangerLargeState> {

    public HeatExchangerLargeMenu(
            int containerId,
            Inventory inventory,
            HeatExchangerLargeState state,
            IMultiblockContext<HeatExchangerLargeState> context,
            BlockPos masterPos,
            BlockPos interactionPos
    ) {
        super(
                multiblockCtx(
                        ICHEME_Contents.HEAT_EXCHANGER_LARGE_MENU.get(),
                        containerId,
                        new MultiblockMenuContext<>(context, masterPos)
                ),
                inventory,
                state,
                context,
                masterPos,
                interactionPos,
                ICHEME_Contents.HEAT_EXCHANGER_LARGE.block().get()
        );
    }

    public static HeatExchangerLargeMenu fromNetwork(
            int containerId,
            Inventory inventory,
            FriendlyByteBuf extraData
    ) {
        BlockPos masterPos = extraData.readBlockPos();
        return new HeatExchangerLargeMenu(containerId, inventory, masterPos);
    }

    private HeatExchangerLargeMenu(
            int containerId,
            Inventory inventory,
            BlockPos masterPos
    ) {
        super(
                clientCtx(ICHEME_Contents.HEAT_EXCHANGER_LARGE_MENU.get(), containerId),
                inventory,
                null,
                null,
                masterPos,
                masterPos,
                null
        );
    }
}
