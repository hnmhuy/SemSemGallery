package com.example.semsemgallery.activities.base;

import static com.example.semsemgallery.activities.base.GridMode.NORMAL;
import static com.example.semsemgallery.activities.base.GridMode.SELECTING;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.google.android.material.checkbox.MaterialCheckBox;

public abstract class SelectableItem<DataType> extends RecyclerView.ViewHolder implements GridModeListener {

    protected ObservableGridMode<DataType> observedObj;
    public MaterialCheckBox selector;
    public SelectableItem(@NonNull View itemView, ObservableGridMode<DataType> observedObj) {
        super(itemView);
        this.observedObj = observedObj;
        this.observedObj.addObserver(this);
        this.selector = itemView.findViewById(R.id.selector);
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (observedObj.getCurrentMode() == NORMAL) {
                    observedObj.setGridMode(SELECTING);
                    observedObj.selectItemAt(getAbsoluteAdapterPosition());
                    selector.setChecked(true);
                }
                return true;
            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (observedObj.getCurrentMode() == NORMAL) {
                    clickOnNormalMode(v);
                } else if (observedObj.getCurrentMode() == SELECTING) {
                    clickOnSelectingMode(v);
                }
            }
        });
    }
    public abstract void clickOnNormalMode(View v);

    public void clickOnSelectingMode(View v) {
        boolean state = !selector.isChecked();
        selector.setChecked(state);
        if (state) observedObj.selectItemAt(getAbsoluteAdapterPosition());
        else observedObj.unselectItemAt(getAbsoluteAdapterPosition());
    }
    @Override
    public void onModeChange(GridModeEvent event) {
        if (event.getGridMode() == SELECTING) selector.setVisibility(View.VISIBLE);
        else if (event.getGridMode() == GridMode.NORMAL) selector.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSelectingAll(GridModeEvent event) {
        this.selector.setVisibility(View.VISIBLE);
        this.selector.setChecked(event.getNewSelectionForAll());
        this.observedObj.getObservedObjects().get(getAbsoluteAdapterPosition()).isSelected = event.getNewSelectionForAll();
    }

    @Override
    public void onSelectionChange(GridModeEvent event) {

    }
}
