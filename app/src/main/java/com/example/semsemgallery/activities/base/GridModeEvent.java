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
    private boolean newSelectionForAll;

    public void setGridMode(GridMode newMode) {
        this.gridMode = newMode;
    }

    public GridMode getGridMode() {
        return this.gridMode;
    }

    public void setNewSelectionForAll(boolean value) {
        this.newSelectionForAll = value;
    }

    public boolean getNewSelectionForAll() {
        return newSelectionForAll;
    }

    public GridModeEvent(Object source, GridMode newMode, boolean newSelection) {
        super(source);
        this.gridMode = newMode;
        this.newSelectionForAll = newSelection;
    }
}
