package fr.upem.android.geomsgclient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import fr.upem.android.geomsgclient.R;
import fr.upem.android.geomsgclient.Singleton;
import fr.upem.android.geomsgclient.client.User;
import fr.upem.android.geomsgclient.client.UserListAdapter;
import io.socket.emitter.Emitter;

public class UserListActivity extends AppCompatActivity {
    private ListView userListView;
    private UserListAdapter userListAdapter;
    private SwipeRefreshLayout swipeLayout;


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
