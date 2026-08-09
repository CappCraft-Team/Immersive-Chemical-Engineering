package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkHooks;
import team.cappcraft.immersivechemical.common.menu.HeatExchangerMediumMenu;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;

/** Legacy medium port layout layered on the shared exchanger transaction logic. */
public final class HeatExchangerMediumLogic
        extends AbstractHeatExchangerLogic<HeatExchangerMediumState> {

    static final CapabilityPosition OUTPUT_A = new CapabilityPosition(
            new BlockPos(0, 0, 1), RelativeBlockFace.DOWN);
    static final CapabilityPosition OUTPUT_B = new CapabilityPosition(
            new BlockPos(1, 1, 1), RelativeBlockFace.UP);

    public HeatExchangerMediumLogic() {
        super(
                HeatExchangerSizeVariants.MEDIUM,
                HeatExchangerMediumMultiblock.MASTER_FROM_ORIGIN,
                new CapabilityPosition(new BlockPos(0, 1, 0), RelativeBlockFace.UP),
                OUTPUT_A,
                new CapabilityPosition(new BlockPos(1, 0, 0), RelativeBlockFace.DOWN),
                OUTPUT_B,
                false
        );
    }

    @Override
    protected HeatExchangerMediumState createState(
            int tankCapacity,
            IInitialMultiblockContext<HeatExchangerMediumState> context,
            CapabilityReference<IFluidHandler> automaticOutputA,
            CapabilityReference<IFluidHandler> automaticOutputB
    ) {
        return new HeatExchangerMediumState(
                tankCapacity,
                context,
                automaticOutputA,
                automaticOutputB
        );
    }

    @Override
    protected void openMenu(
            IMultiblockContext<HeatExchangerMediumState> context,
            ServerPlayer player,
            BlockPos masterPos,
            BlockPos interactionPos
    ) {
        HeatExchangerMediumState state = context.getState();
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, ignoredPlayer) -> new HeatExchangerMediumMenu(
                        containerId,
                        inventory,
                        state,
                        context,
                        masterPos,
                        interactionPos
                ),
                Component.translatable("container.immersivechemical.heat_exchanger_medium")
        );
        NetworkHooks.openScreen(player, provider, masterPos);
    }
}
