package com.saiflimited.sshapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoginActivity extends AppCompatActivity {

    EditText hostname, port, username, password;
    Button login;
    TextView result;
    Session session;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Button exit = (Button) toolbar.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        preferences = ((MainApplication) getApplicationContext()).getSharedPreferences();
        session = ((MainApplication) getApplicationContext()).getSession();
        boolean loggedIn = preferences.getBoolean("loggedin", false);
        if(loggedIn)
        {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        hostname = (EditText) findViewById(R.id.input_hostname);
        port = (EditText) findViewById(R.id.input_port);
        username = (EditText) findViewById(R.id.input_username);
        password = (EditText) findViewById(R.id.input_password);
        login = (Button) findViewById(R.id.login);
        result = (TextView) findViewById(R.id.result);
        hostname.setText("ec2-54-186-75-186.us-west-2.compute.amazonaws.com");
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText("");
                String hostnameString = hostname.getText().toString();
                String portString = port.getText().toString();
                String usernameString = username.getText().toString();
                String passwordString = password.getText().toString();
                if (!hostnameString.isEmpty() && !portString.isEmpty() && !usernameString.isEmpty() && !passwordString.isEmpty()) {
                    connectToSSH(hostnameString, portString, usernameString, passwordString);
                } else {
                    result.setText("All fields are mandatory");

                }
            }
        });

    }

    private void connectToSSH(final String hostnameString, final String portString, final String usernameString, final String passwordString) {
        new AsyncTask<Integer, Void, String>() {
            ProgressDialog dialog;
            @Override
            protected String doInBackground(Integer... params) {
                try {
                    return executeRemoteCommand(usernameString, passwordString, hostnameString, Integer.parseInt(portString));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String resultString) {
                super.onPostExecute(resultString);
                dialog.dismiss();
                if (resultString.equals("success")) {
                    preferences.edit().putBoolean("loggedin",true).commit();
                    preferences.edit().putString("hostname", hostnameString).commit();
                    preferences.edit().putString("port", portString).commit();
                    preferences.edit().putString("username", usernameString).commit();
                    preferences.edit().putString("password",passwordString).commit();
                    Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    result.setText(resultString);
                }

            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(LoginActivity.this);
                dialog.setCancelable(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle("Login in progress");
                dialog.setMessage("Please wait..");
                dialog.show();
            }
        }.execute(1);

    }

    public String executeRemoteCommand(String username, String password, String hostname, int port) throws IOException {

        JSch jsch = new JSch();
        try {
            session = ((MainApplication) getApplicationContext()).getSession();
            if (session!=null && session.isConnected()) {
                session.disconnect();

            }

            session = jsch.getSession(username, hostname, port);
            session.setPassword(password);
            session.setTimeout(20000);
            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            session.connect();
            if (session.isConnected()) {
                ((MainApplication) getApplicationContext()).setSession(session);
                return "success";
            }
            else {
                return "Login failed";
            }

        } catch (JSchException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

    }

}
