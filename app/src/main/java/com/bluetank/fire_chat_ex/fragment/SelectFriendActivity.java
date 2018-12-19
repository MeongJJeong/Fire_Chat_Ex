package com.bluetank.fire_chat_ex.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetank.fire_chat_ex.R;
import com.bluetank.fire_chat_ex.model.ChatModel;
import com.bluetank.fire_chat_ex.model.UserModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM =10 ;
    private ChatModel chatModel=new ChatModel();

    private ImageView imageView;
    private Button button;
    private Toolbar toolbar;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.selectFriend_recyclerview);
        recyclerView.setAdapter(new SelectFriendRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        toolbar=(Toolbar)findViewById(R.id.selectFriend_toolbar);
        setSupportActionBar(toolbar);
        setTitle("친구 선택");

        button=(Button)findViewById(R.id.selectFriend_btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(SelectFriendActivity.this).create();
                LayoutInflater layoutInflater=LayoutInflater.from(SelectFriendActivity.this);
                View view=layoutInflater.inflate(R.layout.dialog_group,null);

                imageView=(ImageView)view.findViewById(R.id.group_image_profile);        //단톡방 이미지
                final EditText editText=(EditText)view.findViewById(R.id.group_edt_name); //단톡방 이름입력
                final Button button=(Button)view.findViewById(R.id.group_btn_ok);         //alertdialog의 확인버튼

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Intent.ACTION_PICK);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        startActivityForResult(intent,PICK_FROM_ALBUM);
                    }
                });

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(editText.getText().length()==0){
                            Toast.makeText(getApplicationContext(),"채팅방 이름을 설정해 주세요.",Toast.LENGTH_SHORT).show();
                            return;
                        }else if (imageUri==null) {
                            Toast.makeText(getApplicationContext(),"사진을 지정해주세요.",Toast.LENGTH_SHORT).show();
                            return;
                        }else {
                            button.setEnabled(false);

                            final String str1=editText.getText().toString();
                            final String myUid=FirebaseAuth.getInstance().getCurrentUser().getUid();

                            final StorageReference profileImageRef=FirebaseStorage.getInstance().getReference().child("roomImages").child(str1);
                            profileImageRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    Task<Uri> uriTask=profileImageRef.getDownloadUrl();
                                    while (!uriTask.isSuccessful());
                                    Uri downloadUrl=uriTask.getResult();
                                    String imageUrl=String.valueOf(downloadUrl);

                                    chatModel.users.put(myUid,true);
                                    chatModel.profileImageUrl=imageUrl;
                                    chatModel.roomName=str1;

                                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getApplicationContext(),"채팅방이 생성되었습니다.",Toast.LENGTH_SHORT).show();
                                            button.setEnabled(true);
                                            finish();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
                alertDialog.setView(view);
                alertDialog.show();
            }
        });
    }

    class SelectFriendRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;

        public SelectFriendRecyclerViewAdapter(){
            userModels =new ArrayList<>();
            final String myUid=FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
            FirebaseDatabase.getInstance().getReference().child("user").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userModels.clear(); //초기화 해줘야 중복이 안발생

                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                        UserModel userModel=snapshot.getValue(UserModel.class);
                        if(userModel.uid.equals(myUid)){  //내 uid와 같을경우 목록에 추가하지않고 진행
                            continue;
                        }
                        userModels.add(userModel);
                    }
                    notifyDataSetChanged();//새로고침
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friend_select,viewGroup,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {

            Glide.with(viewHolder.itemView.getContext())
                    .load(userModels.get(i).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)viewHolder).image);
            ((CustomViewHolder) viewHolder).text.setText(userModels.get(i).userName);


            ((CustomViewHolder) viewHolder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked==true){ //체크 된 상태
                        chatModel.users.put(userModels.get(i).uid,true);

                    }else {              //체크 취소 상태
                        chatModel.users.remove(userModels.get(i));

                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView image;
            public TextView text;
            public TextView comment;
            public CheckBox checkBox;

            public CustomViewHolder(View view) {
                super(view);

                image=(ImageView) view.findViewById(R.id.item_friend_image);
                text=(TextView) view.findViewById(R.id.item_friend_text);
                comment=(TextView) view.findViewById(R.id.item_friend_comment);
                checkBox=(CheckBox)view.findViewById(R.id.item_friend_cbx);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        if(requestCode==PICK_FROM_ALBUM&&resultCode==RESULT_OK){
            imageView.setImageURI(data.getData()); //가운데 뷰 변경
            imageUri=data.getData(); //이미지 경로 원본
        }
    }
}
