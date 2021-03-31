package team.cappcraft.immersivechemical.common.tileentity;

public interface IHeatExchangerProperties {
    int getCapacity();

    /**
     * @return Tick required to exchange one heat
     */
    float getTickMultiplier();
}
