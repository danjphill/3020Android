package com.example.danielphillips.a3020androidarnative;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by danielphillips on 1/23/18.
 */

public class IPManager {

    static void SaveIPAddress(String Address,Context context){
        //TODO:Verify Address Is Valid
        SharedPreferences pref = context.getSharedPreferences("3020Preferences", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("RemoteIPAddress", Address);
        editor.commit();
    }

    static String GetIPAddress(Context context){
        SharedPreferences pref = context.getSharedPreferences("3020Preferences", 0);
        return pref.getString("RemoteIPAddress", null);

    }
}
