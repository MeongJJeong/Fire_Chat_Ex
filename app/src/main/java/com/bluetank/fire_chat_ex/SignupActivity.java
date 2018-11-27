package com.bluetank.fire_chat_ex;

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
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText name,email,pw;
    private Button signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name=(EditText)findViewById(R.id.signup_edt_name);
        email=(EditText)findViewById(R.id.signup_edt_email);
        pw=(EditText)findViewById(R.id.signup_edt_pw);
        signup=(Button)findViewById(R.id.signup_btn_signup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(name.getText().length()==0||email.getText().length()==0||pw.getText().length()==0){
                    Toast.makeText(getApplicationContext(),"입력 바로하세욧!",Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(),pw.getText().toString())
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                UserModel usermodel=new UserModel();
//                                usermodel.userName=name.getText().toString();
////
//                                String uid=task.getResult().getUser().getUid();
//                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(usermodel);
                            }
                        });
            }
        });
    }
}
