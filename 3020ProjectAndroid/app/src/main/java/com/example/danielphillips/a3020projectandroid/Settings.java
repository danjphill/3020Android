package com.example.danielphillips.a3020projectandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by danielphillips on 1/23/18.
 */

public class Settings extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Button SaveAddress = (Button) findViewById(R.id.settings_save);

        final EditText IPAddress = (EditText) findViewById(R.id.settings_ip_address);
        if (GetIPAddress() != null) {
            IPAddress.setText(GetIPAddress());
        }

        SaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveIPAddress(IPAddress.getText().toString());
                finish();
            }
        });

    }

    void SaveIPAddress(String Address){
        //TODO:Verify Address Is Valid
        SharedPreferences pref = getApplicationContext().getSharedPreferences("3020Preferences", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("RemoteIPAddress", Address);
        editor.commit();
    }

    String GetIPAddress(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("3020Preferences", 0);
        return pref.getString("RemoteIPAddress", null);

    }
}
