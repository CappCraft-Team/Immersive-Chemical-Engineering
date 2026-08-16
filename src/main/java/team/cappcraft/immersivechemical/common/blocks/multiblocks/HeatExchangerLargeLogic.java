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
import team.cappcraft.immersivechemical.common.menu.HeatExchangerLargeMenu;
import team.cappcraft.immersivechemical.common.tileentity.constant.HeatExchangerSizeVariants;


public final class HeatExchangerLargeLogic
        extends AbstractHeatExchangerLogic<HeatExchangerLargeState> {

    static final CapabilityPosition OUTPUT_A = new CapabilityPosition(
            new BlockPos(1, 0, 6), RelativeBlockFace.DOWN);
    static final CapabilityPosition OUTPUT_B = new CapabilityPosition(
            new BlockPos(1, 2, 1), RelativeBlockFace.UP);

    public HeatExchangerLargeLogic() {
        super(
                HeatExchangerSizeVariants.LARGE,
                HeatExchangerLargeMultiblock.MASTER_FROM_ORIGIN,
                new CapabilityPosition(new BlockPos(1, 2, 6), RelativeBlockFace.UP),
                OUTPUT_A,
                new CapabilityPosition(new BlockPos(1, 0, 4), RelativeBlockFace.DOWN),
                OUTPUT_B,
                false
        );
    }

    @Override
    protected HeatExchangerLargeState createState(
            int tankCapacity,
            IInitialMultiblockContext<HeatExchangerLargeState> context,
            CapabilityReference<IFluidHandler> automaticOutputA,
            CapabilityReference<IFluidHandler> automaticOutputB
    ) {
        return new HeatExchangerLargeState(
                tankCapacity,
                context,
                automaticOutputA,
                automaticOutputB
        );
    }

    @Override
    protected void openMenu(
            IMultiblockContext<HeatExchangerLargeState> context,
            ServerPlayer player,
            BlockPos masterPos,
            BlockPos interactionPos
    ) {
        HeatExchangerLargeState state = context.getState();
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, ignoredPlayer) -> new HeatExchangerLargeMenu(
                        containerId,
                        inventory,
                        state,
                        context,
                        masterPos,
                        interactionPos
                ),
                Component.translatable("container.immersivechemical.heat_exchanger_large")
        );
        NetworkHooks.openScreen(player, provider, masterPos);
    }
}
