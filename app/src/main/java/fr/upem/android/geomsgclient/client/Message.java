package fr.upem.android.geomsgclient.client;

import java.util.Date;

/**
 * Created by phm on 23/01/2017.
 */

public class Message {

    private final String message;
    private MessageStatus status;
    private final int userId;
    private final Date messageTime;
    private boolean planified;

    public Message(String message, MessageStatus status, int userId, Date messageTime) {
        this.message = message;
        this.status = status;
        this.userId = userId;
        this.messageTime = messageTime;
    }

    public Message(String message, MessageStatus status, int userId, Date messageTime, boolean planified) {
        this.message = message;
        this.status = status;
        this.userId = userId;
        this.messageTime = messageTime;
        this.planified=planified;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getUserId() {
        return userId;
    }

    public Date getMessageTime() {
        return messageTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", status=" + status +
                ", userId=" + userId +
                ", messageTime=" + messageTime +
                '}';
    }

    public boolean isPlanified() {
        return planified;
    }

    public void setPlanified(boolean planified) {
        this.planified = planified;
    }
}
