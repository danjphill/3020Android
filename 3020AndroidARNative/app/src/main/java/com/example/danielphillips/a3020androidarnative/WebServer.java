package com.example.danielphillips.a3020androidarnative;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by danielphillips on 1/23/18.
 */

public class WebServer extends NanoHTTPD {
    static String path;

    public WebServer(int port) {
        super(port);
    }

    public WebServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        FileInputStream fis = null;
        File file = new File(path); //path exists and its correct
        try {

            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/jpeg", fis,file.length());

    }



//    @Override
//    public Response serve(IHTTPSession session) {
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        Map<String, String> parms = session.getParms();
//        if (parms.get("username") == null) {
//            msg += "<form action='?' method='get'>\n";
//            msg += "<p>Your name: <input type='text' name='username'></p>\n";
//            msg += "</form>\n";
//        } else {
//            msg += "<p>Hello, " + parms.get("username") + "!</p>";
//        }
//        return newFixedLengthResponse( msg + "</body></html>\n" );
//    }
//
//    public class StackOverflowMp3Server extends NanoHTTPD {
//
//        public StackOverflowMp3Server() {
//            super(8089);
//        }

//        @Override
//        public Response serve(String uri, Method method,
//                              Map<String, String> header, Map<String, String> parameters,
//                              Map<String, String> files) {
//            String answer = "";
//
//            FileInputStream fis = null;
//            try {
//                fis = new FileInputStream(Environment.getExternalStorageDirectory()
//                        + "/music/musicfile.mp3");
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
//        }
    //}
}