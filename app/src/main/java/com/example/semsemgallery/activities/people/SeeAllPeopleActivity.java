package com.example.semsemgallery.activities.people;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.models.People;
import com.example.semsemgallery.R;

import java.util.ArrayList;

public class SeeAllPeopleActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private People mitem;
    private ArrayList<People> listitem;
//    private PeopleAdapter mitemAdapter;
    private void data(){
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.react));
        listitem.add(new People(R.drawable.icon_people));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.component_tag_list);
        recyclerView =(RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        listitem = new ArrayList<>();
        data();
//        mitemAdapter = new PeopleAdapter(listitem,this);
//        recyclerView.setAdapter(mitemAdapter);
    }
}
