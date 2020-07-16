package com.thecowking.shaftdriller.tools;

/*
    Source - https://github.com/Terpo/Waterworks/blob/1.15/src/main/java/org/terpo/waterworks/fluid/WaterworksTank.java
 */


import com.thecowking.shaftdriller.blocks.IMultiBlockControllerTile;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class CustomFluidStorage extends FluidTank {

    private final IMultiBlockControllerTile tile;
    private boolean allowFilling;
    public CustomFluidStorage(int capacity, IMultiBlockControllerTile tile) {
        super(capacity);
        this.tile = tile;
        this.allowFilling = false;
    }

    @Override
    public boolean isEmpty() {
        return this.getFluidAmount() == 0;
    }

    public boolean isFull() {
        return this.getFluidAmount() >= this.getCapacity();
    }
    @Override
    protected void onContentsChanged() {
        this.tile.setDirty(true);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (this.allowFilling) {
            return super.fill(resource, action);
        }
        return 0;
    }

    public int fillInternal(FluidStack resource, FluidAction action) {
        return super.fill(resource, action);
    }

    public CustomFluidStorage allowFilling(boolean newAllowFilling) {
        setAllowFilling(newAllowFilling);
        return this;
    }

    public boolean isAllowFilling() {
        return this.allowFilling;
    }

    public void setAllowFilling(boolean allowFilling) {
        this.allowFilling = allowFilling;
    }

}