package fr.upem.android.geomsgclient.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

    private EditText nameEditText;
    private Socket socket;
    private String serverAddress = "http://geomsgserver.herokuapp.com/";
    //private String serverAddress = "http://192.168.0.15:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
    }

    public void onLogin(View v) {
        if (nameEditText.getText().toString().trim().isEmpty() || nameEditText == null) {
            createAlertDialog("Name cannot be empty, please try again.");
            return;
        }
        login(nameEditText.getText().toString().trim());

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

        socket.emit("new connection", Singleton.getInstance().getUserId());
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
}
