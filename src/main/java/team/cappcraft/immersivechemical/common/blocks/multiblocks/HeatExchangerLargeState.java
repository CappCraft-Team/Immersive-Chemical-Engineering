package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraftforge.fluids.capability.IFluidHandler;


public final class HeatExchangerLargeState extends HeatExchangerState {
    public HeatExchangerLargeState(
            int tankCapacity,
            IInitialMultiblockContext<HeatExchangerLargeState> context,
            CapabilityReference<IFluidHandler> automaticOutputA,
            CapabilityReference<IFluidHandler> automaticOutputB
    ) {
        super(tankCapacity, context, automaticOutputA, automaticOutputB);
    }
}
