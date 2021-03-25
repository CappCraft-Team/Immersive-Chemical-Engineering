package team.cappcraft.immersivechemical.common.tileentity;

interface ILockableSlot {
    /**
     * Check if the slot is locked
     *
     * @return is locked
     */
    boolean isLocked();

    /**
     * Set slot lock state
     *
     * @param locked lock state
     */
    void setLocked(boolean locked);

    /**
     * Check if the slot is locked its content type
     *
     * @return is locked
     */
    boolean isLockedType();

    /**
     * Set the slot to locked its content type
     */
    void setLockedType(boolean locked);
}
