package team.cappcraft.immersivechemical.common.tileentity;

import net.minecraft.util.ITickable;

public interface ITickableStateMachine<T extends ITickable> {
    default ITickableStateMachine<T> nextState(T tickAble) {
        return this;
    }
}
