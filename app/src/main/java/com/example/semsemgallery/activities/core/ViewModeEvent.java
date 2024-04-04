package com.example.semsemgallery.activities.core;

import com.example.semsemgallery.activities.core.ViewMode;

import java.util.EventObject;
import java.util.List;

public class ViewModeEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    private final ViewMode viewMode;
    private final List<Integer> selectedPositions;
    private final boolean isSelectAll;

    public ViewModeEvent(Object source, ViewMode viewMode, List<Integer> selectedPositions, boolean isSelectAll) {
        super(source);
        this.viewMode = viewMode;
        this.selectedPositions = selectedPositions;
        this.isSelectAll = isSelectAll;
    }

    public ViewMode getViewMode() {
        return this.viewMode;
    }

    public List<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public boolean getIsSelectAll() {
        return isSelectAll;
    }
}
