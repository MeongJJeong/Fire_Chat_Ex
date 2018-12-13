package com.bluetank.fire_chat_ex.fragment;

//import android.app.Fragment;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluetank.fire_chat_ex.R;
import com.bluetank.fire_chat_ex.chat.MessageActivity;
import com.bluetank.fire_chat_ex.model.UserModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_people,container,false);

        RecyclerView recyclerView=(RecyclerView)view.findViewById(R.id.frag_people_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());

        return view;
    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;

        public PeopleFragmentRecyclerViewAdapter(){
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
            View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friend,viewGroup,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {

            Glide.with(viewHolder.itemView.getContext())
                    .load(userModels.get(i).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)viewHolder).image);

            ((CustomViewHolder) viewHolder).text.setText(userModels.get(i).userName);

            if (userModels.get(i).comment!=null){
                ((CustomViewHolder) viewHolder).comment.setText(userModels.get(i).comment);
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(view.getContext(),MessageActivity.class);
                    intent.putExtra("destinationUid", userModels.get(i).uid);
                    ActivityOptions activityOptions=ActivityOptions.makeCustomAnimation(view.getContext(),R.anim.frombottom,R.anim.totop);
                    startActivity(intent,activityOptions.toBundle());
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

            public CustomViewHolder(View view) {
                super(view);

                image=(ImageView) view.findViewById(R.id.item_friend_image);
                text=(TextView) view.findViewById(R.id.item_friend_text);
                comment=(TextView) view.findViewById(R.id.item_friend_comment);
            }
        }
    }
}
