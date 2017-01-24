package fr.upem.android.geomsgclient.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import fr.upem.android.geomsgclient.R;
import fr.upem.android.geomsgclient.client.ChatAdapter;
import fr.upem.android.geomsgclient.client.User;
import fr.upem.android.geomsgclient.client.UserListAdapter;

public class UserListActivity extends AppCompatActivity {
    private ListView userListView;
    private UserListAdapter userListAdapter;
    private ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        users = new ArrayList<>();
        userListView = (ListView) findViewById(R.id.userListView);
        userListAdapter = new UserListAdapter(this, users);
        userListView.setAdapter(userListAdapter);
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
        addUser("Tameh");
    }

    private void addUser(String user) {
        User userObj = new User(user, -40., 80.);
        users.add(userObj);
        if (userListAdapter != null) {
            userListAdapter.notifyDataSetChanged();
        }
    }
}
