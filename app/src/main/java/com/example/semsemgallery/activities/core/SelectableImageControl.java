package com.example.semsemgallery.activities.core;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.semsemgallery.R;

public class SelectableImageControl implements ChangeViewModeListener {
    private ImageView thumbnail;
    private CheckBox selector;
    private RelativeLayout overlay;
    private ViewMode viewMode;

    private boolean isFireEvent = true;
    public ObservableViewModeEvent observableObject;

    public ViewMode getViewMode() {
        return viewMode;
    }

    public ImageView getThumbnail() {
        return thumbnail;
    }

    public CheckBox getSelector() {
        return selector;
    }

    public RelativeLayout getOverlay() {
        return overlay;
    }

    public void setOverlay(RelativeLayout overlay) {
        this.overlay = overlay;
    }

    public void setViewMode(ViewMode selectingMode) {
        viewMode = selectingMode;
    }

    public void setSelector(CheckBox selector) {
        this.selector = selector;
    }

    public void setThumbnail(ImageView thumbnail) {
        this.thumbnail = thumbnail;
    }

    private int currPosition;

    public void setCurrPosition(int newIndex) {
        this.currPosition = newIndex;
        this.selector.setOnCheckedChangeListener((buttonView, isChecked) -> selectItem(isChecked));
        this.selector.setChecked(observableObject.getSelectedPositions().contains(newIndex));
    }

    public SelectableImageControl(View layout, ObservableViewModeEvent observableObject) {
        this.thumbnail = layout.findViewById(R.id.thumbnail);
        this.selector = layout.findViewById(R.id.selector);
        this.overlay = layout.findViewById(R.id.overlay);
        this.observableObject = observableObject;
        observableObject.addObservers(this);
        this.viewMode = observableObject.getViewMode();
        changeMode();
        this.thumbnail.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                observableObject.setState(ViewMode.SELECTING_VIEW);
                return true;
            }
        });

        this.overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox selector = v.findViewById(R.id.selector);
                selector.setChecked(!selector.isChecked());
            }
        });
    }

    public void selectItem(boolean isSelected) {
        int temp = observableObject.getSelectedPositions().indexOf(currPosition);
        if (isSelected) {
            if (temp == -1) observableObject.getSelectedPositions().add(currPosition);
        } else {
            Log.d("selected", "Deleting " + currPosition);
            if (temp != -1) observableObject.getSelectedPositions().remove(temp);
        }
        Log.d("selected", currPosition + " - " + isFireEvent);
        if (isFireEvent) observableObject.fireSelectionChangeEvent(false);
    }

    public void changeToNormalMode() {
        this.overlay.setVisibility(View.INVISIBLE);
    }

    public void changeToSelectingMode() {
        this.overlay.setVisibility(View.VISIBLE);
    }

    public void changeMode() {
        if (viewMode == ViewMode.SELECTING_VIEW) changeToSelectingMode();
        else changeToNormalMode();
    }


    @Override
    public void onChangeMode(ViewModeEvent event) {
        this.viewMode = event.getViewMode();
        changeMode();
    }

    @Override
    public void onSelectionChange(ViewModeEvent event) {
        if (event.getIsSelectAll()) {
            isFireEvent = false;
            this.selector.setChecked(!event.getSelectedPositions().isEmpty());
        }
        isFireEvent = true;
    }
}
