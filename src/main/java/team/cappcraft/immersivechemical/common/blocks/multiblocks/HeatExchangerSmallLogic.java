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
import team.cappcraft.immersivechemical.common.menu.HeatExchangerSmallMenu;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;


public final class HeatExchangerSmallLogic
        extends AbstractHeatExchangerLogic<HeatExchangerSmallState> {

    private static final BlockPos PORT_A = new BlockPos(0, 0, 0);
    private static final BlockPos PORT_B = new BlockPos(0, 0, 3);
    static final CapabilityPosition OUTPUT_A =
            new CapabilityPosition(PORT_A, RelativeBlockFace.DOWN);
    static final CapabilityPosition OUTPUT_B =
            new CapabilityPosition(PORT_B, RelativeBlockFace.DOWN);

    public HeatExchangerSmallLogic() {
        super(
                HeatExchangerSizeVariants.SMALL,
                HeatExchangerSmallMultiblock.MASTER_FROM_ORIGIN,
                new CapabilityPosition(PORT_A, RelativeBlockFace.UP),
                OUTPUT_A,
                new CapabilityPosition(PORT_B, RelativeBlockFace.UP),
                OUTPUT_B,
                true
        );
    }

    @Override
    protected HeatExchangerSmallState createState(
            int tankCapacity,
            IInitialMultiblockContext<HeatExchangerSmallState> context,
            CapabilityReference<IFluidHandler> automaticOutputA,
            CapabilityReference<IFluidHandler> automaticOutputB
    ) {
        return new HeatExchangerSmallState(
                tankCapacity,
                context,
                automaticOutputA,
                automaticOutputB
        );
    }

    @Override
    protected void openMenu(
            IMultiblockContext<HeatExchangerSmallState> context,
            ServerPlayer player,
            BlockPos masterPos,
            BlockPos interactionPos
    ) {
        HeatExchangerSmallState state = context.getState();
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, ignoredPlayer) -> new HeatExchangerSmallMenu(
                        containerId,
                        inventory,
                        state,
                        context,
                        masterPos,
                        interactionPos
                ),
                Component.translatable("container.immersivechemical.heat_exchanger_small")
        );
        NetworkHooks.openScreen(player, provider, masterPos);
    }
}
