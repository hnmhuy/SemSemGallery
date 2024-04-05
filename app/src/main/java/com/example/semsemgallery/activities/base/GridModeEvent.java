package com.example.semsemgallery.activities.base;

import java.util.EventObject;

public class GridModeEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    private GridMode gridMode;
    private boolean isSelectingAll;

    public void setGridMode(GridMode newMode) {
        this.gridMode = newMode;
    }

    public GridMode getGridMode() {
        return this.gridMode;
    }

    public void setSelectingAll(boolean value) {
        this.isSelectingAll = value;
    }

    public boolean getSelectingAll() {
        return isSelectingAll;
    }

    public GridModeEvent(Object source, GridMode newMode, boolean isSelectingAll) {
        super(source);
        this.gridMode = newMode;
        this.isSelectingAll = isSelectingAll;
    }
}
