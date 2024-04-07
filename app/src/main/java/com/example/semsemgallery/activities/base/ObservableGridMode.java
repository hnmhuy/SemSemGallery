package com.example.semsemgallery.activities.base;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ObservableGridMode<DataType> {
    private final List<GridModeListener> observers = new ArrayList<>();

    public GridModeListener getMaster() {
        return master;
    }

    public void setMaster(GridModeListener master) {
        this.master = master;
    }

    private GridModeListener master;

    public List<DataItem> getObservedObjects() {
        return observedObjects;
    }

    private List<DataItem> observedObjects = null;
    private GridMode currentMode;

    public ObservableGridMode(List<DataType> data, GridMode initMode) {
        this.currentMode = initMode;
        observedObjects = new ArrayList<>(data.size());
        for (DataType value : data) {
            this.observedObjects.add(new DataItem(value));
        }
    }

    public void addData(DataType value) {
        this.observedObjects.add(new DataItem(value));
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

        // Set for all data
        for (DataItem item : observedObjects) {
            item.isSelected = selectionForAll;
        }

        GridModeEvent event = new GridModeEvent(this, currentMode, selectionForAll);
        for (GridModeListener observer : observers) {
            observer.onSelectingAll(event);
        }
    }

    public void setGridMode(GridMode value) {
        currentMode = value;
        fireModeChange(false);
    }

    public long getNumberOfSelected() {
        return this.observedObjects.stream().filter(item -> item.isSelected).count();
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
        Log.i("ObservableObj", "Selected: " + position + " - " + observedObjects.get(position).isSelected);
        if (master != null) master.onSelectionChange(new GridModeEvent(this, currentMode, false));
    }

    public void unselectItemAt(int position) {
        this.observedObjects.get(position).isSelected = false;
        Log.d("ObservableObj", "Unselected: " + position + " - " + observedObjects.get(position).isSelected);
        if (master != null) master.onSelectionChange(new GridModeEvent(this, currentMode, false));
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
