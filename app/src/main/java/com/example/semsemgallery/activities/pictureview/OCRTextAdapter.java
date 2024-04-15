package com.example.semsemgallery.activities.pictureview;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.semsemgallery.databinding.LayoutItemOcrBinding;

public class OCRTextAdapter extends RecyclerView.Adapter<OCRTextAdapter.MyViewHolder> {

    private final List<String> strings;
    private OnCopyClickListener copyClickListener;

    public OCRTextAdapter(List<String> strings, OnCopyClickListener copyClickListener) {
        this.strings = strings;
        this.copyClickListener = copyClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutItemOcrBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.binding.itemTextview.setText(strings.get(position));
        holder.binding.itemCopybutton.setOnClickListener(view -> {
            if (copyClickListener != null) {
                copyClickListener.onCopyClick(strings.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final LayoutItemOcrBinding binding;

        public MyViewHolder(LayoutItemOcrBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnCopyClickListener {
        void onCopyClick(String text);
    }
}