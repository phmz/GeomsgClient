package fr.upem.android.geomsgclient.client;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import fr.upem.android.geomsgclient.R;

/**
 * Created by phm on 23/01/2017.
 */

public class ChatAdapter extends BaseAdapter {

    private static class MessageHolderSent {
        private TextView messageTextView;
        private TextView dateTextView;
        private ImageView messageStatus;
    }

    private static class MessageHolderReceived {
        private TextView messageTextView;
        private TextView dateTextView;
    }

    private static class MessageHolderDate {
        private TextView dateTextView;
    }

    private final Context context;
    private ArrayList<Message> messages;

    public ChatAdapter(Context context, ArrayList<Message> messages) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = messages.get(position);
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        switch (message.getUserId()) {
            case -1:
                // DATE CASE
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
                MessageHolderDate holderDate;
                // if(convertView == null) {
                // new view method
                convertView = LayoutInflater.from(context).inflate(R.layout.chat_date, null, false);
                holderDate = new MessageHolderDate();
                holderDate.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
                convertView.setTag(holderDate);
               /* } else {
                    holderSent = (MessageHolderSent) convertView.getTag();
                }*/
                holderDate.dateTextView.setText(dateFormat.format(message.getMessageTime()));
                break;
            case 0:
                // SELF CASE;
                MessageHolderSent holderSent;
               // if(convertView == null) {
                    // new view method
                    convertView = LayoutInflater.from(context).inflate(R.layout.chat_sent, null, false);
                    holderSent = new MessageHolderSent();
                    holderSent.messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
                    holderSent.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
                    convertView.setTag(holderSent);
               /* } else {
                    holderSent = (MessageHolderSent) convertView.getTag();
                }*/
                holderSent.messageTextView.setText(message.getMessage());
                holderSent.dateTextView.setText(hourFormat.format(message.getMessageTime()));
                break;
            default:
                // OTHERWISE
                MessageHolderReceived holderReceived;
               // if(convertView == null) {
                    // new view method
                    convertView = LayoutInflater.from(context).inflate(R.layout.chat_received, null, false);
                    holderReceived = new MessageHolderReceived();
                    holderReceived.messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
                    holderReceived.dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
                    convertView.setTag(holderReceived);
               /* } else {
                    holderReceived = (MessageHolderReceived) convertView.getTag();
                }*/
                holderReceived.messageTextView.setText(message.getMessage());
                holderReceived.dateTextView.setText(hourFormat.format(message.getMessageTime()));
                break;
        }
        return convertView;
    }
}
