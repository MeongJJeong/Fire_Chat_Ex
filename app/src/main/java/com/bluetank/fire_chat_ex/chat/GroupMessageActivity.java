package com.bluetank.fire_chat_ex.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class GroupMessageActivity extends AppCompatActivity {
    Map<String,UserModel> users=new HashMap<>();  //이부분에 그거
    String destinationRoom;
    String uid;
    EditText editText;

   // private UserModel destinationUserModel;
    private DatabaseReference databaseReference;
    private  ValueEventListener valueEventListener;

    private RecyclerView recyclerView;
    private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm"); //대문자 HH는 24시 표기법

   List<ChatModel.Comment> comments=new ArrayList<>();

   int peopleCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        destinationRoom=getIntent().getStringExtra("destinationRoom");
        uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText=(EditText)findViewById(R.id.groupmessage_edt);

        FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //데이터가 배열 형식으로 지정
                for (DataSnapshot item:dataSnapshot.getChildren()){
                    users.put(item.getKey(),item.getValue(UserModel.class)); //저부분에 이거
                }
                init();
                recyclerView=(RecyclerView)findViewById(R.id.groupmessage_recycle);
                recyclerView.setAdapter(new GroupMessageRecyclerAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void init(){
        Button button=(Button)findViewById(R.id.groupmessage_sendbtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel.Comment comment=new ChatModel.Comment();
                comment.uid=uid;
                comment.message=editText.getText().toString();
                comment.time=ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        editText.setText(null);
                    }
                });
            }
        });
    }

    class GroupMessageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public GroupMessageRecyclerAdapter(){
           getMessageList();
        }

        void getMessageList(){  //메세지를 읽는 코드
            databaseReference=FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments");
            valueEventListener=databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //읽어들인 데이터는 이곳으로 이동
                    comments.clear();  //대화내용이 계속 쌓이기 때문에 초기화가 필요하다

                    Map<String,Object> readUserMap=new HashMap<>();
                    for(DataSnapshot item:dataSnapshot.getChildren()){
                        String key=item.getKey();
                        ChatModel.Comment comment_origin=item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_motify=item.getValue(ChatModel.Comment.class);
                        comment_motify.readUsers.put(uid,true); //메세지를 읽었는지 안읽었는지를 구별

                        readUserMap.put(key,comment_motify); //읽었다는 것 을 알 수 있음
                        comments.add(comment_origin);
                        //comments.add(item.getValue(ChatModel.Comment.class));
                    }

                    //comments 체크 입력 시작
                    if(comments.size() == 0){return;}//끝
                    if(!comments.get(comments.size()-1).readUsers.containsKey(uid)){
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("comments")
                                .updateChildren(readUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) { //readUser해쉬가 전달 될 경우 돌아가도록 callBack 구성

                                notifyDataSetChanged(); //데이터 갱신
                                recyclerView.scrollToPosition(comments.size()-1); //대화목록을 최신판으로 갱신, comment-1이 가장 최근 보낸 메세지
                            }
                        });
                    }else {
                        notifyDataSetChanged(); //데이터 갱신
                        recyclerView.scrollToPosition(comments.size()-1); //대화목록을 최신판으로 갱신, comment-1이 가장 최근 보낸 메세지
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message,viewGroup,false);

            return new GroupMessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            GroupMessageViewHolder messageViewHolder= (GroupMessageViewHolder) viewHolder;

            if (comments.get(i).uid.equals(uid)){  //내 uid일 경우
                messageViewHolder.textView_message.setText(comments.get(i).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rbubble3); //말풍선을 설정, 오른쪽 말풍선
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE); //내가 보내는 경우이기 때문에 프로필을 감춘다.
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                messageViewHolder.linearLayout_message.setGravity(Gravity.RIGHT);
                messageViewHolder.textView_counter_right.setVisibility(View.INVISIBLE);
                messageViewHolder.textView_counter_left.setVisibility(View.VISIBLE);
                setReadCounter(i,messageViewHolder.textView_counter_left);

            }else { //상대방이 보낸 메세지
                Glide.with(viewHolder.itemView.getContext())
                        .load(users.get(comments.get(i).uid).profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.image_profile);
                messageViewHolder.textView_name.setText(users.get(comments.get(i).uid) .userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.lbubble3);
                messageViewHolder.textView_message.setText(comments.get(i).message);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
                messageViewHolder.linearLayout_message.setGravity(Gravity.LEFT);
                messageViewHolder.textView_counter_right.setVisibility(View.VISIBLE);
                messageViewHolder.textView_counter_left.setVisibility(View.INVISIBLE);
                setReadCounter(i,messageViewHolder.textView_counter_right);
            }
            long unixTime=(long)comments.get(i).time;
            Date date=new Date(unixTime);

            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time=simpleDateFormat.format(date);
            messageViewHolder.textView_time.setText(time);
        }
        void setReadCounter(final int position, final TextView textView){  //전체 인원수를 물어보는 코드가 계속 진행되면 무리가감, 따라서 처음에만 물어보도록 수정

            if (peopleCount==0){
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String,Boolean> users= (Map<String, Boolean>) dataSnapshot.getValue(); //DB에 uid값과 true false값을 해쉬맵으로 받는 방식
                        peopleCount=users.size(); //인원수 저장
                        int count=peopleCount-comments.get(position).readUsers.size(); //전체 인원수 - 읽은 인원수
                        if(count>0){
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        }else {
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else { //이미 peopleCount값을 알기때문에 서버에 물어볼 필요가 없어진다.
                int count=peopleCount-comments.get(position).readUsers.size(); //전체 인원수 - 읽은 인원수
                if(count>0){
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                }else {
                    textView.setVisibility(View.INVISIBLE);
                }
            }

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class GroupMessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public TextView textView_time;
            public TextView textView_counter_left;
            public TextView textView_counter_right;
            public ImageView image_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public LinearLayout linearLayout_message;

            public GroupMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                textView_message=(TextView) itemView.findViewById(R.id.item_message_text_message);
                textView_name=(TextView) itemView.findViewById(R.id.item_message_text_name);
                image_profile=(ImageView) itemView.findViewById(R.id.item_message_image_profile);
                linearLayout_destination=(LinearLayout)itemView.findViewById(R.id.item_message_linear_destination);
                linearLayout_main=(LinearLayout)itemView.findViewById(R.id.item_message_linear_main);
                linearLayout_message=(LinearLayout)itemView.findViewById(R.id.item_message_linear_message);
                textView_time=(TextView)itemView.findViewById(R.id.item_message_text_time);
                textView_counter_right=(TextView)itemView.findViewById(R.id.item_message_count_right);
                textView_counter_left=(TextView)itemView.findViewById(R.id.item_message_count_left);
            }
        }
    }
}
