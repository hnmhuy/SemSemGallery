package com.example.semsemgallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.activities.MainActivity;

import java.util.ArrayList;

public class ChoosePeopleFragment extends Fragment implements FragmentCallbacks {
    MainActivity activity;
    View viewInFragment;
    public static ChoosePeopleFragment newInstance(String name) {
        ChoosePeopleFragment fragment = new ChoosePeopleFragment();
        Bundle bundle = new Bundle();
        bundle.putString("fragmentName", name);
        fragment.setArguments(bundle);
        return fragment;
    }
    private RecyclerView recyclerView;
    private People mitem;
    private ArrayList<People> listitem;
    private PeopleAdapter mitemAdapter;
    private LinearLayout linearLayout;
    private void data(){
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.icon_people));
        listitem.add(new People(R.drawable.icon_people));
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewInFragment =  inflater.inflate(R.layout.component_choose_people, container, false);
        // Inflate the layout for this fragment
        linearLayout = (LinearLayout) viewInFragment.findViewById(R.id.linearChoosePeople);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SeeAllPeople.class);
                startActivity(intent);
            }
        });
        recyclerView =(RecyclerView) viewInFragment.findViewById(R.id.shortListPeople);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        listitem = new ArrayList<>();
        data();
        mitemAdapter = new PeopleAdapter(listitem,getContext());
        recyclerView.setAdapter(mitemAdapter);

        return viewInFragment;
    }
    @Override
    public void onMsgFromMainToFragment(People people) {

    }
}
