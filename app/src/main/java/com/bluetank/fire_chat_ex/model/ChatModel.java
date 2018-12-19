package com.bluetank.fire_chat_ex.model;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {
    //채팅 데이터 모델

    public Map<String,Boolean> users=new HashMap<>();     //채팅방의 유저들  //uid와 destinationUid를 모두 포함하는 방식
    public Map<String,Comment> comments=new HashMap<>(); //채팅방의 내용

    //단톡방일 경우 사용하는 변수
    public String roomName=null;            //방이름
    public String profileImageUrl=null;    //단톡방 프로필 이미지

    public static class Comment{
        public String uid;                                       //메세지를 보낸 유저 id
        public String message;                                  //메세지
        public Object time;                                      //timeStamp
        public Map<String,Object> readUsers=new HashMap<>();    //읽은 유저 데이터
    }
}
