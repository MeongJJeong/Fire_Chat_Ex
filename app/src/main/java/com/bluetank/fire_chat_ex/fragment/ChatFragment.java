package com.bluetank.fire_chat_ex.fragment;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.shape.CutCornerTreatment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluetank.fire_chat_ex.R;
import com.bluetank.fire_chat_ex.model.ChatModel;
import com.bluetank.fire_chat_ex.model.UserModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChatFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_chat,container,false);

        RecyclerView recyclerView=(RecyclerView)view.findViewById(R.id.frag_chat_recycler);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels=new ArrayList<>();
        private String uid;
        public ChatRecyclerViewAdapter() {
            uid=FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("user/"+uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    for (DataSnapshot item:dataSnapshot.getChildren()){
                        chatModels.add(item.getValue(ChatModel.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat,viewGroup,false);
            
            
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            final CustomViewHolder customViewHolder=(CustomViewHolder)viewHolder;
            String detinationUid=null;

            for(String user:chatModels.get(i).users.keySet()){  //챗방에 유저들 채크
                if(!user.equals(uid)){
                    //내가 아닌 사람들 추출
                    detinationUid=user;
                }
            }
            FirebaseDatabase.getInstance().getReference().child("user").child(detinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserModel userModel=dataSnapshot.getValue(UserModel.class);
                    Glide.with(customViewHolder.itemView.getContext())
                            .load(userModel.profileImageUrl)
                            .apply(new RequestOptions().circleCrop())
                            .into(customViewHolder.imageView);
                    customViewHolder.textView_title.setText(userModel.userName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
 
                }
            });

            Map<String,ChatModel.Comment> commentMap=new TreeMap<>(Collections.reverseOrder());  //메세지를 내림차순으로 정렬
            commentMap.putAll(chatModels.get(i).comments);
            String lastMessageKey=(String) commentMap.keySet().toArray()[0]; //0번째 메세지의 키값을 추출
            customViewHolder.textView_last.setText(chatModels.get(i).comments.get(lastMessageKey).message);

        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView_title,textView_last;

            public CustomViewHolder(View view) {
                super(view);

                imageView=(ImageView)view.findViewById(R.id.item_chat_image);
                textView_last=(TextView)view.findViewById(R.id.item_chat_talk);
                textView_title=(TextView)view.findViewById(R.id.item_chat_title);
            }
        }
    }
}
