package com.example.semsemgallery.interfaces;

import com.example.semsemgallery.adapters.ViewModeEvent;

import java.util.EventListener;

public interface ChangeViewModeListener extends EventListener {
    void onChangeMode(ViewModeEvent event);

    void onSelectionChange(ViewModeEvent event);
}
