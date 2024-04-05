package com.example.semsemgallery.activities.base;

import java.util.ArrayList;
import java.util.List;

public class ObservableGridMode<DataType> {
    private final List<GridModeListener> observers = new ArrayList<>();
    private List<DataType> observedObjects = null;
    private GridMode currentMode;

    public ObservableGridMode(List<DataType> data, GridMode initMode) {
        this.currentMode = initMode;
        this.observedObjects = data;
    }

    public void addObserver(GridModeListener observer) {
        this.observers.add(observer);
    }

    public void removeObserver(GridModeListener observer) {
        this.observers.remove(observer);
    }

    public void fireModeChange() {
        GridModeEvent event = new GridModeEvent(this, currentMode, false);
        for (GridModeListener observer : observers) {
            observer.onModeChange(event);
        }
    }

    public void fireSelectAll() {
        GridModeEvent event = new GridModeEvent(this, currentMode, true);
        for (GridModeListener observer : observers) {
            observer.onSelectingAll(event);
        }
    }

    public void setGridMode(GridMode value) {
        currentMode = value;
        fireModeChange();
    }

    public int getDataSize() {
        return observedObjects.size();
    }

    public DataType getDataAt(int position) {
        return observedObjects.get(position);
    }

    public GridMode getCurrentMode() {
        return currentMode;
    }

}
