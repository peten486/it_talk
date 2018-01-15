package project001.itcore.it_talk.model;

import java.util.UUID;

/**
 * Created by peten on 2017. 7. 10..
 */

public class ChatMessage {
    private String id;
    private String message;
    private String userId;
    private String otherUserId;
    private String dateTime;
    private int type;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public int getType(){return this.type;}
    public String getDate() {
        return dateTime;
    }
    public void setDate(String dateTime) {
        this.dateTime = dateTime;
    }
    public String getOtherUserId(){return otherUserId;}
    public void setOtherUserId(String otherUserId){this.otherUserId = otherUserId;}

    public ChatMessage(){ id = UUID.randomUUID().toString().replace("-",""); }

    public ChatMessage(String userId, String otherUserId, String message, String dateTime, int type){
        id = UUID.randomUUID().toString().replace("-","");
        this.userId = userId;
        this.otherUserId = otherUserId;
        this.message = message;
        this.dateTime = dateTime;
        this.type = type;
    }
}
