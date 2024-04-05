package com.example.semsemgallery.activities.base;

import java.util.ArrayList;
import java.util.List;

public class ObservableGridMode<DataType> {
    private final List<GridModeListener> observers = new ArrayList<>();
    private List<DataItem> observedObjects = null;
    private GridMode currentMode;

    public ObservableGridMode(List<DataType> data, GridMode initMode) {
        this.currentMode = initMode;
        observedObjects = new ArrayList<>(data.size());
        for (DataType value : data) {
            this.observedObjects.add(new DataItem(value));
        }
    }

    public void addObserver(GridModeListener observer) {
        this.observers.add(observer);
    }

    public void removeObserver(GridModeListener observer) {
        this.observers.remove(observer);
    }

    public void fireModeChange(boolean selectionForAll) {
        GridModeEvent event = new GridModeEvent(this, currentMode, selectionForAll);
        for (GridModeListener observer : observers) {
            observer.onModeChange(event);
        }
    }

    public void fireSelectionChangeForAll(boolean selectionForAll) {
        GridModeEvent event = new GridModeEvent(this, currentMode, selectionForAll);
        for (GridModeListener observer : observers) {
            observer.onSelectingAll(event);
        }
    }

    public void setGridMode(GridMode value) {
        currentMode = value;
        fireModeChange(false);
    }

    public int getDataSize() {
        return observedObjects.size();
    }

    public DataItem getDataAt(int position) {
        return observedObjects.get(position);
    }

    public GridMode getCurrentMode() {
        return currentMode;
    }

    public void selectItemAt(int position) {
        this.observedObjects.get(position).isSelected = true;
        if (this.observedObjects.stream().filter(item -> item.isSelected).count() == this.observedObjects.size()) {
            fireSelectionChangeForAll(true);
        }
    }

    public void unselectItemAt(int position) {
        this.observedObjects.get(position).isSelected = false;
        if (this.observedObjects.stream().noneMatch(item -> item.isSelected)) {
            fireSelectionChangeForAll(false);
        }
    }

    public class DataItem {
        public boolean isSelected;
        public DataType data;

        public DataItem(DataType data) {
            this.data = data;
            this.isSelected = false;
        }
    }

}
