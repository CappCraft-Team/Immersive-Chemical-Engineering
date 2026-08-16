package team.cappcraft.immersivechemical.common.menu;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import team.cappcraft.immersivechemical.ICHEME_Contents;
import team.cappcraft.immersivechemical.common.blocks.multiblocks.HeatExchangerMediumState;


public final class HeatExchangerMediumMenu
        extends AbstractHeatExchangerMenu<HeatExchangerMediumState> {

    public HeatExchangerMediumMenu(
            int containerId,
            Inventory inventory,
            HeatExchangerMediumState state,
            IMultiblockContext<HeatExchangerMediumState> context,
            BlockPos masterPos,
            BlockPos interactionPos
    ) {
        super(
                multiblockCtx(
                        ICHEME_Contents.HEAT_EXCHANGER_MEDIUM_MENU.get(),
                        containerId,
                        new MultiblockMenuContext<>(context, masterPos)
                ),
                inventory,
                state,
                context,
                masterPos,
                interactionPos,
                ICHEME_Contents.HEAT_EXCHANGER_MEDIUM.block().get()
        );
    }

    public static HeatExchangerMediumMenu fromNetwork(
            int containerId,
            Inventory inventory,
            FriendlyByteBuf extraData
    ) {
        BlockPos masterPos = extraData.readBlockPos();
        return new HeatExchangerMediumMenu(containerId, inventory, masterPos);
    }

    private HeatExchangerMediumMenu(
            int containerId,
            Inventory inventory,
            BlockPos masterPos
    ) {
        super(
                clientCtx(ICHEME_Contents.HEAT_EXCHANGER_MEDIUM_MENU.get(), containerId),
                inventory,
                null,
                null,
                masterPos,
                masterPos,
                null
        );
    }
}
