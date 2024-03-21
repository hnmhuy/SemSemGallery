package com.example.semsemgallery.adapters;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ObservableViewModeEvent {
    private final List<ChangeViewModeListener> observers = new ArrayList<>();
    private final List<Integer> selectedPositions = new ArrayList<>();
    private ViewMode currState;

    public ViewMode getViewMode() {
        return currState;
    }

    public List<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public void addObservers(ChangeViewModeListener observer) {
        observers.add(observer);
        Log.d("OBSERVABLE", "Add observer at " + observers.size());
    }

    public void removeObservers(ChangeViewModeListener observer) {
        observers.remove(observer);
    }

    public void fireChangeModeEvent() {
        ViewModeEvent event = new ViewModeEvent(this, currState, this.selectedPositions, false);
        for (ChangeViewModeListener observer : observers) {
            observer.onChangeMode(event);
        }
    }

    public void fireSelectionChangeEvent(boolean isSelectAll) {
        ViewModeEvent event = new ViewModeEvent(this, currState, selectedPositions, isSelectAll);
        for (ChangeViewModeListener observer : observers) {
            observer.onSelectionChange(event);
        }
    }

    public void setState(ViewMode viewMode) {
        this.currState = viewMode;
        fireChangeModeEvent();
    }

}
