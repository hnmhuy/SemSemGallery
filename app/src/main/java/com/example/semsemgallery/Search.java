package com.example.semsemgallery;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class Search extends AppCompatActivity implements MainCallbacks {
    FragmentTransaction ft;
    ChoosePeopleFragment choosePeopleFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_search);
        ft = getSupportFragmentManager().beginTransaction();
        choosePeopleFragment =ChoosePeopleFragment.newInstance("ChoosePeople");
        ft.replace(R.id.listPeople, choosePeopleFragment);
        ft.commit();
    }
    @Override
    public void onMsgFromFragToMain(String sender, People data ) {
        if(sender.equals("List")) {
            choosePeopleFragment.onMsgFromMainToFragment(data);
        }
    }
}
