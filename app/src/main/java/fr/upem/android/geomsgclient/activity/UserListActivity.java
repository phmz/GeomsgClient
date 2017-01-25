package fr.upem.android.geomsgclient.activity;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import fr.upem.android.geomsgclient.R;
import fr.upem.android.geomsgclient.Singleton;
import fr.upem.android.geomsgclient.client.ChatAdapter;
import fr.upem.android.geomsgclient.client.User;
import fr.upem.android.geomsgclient.client.UserListAdapter;

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
        addUser("Tameh");

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshUserList();
            }
        });

    }

    private void startChatWithUser(int position) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userIdChatWith", userListAdapter.getItem(position).getUsername());
        startActivity(intent);
    }

    private void refreshUserList() {
        addUser("Olivier");
        swipeLayout.setRefreshing(false);
    }

    private void addUser(String user) {
        User userObj = new User(user, -40., 80.);
        Singleton.getInstance().getUsers().add(userObj);
        if (userListAdapter != null) {
            userListAdapter.notifyDataSetChanged();
        }

    }
}
