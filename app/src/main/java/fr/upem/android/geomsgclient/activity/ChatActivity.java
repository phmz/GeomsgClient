package fr.upem.android.geomsgclient.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fr.upem.android.geomsgclient.R;
import fr.upem.android.geomsgclient.Singleton;
import fr.upem.android.geomsgclient.client.ChatAdapter;
import fr.upem.android.geomsgclient.client.Message;
import fr.upem.android.geomsgclient.client.MessageStatus;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity {
    private Socket socket;
    private String userId;
    private EditText messageText;
    private Location currentLocation;
    private ListView chatListView;
    private ChatAdapter chatAdapter;
    private ArrayList<Message> messages;
    private String correspondentId;
    private BroadcastReceiver airplaneBroadcast;
    private IntentFilter intentBroadcast;
    private ImageButton sendButton;

    private View.OnClickListener sendMsgOn =  new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String msg = messageText.getText().toString().trim();
            attemptSend(msg);
        }
    };

    private View.OnClickListener sendMsgOff = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder
                    .setTitle(R.string.airplane_title)
                    .setMessage(R.string.airplane_message)
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    private View.OnClickListener sendMsgReconnect = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder
                    .setTitle(R.string.Reconnection_title)
                    .setMessage(R.string.reconnection_message)
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };
    private final String ACTION__AIRPLANE_MODE =  "android.intent.action.AIRPLANE_MODE";

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

        Singleton.getInstance().getSocket().on("chat message", onUpdateChat);


        chatListView = (ListView) findViewById(R.id.chatListView);
        chatAdapter = new ChatAdapter(this, messages);
        chatListView.setAdapter(chatAdapter);

        sendButton = (ImageButton) findViewById(R.id.sendButton);
        if (Settings.System.getInt(this.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
            sendButton.setOnClickListener(sendMsgOff);
        } else{
            sendButton.setOnClickListener(sendMsgOn);
        }

        intentBroadcast = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        airplaneBroadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Si on a detecte un airplane mode changement
                if (ACTION__AIRPLANE_MODE.equals(intent.getAction())) {
                    if (Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
                        sendButton.setOnClickListener(sendMsgOff);
                    } else{
                        sendButton.setOnClickListener(sendMsgReconnect);
                    }
                    // Button will be reactivated when EVENT_RECONNECTING detected.
                }
            }
        };

        socket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("EVENT_RECONNECTING");
                String jsonString = "{ username:" + Singleton.getInstance().getUserId() +" }";
                JSONObject jsonObj = null;
                try {
                    jsonObj = new JSONObject(jsonString);
                } catch (JSONException e) {
                    Log.e("GeomsgClient", "Could not parse malformed JSON: \"" + jsonString + "\"");
                    e.printStackTrace();
                }

                socket.emit("reconnection", jsonObj);
                sendButton.setOnClickListener(sendMsgOn);
            }
        });
        registerReceiver(airplaneBroadcast, intentBroadcast);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(airplaneBroadcast);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(airplaneBroadcast, intentBroadcast);
        if (Settings.System.getInt(this.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
            sendButton.setOnClickListener(sendMsgOff);
        } else{
            sendButton.setOnClickListener(sendMsgOn);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
        String jsonString = "{fromUser:" + userId + ",toUser:" + correspondentId + ",message:\"" + message + "\"}";
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

    private Emitter.Listener onUpdateChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String userId = data.getString("userId");
                        if (userId == null || correspondentId == null) {
                            return;
                        }
                        if (correspondentId.equals(userId)) {
                            if (chatAdapter != null) {
                                chatAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };
}
