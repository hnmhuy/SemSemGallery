package com.example.semsemgallery.adapters;

import java.util.EventListener;

public interface ChangeViewModeListener extends EventListener {
    void onChangeMode(ViewModeEvent event);

    void onSelectionChange(ViewModeEvent event);
}
