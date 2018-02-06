package com.example.danielphillips.a3020androidarnative;

import android.os.Bundle;
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
        if (IPManager.GetIPAddress(Settings.this) != null) {
            IPAddress.setText(IPManager.GetIPAddress(Settings.this));
        }

        SaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IPManager.SaveIPAddress(IPAddress.getText().toString(),Settings.this);
                finish();
            }
        });

    }


}
