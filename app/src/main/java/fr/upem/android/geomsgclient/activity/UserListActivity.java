package fr.upem.android.geomsgclient.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import fr.upem.android.geomsgclient.R;
import fr.upem.android.geomsgclient.Singleton;
import fr.upem.android.geomsgclient.client.User;
import fr.upem.android.geomsgclient.client.UserListAdapter;
import io.socket.emitter.Emitter;

public class UserListActivity extends AppCompatActivity implements LocationListener {
    private ListView userListView;
    private UserListAdapter userListAdapter;
    private SwipeRefreshLayout swipeLayout;
    private NotificationCompat.Builder mBuilder;
    private int notifCounter = 0;

    /*localisation*/
    private LocationManager lm;

    private final static String GROUP_KEY_NOTIF = "GROUP_KEY_NOTIF";

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

        updateLoc();
        // Careful here, server crash if location is null
        Singleton.getInstance().getSocket().on("update list", onUpdateList);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshUserList();
            }
        });

        // if we receive a message
        Singleton.getInstance().getSocket().on("chat message", onNewMessage);
        // if we are typing
        Singleton.getInstance().getSocket().on("typing", onTyping);
        // if we are not typing anymore
        Singleton.getInstance().getSocket().on("stop typing", onStopTyping);

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name);
    }

    @Override
    public void onPause() {
        super.onPause();
        lm.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(final Location location) {
        Singleton.getInstance().setCurrentLocation(location);
        final StringBuilder msg = new StringBuilder("lat : ");
        msg.append(location.getLatitude());
        msg.append(" lng : ");
        msg.append(location.getLongitude());

        //afficher la position
        Toast.makeText(this, msg.toString(), Toast.LENGTH_SHORT).show();
        updateLoc();
    }

    @Override
    public void onProviderDisabled(final String provider) {
        //do nothing
    }

    @Override
    public void onProviderEnabled(final String provider) {
        //do nothing
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        //do nothing
    }

    private void updateLoc() {
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

    private void startChatWithUser(int position) {
        notifCounter = 0;
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userIdChatWith", userListAdapter.getItem(position).getUsername());
        startActivity(intent);
    }

    private void refreshUserList() {
        Singleton.getInstance().getSocket().emit("request list", Singleton.getInstance().getUserId());
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUserList();

        //GPS
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            // TODO REQUEST UPDATE LOCATION LESS OFTEN
            //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
        }
        // TODO REQUEST UPDATE LOCATION LESS OFTEN
        //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0,this);
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
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //JSONObject data = (JSONObject) args[0];
                    JSONObject data = (JSONObject) args[0];
                    refreshUserList();
                    try {
                        String userId = data.getString("userId");
                        String message = data.getString("message");
                        // Because clicking the notification opens a new ("special") activity, there's
                        // no need to create an artificial back stack.
                        Intent resultIntent;

                        if (notifCounter == 0) {
                            notifCounter++;
                            resultIntent = new Intent(UserListActivity.this, ChatActivity.class);
                            resultIntent.putExtra("userIdChatWith", userId);
                            mBuilder.setContentTitle(userId)
                                    .setContentText(message)
                                    .setAutoCancel(true)
                                    .setVibrate(new long[]{1000, 1000})
                                    .setGroup(GROUP_KEY_NOTIF)
                                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                        } else {
                            notifCounter++;
                            resultIntent = new Intent(UserListActivity.this, UserListActivity.class);
                            NotificationCompat.InboxStyle notificationStyle = new NotificationCompat.InboxStyle();
                            mBuilder.setContentTitle(notifCounter + " new messages")
                                    .setStyle(notificationStyle.setBigContentTitle(notifCounter + " new messages"))
                                    .setContentText("");
                        }
                        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                                UserListActivity.this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(resultPendingIntent);
                        // Sets an ID for the notification
                        int mNotificationId = 001;
                        // Gets an instance of the NotificationManager service
                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        // Builds the notification and issues it.
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());
                        Singleton.getInstance().addMessage(userId, message, 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (userListAdapter != null) {
                        userListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO
                    // inform server that we are typing
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO
                    // inform server that we are not typing anymore
                }
            });
        }
    };
}
