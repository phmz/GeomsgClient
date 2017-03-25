package fr.upem.android.geomsgclient.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import fr.upem.android.geomsgclient.R;
import fr.upem.android.geomsgclient.Singleton;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoginActivity extends AppCompatActivity {

    private CheckBox save_login_checkBox;
    private SharedPreferences login_preferences;
    private SharedPreferences.Editor login_prefs_editor;
    private Boolean saveLogin;
    Button login_button;
    Button register_button;

    private EditText username;
    private EditText password;
    private Socket socket;
    private String serverAddress = "http://geomsgserver.herokuapp.com/";
    //private String serverAddress = "http://192.168.0.11:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        username = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        login_button = (Button) findViewById(R.id.loginButton);
        register_button = (Button) findViewById(R.id.registerButton);
        save_login_checkBox = (CheckBox) findViewById(R.id.saveCheckBox);

        login_preferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        login_prefs_editor = login_preferences.edit();
        saveLogin = login_preferences.getBoolean("saveLogin", false);

        if (saveLogin == true) {
            username.setText(login_preferences.getString("username", ""));
            password.setText(login_preferences.getString("password", ""));
            save_login_checkBox.setChecked(true);
        }

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin(v);
            }
        });
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegister(v);
            }
        });
    }

    public void onLogin(View v) {
        if (username.getText().toString().trim().isEmpty() || username == null) {
            createAlertDialog("Name cannot be empty, please try again.");
            return;
        }
        if (save_login_checkBox.isChecked()) {
            login_prefs_editor.putBoolean("saveLogin", true);
            login_prefs_editor.putString("username", username.toString());
            login_prefs_editor.putString("password", password.toString());
            login_prefs_editor.commit();
        } else {
            login_prefs_editor.clear();
            login_prefs_editor.commit();
        }
        login(username.getText().toString().trim());
    }

    public void onRegister(View v){
        if (username.getText().toString().trim().isEmpty() || username == null) {
            createAlertDialog("Name cannot be empty, please try again.");
            return;
        }
        if (save_login_checkBox.isChecked()) {
            login_prefs_editor.putBoolean("saveLogin", true);
            login_prefs_editor.putString("username", username.toString());
            login_prefs_editor.putString("password", password.toString());
            login_prefs_editor.commit();
        } else {
            login_prefs_editor.clear();
            login_prefs_editor.commit();
        }
        register();
    }

    private void login(String userId) {
        try {
            IO.Options options = new IO.Options();
            options.query = "userId=" + userId;
            socket = IO.socket(serverAddress, options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.connect();
        Location location = new Location("dummyprovider");
        location.setLatitude(2.);
        location.setLongitude(2.);
        Singleton.getInstance().init(socket, userId, location, serverAddress);

        //TODO : si login ok coté serveur ( mdp ok et user ok) alors continue
        socket.emit("new connection", userId, password.getText().toString());
        Intent intent = new Intent(this, UserListActivity.class);
        startActivity(intent);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "You are now connected to " + serverAddress, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "You disconnected from " + serverAddress, Toast.LENGTH_LONG).show();

                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    createAlertDialog("Error: cannot connect");
                }
            });
        }
    };

    private void createAlertDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean isEmailValid(String email) {
        return (email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"));
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void register(){
        if(isEmailValid(username.getText().toString()) && isPasswordValid(password.getText().toString())){
            //TODO : try add user to serveur then connect
        }
        login(username.getText().toString());
    }
}
