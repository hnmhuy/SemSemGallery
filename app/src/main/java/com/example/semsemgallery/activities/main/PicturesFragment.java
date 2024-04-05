package com.example.semsemgallery.activities.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.main.adapter.PicturesByDateRecyclerAdapter;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.domain.MediaRetriever;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PicturesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        List<Picture> picturesRetriever = new MediaRetriever(appCompatActivity).getAllPictureList();

        View view = inflater.inflate(R.layout.fragment_pictures, container, false);

        Map<LocalDate, List<Picture>> partitionedMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for(Picture picture: picturesRetriever) {
            LocalDate dateAdded = picture.getDateTaken().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String dateString = dateAdded.format(formatter);
            LocalDate formattedDate = LocalDate.parse(dateString, formatter);

            if(partitionedMap.containsKey(formattedDate)) {
                Objects.requireNonNull(partitionedMap.get(formattedDate)).add(picture);
            } else {
                List<Picture> newList = new ArrayList<>();
                newList.add(picture);
                partitionedMap.put(formattedDate, newList);
            }
        }
        List<List<Picture>> picturesByDateAdded = new ArrayList<>(partitionedMap.values());
        Collections.sort(picturesByDateAdded, (list1, list2) -> {
            LocalDate date1 = list1.get(0).getDateTaken().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate date2 = list2.get(0).getDateTaken().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return date2.compareTo(date1); // descending order
        });

        RecyclerView recyclerView = view.findViewById(R.id.picture_recycler_view);
        PicturesByDateRecyclerAdapter adapter = new PicturesByDateRecyclerAdapter(picturesByDateAdded, appCompatActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(appCompatActivity));
        recyclerView.setAdapter(adapter);
        return view;
    }
}

