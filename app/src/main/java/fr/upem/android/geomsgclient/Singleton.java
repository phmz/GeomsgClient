package fr.upem.android.geomsgclient;

import android.location.Location;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import fr.upem.android.geomsgclient.client.Message;
import fr.upem.android.geomsgclient.client.MessageStatus;
import fr.upem.android.geomsgclient.client.User;
import io.socket.client.Socket;

/**
 * Created by phm on 24/01/2017.
 */

public class Singleton {
    private static Singleton instance = null;
    private Socket socket;
    private ArrayList<User> users;
    private HashMap<String, ArrayList<Message>> messages;
    private String userId;
    private boolean register = false;
    private boolean login = false;
    private Location currentLocation;
    private String serverAddress;
    public final static int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    private Singleton() {
    }

    public static Singleton getInstance(){
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    public void init(Socket socket, String userId, Location currentLocation, String serverAddress) {
        if (instance == null) {
            return;
        }
        this.socket = socket;
        this.userId = userId;
        this.currentLocation = currentLocation;
        this.serverAddress = serverAddress;
        messages = new HashMap<>();
        users = new ArrayList<>();
    }
    public void init(Socket socket, String userId,String serverAddress) {
        if (instance == null) {
            return;
        }
        this.socket = socket;
        this.userId = userId;
        this.currentLocation = null;
        this.serverAddress = serverAddress;
        messages = new HashMap<>();
        users = new ArrayList<>();
    }

    public Socket getSocket() {
        return socket;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public ArrayList<Message> getMessages(String correspondentId) {
        if(messages.get(correspondentId) == null) {
            messages.put(correspondentId, new ArrayList<Message>());
        }
        return messages.get(correspondentId);
    }

    public String getUserId() {
        return userId;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setLogin(boolean b){
        login = b;
    }

    public boolean getLogin(){ return login;}

    public void setRegister(boolean b){
        login = b;
    }

    public boolean getRegister(){ return register;}

    public void addMessage(String correspondentId, String message, int id) {
        Message msgObj = new Message(message, MessageStatus.SENT, id, new Date());
        ArrayList<Message> m = getMessages(correspondentId);
        Log.d("GeomsgClient", msgObj.toString());
        Log.d("GeomsgClient", msgObj.getMessageTime().toString());
        if (m.isEmpty() || !isSameDay(m.get(m.size() - 1).getMessageTime(), msgObj.getMessageTime())) {
            m.add(new Message("new date", MessageStatus.SENT, -1, new Date()));
        }
        m.add(msgObj);
    }

    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        return sdf.format(date1).equals(sdf.format(date2));
    }
}
