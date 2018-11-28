package com.bluetank.fire_chat_ex.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bluetank.fire_chat_ex.R;
import com.bluetank.fire_chat_ex.model.ChatModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid; //대화상대 uid 주소
    private Button btn;
    private EditText edt;
    private RecyclerView recyclerView;

    private String uid;
    private String chatRoomuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid=FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구하는 uid
        destinationUid=getIntent().getStringExtra("destinationUid"); //채팅을 당하는 id
        btn=(Button)findViewById(R.id.message_btn);
        edt=(EditText)findViewById(R.id.message_edt);
        recyclerView=(RecyclerView)findViewById(R.id.message_recycle);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomuid).child("comments").push().setValue(comment);
                    edt.setText(null);
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
        public RecyclerViewAdapter(){
            comments=new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomuid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //읽어들인 데이터는 이곳으로 이동
                    comments.clear();  //대화내용이 계속 쌓이기 때문에 초기화가 필요하다

                    for(DataSnapshot item:dataSnapshot.getChildren()){
                        comments.add(item.getValue(ChatModel.Comment.class));
                    }
                    notifyDataSetChanged(); //데이터 갱신
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

            ((MessageViewHolder)viewHolder).textView_message.setText(comments.get(i).message);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;

            public MessageViewHolder(View view) {
                super(view);
                textView_message=view.findViewById(R.id.item_message_text);
            }
        }
    }
}
