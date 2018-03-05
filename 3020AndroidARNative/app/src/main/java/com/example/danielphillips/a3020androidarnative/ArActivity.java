package com.example.danielphillips.a3020androidarnative;

/**
 * Created by danielphillips on 1/30/18.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.Image;
import android.media.ImageReader;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.wikitude.WikitudeSDK;
import com.wikitude.NativeStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.plugins.PluginManager;
import com.wikitude.common.rendering.RenderExtension;

import com.wikitude.common.util.Vector2;
import com.wikitude.common.util.Vector3;

import com.wikitude.rendering.ExternalRendering;

import com.wikitude.tracker.InstantTracker;
import com.wikitude.tracker.InstantTrackerListener;
import com.wikitude.tracker.InstantTarget;
import com.wikitude.tracker.InitializationPose;
import com.wikitude.tracker.InstantTrackerScenePickingCallback;
import com.wikitude.tracker.InstantTrackingState;
import com.wikitude.tracker.TrackerManager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import java.lang.reflect.Method;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.List;


public class ArActivity extends Activity implements InstantTrackerListener, ExternalRendering {
    String ConnectionResult;
    LinearLayout TutorialLinearLayout;
    TextView LoadingText;
    ImageView TutorialReload;
    ImageView TutorialBack;
    ImageView TutorialNext;
    TextView TutorialText;
    WebServer androidWebServer;
    ProgressDialog mProgressDialog;
    int current_index = -1;
    String ResultURL = "";
    TrackerManager tm;
    float x;
    float y;
    float z;
    private WikitudeCamera mWikitudeCamera;
    byte[] CurrentBuff;

    private static final String TAG = "InstantScenePicking";

    private static int cubeID = 0;
    private static int rectID = 0;

    private WikitudeSDK mWikitudeSDK;
    private CustomSurfaceView mSurfaceView;
    private Driver mDriver;
    private GLRenderer mGLRenderer;


    private InstantTracker mInstantTracker;

    private InstantTrackingState mCurrentTrackingState = InstantTrackingState.Initializing;
    private InstantTrackingState mRequestedTrackingState = InstantTrackingState.Initializing;

    private LinearLayout mHeightSettingsLayout;

    static boolean FrameCaptureEnabled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mProgressDialog = new ProgressDialog(ArActivity.this);
        mProgressDialog.setMessage("Downloading Result");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);


        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);

        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);
        tm = mWikitudeSDK.getTrackerManager();

        mInstantTracker = mWikitudeSDK.getTrackerManager().createInstantTracker(this, null);

        this.mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    Vector2<Float> screenCoordinates = new Vector2<>();
                    screenCoordinates.x = event.getX();
                    screenCoordinates.y = event.getY();
                    Log.d("TouchCordx", event.getX() + "");
                    Log.d("TouchCordy", event.getY() + "");

                    mInstantTracker.convertScreenCoordinateToPointCloudCoordinate(screenCoordinates, new InstantTrackerScenePickingCallback() {
                        @Override
                        public void onCompletion(boolean success, Vector3<Float> result) {
                            if (success) {
                                StrokedRectangle2 strokedCube = new StrokedRectangle2(StrokedRectangle2.Type.EXTENDED);
                                strokedCube.setXScale(0.05f);
                                strokedCube.setYScale(0.05f);
                                //strokedCube.setZScale(0.05f);
                                strokedCube.setXTranslate(result.x);
                                strokedCube.setYTranslate(result.y);
                                //strokedCube.setZTranslate(result.z);
                                x = result.x;
                                y = result.y;
                                z = result.z;
                                mGLRenderer.setRenderablesForKey("" + rectID++, strokedCube, null);
                                Log.d("TouchXYZ_TC", result.x + "," + result.y + "," + result.z);
                            }else{
                                Log.d("TouchXYZ_TC", "Failed");
                            }
                        }
                    });


                    Class<?> pclUtilClass = null;

                    try {
                        pclUtilClass = Class.forName("com.wikitude.tracker.internal.pcl.PointCloudUtil");
                        Method method = pclUtilClass.getDeclaredMethod("getPointCloudPoints", TrackerManager.class);
                        method.setAccessible(true);


                        Object[] pcl = (Object[]) method.invoke(pclUtilClass, tm);

                        int Counter = 0;
                        List<Float> xValues = new ArrayList<Float>();
                        List<Float> yValues = new ArrayList<Float>();
                        List<Float> zValues = new ArrayList<Float>();


                        for (Object o : pcl) {
                            Class<?> clazz = o.getClass();
                            Field xF = clazz.getField("x");
                            Field yF = clazz.getField("y");
                            Field zF = clazz.getField("z");

                            float x = (float) xF.get(o);
                            float y = (float) yF.get(o);
                            float z = (float) zF.get(o);

                            xValues.add(x);
                            yValues.add(y);
                            zValues.add(z);


                            Counter++;
//                        if (Counter % 10 == 0) {
//
//
//                        }

                        }
                        int closestIndex = getClosestIndex(xValues, yValues, zValues, x, y, z);
                        StrokedRectangle2 strokedRect = new StrokedRectangle2();
                        strokedRect.setXScale(0.05f);
                        strokedRect.setYScale(0.05f);
//                        //strokedCube.setZScale(0.05f);
                        float seletcted_x = xValues.get(closestIndex);
                        float seletcted_y = yValues.get(closestIndex);
//                        float seletcted_z = zValues.get(closestIndex);


                        strokedRect.setXTranslate(seletcted_x);
                        strokedRect.setYTranslate(seletcted_y);
//                        strokedCube.setZTranslate(seletcted_z);
                        Log.d("TouchXYZ_PC", seletcted_x + "," + seletcted_y + ",");
                        mGLRenderer.setRenderablesForKey("" + rectID++, strokedRect, null);
                        StrokedRectangle strokedRectangle = (StrokedRectangle) mGLRenderer.getRenderableForKey("");

                        strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);


                        mGLRenderer.setRenderablesForKey("", strokedRectangle, null);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
        });

        mWikitudeSDK.getPluginManager().registerNativePlugins("wikitudePlugins", "customcamera", new PluginManager.PluginErrorCallback() {
            @Override
            public void onRegisterError(int errorCode, String errorMessage) {
                Log.v(TAG, "Plugin failed to load. Reason: " + errorMessage);
            }
        });
        initNative();
    }

    private int getClosestIndex(List<Float> xValues, List<Float> yValues, List<Float> zValues, float xCalculated, float yCalculated, float zCalculated) {
        int minDistIndex = 0;
        float minDist = 9999;
        float currentDist;
        for (int i = 0; i < xValues.size(); i++) {
            //currentAverage = (Math.abs(xValues.get(i) - Math.abs(xCalculated)) + (Math.abs(yValues.get(i))) - Math.abs(yCalculated)) + (Math.abs(zValues.get(i)) - Math.abs(zCalculated));
            currentDist = (float) Math.sqrt(Math.pow((xValues.get(i) - xCalculated), 2.0) + Math.pow((yValues.get(i) - yCalculated), 2.0) + Math.pow((zValues.get(i) - zCalculated), 2.0));
            //Log.d("distanceIndex", "dist:" + currentDist + " i:" + i);
            if (minDist > currentDist) {
                minDist = currentDist;
                minDistIndex = i;
            }
        }
        Log.d("closestIndex", minDistIndex + "");
        return minDistIndex;
    }

    Bitmap Img2Bmp(ByteBuffer buff) {
        //ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buff.capacity()];
        buff.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    public class ConnectAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Recognize recognizer = new Recognize();
            ConnectionResult = recognizer.Connect(IPManager.GetIPAddress(ArActivity.this));
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            ConnectionResult = ConnectionResult.replace("\n", "");
            Toast.makeText(ArActivity.this, ConnectionResult, Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPreExecute() {


        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mWikitudeSDK.onResume();
        mSurfaceView.onResume();
        mDriver.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWikitudeSDK.onPause();
        mSurfaceView.onPause();
        mDriver.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWikitudeSDK.clearCache();
        mWikitudeSDK.onDestroy();
    }

    FrameLayout viewHolder;

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension_) {
        mGLRenderer = new GLRenderer(renderExtension_);
        mSurfaceView = new CustomSurfaceView(getApplicationContext(), mGLRenderer);
        mSurfaceView.setDrawingCacheEnabled(true);


        mDriver = new Driver(mSurfaceView, 30);
        setContentView(mSurfaceView);

        viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(mSurfaceView);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        RelativeLayout controls = (RelativeLayout) inflater.inflate(R.layout.ar_activity, null);
        viewHolder.addView(controls);

        mHeightSettingsLayout = (LinearLayout) findViewById(R.id.heightSettingsLayout);
        LoadingText = (TextView) findViewById(R.id.AR_LoadingBar);
        TutorialLinearLayout = (LinearLayout) findViewById(R.id.LinearLayoutTutorial);
        TutorialReload = (ImageView) findViewById(R.id.TutorialReload);
        TutorialBack = (ImageView) findViewById(R.id.TutorialBack);
        TutorialNext = (ImageView) findViewById(R.id.TutorialNext);
        TutorialText = (TextView) findViewById(R.id.tutorial_text);

        TutorialBack.setVisibility(View.INVISIBLE);
        TutorialNext.setVisibility(View.INVISIBLE);
        LoadingText.setVisibility(View.GONE);
        TutorialText.setVisibility(View.GONE);

        TutorialBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                current_index--;
                new RecognizeAsync().execute("");
                if (current_index == 0) {
                    TutorialBack.setVisibility(View.INVISIBLE);
                }
                if (current_index < 3) {
                    TutorialNext.setVisibility(View.VISIBLE);
                }

                //TODO:Allow For Restarting The Tutorials


            }
        });

        TutorialNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                current_index++;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        new RecognizeAsync().execute("");
                    }
                };

                LoadingText.setText("Loading...");
                LoadingText.setVisibility(View.VISIBLE);
                TutorialText.setVisibility(View.INVISIBLE);
                thread.start();

//                if (current_index == 3) {
//                    TutorialNext.setVisibility(View.INVISIBLE);
//                }
//                if (current_index > 0) {
//                    TutorialBack.setVisibility(View.VISIBLE);
//                }
            }
        });

        TutorialReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        new RecognizeAsync().execute("");
                    }
                };
                LoadingText.setText("Loading...");
                LoadingText.setVisibility(View.VISIBLE);
                TutorialText.setVisibility(View.INVISIBLE);
                thread.start();
            }
        });

        //Test Connection
        new ConnectAsync().execute("");


        final Button changeStateButton = (Button) findViewById(R.id.on_change_tracker_state);
        changeStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                changeStateButton.setVisibility(View.GONE);
                TutorialNext.setVisibility(View.VISIBLE);
                LoadingText.setText("Select Next to Begin...");
                LoadingText.setVisibility(View.VISIBLE);
                if (mRequestedTrackingState == InstantTrackingState.Initializing) {
                    if (mCurrentTrackingState == InstantTrackingState.Initializing) {
                        mRequestedTrackingState = InstantTrackingState.Tracking;
                        mInstantTracker.setState(mRequestedTrackingState);
                        changeStateButton.setText("Initialization");
                    } else {
                        Log.e(TAG, "Tracker did not change state yet.");
                    }
                } else {
                    if (mCurrentTrackingState == InstantTrackingState.Tracking) {
                        mRequestedTrackingState = InstantTrackingState.Initializing;
                        mInstantTracker.setState(mRequestedTrackingState);
                        changeStateButton.setText("Start Tracking");
                    } else {
                        Log.e(TAG, "Tracker did not change state yet.");
                    }
                }
            }
        });

        final TextView heightBox = (TextView) findViewById(R.id.heightTextView);

        final SeekBar heightSlider = (SeekBar) findViewById(R.id.heightSeekBar);
        heightSlider.setMax(190);
        heightSlider.setProgress(90);
        heightSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float height = (progress + 10) / 100.f;
                mInstantTracker.setDeviceHeightAboveGround(height);
                heightBox.setText(String.format("%.2f", height));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public class RecognizeAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            Recognize recognizer = new Recognize();
            //http://10.42.0.188:5002/
            try {
                ResultURL = recognizer.recognizeImage(IPManager.GetIPAddress(ArActivity.this), "http://" + getDeviceIP() + ":5002/");
                Log.d("ResultURL", ResultURL);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            LoadingText.setText("Getting Data Points...");
            androidWebServer.stop();
            //ResultURL.replace("\n","");
            //String ResultFullURL = "http://"+IPManager.GetIPAddress(ArActivity.this)+"/"+ResultURL;
            Log.d("ResultURL", ResultURL);
            BufferedReader bufReader = new BufferedReader(new StringReader(ResultURL));
            String line = null;
            boolean has_heatsink = false;
            boolean has_memory = false;
            boolean has_pcie = false;
            String warning = "none";

            List<Float> xValues = new ArrayList<Float>();
            List<Float> yValues = new ArrayList<Float>();
            List<Float> zValues = new ArrayList<Float>();
            Class<?> pclUtilClass = null;

            try {
                pclUtilClass = Class.forName("com.wikitude.tracker.internal.pcl.PointCloudUtil");
                Method method = pclUtilClass.getDeclaredMethod("getPointCloudPoints", TrackerManager.class);
                method.setAccessible(true);


                Object[] pcl = (Object[]) method.invoke(pclUtilClass, tm);

                int Counter = 0;


                for (Object o : pcl) {
                    Class<?> clazz = o.getClass();
                    Field xF = clazz.getField("x");
                    Field yF = clazz.getField("y");
                    Field zF = clazz.getField("z");

                    float x = (float) xF.get(o);
                    float y = (float) yF.get(o);
                    float z = (float) zF.get(o);

                    xValues.add(x);
                    yValues.add(y);
                    zValues.add(z);


                    Counter++;
//                        if (Counter % 10 == 0) {
//
//
//                        }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mGLRenderer.removeAllRenderables();
            LoadingText.setText("Adding Data Points...");
            try {
                while ((line = bufReader.readLine()) != null) {

                    String[] CurrLine = line.split(",");
                    if (CurrLine[0].contains("Memory")) {
                        has_memory = true;
                    } else if (CurrLine[0].contains("Heatsink")) {
                        has_heatsink = true;
                    } else if (CurrLine[0].contains("Card Slot")) {
                        has_pcie = true;
                    } else if(CurrLine[0].contains("Warn")){
                        warning = CurrLine[1];
                        Toast.makeText(ArActivity.this, warning, Toast.LENGTH_LONG).show();
                    }

                    Log.d("has_heatsink", has_heatsink + "");
                    Log.d("has_memory", has_memory + "");
                    Log.d("has_pcie", has_memory + "");
                    Log.d("Warning",warning);
                    displayTutorialText(has_heatsink, has_memory, has_pcie);

                    Log.d(CurrLine[0] + " min_x : ", CurrLine[2]);
                    Log.d(CurrLine[0] + " max_x : ", CurrLine[1]);
                    Log.d(CurrLine[0] + " min_y : ", CurrLine[4]);
                    Log.d(CurrLine[0] + " max_y : ", CurrLine[3]);

                    for (int i = 0; i < 4; i++) {
                        float a = 0;
                        float b = 0;
                        switch (i) {
                            case 0:
                                a = Float.valueOf(CurrLine[2]);
                                b = Float.valueOf(CurrLine[4]);
                                break;
                            case 1:
                                a = Float.valueOf(CurrLine[2]);
                                b = Float.valueOf(CurrLine[3]);
                                break;
                            case 2:
                                a = Float.valueOf(CurrLine[1]);
                                b = Float.valueOf(CurrLine[4]);
                                break;
                            case 3:
                                a = Float.valueOf(CurrLine[1]);
                                b = Float.valueOf(CurrLine[3]);
                                break;
                        }
                        Vector2<Float> screenCoordinates = new Vector2<>();
                        screenCoordinates.x = a;
                        screenCoordinates.y = b;

//                        mInstantTracker.convertScreenCoordinateToPointCloudCoordinate(screenCoordinates, new InstantTracker.ScenePickingCallback() {
//                            @Override
//                            public void onCompletion(boolean success, Vector3<Float> result) {
//                                if (success) {
//                                    StrokedCube strokedCube = new StrokedCube();
//                                    strokedCube.setXScale(0.05f);
//                                    strokedCube.setYScale(0.05f);
//                                    strokedCube.setZScale(0.05f);
//                                    strokedCube.setXTranslate(result.x);
//                                    strokedCube.setYTranslate(result.y);
//                                    strokedCube.setZTranslate(result.z);
//                                    mGLRenderer.setRenderablesForKey("" + cubeID++, strokedCube, null);
//                                    Log.d("XYCube", "Added");
//                                } else {
//                                    Log.d("XYCube", "Failed");
//                                }
//
//                            }
//                        });
                        mInstantTracker.convertScreenCoordinateToPointCloudCoordinate(screenCoordinates, new InstantTrackerScenePickingCallback() {
                            @Override
                            public void onCompletion(boolean success, Vector3<Float> result) {
                                if (success) {
//                                StrokedCube strokedCube = new StrokedCube();
//                                strokedCube.setXScale(0.05f);
//                                strokedCube.setYScale(0.05f);
//                                strokedCube.setZScale(0.05f);
//                                strokedCube.setXTranslate(result.x);
//                                strokedCube.setYTranslate(result.y);
//                                strokedCube.setZTranslate(result.z);
                                    x = result.x;
                                    y = result.y;
                                    z = result.z;
//                                mGLRenderer.setRenderablesForKey("" + cubeID++, strokedCube, null);
                                    Log.d("TouchXYZ_TC", result.x + "," + result.y + "," + result.z);
                                }else {
                                    Log.d("TouchXYZ_TC", "Failed: " + result.x + "," + result.y + "," + result.z);
                                }
                            }
                        });


//                        int closestIndex = getClosestIndex(xValues, yValues, zValues, x, y, z);
//                        StrokedCube strokedCube = new StrokedCube();
//                        strokedCube.setXScale(0.05f);
//                        strokedCube.setYScale(0.05f);
//                        strokedCube.setZScale(0.05f);
//                        float seletcted_x = xValues.get(closestIndex);
//                        float seletcted_y = yValues.get(closestIndex);
//                        float seletcted_z = zValues.get(closestIndex);
//
//                        strokedCube.setXTranslate(seletcted_x);
//                        strokedCube.setYTranslate(seletcted_y);
//                        strokedCube.setZTranslate(seletcted_z);
//                        Log.d("TouchXYZ_PC", seletcted_x + "," + seletcted_y + "," + seletcted_z);
//                        mGLRenderer.setRenderablesForKey("" + cubeID++, strokedCube, null);

                        int closestIndex = getClosestIndex(xValues, yValues, zValues, x, y, z);
                        StrokedRectangle2 strokedRect = new StrokedRectangle2();
                        strokedRect.setXScale(0.05f);
                        strokedRect.setYScale(0.05f);
//                        //strokedCube.setZScale(0.05f);
                        float seletcted_x = xValues.get(closestIndex);
                        float seletcted_y = yValues.get(closestIndex);
//                        float seletcted_z = zValues.get(closestIndex);


                        strokedRect.setXTranslate(seletcted_x);
                        strokedRect.setYTranslate(seletcted_y);
//                        strokedCube.setZTranslate(seletcted_z);
                        Log.d("TouchXYZ_PC", seletcted_x + "," + seletcted_y + ",");
                        mGLRenderer.setRenderablesForKey("" + rectID++, strokedRect, null);
                        StrokedRectangle strokedRectangle = (StrokedRectangle) mGLRenderer.getRenderableForKey("");

                        strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);


                        mGLRenderer.setRenderablesForKey("", strokedRectangle, null);


                    }

                }

                File mydir = new File(Environment.getExternalStorageDirectory() + "/ECNG3020Temp/");
                if (!mydir.exists())
                    mydir.mkdirs();
                else
                    Log.d("error", "dir. already exists");
                File tempImgdir = new File(Environment.getExternalStorageDirectory() + "/ECNG3020Temp/Uploads/");
                if (!tempImgdir.exists())
                    tempImgdir.mkdirs();
                else
                    Log.d("error", "dir. already exists");
                GLRenderer.Current = null;
                GLRenderer.FrameCaptureEnabled = true;
                while (GLRenderer.Current == null) {
                    Log.d("ArActivity, ", "Waiting For Result Image");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                GLRenderer.FrameCaptureEnabled = false;
                final File imgFile = new File(tempImgdir, "temp_file_result.png");
                Bitmap Image = GLRenderer.Current;
                FileOutputStream out = null;
                try {

                    out = new FileOutputStream(imgFile);

                    Image.compress(Bitmap.CompressFormat.PNG, 100, out);
                    WebServer.path = imgFile.toString();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    LoadingText.setVisibility(View.GONE);
                }





            } catch (IOException e) {
                e.printStackTrace();
            }

            LoadingText.setVisibility(View.GONE);
        }


        void displayTutorialText(boolean heatsink, boolean memory, boolean pcie) {

            if (heatsink && memory && pcie) {
                TutorialText.setText("Please Remove the Heatsink (Step 1 of 3)");
                TutorialNext.setVisibility(View.VISIBLE);
            } else if (!heatsink && memory && pcie) {
                TutorialText.setText("Please Remove the Memory (Step 2 of 3)");
                TutorialNext.setVisibility(View.VISIBLE);
            } else if (!heatsink && !memory && pcie) {
                TutorialText.setText("Please Remove the PCIE Cards (Step 3 of 3)");
                TutorialNext.setVisibility(View.VISIBLE);
            } else if (!heatsink && !memory && !pcie) {
                TutorialText.setText("Tutorial Complete");
                TutorialNext.setVisibility(View.INVISIBLE);
            } else if (heatsink && !memory && pcie) {
                TutorialText.setText("Incorrect Component Removed - Replace Memory");
                TutorialNext.setVisibility(View.VISIBLE);
            } else if (heatsink && !memory && !pcie) {
                TutorialText.setText("Incorrect Component Removed - Replace PCIE Card");
                TutorialNext.setVisibility(View.VISIBLE);
            } else if (!heatsink && memory && !pcie) {
                TutorialText.setText("Incorrect Component Removed - Replace PCIE Card");
                TutorialNext.setVisibility(View.VISIBLE);
            } else if (heatsink && memory && !pcie) {
                TutorialText.setText("Incorrect Component Removed - Replace PCIE Card");
                TutorialNext.setVisibility(View.VISIBLE);
            } else {
                TutorialText.setText("Please Restart Tutorial");
                TutorialNext.setVisibility(View.INVISIBLE);
                TutorialReload.setVisibility(View.VISIBLE);
            }
            TutorialText.setVisibility(View.VISIBLE);
        }


        @Override
        protected void onPreExecute() {
            //Create Folder for storing of recognized image
            ArActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    LoadingText.setText("Getting Camera Feed...");
                }
            });


            File mydir = new File(Environment.getExternalStorageDirectory() + "/ECNG3020Temp/");
            if (!mydir.exists())
                mydir.mkdirs();
            else
                Log.d("error", "dir. already exists");
            File tempImgdir = new File(Environment.getExternalStorageDirectory() + "/ECNG3020Temp/Uploads/");
            if (!tempImgdir.exists())
                tempImgdir.mkdirs();
            else
                Log.d("error", "dir. already exists");


            final File imgFile = new File(tempImgdir, "temp_file.png");


            CurrentBuff = null;
            FrameCaptureEnabled = true;

            Bitmap Image;
            int i = 0;

            while (CurrentBuff == null) {
                // do stuff
                Log.d("ArActivity", "Waiting On Frame " + i);
                i++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            FrameCaptureEnabled = false;
            Log.d("ByteSize", CurrentBuff.length + "");
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(CurrentBuff, ImageFormat.NV21, mWikitudeCamera.getFrameWidth(), mWikitudeCamera.getFrameHeight(), null);
            double ratio = mSurfaceView.getWidth() / mSurfaceView.getHeight();
            yuvImage.compressToJpeg(new Rect(0, 0, mWikitudeCamera.getFrameWidth(), mWikitudeCamera.getFrameHeight()), 100, out1);
            byte[] imageBytes = out1.toByteArray();
            Image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Image = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix, true);

            if (Image == null) {
                Log.d("ArActivity", "Image Is Null");
            }

            FrameCaptureEnabled = false;

            FileOutputStream out = null;
            try {

                out = new FileOutputStream(imgFile);

                Image.compress(Bitmap.CompressFormat.PNG, 100, out);
                WebServer.path = imgFile.toString();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    androidWebServer = new WebServer(getDeviceIP(), 5002);
                    Log.d("WebServerIP", getDeviceIP());
                    try {
                        androidWebServer.start();

                        ArActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                LoadingText.setText("Processing Image...");
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private void save(byte[] bytes, File file) throws IOException {
        Log.i("JpegSaver", "save");
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    @Override
    public void onStateChanged(InstantTracker tracker, InstantTrackingState state) {
        Log.v(TAG, "onStateChanged");
        mCurrentTrackingState = state;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // deviceHeightAboveGround may not be called during tracking
                mHeightSettingsLayout.setVisibility(mCurrentTrackingState == InstantTrackingState.Tracking ? View.INVISIBLE : View.VISIBLE);
            }
        });

        if (mCurrentTrackingState == InstantTrackingState.Initializing) {
//            for (int i = 0; i < cubeID; ++i) {
//                mGLRenderer.removeRenderablesForKey("" + i);
//            }
            for (int y = 0; y < (rectID+cubeID); ++y) {
                mGLRenderer.removeRenderablesForKey("" + y);
            }

            cubeID = 0;
            rectID = 0;
        }
    }

    @Override
    public void onInitializationPoseChanged(InstantTracker tracker, InitializationPose pose) {
        Log.v(TAG, "onInitializationPoseChanged");

        StrokedRectangle strokedRectangle = (StrokedRectangle) mGLRenderer.getRenderableForKey("");
        if (strokedRectangle == null) {
            strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);
        }

        strokedRectangle.projectionMatrix = pose.getProjectionMatrix();
        strokedRectangle.viewMatrix = pose.getViewMatrix();

        mGLRenderer.setRenderablesForKey("", strokedRectangle, null);
    }

    @Override
    public void onTrackingStarted(InstantTracker tracker) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });


        Log.v(TAG, "onTrackingStarted");
    }

    Bitmap save(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

    @Override
    public void onTracked(InstantTracker tracker, InstantTarget target) {
        StrokedRectangle strokedRectangle = (StrokedRectangle) mGLRenderer.getRenderableForKey("");
        if (strokedRectangle == null) {
            strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);
        }

        strokedRectangle.projectionMatrix = target.getProjectionMatrix();
        strokedRectangle.viewMatrix = target.getViewMatrix();

        mGLRenderer.setRenderablesForKey("", strokedRectangle, null);

        for (int i = 0; i < cubeID; ++i) {
            try {
                StrokedCube strokedCube = (StrokedCube) mGLRenderer.getRenderableForKey("" + i);
                if (strokedCube != null) {
                    strokedCube.projectionMatrix = target.getProjectionMatrix();
                    strokedCube.viewMatrix = target.getViewMatrix();
                }
            } catch (java.lang.ClassCastException e) {
                e.printStackTrace();
            }
        }
        for (int y = 0; y < rectID; ++y) {
            try {
                StrokedRectangle2 strokedRectangle2 = (StrokedRectangle2) mGLRenderer.getRenderableForKey("" + y);
                if (strokedRectangle2 != null) {
                    strokedRectangle2.projectionMatrix = target.getProjectionMatrix();
                    strokedRectangle2.viewMatrix = target.getViewMatrix();
                }
            } catch (java.lang.ClassCastException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onTrackingStopped(InstantTracker tracker) {
        Log.v(TAG, "onTrackingStopped");

        mGLRenderer.removeRenderablesForKey("");

        for (int i = 0; i < cubeID; ++i) {
            try {
                StrokedCube strokedCube = (StrokedCube) mGLRenderer.getRenderableForKey("" + i);
                if (strokedCube != null) {
                    strokedCube.projectionMatrix = null;
                    strokedCube.viewMatrix = null;
                }
            }catch (java.lang.ClassCastException e){
                e.printStackTrace();
            }
        }
        for (int i = 0; i < rectID; ++i) {
            try{
            StrokedRectangle2 strokedCube = (StrokedRectangle2) mGLRenderer.getRenderableForKey("" + i);
            if (strokedCube != null) {
                strokedCube.projectionMatrix = null;
                strokedCube.viewMatrix = null;
            }
            }catch (java.lang.ClassCastException e){
                e.printStackTrace();
            }
        }
    }

    private void LogMatrix(String name, float[] matrix) {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append(": ");
        for (float value : matrix) {
            builder.append(value);
            builder.append(" ");
        }
        Log.v(TAG, builder.toString());
    }

    String getDeviceIP() {

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }


    public void onInputPluginInitialized() {
        Log.v(TAG, "onInputPluginInitialized");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mWikitudeCamera = new WikitudeCamera(1920, 1080);
                setFrameSize(mWikitudeCamera.getFrameWidth(), mWikitudeCamera.getFrameHeight());
                Log.d("Frame_Height_Width", mWikitudeCamera.getFrameHeight() + "_" + mWikitudeCamera.getFrameWidth());
                Log.d("Screen_Height_Width", mSurfaceView.getHeight() + "_" + mSurfaceView.getWidth());

                if (isCameraLandscape()) {
                    setDefaultDeviceOrientationLandscape(true);
                }

                int imageSensorRotation = mWikitudeCamera.getImageSensorRotation();
                if (imageSensorRotation != 0) {
                    setImageSensorRotation(imageSensorRotation);
                }

            }
        });
    }

    public void onInputPluginPaused() {
        Log.v(TAG, "onInputPluginPaused");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mWikitudeCamera.close();
            }
        });
    }

    @SuppressLint("NewApi")
    public void onInputPluginResumed() {
        Log.v(TAG, "onInputPluginResumed");


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWikitudeCamera.start(new Camera.PreviewCallback() {

                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        // Log.d("CustomCameraActivity","onPrev");


                        if (FrameCaptureEnabled) {

                            CurrentBuff = data;

                        }
                        notifyNewCameraFrameN21(data);
                    }
                });
                setCameraFieldOfView(mWikitudeCamera.getCameraFieldOfView());

            }
        });
    }

    public void onInputPluginDestroyed() {
        Log.v(TAG, "onInputPluginDestroyed");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mWikitudeCamera.close();

            }
        });
    }

    private byte[] getPlanePixelPointer(ByteBuffer pixelBuffer) {
        byte[] bytes;
        if (pixelBuffer.hasArray()) {
            bytes = pixelBuffer.array();
        } else {
            bytes = new byte[pixelBuffer.remaining()];
            pixelBuffer.get(bytes);
        }

        return bytes;
    }

    public boolean isCameraLandscape() {
        final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final DisplayMetrics dm = new DisplayMetrics();
        final int rotation = display.getRotation();

        display.getMetrics(dm);

        final boolean is90off = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;
        final boolean isLandscape = dm.widthPixels > dm.heightPixels;

        return is90off ^ isLandscape;
    }

    private native void initNative();

    private native void notifyNewCameraFrame(int widthLuminance, int heightLuminance, byte[] pixelPointerLuminance, int pixelStrideLuminance, int rowStrideLuminance, int widthChrominance, int heightChrominance, byte[] pixelPointerChromaBlue, int pixelStrideBlue, int rowStrideBlue, byte[] pixelPointerChromaRed, int pixelStrideRed, int rowStrideRed);

    private native void notifyNewCameraFrameN21(byte[] frameData);

    private native void setCameraFieldOfView(double fieldOfView);

    private native void setFrameSize(int frameWidth, int frameHeight);

    private native void setDefaultDeviceOrientationLandscape(boolean isLandscape);

    private native void setImageSensorRotation(int rotation);

}