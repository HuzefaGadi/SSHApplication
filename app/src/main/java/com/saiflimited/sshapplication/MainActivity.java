package com.saiflimited.sshapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {


    EditText command, result;
    Button execute;
    Session session;
    SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Button logout = (Button) toolbar.findViewById(R.id.logout);
        TextView username = (TextView)toolbar.findViewById(R.id.username);
        preferences = ((MainApplication) getApplicationContext()).getSharedPreferences();
        username.setText(preferences.getString("username",""));
        session = ((MainApplication) getApplicationContext()).getSession();
        command = (EditText) findViewById(R.id.command);
        result = (EditText) findViewById(R.id.result);
        execute = (Button) findViewById(R.id.execute);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Session session = ((MainApplication) getApplicationContext()).getSession();
                    if(session!=null && session.isConnected())
                    {
                        session.disconnect();
                    }
                    session = null;
                    ((MainApplication) getApplicationContext()).setSession(session);
                    preferences.edit().putBoolean("loggedin",false).commit();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }

            }
        });
        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText("");
                final String commandText = command.getText().toString();

                new AsyncTask<Integer, Void, String>() {

                    ProgressDialog dialog;

                    @Override
                    protected String doInBackground(Integer... params) {
                        try {
                            return executeRemoteCommand(session, commandText);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String resultString) {
                        super.onPostExecute(resultString);
                        dialog.dismiss();
                        result.setText(resultString);

                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = new ProgressDialog(MainActivity.this);
                        dialog.setCancelable(false);
                        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        dialog.setTitle("Command in progress");
                        dialog.setMessage("Please wait..");
                        dialog.show();
                    }
                }.execute(1);
            }
        });


    }

    public String executeRemoteCommand(Session session, String command) {

        try {
            JSch jsch = new JSch();
            String username = preferences.getString("username", "");
            String password = preferences.getString("password", "");
            String port = preferences.getString("port", "0");
            String hostname = preferences.getString("hostname", "");

            if (session==null || !session.isConnected()) {
                session = jsch.getSession(username, hostname, Integer.parseInt(port));
                session.setPassword(password);
                session.setTimeout(20000);
                // Avoid asking for key confirmation
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);

                session.connect();
                if (session.isConnected()) {
                    ((MainApplication) getApplicationContext()).setSession(session);

                } else {
                    return "Login failed";
                }
            }
            ChannelExec channelssh = (ChannelExec)
                    session.openChannel("exec");
            channelssh.setCommand(command);
            channelssh.connect();

            StringBuffer outputBuffer = new StringBuffer();
            InputStream input = channelssh.getInputStream();
            int data = input.read();

            while (data != -1) {
                outputBuffer.append((char) data);
                data = input.read();
            }
            channelssh.disconnect();
            return outputBuffer.toString();
        } catch (JSchException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();

        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }


}
