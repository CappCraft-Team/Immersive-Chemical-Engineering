package team.cappcraft.immersivechemical.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** State marker for the medium heat-exchanger registration. */
public final class HeatExchangerMediumState extends HeatExchangerState {
    public HeatExchangerMediumState(
            int tankCapacity,
            IInitialMultiblockContext<HeatExchangerMediumState> context,
            CapabilityReference<IFluidHandler> automaticOutputA,
            CapabilityReference<IFluidHandler> automaticOutputB
    ) {
        super(tankCapacity, context, automaticOutputA, automaticOutputB);
    }
}
