package com.bluetank.fire_chat_ex.model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    public Map<String,Boolean> users=new HashMap<>(); //채팅방의 유저들  //uid와 destinationUid를 모두 포함하는 방식
    public Map<String,Comment> comments=new HashMap<>(); //채팅방의 내용

    public static class Comment{
        public String uid;
        public String message;
        public Object time;
    }

//    public String uid;
//    public String destinationUid;
}
