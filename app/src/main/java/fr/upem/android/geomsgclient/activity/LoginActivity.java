package fr.upem.android.geomsgclient.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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
    private Button login_button;
    private Button register_button;
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

        if (saveLogin) {
            username.setText(login_preferences.getString("username", ""), TextView.BufferType.EDITABLE);
            password.setText(login_preferences.getString("password", ""), TextView.BufferType.EDITABLE);
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
        if(Singleton.getInstance().getUserId() != null){
            changeActivity();
            LoginActivity.this.finish();
        }

        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    public void onLogin(View v) {
        if (username.getText().toString().trim().isEmpty() || username == null) {
            createAlertDialog("Name cannot be empty, please try again.");
            return;
        }
        login_prefs_editor.clear();
        if (save_login_checkBox.isChecked()) {
            login_prefs_editor.putBoolean("saveLogin", true);
            login_prefs_editor.putString("username", username.getText().toString());
            login_prefs_editor.putString("password", password.getText().toString());
        } else {
            login_prefs_editor.putBoolean("saveLogin", false);
        }
        login_prefs_editor.apply();
        login(username.getText().toString().trim());
    }

    public void onRegister(View v) {
        if (username.getText().toString().trim().isEmpty() || username == null) {
            createAlertDialog("Name cannot be empty, please try again.");
            return;
        }
        login_prefs_editor.clear();
        if (save_login_checkBox.isChecked()) {
            login_prefs_editor.putBoolean("saveLogin", true);
            login_prefs_editor.putString("username", username.getText().toString());
            login_prefs_editor.putString("password", password.getText().toString());
        } else {
            login_prefs_editor.putBoolean("saveLogin", false);
        }
        login_prefs_editor.apply();
        register();
    }

    private void login(String userId) {
        connectServeur(userId);

        String jsonString = "{ username:" + username.getText().toString() + ", password:" + password.getText().toString() + "}";
        JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(jsonString);
            socket.emit("new connection", jsonObj);
        } catch (JSONException e) {
            Log.e("GeomsgClient", "Could not parse malformed JSON: \"" + jsonString + "\"");
            e.printStackTrace();
        }
    }

    private void connectServeur(String userId) {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.query = "userId=" + userId;
            socket = IO.socket(serverAddress, options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on("connection_val", onConnected);
        socket.on("register_val", onRegistered);
        socket.connect();
        Location location = new Location("dummyprovider");
        location.setLatitude(10.);
        location.setLongitude(10.);
        Singleton.getInstance().init(socket, userId, location, serverAddress);
    }


    private Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        if(data.getBoolean("connected")){
                            changeActivity();
                            LoginActivity.this.finish();
                        }
                        else{
                            createAlertDialog("invalid email or password");
                        }
                    } catch (JSONException e) {
                        createAlertDialog("Login error");
                    }
                }
            });
        }
    };

    private void changeActivity(){
        Intent intent = new Intent(this, UserListActivity.class);
        startActivity(intent);
    }

    private Emitter.Listener onRegistered = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        if(data.getBoolean("registered")){
                            String jsonString = "{ username:" + username.getText().toString() + ", password:" + password.getText().toString() + "}";
                            JSONObject jsonObj = null;
                            try {
                                jsonObj = new JSONObject(jsonString);
                                socket.emit("new connection", jsonObj);

                            } catch (JSONException e) {
                                Log.e("GeomsgClient", "Could not parse malformed JSON: \"" + jsonString + "\"");
                                e.printStackTrace();
                            }
                        } else {
                            createAlertDialog("email already registered");
                        }
                    } catch (JSONException e) {
                        createAlertDialog("register error");
                    }
                }
            });
        }
    };

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

    // unused methode
    private boolean isEmailValid(String email) { return (email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")); }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void register() {

        connectServeur(username.getText().toString());
        if (isPasswordValid(password.getText().toString())) {
            String jsonString = "{ username:" + username.getText().toString() + ", password:" + password.getText().toString() + "}";
            JSONObject jsonObj = null;
            try {
                jsonObj = new JSONObject(jsonString);
                socket.emit("register", jsonObj);
            } catch (JSONException e) {
                Log.e("GeomsgClient", "Could not parse malformed JSON: \"" + jsonString + "\"");
                e.printStackTrace();
            }
        } else {
            createAlertDialog("Invalid password");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Button login = (Button) findViewById(R.id.loginButton);
                    login.setClickable(false);
                    Location location = new Location("dummyprovider");
                    location.setLatitude(2.);
                    location.setLongitude(2.);
                    createAlertDialog(getString(R.string.Location_needed));
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }

    }
}
