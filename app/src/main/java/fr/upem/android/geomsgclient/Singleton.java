package fr.upem.android.geomsgclient;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;

import fr.upem.android.geomsgclient.client.Message;
import fr.upem.android.geomsgclient.client.User;
import io.socket.client.Socket;

/**
 * Created by phm on 24/01/2017.
 */

public class Singleton {
    private static Singleton instance = null;
    private Socket socket;
    private ArrayList<User> users;
    private ArrayList<Message> messages;
    private String userId;
    private Location currentLocation;
    private String serverAddress;

    private Singleton() {
        users = new ArrayList<>();
        messages = new ArrayList<>();
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
    }

    public Socket getSocket() {
        return socket;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public ArrayList<Message> getMessages() {
        return messages;
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
}
