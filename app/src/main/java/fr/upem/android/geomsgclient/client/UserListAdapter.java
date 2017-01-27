package fr.upem.android.geomsgclient.client;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fr.upem.android.geomsgclient.R;
import fr.upem.android.geomsgclient.Singleton;

/**
 * Created by phm on 24/01/2017.
 */

public class UserListAdapter extends BaseAdapter {
    private static class UserHolder {
        private TextView userTextView;
        private TextView messageTextView;
        private TextView dateTextView;
        private TextView distanceTextView;
    }

    private final Context context;
    private ArrayList<User> users;

    public UserListAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("GeomsgClient", "getView Start");
        User user = users.get(position);
        UserHolder userHolder;
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user, null, false);
            userHolder = new UserHolder();
            userHolder.userTextView = (TextView) convertView.findViewById(R.id.userTextView);
            userHolder.messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            userHolder.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            userHolder.distanceTextView = (TextView) convertView.findViewById(R.id.distanceTextView);
            convertView.setTag(userHolder);
        } else {
            userHolder = (UserHolder) convertView.getTag();
        }
        userHolder.userTextView.setText(user.getUsername());
        ArrayList<Message> messages = Singleton.getInstance().getMessages(user.getUsername());
        if (messages.isEmpty()) {
            userHolder.messageTextView.setText("Nothing there yet.");
            userHolder.dateTextView.setText(hourFormat.format(new Date()));
        } else {
            Message message = messages.get(messages.size() - 1);
            userHolder.messageTextView.setText(message.getMessage());
            userHolder.dateTextView.setText(hourFormat.format(message.getMessageTime()));
        }

        userHolder.distanceTextView.setText((new DecimalFormat("##.#").format(user.getDistance())) + " km");
        return convertView;
    }
}
