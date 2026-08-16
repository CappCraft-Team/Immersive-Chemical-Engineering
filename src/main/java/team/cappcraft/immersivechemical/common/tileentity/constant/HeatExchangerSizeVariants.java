package team.cappcraft.immersivechemical.common.tileentity.constant;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import team.cappcraft.immersivechemical.Config;
import team.cappcraft.immersivechemical.ImmersiveChemicalEngineering;

import java.util.function.Predicate;

public enum HeatExchangerSizeVariants {
    SMALL(
            "heat_exchanger_small",
            new BlockPos(0, 0, 0),
            new BlockPos(0, 0, 0),
            new BlockPos(1, 1, 4),
            new CapabilityPosition(new BlockPos(0, 0, 0), RelativeBlockFace.UP),
            new CapabilityPosition(new BlockPos(0, 0, 0), RelativeBlockFace.DOWN),
            new CapabilityPosition(new BlockPos(0, 0, 3), RelativeBlockFace.UP),
            new CapabilityPosition(new BlockPos(0, 0, 3), RelativeBlockFace.DOWN),
            true,
            false,
            16.0F,
            new BlockPos(0, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, 3),
            new BlockPos(0, 0, 2),
            position -> position.getZ() == 0 || position.getZ() == 3
    ),
    MEDIUM(
            "heat_exchanger_medium",
            new BlockPos(2, 0, 1),
            new BlockPos(0, 1, 1),
            new BlockPos(5, 2, 2),
            new CapabilityPosition(new BlockPos(0, 1, 0), RelativeBlockFace.UP),
            new CapabilityPosition(new BlockPos(0, 0, 1), RelativeBlockFace.DOWN),
            new CapabilityPosition(new BlockPos(1, 0, 0), RelativeBlockFace.DOWN),
            new CapabilityPosition(new BlockPos(1, 1, 1), RelativeBlockFace.UP),
            false,
            true,
            16.0F,
            new BlockPos(0, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(1, 0, 1),
            position -> position.getX() == 0
    ),
    LARGE(
            "heat_exchanger_large",
            new BlockPos(1, 0, 3),
            new BlockPos(1, 1, 6),
            new BlockPos(3, 3, 7),
            new CapabilityPosition(new BlockPos(1, 2, 6), RelativeBlockFace.UP),
            new CapabilityPosition(new BlockPos(1, 0, 6), RelativeBlockFace.DOWN),
            new CapabilityPosition(new BlockPos(1, 0, 4), RelativeBlockFace.DOWN),
            new CapabilityPosition(new BlockPos(1, 2, 1), RelativeBlockFace.UP),
            false,
            false,
            12.0F,
            new BlockPos(0, 0, 6),
            new BlockPos(0, 0, 5),
            new BlockPos(2, 0, 6),
            new BlockPos(2, 0, 5),
            position -> position.getZ() == 6
                    || (position.getX() == 1
                    && position.getY() == 1
                    && position.getZ() >= 1
                    && position.getZ() <= 5)
    );

    private final ResourceLocation id;
    private final BlockPos masterPos;
    private final BlockPos triggerPos;
    private final BlockPos size;
    private final CapabilityPosition inputA;
    private final CapabilityPosition outputA;
    private final CapabilityPosition inputB;
    private final CapabilityPosition outputB;
    private final boolean exposeUnsidedPorts;
    private final boolean mirrorable;
    private final float manualScale;
    private final BlockPos comparatorInputA;
    private final BlockPos comparatorOutputA;
    private final BlockPos comparatorInputB;
    private final BlockPos comparatorOutputB;
    private final Predicate<BlockPos> radiatorPredicate;

    HeatExchangerSizeVariants(
            String id,
            BlockPos masterPos,
            BlockPos triggerPos,
            BlockPos size,
            CapabilityPosition inputA,
            CapabilityPosition outputA,
            CapabilityPosition inputB,
            CapabilityPosition outputB,
            boolean exposeUnsidedPorts,
            boolean mirrorable,
            float manualScale,
            BlockPos comparatorInputA,
            BlockPos comparatorOutputA,
            BlockPos comparatorInputB,
            BlockPos comparatorOutputB,
            Predicate<BlockPos> radiatorPredicate
    ) {
        this.id = new ResourceLocation(ImmersiveChemicalEngineering.MODID, id);
        this.masterPos = masterPos;
        this.triggerPos = triggerPos;
        this.size = size;
        this.inputA = inputA;
        this.outputA = outputA;
        this.inputB = inputB;
        this.outputB = outputB;
        this.exposeUnsidedPorts = exposeUnsidedPorts;
        this.mirrorable = mirrorable;
        this.manualScale = manualScale;
        this.comparatorInputA = comparatorInputA;
        this.comparatorOutputA = comparatorOutputA;
        this.comparatorInputB = comparatorInputB;
        this.comparatorOutputB = comparatorOutputB;
        this.radiatorPredicate = radiatorPredicate;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getContainerKey() {
        return "container." + id.getNamespace() + "." + id.getPath();
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public BlockPos getTriggerPos() {
        return triggerPos;
    }

    public BlockPos getSize() {
        return size;
    }

    public CapabilityPosition getInputA() {
        return inputA;
    }

    public CapabilityPosition getOutputA() {
        return outputA;
    }

    public CapabilityPosition getInputB() {
        return inputB;
    }

    public CapabilityPosition getOutputB() {
        return outputB;
    }

    public boolean isExposeUnsidedPorts() {
        return exposeUnsidedPorts;
    }

    public boolean isMirrorable() {
        return mirrorable;
    }

    public float getManualScale() {
        return manualScale;
    }

    public BlockPos getComparatorInputA() {
        return comparatorInputA;
    }

    public BlockPos getComparatorOutputA() {
        return comparatorOutputA;
    }

    public BlockPos getComparatorInputB() {
        return comparatorInputB;
    }

    public BlockPos getComparatorOutputB() {
        return comparatorOutputB;
    }

    public boolean isRadiator(BlockPos position) {
        return radiatorPredicate.test(position);
    }

    public int getCapacity() {
        switch (this) {
            case SMALL: return Config.getSmallCapacity();
            case MEDIUM: return Config.getMediumCapacity();
            case LARGE: return Config.getLargeCapacity();
        }
        throw new IllegalStateException("Unknown size: " + this);
    }

    public float getTickMultiplier() {
        switch (this) {
            case SMALL: return (float) Config.getSmallTickMultiplier();
            case MEDIUM: return (float) Config.getMediumTickMultiplier();
            case LARGE: return (float) Config.getLargeTickMultiplier();
        }
        throw new IllegalStateException("Unknown size: " + this);
    }
}
