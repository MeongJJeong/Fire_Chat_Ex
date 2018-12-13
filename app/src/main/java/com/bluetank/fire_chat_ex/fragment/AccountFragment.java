package com.bluetank.fire_chat_ex.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimatedImageDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetank.fire_chat_ex.LoginActivity;
import com.bluetank.fire_chat_ex.R;
import com.bluetank.fire_chat_ex.model.UserModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment implements View.OnClickListener {

    Button btnEdt,btnLogout;
    ImageView imageView;
    TextView text,name;

    FirebaseAuth firebaseAuth;
    FirebaseUser userAuth;
    DatabaseReference firebaseDatabase;
    Animation animation;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_account,container,false);

        btnEdt=(Button)view.findViewById(R.id.frag_account_btn1);
        btnLogout=(Button)view.findViewById(R.id.frag_account_logout);
        imageView=(ImageView)view.findViewById(R.id.frag_account_image);
        text =(TextView)view.findViewById(R.id.frag_account_text);
        name=(TextView)view.findViewById(R.id.frag_account_name) ;

        animation=AnimationUtils.loadAnimation(getContext(),R.anim.swing);
        animation.setRepeatCount(2);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance().getReference();
        userAuth=FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase.getInstance().getReference().child("user").child(userAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(getActivity()==null){
                    return;
                }
                UserModel userModel=dataSnapshot.getValue(UserModel.class);
                Glide.with(AccountFragment.this)
                        .load(userModel.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(imageView);
                name.setText(userModel.userName);
                text.setText(userModel.comment);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imageView.setOnClickListener(this);
        btnEdt.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        LayoutInflater layoutInflater=getActivity().getLayoutInflater();
        View view=layoutInflater.inflate(R.layout.dialog_comment,null);
        final TextView textView=(TextView)view.findViewById(R.id.dialog_text_logout);
        final EditText editText=(EditText)view.findViewById(R.id.dialog_edt_comment); //다이얼로그에 edt

        if (v==imageView){
            //프로필 사진 바꾸는 코드 추가 예정
            Toast.makeText(getContext(),"안녕",Toast.LENGTH_SHORT).show();
        }
        if (v==btnEdt){

            editText.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);

            FirebaseDatabase.getInstance().getReference().child("user").child(userAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel userModel=dataSnapshot.getValue(UserModel.class);
                    editText.setText(userModel.comment);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Map<String,Object> stringObjectMap=new HashMap<>();
                    String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    stringObjectMap.put("comment",editText.getText().toString());
                    text.setText(editText.getText().toString());
                    FirebaseDatabase.getInstance().getReference().child("user").child(uid).updateChildren(stringObjectMap);

                }
            }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }
        if (v==btnLogout){

            editText.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);

            builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    firebaseAuth.signOut();
                    Intent intent=new Intent(getContext(),LoginActivity.class);
                    getActivity().finish();
                    startActivity(intent);


                }
            }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }
    }
}
