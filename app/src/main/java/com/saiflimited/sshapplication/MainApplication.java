package com.saiflimited.sshapplication;

import android.app.Application;
import android.content.SharedPreferences;

import com.jcraft.jsch.Session;

/**
 * Created by Rashida on 01/05/16.
 */
public class MainApplication extends Application {

    private Session session;
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;
    @Override
    public void onCreate() {
        super.onCreate();
        session = null;
        preferences = getSharedPreferences("Main",MODE_PRIVATE);
        edit = preferences.edit();
    }

    public Session getSession()
    {
        return session;
    }

    public void setSession(Session session)
    {
        this.session = session;
    }


    public SharedPreferences getSharedPreferences()
    {
        return preferences;
    }

    public SharedPreferences.Editor getEditor()
    {
        return edit;
    }
}
