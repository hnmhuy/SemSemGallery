package com.example.semsemgallery.activities.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.activities.pictureview.TagsAdapter;
import com.example.semsemgallery.domain.TagUtils;
import com.example.semsemgallery.models.People;
import com.example.semsemgallery.R;
import com.example.semsemgallery.models.Tag;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SearchViewActivity extends AppCompatActivity implements TagsAdapter.TagClickListener, SearchHistoryAdapter.SearchedTagClickListener{
    FragmentTransaction ft;
    private TextView erase;
    ListView listView;
    SearchHistoryAdapter newAdapter;
    private ArrayList<String> tagName = new ArrayList<>();
    private ArrayList<Tag> searchedKeyword = new ArrayList<>();
    Tag searchResult;
    ArrayAdapter<String> arrayAdapter;
    SQLiteDatabase db;
    private RecyclerView tagsRv;
    private RecyclerView search;
    private ArrayList<Tag> tagData = new ArrayList<>();
    private LinearLayout linearLayout;
    TagUtils tagSQL;
    MaterialToolbar toolbar;

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_view);
        LinearLayout beforeSearch = findViewById(R.id.beforeSearch);
        LinearLayout afterSearch = findViewById(R.id.afterSearch);
        LinearLayout duringSearch = findViewById(R.id.duringSearch);
        sharedPreferences = getSharedPreferences("SearchHistory", MODE_PRIVATE);

        tagsRv = findViewById(R.id.tags_rv);
        toolbar = findViewById(R.id.topAppBarNormal);
        tagSQL = new TagUtils(this);
        db = tagSQL.myGetDatabase(this);

        tagData = tagSQL.getAllTags(db);

        Set<String> searchHistory = sharedPreferences.getStringSet("searchHistory", new HashSet<>());

        // Convert set to list and display
        List<String> searchHistoryList = new ArrayList<>(searchHistory);
        for (String search: searchHistoryList
        ) {
            searchedKeyword.add(tagSQL.searchTag(db, search));
        }

        if(!tagData.isEmpty()) {
            TagsAdapter adapter = new TagsAdapter(this, tagData, false);
            tagsRv.setVisibility(View.VISIBLE);
            FlexboxLayoutManager flexboxLayout = new FlexboxLayoutManager(this);
            flexboxLayout.setFlexDirection(FlexDirection.ROW);
            flexboxLayout.setFlexWrap(FlexWrap.WRAP);
            tagsRv.setLayoutManager(flexboxLayout);
            adapter.setOnItemListener(this);
            tagsRv.setAdapter(adapter);
            for(int i = 0; i < tagData.size(); i++){
                tagName.add(tagData.get(i).getName());
            }
        }

        listView = findViewById(R.id.searchResult);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,tagName);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                searchResult = tagSQL.searchTag(db, selectedItem);
                if(searchResult != null){
                    searchedKeyword.add(searchResult);
                    newAdapter.notifyDataSetChanged();
                    saveSearchQuery(selectedItem);
                    Intent intent = new Intent(SearchViewActivity.this, SearchResultActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("tagName", searchResult.getName());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    duringSearch.setVisibility(View.GONE);
                }
            }
        });

        search = findViewById(R.id.search_history);
        erase = findViewById(R.id.erase_all);
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchedKeyword.clear();
                newAdapter.notifyDataSetChanged();
                // Retrieve existing search history
                Set<String> searchHistory = sharedPreferences.getStringSet("searchHistory", new HashSet<>());

                // Create a copy of the searchHistory set
                Set<String> newSearchHistory = new HashSet<>(searchHistory);

                // Convert set to list and add new query
                LinkedList<String> searchHistoryList = new LinkedList<>(newSearchHistory);
                searchHistoryList.clear();

                // Convert list back to set
                newSearchHistory = new HashSet<>(searchHistoryList);

                // Save updated search history to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet("searchHistory", newSearchHistory);
                editor.apply();
            }
        });
        newAdapter = new SearchHistoryAdapter(this, searchedKeyword, false);
        FlexboxLayoutManager newFlexboxLayout = new FlexboxLayoutManager(this);
        newFlexboxLayout.setFlexDirection(FlexDirection.ROW);
        newFlexboxLayout.setFlexWrap(FlexWrap.WRAP);
        search.setLayoutManager(newFlexboxLayout);
        newAdapter.setOnItemListener(this);
        search.setAdapter(newAdapter);

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
                            duringSearch.setVisibility(View.GONE);
                            return true; // Returning true will allow the collapse action to occur
                        }

                        @Override
                        public boolean onMenuItemActionExpand(MenuItem item) {
                            // Handle expand
                            beforeSearch.setVisibility(View.GONE);
                            afterSearch.setVisibility(View.VISIBLE);
                            duringSearch.setVisibility(View.GONE);
                            return true; // Returning true will allow the expand action to occur
                        }
                    });

                    SearchView search = (SearchView) item.getActionView();
                    search.setOnCloseListener(new SearchView.OnCloseListener() {
                        @Override
                        public boolean onClose() {
                            beforeSearch.setVisibility(View.VISIBLE);
                            afterSearch.setVisibility(View.GONE);
                            duringSearch.setVisibility(View.GONE);
                            return false;
                        }
                    });
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
    }
    private void saveSearchQuery(String query) {
        // Retrieve existing search history
        Set<String> searchHistory = sharedPreferences.getStringSet("searchHistory", new HashSet<>());

        // Create a copy of the searchHistory set
        Set<String> newSearchHistory = new HashSet<>(searchHistory);

        // Convert set to list and add new query
        LinkedList<String> searchHistoryList = new LinkedList<>(newSearchHistory);
        searchHistoryList.addFirst(query);

        // Remove oldest query if more than 10
        while (searchHistoryList.size() > 10) {
            searchHistoryList.removeLast();
        }

        // Convert list back to set
        newSearchHistory = new HashSet<>(searchHistoryList);

        // Save updated search history to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("searchHistory", newSearchHistory);
        editor.apply();
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

    @Override
    public void onTagClick(View view, int position) {

        Intent intent = new Intent(SearchViewActivity.this, SearchResultActivity.class);

        Tag tag = tagData.get(position);
        Bundle bundle = new Bundle();

        bundle.putString("tagName", tag.getName());
        bundle.putInt("tagId", tag.getTagId());

        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onSearchedTagClick(View view, int position) {
        Intent intent = new Intent(SearchViewActivity.this, SearchResultActivity.class);

        Tag tag = searchedKeyword.get(position);
        Bundle bundle = new Bundle();

        bundle.putString("tagName", tag.getName());
        bundle.putInt("tagId", tag.getTagId());

        intent.putExtras(bundle);
        startActivity(intent);
    }
}
