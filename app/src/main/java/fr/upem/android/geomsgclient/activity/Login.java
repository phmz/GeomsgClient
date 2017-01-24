package fr.upem.android.geomsgclient.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import fr.upem.android.geomsgclient.R;

public class Login extends AppCompatActivity {

    private EditText nameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private String name;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        loginButton = (Button) findViewById(R.id.loginButton);
    }

    public void onLogin(View v) {
        if (nameEditText.getText().toString().trim().isEmpty() || nameEditText == null) {
            createAlertDialog("Name cannot be empty, please try again.");
            return;
        }
        if (passwordEditText.toString().trim().isEmpty()) {
            passwordEditText.setText("");
        }
        // TODO
        // login to server
        // if ok start mainactivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

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
