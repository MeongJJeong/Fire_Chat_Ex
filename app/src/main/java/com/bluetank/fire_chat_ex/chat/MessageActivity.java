package com.bluetank.fire_chat_ex.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.google.android.gms.tasks.OnSuccessListener;
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

public class MessageActivity extends AppCompatActivity {

    private String destinationUid; //대화상대 uid 주소
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    private Button btn;
    private EditText edt;
    private RecyclerView recyclerView;

    private String uid;
    private String chatRoomuid;
    private Toolbar toolbar;

    int peopleCount=0;

    //private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy.MM.dd HH:mm"); //날짜 포멧 설정
    private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm"); //대문자 HH는 24시 표기법

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid=FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구하는 uid
        destinationUid=getIntent().getStringExtra("destinationUid"); //채팅을 당하는 id

        FirebaseDatabase.getInstance().getReference().child("user").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel userModel=dataSnapshot.getValue(UserModel.class);
                setTitle(userModel.userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn=(Button)findViewById(R.id.message_btn);
        edt=(EditText)findViewById(R.id.message_edt);
        recyclerView=(RecyclerView)findViewById(R.id.message_recycle);

        toolbar=(Toolbar)findViewById(R.id.message_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_up);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edt.getText().toString().getBytes().length<=0){ //입력된 것이 없을경우 리턴
                    return;
                }else {
                    ChatModel chatModel=new ChatModel();
                    chatModel.users.put(uid,true);
                    chatModel.users.put(destinationUid,true);

                    if (chatRoomuid==null){
                        btn.setEnabled(false); //버튼을 잠시 비활성화
                        //database에 push를 통해 새로운 트리가 생성
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                checkChatRoom(); //콜백 메서드로 중복되는 채팅방 생성 방지
                            }
                        });
                    }else {

                        ChatModel.Comment comment=new ChatModel.Comment();
                        comment.uid=uid;
                        comment.message=edt.getText().toString();
                        comment.time=ServerValue.TIMESTAMP; //Firebase에서 지원하는 method
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomuid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                edt.setText(null);  //입력부 초기화
                            }
                        });
                    }
                }
            }
        });
        checkChatRoom();

    }
    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            //orderByChild가 중복을 채크하는 코드
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item:dataSnapshot.getChildren()){
                    ChatModel chatModel=item.getValue(ChatModel.class);
                        if(chatModel.users.containsKey(destinationUid)){
                            chatRoomuid=item.getKey(); //방에 대한 uid값
                            btn.setEnabled(true); //비활성화된 버튼을 다시 활성화
                            recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                            recyclerView.setAdapter(new RecyclerViewAdapter());
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comment> comments;
        UserModel user;

        public RecyclerViewAdapter(){
            comments=new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("user").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user=dataSnapshot.getValue(UserModel.class);
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        void getMessageList(){  //메세지를 읽는 코드
            databaseReference=FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomuid).child("comments");
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
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomuid).child("comments")
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

            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            MessageViewHolder messageViewHolder= (MessageViewHolder) viewHolder;

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
                        .load(user.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.image_profile);

                messageViewHolder.textView_name.setText(user.userName);
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
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomuid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
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

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public TextView textView_time;
            public TextView textView_counter_left;
            public TextView textView_counter_right;
            public ImageView image_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public LinearLayout linearLayout_message;


            public MessageViewHolder(View view) {
                super(view);
                textView_message=(TextView) view.findViewById(R.id.item_message_text_message);
                textView_name=(TextView) view.findViewById(R.id.item_message_text_name);
                image_profile=(ImageView) view.findViewById(R.id.item_message_image_profile);
                linearLayout_destination=(LinearLayout)view.findViewById(R.id.item_message_linear_destination);
                linearLayout_main=(LinearLayout)view.findViewById(R.id.item_message_linear_main);
                linearLayout_message=(LinearLayout)view.findViewById(R.id.item_message_linear_message);
                textView_time=(TextView)view.findViewById(R.id.item_message_text_time);
                textView_counter_right=(TextView)view.findViewById(R.id.item_message_count_right);
                textView_counter_left=(TextView)view.findViewById(R.id.item_message_count_left);

            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(databaseReference!=null){
            databaseReference.removeEventListener(valueEventListener); //뒤로가기 키 누르면 읽고있는 상태 해제
        }
        finish();
        overridePendingTransition(R.anim.fromtop,R.anim.tobottom);
    }
}
