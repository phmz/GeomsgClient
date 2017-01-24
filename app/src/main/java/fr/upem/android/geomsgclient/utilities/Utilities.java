package fr.upem.android.geomsgclient.utilities;

import android.location.Location;

/**
 * Created by phm on 24/01/2017.
 */

public class Utilities {

    public static String distanceBetween(double latitude, double longitude) {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        //location.distanceTo()
        return "2 km";
    }
}
