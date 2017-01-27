package fr.upem.android.geomsgclient.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fr.upem.android.geomsgclient.R;
import fr.upem.android.geomsgclient.Singleton;
import fr.upem.android.geomsgclient.client.ChatAdapter;
import fr.upem.android.geomsgclient.client.Message;
import fr.upem.android.geomsgclient.client.MessageStatus;
import fr.upem.android.geomsgclient.utilities.Utilities;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {
    private Socket socket;
    private String userId;
    private EditText messageText;
    private String serverAddress;
    private Location currentLocation;
    private ListView chatListView;
    private ChatAdapter chatAdapter;
    private ArrayList<Message> messages;
    private String correspondentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        correspondentId = getIntent().getStringExtra("userIdChatWith");

        messageText = (EditText) findViewById(R.id.messageText);

        socket = Singleton.getInstance().getSocket();
        messages = Singleton.getInstance().getMessages(correspondentId);
        currentLocation = Singleton.getInstance().getCurrentLocation();
        userId = Singleton.getInstance().getUserId();



        // if we receive a message
        socket.on("chat message", onNewMessage);
        // if we are typing
        socket.on("typing", onTyping);
        // if we are not typing anymore
        socket.on("stop typing", onStopTyping);


        chatListView = (ListView) findViewById(R.id.chatListView);
        chatAdapter = new ChatAdapter(this, messages);
        chatListView.setAdapter(chatAdapter);

    }

    private void addMessage(String message, int id) {
        Message msgObj = new Message(message, MessageStatus.SENT, id, new Date());

        Log.d("GeomsgClient", msgObj.toString());
        Log.d("GeomsgClient", msgObj.getMessageTime().toString());
        if (messages.isEmpty() || !isSameDay(messages.get(messages.size() - 1).getMessageTime(), msgObj.getMessageTime())) {
            messages.add(new Message("new date", MessageStatus.SENT, -1, new Date()));
        }
        messages.add(msgObj);
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        return sdf.format(date1).equals(sdf.format(date2));
    }

    private void attemptSend(String message) {
        if (message.isEmpty()) {
            return;
        }
        messageText.setText("");
        String jsonString = "{fromUser:"+userId+",toUser:" + correspondentId + ",message:\"" + message + "\"}";
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(jsonString);
            Log.d("GeomsgClient", jsonObj.toString());
        } catch (JSONException e) {
            Log.e("GeomsgClient", "Could not parse malformed JSON: \"" + jsonString + "\"");
            e.printStackTrace();
        }

        socket.emit("chat message", jsonObj);
        //socket.emit("chat message", message);
        addMessage(message, 0);
    }

    public void sendMessage(View v) {
        String msg = messageText.getText().toString().trim();
        //displayToast(msg);
        attemptSend(msg);
        //attemptSend("Serie volui potui iis paulo uno primo nulli est. Liberet effingo im gi quantum id ad facilem.");
        //attemptSend("Persuasi fortasse aliaeque ex du supponit periculi.");
        //attemptSend("Abducendam imo his mem inchoandum geometriam conjunctam credidisse. Tur fal amen vix ipsa cum suae. An ut cognosco earundem credimus. De simus si vi utrum aliud omnis istas. Judicem studiis ac proponi nemoque ex. De quoties ex virorum effingo. De totamque de occurret an credenda referrem.");

    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //JSONObject data = (JSONObject) args[0];
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String userId = data.getString("userId");
                        String message = data.getString("message");
                        addMessage(message, 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
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
