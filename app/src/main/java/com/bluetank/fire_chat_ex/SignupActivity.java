package com.bluetank.fire_chat_ex;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bluetank.fire_chat_ex.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class SignupActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM =10 ;
    private EditText name,email,pw;
    private Button signup;
    private String str1,str2,str3;
    private ImageView profile;
    private Uri imageUri;

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

        profile=(ImageView)findViewById(R.id.signup_image_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,PICK_FROM_ALBUM);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(name.getText().length()==0||email.getText().length()==0||pw.getText().length()==0){
                    Toast.makeText(getApplicationContext(),"입력 바로하세욧!",Toast.LENGTH_SHORT).show();
                    return;
                }else if (imageUri==null) {
                    Toast.makeText(getApplicationContext(),"사진을 지정해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    str1=email.getText().toString();
                    str2=pw.getText().toString();
                    str3=name.getText().toString();

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(str1,str2)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    final String uid=task.getResult().getUser().getUid();
                                    FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                            String imageUrl=task.getResult().getUploadSessionUri().toString();

                                            UserModel user=new UserModel();
                                            user.userName=str3;
                                            user.profileImageUrl=imageUrl;

                                            FirebaseDatabase.getInstance().getReference().child("user").child(uid).setValue(user);
                                        }
                                    });
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
       if(requestCode==PICK_FROM_ALBUM&&resultCode==RESULT_OK){
           profile.setImageURI(data.getData()); //가운데 뷰 변경
           imageUri=data.getData(); //이미지 경로 원본
       }
    }
}
