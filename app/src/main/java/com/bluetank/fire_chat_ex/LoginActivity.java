package com.bluetank.fire_chat_ex;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private Button login, signup;
    FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background=mFirebaseRemoteConfig.getString("splash_background");
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        login=(Button)findViewById(R.id.login_btn_login);
        signup =(Button)findViewById(R.id.login_btn_signup);
        login.setBackgroundColor(Color.parseColor(splash_background));
        signup.setBackgroundColor(Color.parseColor(splash_background));

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });
    }
}
