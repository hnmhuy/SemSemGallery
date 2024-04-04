package com.example.semsemgallery.activities.core;

import com.example.semsemgallery.activities.core.ViewModeEvent;

import java.util.EventListener;

public interface ChangeViewModeListener extends EventListener {
    void onChangeMode(ViewModeEvent event);

    void onSelectionChange(ViewModeEvent event);
}
