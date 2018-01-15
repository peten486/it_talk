package project001.itcore.it_talk.model;

import android.os.Build;

import java.util.UUID;

/**
 * Created by peten on 2017. 7. 14..
 */

public class ChatUser {
    private String id;
    private String nickName;
    private ChatMessage lastMessage;

    public String getId(){return id;}
    public String getNickName(){return nickName;}
    public ChatMessage getLastMessage(){return lastMessage;}

    public void setId(String id){ this.id = id; }
    public void setNickName(String nickName) { this.nickName = nickName;}
    public void setLastMessage(ChatMessage lastMessage){this.lastMessage = lastMessage;}

    public ChatUser(String nickName, ChatMessage lastMessage){
        id = getDeviceSerialNumber();
        this.lastMessage = lastMessage;
        this.nickName = nickName;
    }

    public ChatUser(String nickName){
        id = getDeviceSerialNumber();
        this.nickName = nickName;
    }

    public void setDeviceId(){
        this.id = getDeviceSerialNumber();
    }

    public ChatUser(String id,String nickName){
        if(id == null){
            this.id = getDeviceSerialNumber();
        }
        else {
            this.id = id;
        }

        if(nickName == null) {
            this.nickName = UUID.randomUUID().toString().replace("-","");
        } else {
            this.nickName = nickName;
        }
    }

    public ChatUser(){
        nickName = UUID.randomUUID().toString().replace("-","");
        id = getDeviceSerialNumber();
        if( id.equals("unknown")){
            id = UUID.randomUUID().toString().replace("-","");
        }
    }

    // 안드로이드 2.3 이후부터만 제공하는 고유번호
    private static String getDeviceSerialNumber() {
        try {
            return (String) Build.class.getField("SERIAL").get(null);
        } catch (Exception ignored) {
            return null;
        }
    }

}
