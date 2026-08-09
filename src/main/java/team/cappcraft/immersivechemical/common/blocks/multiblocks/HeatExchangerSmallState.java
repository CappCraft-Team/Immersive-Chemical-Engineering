package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** State marker for the small registration; storage and processing fields live in the base. */
public final class HeatExchangerSmallState extends HeatExchangerState {
    public HeatExchangerSmallState(
            int tankCapacity,
            IInitialMultiblockContext<HeatExchangerSmallState> context,
            CapabilityReference<IFluidHandler> automaticOutputA,
            CapabilityReference<IFluidHandler> automaticOutputB
    ) {
        super(tankCapacity, context, automaticOutputA, automaticOutputB);
    }
}
