package fr.upem.android.geomsgclient.activity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fr.upem.android.geomsgclient.R;
import fr.upem.android.geomsgclient.Singleton;
import fr.upem.android.geomsgclient.client.ChatAdapter;
import fr.upem.android.geomsgclient.client.User;
import fr.upem.android.geomsgclient.client.UserListAdapter;
import fr.upem.android.geomsgclient.utilities.Utilities;
import io.socket.emitter.Emitter;

public class UserListActivity extends AppCompatActivity {
    private ListView userListView;
    private UserListAdapter userListAdapter;
    private SwipeRefreshLayout swipeLayout;
    private LocationManager locationManager;
    private Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        userListView = (ListView) findViewById(R.id.userListView);
        userListAdapter = new UserListAdapter(this, Singleton.getInstance().getUsers());
        userListView.setAdapter(userListAdapter);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startChatWithUser(position);
            }
        });
        // TODO
        // ask server for user list
        // add user that are in range
        // then refreshUserList();
        Singleton.getInstance().getSocket().on("update list", onUpdateList);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshUserList();
            }
        });
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Utilities.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 100, locationListener);
        Singleton.getInstance().setCurrentLocation(locationManager.getLastKnownLocation("network"));
    }

    private void startChatWithUser(int position) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userIdChatWith", userListAdapter.getItem(position).getUsername());
        startActivity(intent);
    }

    private void refreshUserList() {
        Singleton.getInstance().getSocket().emit("request list", Singleton.getInstance().getUserId());
        swipeLayout.setRefreshing(false);
    }

    private void addUser(User userObj) {
        // TODO
        // do it in singleton ?
        if (Singleton.getInstance().getUsers().contains(userObj)) {
            Log.d("GeomsgClient", userObj.getUsername() + " is already in the list, setting new distance");
            int i = Singleton.getInstance().getUsers().indexOf(userObj);
            Log.d("GeomsgClient", "old distance " + (new DecimalFormat("##.#").format(Singleton.getInstance().getUsers().get(i).getDistance())) + " km");
            Log.d("GeomsgClient", "new distance " + (new DecimalFormat("##.#").format(userObj.getDistance())) + " km");
            Singleton.getInstance().getUsers().get(i).setDistance(userObj.getDistance());
            Log.d("GeomsgClient", "new new distance " + (new DecimalFormat("##.#").format(Singleton.getInstance().getUsers().get(i).getDistance())) + " km");
        } else {
            Log.d("GeomsgClient", userObj.getUsername() + " is not in the list");
            Singleton.getInstance().getUsers().add(userObj);
        }
        if (userListAdapter != null) {
            userListAdapter.notifyDataSetChanged();
        }

    }

    private Emitter.Listener onUpdateList = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d("GeomsgClient", data.toString());
                    JSONArray users;
                    try {
                        users = data.getJSONArray("users");
                        Log.d("GeomsgClient", users.toString());
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);
                            String userId = user.getString("userId");
                            Double distance = user.getDouble("distance");
                            addUser(new User(userId, distance));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                   /* String username;
                    String distance;
                    try {
                        username = data.getString("username");
                        distance = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }*/

                }
            });
        }
    };


    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Singleton.getInstance().setCurrentLocation(location);
            String jsonString = "{name:" + Singleton.getInstance().getUserId() + ",latitude:" + Singleton.getInstance().getCurrentLocation().getLatitude() + ",longitude:" + Singleton.getInstance().getCurrentLocation().getLongitude() + "}";
            JSONObject jsonObj = null;
            try {
                jsonObj = new JSONObject(jsonString);
                Log.d("GeomsgClient", jsonObj.toString());
            } catch (JSONException e) {
                Log.e("GeomsgClient", "Could not parse malformed JSON: \"" + jsonString + "\"");
                e.printStackTrace();
            }

            Singleton.getInstance().getSocket().emit("update loc", jsonObj);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("GeomsgClient", "HEY IM HERE STATUS CHANGED");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("GeomsgClient", "HEY IM HERE PROVIDER ENABLED");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("GeomsgClient", provider + " HEY IM HERE PROVIDER DISABLED");
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Utilities.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        Singleton.getInstance().setCurrentLocation(locationManager.getLastKnownLocation("gps"));
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
