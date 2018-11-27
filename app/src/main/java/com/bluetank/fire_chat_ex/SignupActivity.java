package com.bluetank.fire_chat_ex;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bluetank.fire_chat_ex.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class SignupActivity extends AppCompatActivity {

    private EditText name,email,pw;
    private Button signup;
    private FirebaseAuth mAuth;
    private String str1,str2,str3;

    Button test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseRemoteConfig mFirebaseRemoteConfig=FirebaseRemoteConfig.getInstance();
        String splash_background=mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        name=(EditText)findViewById(R.id.signup_edt_name);
        email=(EditText)findViewById(R.id.signup_edt_email);
        pw=(EditText)findViewById(R.id.signup_edt_pw);
        signup=(Button)findViewById(R.id.signup_btn_signup);
        signup.setBackgroundColor(Color.parseColor(splash_background));
        mAuth=FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(name.getText().length()==0||email.getText().length()==0||pw.getText().length()==0){
                    Toast.makeText(getApplicationContext(),"입력 바로하세욧!",Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    str1=email.getText().toString();
                    str2=pw.getText().toString();
                    str3=name.getText().toString();

                    mAuth.createUserWithEmailAndPassword(str1,str2)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    UserModel user=new UserModel();
                                    user.userName=str3;

                                    String uid=task.getResult().getUser().getUid();
                                    FirebaseDatabase.getInstance().getReference().child("user").child(uid).setValue(user);

                                }
                            });
                }
            }
        });
    }


}
