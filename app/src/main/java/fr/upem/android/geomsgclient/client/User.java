package fr.upem.android.geomsgclient.client;

/**
 * Created by phm on 24/01/2017.
 */

public class User {
    private final String username;
    private double distance;

    public User(String username, double distance) {
        this.username = username;
        this.distance = distance;
    }

    public String getUsername() {
        return username;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", distance=" + distance +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        User user = (User) obj;
        return user.username.equals(username);
    }
}
