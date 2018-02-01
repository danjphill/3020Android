package com.example.danielphillips.a3020androidarnative;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView mListView;

        mListView = (ListView) findViewById(R.id.main_list_view);


        String[] listItems = {"Tutorial", "Settings"};

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        Intent TutorialIntent = new Intent(MainActivity.this, ArActivity.class);
                        startActivity(TutorialIntent);
                        Toast.makeText(MainActivity.this, i + "", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
//                        Intent SettingsIntent = new Intent(MainActivity.this, Settings.class);
//                        startActivity(SettingsIntent);
//                        Toast.makeText(MainActivity.this, i+"", Toast.LENGTH_SHORT).show();
//                        break;
                }
            }
        });
    }
}
