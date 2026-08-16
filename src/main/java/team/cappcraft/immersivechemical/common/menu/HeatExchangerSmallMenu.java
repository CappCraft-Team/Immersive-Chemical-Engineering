package team.cappcraft.immersivechemical.common.menu;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerSmallState;


public final class HeatExchangerSmallMenu
        extends AbstractHeatExchangerMenu<HeatExchangerSmallState> {

    public HeatExchangerSmallMenu(
            int containerId,
            Inventory inventory,
            HeatExchangerSmallState state,
            IMultiblockContext<HeatExchangerSmallState> context,
            BlockPos masterPos,
            BlockPos interactionPos
    ) {
        super(
                multiblockCtx(
                        ICHEME_Contents.HEAT_EXCHANGER_SMALL_MENU.get(),
                        containerId,
                        new MultiblockMenuContext<>(context, masterPos)
                ),
                inventory,
                state,
                context,
                masterPos,
                interactionPos,
                ICHEME_Contents.HEAT_EXCHANGER_SMALL.block().get()
        );
    }

    public static HeatExchangerSmallMenu fromNetwork(
            int containerId,
            Inventory inventory,
            FriendlyByteBuf extraData
    ) {
        BlockPos masterPos = extraData.readBlockPos();
        return new HeatExchangerSmallMenu(containerId, inventory, masterPos);
    }

    private HeatExchangerSmallMenu(
            int containerId,
            Inventory inventory,
            BlockPos masterPos
    ) {
        super(
                clientCtx(ICHEME_Contents.HEAT_EXCHANGER_SMALL_MENU.get(), containerId),
                inventory,
                null,
                null,
                masterPos,
                masterPos,
                null
        );
    }
}
