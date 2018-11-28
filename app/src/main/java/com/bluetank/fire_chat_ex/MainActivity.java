package com.bluetank.fire_chat_ex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bluetank.fire_chat_ex.fragment.PeopleFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,new PeopleFragment()).commit();
    }
}
