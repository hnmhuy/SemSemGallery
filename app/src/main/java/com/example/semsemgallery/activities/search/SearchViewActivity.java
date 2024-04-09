package com.example.semsemgallery.activities.search;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.activities.people.PeopleAdapter;
import com.example.semsemgallery.activities.people.SeeAllPeopleActivity;
import com.example.semsemgallery.domain.TagUtils;
import com.example.semsemgallery.models.People;
import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Tag;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class SearchViewActivity extends AppCompatActivity {
    FragmentTransaction ft;
    ListView listView;
    String[] name = {"Nguyen", "Styx", "Drink"};
    String[] tagName;
    Tag searchResult;
    ArrayAdapter<String> arrayAdapter;
    SQLiteDatabase db;
    private ArrayList<Tag> tagData = new ArrayList<>();
    private RecyclerView recyclerView;
    private People mitem;
    private ArrayList<People> listitem;
    private PeopleAdapter mitemAdapter;
    private LinearLayout linearLayout;
    private void loadTag(){

    }
    private void data(){
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.icon_people));
    }
    MaterialToolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_view);
        LinearLayout beforeSearch = findViewById(R.id.beforeSearch);
        LinearLayout afterSearch = findViewById(R.id.afterSearch);
        LinearLayout duringSearch = findViewById(R.id.duringSearch);
        toolbar = findViewById(R.id.topAppBarNormal);
        TagUtils studentSQL = new TagUtils(this);
        db = studentSQL.myGetDatabase(this);
//        studentSQL.myCreate(db);
        tagData = studentSQL.getAllTags(db);

        listView = findViewById(R.id.searchResult);
        if(tagData.size() == 0){
            studentSQL.insertTag(db, "sea");
            studentSQL.insertTag(db, "dance");
            studentSQL.insertTag(db, "food");
            name = new String[tagData.size()];
            for(int i = 0; i < tagData.size(); i++){
                name[i] = tagData.get(i).getName();
            }
        }
        if (tagData.size() >= 1){
            name = new String[tagData.size()];
            for(int i = 0; i < tagData.size(); i++){
                name[i] = tagData.get(i).getName();
            }
            tagName = name;
        } else {
            tagName = name;
        }

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,tagName);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                searchResult = studentSQL.searchTag(db, selectedItem);
                if(searchResult != null){
                    Toast.makeText(SearchViewActivity.this, searchResult.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.action_search){
                    item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem item) {
                            // Handle collapse
                            beforeSearch.setVisibility(View.VISIBLE);
                            afterSearch.setVisibility(View.GONE);
                            return true; // Returning true will allow the collapse action to occur
                        }

                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            // Handle expand
                            beforeSearch.setVisibility(View.GONE);
                            afterSearch.setVisibility(View.VISIBLE);
                            return true; // Returning true will allow the expand action to occur
                        }
                    });

                    SearchView search = (SearchView) item.getActionView();
                    search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if(!query.isEmpty()) {
                                afterSearch.setVisibility(View.GONE);
                                duringSearch.setVisibility(View.VISIBLE);
                                arrayAdapter.getFilter().filter(query);
                            } else {
                                afterSearch.setVisibility(View.VISIBLE);
                                duringSearch.setVisibility(View.GONE);
                            }
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            if(!newText.isEmpty()) {
                                afterSearch.setVisibility(View.GONE);
                                duringSearch.setVisibility(View.VISIBLE);
                                arrayAdapter.getFilter().filter(newText);
                            } else {
                                afterSearch.setVisibility(View.VISIBLE);
                                duringSearch.setVisibility(View.GONE);
                            }
                            return false;
                        }
                    });
                    return true;
                }
                return false;
            }
        });

        linearLayout = (LinearLayout) findViewById(R.id.linearChoosePeople);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchViewActivity.this, SeeAllPeopleActivity.class);
                startActivity(intent);
            }
        });
        recyclerView =(RecyclerView) findViewById(R.id.shortListPeople);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(SearchViewActivity.this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        listitem = new ArrayList<>();
        data();
        mitemAdapter = new PeopleAdapter(listitem, SearchViewActivity.this);
        recyclerView.setAdapter(mitemAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
