package com.example.semsemgallery.activities.base;

import java.util.EventListener;

public interface GridModeListener extends EventListener {
    void onModeChange(GridModeEvent event);

    abstract void onSelectionChange(GridModeEvent event);
    void onSelectingAll(GridModeEvent event);
}
