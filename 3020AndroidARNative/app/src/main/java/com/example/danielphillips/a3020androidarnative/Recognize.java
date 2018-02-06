package com.example.danielphillips.a3020androidarnative;

/**
 * Created by danielphillips on 1/23/18.
 */


import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class Recognize {
    public String Connect (String IP) {

        // Do some validation here

        try {
            //http://10.42.0.1:5002/test/1
            URL url = new URL("http://"+IP+"/test/1");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            Log.d("ConnectionTest", "Connected");
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
                Log.d("ConnectionTest", "Disconnected");
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return "Could Not Connect";
        }
    }
    public String recognizeImage(String IP, String Path) throws UnsupportedEncodingException {

        // Do some validation here
        String encoded_path = URLEncoder.encode(Path, "utf-8");
        encoded_path = URLEncoder.encode(encoded_path, "utf-8");
        Log.d("Encoded Path", encoded_path);

        try {
            URL url = new URL("http://"+IP+"/recognize/"+encoded_path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            Log.d("getQuery", "Connected");
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                if(stringBuilder.toString().length() > 1) {
                    return stringBuilder.toString().substring(0, stringBuilder.length() - 1);
                }else{
                    return "";
                }
            } finally {
                urlConnection.disconnect();
                Log.d("getQuery", "Disconnected");
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return "Not Found";
        }
    }
}
