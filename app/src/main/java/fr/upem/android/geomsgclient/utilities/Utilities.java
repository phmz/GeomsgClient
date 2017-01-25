package fr.upem.android.geomsgclient.utilities;

import android.location.Location;

/**
 * Created by phm on 24/01/2017.
 */

public class Utilities {

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    public static String distanceBetween(double latitude, double longitude) {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        //location.distanceTo()
        return "2 km";
    }
}
