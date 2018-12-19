package com.bluetank.fire_chat_ex;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bluetank.fire_chat_ex.fragment.AccountFragment;
import com.bluetank.fire_chat_ex.fragment.ChatFragment;
import com.bluetank.fire_chat_ex.fragment.PeopleFragment;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=(Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        setTitle("친구");
        BottomNavigationView bottomNavigationView=(BottomNavigationView) findViewById(R.id.main_bottomNavi);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_people:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,new PeopleFragment()).commit();
                        setTitle("친구");
                        return true;
                    case R.id.action_chat:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,new ChatFragment()).commit();
                        setTitle("채팅");
                        return true;
                    case R.id.action_account:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,new AccountFragment()).commit();
                        setTitle("계정");
                        return true;
                }

                return false;
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,new PeopleFragment()).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
