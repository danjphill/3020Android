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

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.HiddenCameraActivity;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;
import com.wikitude.WikitudeSDK;
import com.wikitude.NativeStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.plugins.PluginManager;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.tracking.Frame;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import static com.example.danielphillips.a3020androidarnative.StrokedCube.sCubeVertices;

public class ArActivity extends Activity implements InstantTrackerListener, ExternalRendering {
    String ConnectionResult;
    Bitmap TutorialImage;
    LinearLayout TutorialLinearLayout;
    TextView LoadingText;
    ImageView TutorialReload;
    ImageView TutorialBack;
    ImageView TutorialNext;
    WebServer androidWebServer;
    ProgressDialog mProgressDialog;
    int current_index = -1;
    String ResultURL = "";
    String LocalResultPath = "";
    TrackerManager tm;
    float x;
    float y;
    float z;
    private WikitudeCamera2 mWikitudeCamera2;
    private WikitudeCamera mWikitudeCamera;
     Image CurrentImg;
    static Bitmap CurrentBmp;
     byte[] CurrentBuff;

    private static final String TAG = "InstantScenePicking";

    private static int cubeID = 0;

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
                        StrokedCube strokedCube = new StrokedCube();
                        strokedCube.setXScale(0.05f);
                        strokedCube.setYScale(0.05f);
                        strokedCube.setZScale(0.05f);
                        float seletcted_x = xValues.get(closestIndex);
                        float seletcted_y = yValues.get(closestIndex);
                        float seletcted_z = zValues.get(closestIndex);

                        strokedCube.setXTranslate(seletcted_x);
                        strokedCube.setYTranslate(seletcted_y);
                        strokedCube.setZTranslate(seletcted_z);
                        Log.d("TouchXYZ_PC", seletcted_x + "," + seletcted_y + "," + seletcted_z);
                        mGLRenderer.setRenderablesForKey("" + cubeID++, strokedCube, null);

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
            Log.d("distanceIndex", "dist:" + currentDist + " i:" + i);
            if (minDist > currentDist) {
                minDist = currentDist;
                minDistIndex = i;
            }
        }
        Log.d("closestIndex", minDistIndex + "");
        return minDistIndex;
    }
    Bitmap Img2Bmp(ByteBuffer buff){
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


        // mSurfaceView.getDrawingCache().createBitmap(bmp);
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

        TutorialBack.setVisibility(View.INVISIBLE);
        TutorialReload.setVisibility(View.INVISIBLE);

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
                thread.start();

                if (current_index == 3) {
                    TutorialNext.setVisibility(View.INVISIBLE);
                }
                if (current_index > 0) {
                    TutorialBack.setVisibility(View.VISIBLE);
                }
            }
        });

        TutorialReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RecognizeAsync().execute("");
            }
        });

        //Test Connection
        new ConnectAsync().execute("");


        final Button changeStateButton = (Button) findViewById(R.id.on_change_tracker_state);
        changeStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                changeStateButton.setVisibility(View.GONE);
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
            BufferedReader bufReader = new BufferedReader(new StringReader(ResultURL));
            String line = null;
            LoadingText.setText("Adding Data Points...");
            try {
                while ((line = bufReader.readLine()) != null) {
                    String[] CurrLine = line.split(",");
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

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //String ResultFullURL = "http://10.42.0.1:5001/"+ResultURL;
            //new ImageDownloader().execute(ResultFullURL);
            //new ImageDownloader().execute(ResultFullURL);
            //new DownloadFileFromURL().execute(ResultFullURL);

            //Working Download Task
//            final DownloadTask downloadTask = new DownloadTask(ArActivity.this);
//            downloadTask.execute(ResultFullURL);
//
//            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    downloadTask.cancel(true);
//                }
//            });
            LoadingText.setVisibility(View.GONE);
        }


        FileOutputStream output = null;
        ByteBuffer buffer;
        byte[] bytes;
        boolean success = false;
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

            //File imgFile = new  File(Environment.getExternalStorageDirectory() + "/Download/"+Folders[current_index]+"/"+Folders[current_index]+"/"+Files[current_index]);
            final File imgFile = new File(tempImgdir, "temp_file.png");


//            try {
//
//                Bitmap image = BitmapFactory.decodeResource(ArActivity.this.getResources(),
//                        R.mipmap._20171205_112855);
//                OutputStream fOut = null;
//
//                imgFile.createNewFile();
//                fOut = new FileOutputStream(imgFile);
//
//// 100 means no compression, the lower you go, the stronger the compression
//                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//                fOut.flush();
//                fOut.close();
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//
//            }
//
//
//            if (imgFile.exists()) {
//                if (TutorialImage != null) {
//                    TutorialImage.recycle();
//                    TutorialImage = null;
//                }
//                final BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 128;
//                TutorialImage = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                //TutorialImageView.setImageBitmap(TutorialImage);
//                WebServer.path = imgFile.toString();
//
//
//            } else {
//                Log.e("ERROR", "Not Found: " + imgFile.toString());
//            }


//            GLRenderer.Current = null;
//            GLRenderer.FrameCaptureEnabled = true;
//
//            Bitmap Image;
//            int i = 0;
//
//            while (GLRenderer.Current == null) {
//                // do stuff
//                Log.d("ArActivity", "Waiting On Frame " + i);
//                i++;
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            GLRenderer.FrameCaptureEnabled = false;
//            Image = GLRenderer.Current;


            CurrentBuff= null;
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
            Log.d("ByteSize",CurrentBuff.length+"");
            //Image = BitmapFactory.decodeByteArray(CurrentBuff, 0, CurrentBuff.length);
            //Image = Bitmap.createBitmap(mWikitudeCamera.getFrameWidth(), mWikitudeCamera.getFrameHeight(), Bitmap.Config.ARGB_8888);
            //Image.copyPixelsFromBuffer(ByteBuffer.wrap(CurrentBuff));
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(CurrentBuff, ImageFormat.NV21, mWikitudeCamera.getFrameWidth(), mWikitudeCamera.getFrameHeight(), null);
            double ratio = mSurfaceView.getWidth()/mSurfaceView.getHeight();
            yuvImage.compressToJpeg(new Rect(0, 0, mWikitudeCamera.getFrameWidth(), mWikitudeCamera.getFrameHeight()), 100, out1);
            byte[] imageBytes = out1.toByteArray();
            Image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Image = Bitmap.createBitmap(Image , 0, 0, Image .getWidth(), Image .getHeight(), matrix, true);

            if (Image == null){
                Log.d("ArActivity","Image Is Null");
            }
            //Captureing Frame Using Byte Stream


//            Class pclUtilClass = null;
//
//            try {
//                pclUtilClass = Class.forName("com.wikitude.common.tracking.Frame");
//
//            Method method = pclUtilClass.getDeclaredMethod("getData", null);
//            method.setAccessible(true);
//                byte[] data = (byte[]) method.invoke(pclUtilClass.newInstance());
//                Image = BitmapFactory.decodeByteArray(data, 0, data.length);

//                    File mydir = new File(Environment.getExternalStorageDirectory() + "/ECNG3020Temp/");
//                    if(!mydir.exists()) {
//                        mydir.mkdirs();
//                    }
//                    else {
//                        Log.d("error", "dir. already exists");
//                    }
//                    File image = new File(mydir, "test.png");
//
//
//
//
//            CurrentBuff = null;
//            FrameCaptureEnabled = true;
//
//            Bitmap Image;
//            int i = 0;
//            boolean finished = false;
//            byte[] bytes = null;
//            while (CurrentImg == null || finished == false) {
////                // do stuff
////                Log.d("ArActivity", "Waiting On Frame " + i);
////                i++;
//      try {
////                    //CurrentBmp = Img2Bmp(CurrentBuff);
////                    bytes = new byte[CurrentBuff.remaining()];
////                    CurrentBuff.get(bytes);
////                    Thread.sleep(1000);
////                    finished = true;
//
////
////            }
//
//                switch (CurrentImg.getFormat()) {
//
//
//                    // YUV_420_888 images are saved in a format of our own devising. First write out the
//                    // information necessary to reconstruct the image, all as ints: width, height, U-,V-plane
//                    // pixel strides, and U-,V-plane row strides. (Y-plane will have pixel-stride 1 always.)
//                    // Then directly place the three planes of byte data, uncompressed.
//                    //
//                    // Note the YUV_420_888 format does not guarantee the last pixel makes it in these planes,
//                    // so some cases are necessary at the decoding end, based on the number of bytes present.
//                    // An alternative would be to also encode, prior to each plane of bytes, how many bytes are
//                    // in the following plane. Perhaps in the future.
//                    case ImageFormat.YUV_420_888:
//                        // "prebuffer" simply contains the meta information about the following planes.
//                        ByteBuffer prebuffer = ByteBuffer.allocate(16);
//                        prebuffer.putInt(CurrentImg.getWidth())
//                                .putInt(CurrentImg.getHeight())
//                                .putInt(CurrentImg.getPlanes()[1].getPixelStride())
//                                .putInt(CurrentImg.getPlanes()[1].getRowStride());
//
//                        try {
//                            output = new FileOutputStream(imgFile);
//                            output.write(prebuffer.array()); // write meta information to file
//                            // Now write the actual planes.
//                            for (i = 0; i < 3; i++) {
//                                buffer = CurrentImg.getPlanes()[i].getBuffer();
//                                bytes = new byte[buffer.remaining()]; // makes byte array large enough to hold image
//                                buffer.get(bytes); // copies image from buffer to byte array
//                                output.write(bytes);    // write the byte array to file
//                            }
//                            success = true;
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        } finally {
//                            Log.v("ArActivity", "Closing image to free buffer.");
//                            CurrentImg.close(); // close this to free up buffer for other images
//                            if (null != output) {
//                                try {
//                                    output.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                        break;
//                }
//                          } catch (Exception e) {
//                    e.printStackTrace();
//                    finished = false;
//                }
//            }
            FrameCaptureEnabled = false;

            FileOutputStream out = null;
            try {
                //ByteBuffer buffer = image.getPlanes()[0].getBuffer();

//                try {
//                    save(bytes, imgFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                //image.close();
                out = new FileOutputStream(imgFile);

                Image.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                WebServer.path = imgFile.toString();
                //GLRenderer.FrameCaptureEnabled = false;
                // PNG is a lossless format, the compression factor (100) is ignored
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
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

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
            for (int i = 0; i < cubeID; ++i) {
                mGLRenderer.removeRenderablesForKey("" + i);
            }

            cubeID = 0;
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
//        CameraConfig mCameraConfig = new CameraConfig()
//                .getBuilder(ArActivity.this)
//                .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
//                .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
//                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
//                .setImageRotation(CameraRotation.ROTATION_270)
//                .build();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mWikitudeSDK.onPause();
//        mSurfaceView.onPause();
//        mDriver.stop();
//        startCamera(mCameraConfig);
//        takePicture();


        //Bitmap file = save(viewHolder);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

//stuff that updates ui

            }
        });


        //Bitmap capturedScreen = Bitmap.createBitmap(mSurfaceView.getDrawingCache());
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.RGB_565;


//        FileOutputStream outStream;
//        try {
//
//            outStream = new FileOutputStream(image);
//            GLRenderer.Current.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
//        /* 100 to keep full quality of the image */
//
//            outStream.flush();
//            outStream.close();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
            StrokedCube strokedCube = (StrokedCube) mGLRenderer.getRenderableForKey("" + i);
            if (strokedCube != null) {
                strokedCube.projectionMatrix = target.getProjectionMatrix();
                strokedCube.viewMatrix = target.getViewMatrix();
            }
        }
    }

    @Override
    public void onTrackingStopped(InstantTracker tracker) {
        Log.v(TAG, "onTrackingStopped");

        mGLRenderer.removeRenderablesForKey("");

        for (int i = 0; i < cubeID; ++i) {
            StrokedCube strokedCube = (StrokedCube) mGLRenderer.getRenderableForKey("" + i);
            if (strokedCube != null) {
                strokedCube.projectionMatrix = null;
                strokedCube.viewMatrix = null;
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

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                String targetFileName = "temp_img" + ".png";//Change name and subname
                String PATH = Environment.getExternalStorageDirectory() + "/ECNG3020Temp/";

                LocalResultPath = PATH + targetFileName;
                output = new FileOutputStream(LocalResultPath);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                File imgFile = new File(LocalResultPath);
                if (imgFile.exists()) {
                    if (TutorialImage != null) {
                        TutorialImage.recycle();
                        TutorialImage = null;
                    }
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 128;
                    TutorialImage = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    //TutorialImageView.setImageBitmap(TutorialImage);
                }
            }

        }


    }

    public void onInputPluginInitialized() {
        Log.v(TAG, "onInputPluginInitialized");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//                    mWikitudeCamera2 = new WikitudeCamera2(ArActivity.this, 640, 480);
//                    setFrameSize(mWikitudeCamera2.getFrameWidth(), mWikitudeCamera2.getFrameHeight());
//                    setCameraFieldOfView((mWikitudeCamera2.getCameraFieldOfView()));
//
//                    if(isCameraLandscape()) {
//                        setDefaultDeviceOrientationLandscape(true);
//                    }
//
//                    int imageSensorRotation = mWikitudeCamera2.getImageSensorRotation();
//                    if (imageSensorRotation != 0) {
//                        setImageSensorRotation(imageSensorRotation);
//                    }
//                }
//                else
                //{
                    mWikitudeCamera = new WikitudeCamera(640, 480);
                    setFrameSize(mWikitudeCamera.getFrameWidth(), mWikitudeCamera.getFrameHeight());

                    if(isCameraLandscape()) {
                        setDefaultDeviceOrientationLandscape(true);
                    }

                    int imageSensorRotation = mWikitudeCamera.getImageSensorRotation();
                    if (imageSensorRotation != 0) {
                        setImageSensorRotation(imageSensorRotation);
                    }
                //}
            }
        });
    }

    public void onInputPluginPaused() {
        Log.v(TAG, "onInputPluginPaused");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
//                {
//                    mWikitudeCamera2.close();
//                }
//                else
//                {
                    mWikitudeCamera.close();
                //}
            }
        });
    }

    @SuppressLint("NewApi")
    public void onInputPluginResumed() {
        Log.v(TAG, "onInputPluginResumed");


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//                    ArActivity.this.mWikitudeCamera2.start(new ImageReader.OnImageAvailableListener() {
//                        @Override
//                        public void onImageAvailable(ImageReader reader) {
//                            Image image = reader.acquireLatestImage();
//                            //Log.d("CustomCameraActivity","onPrev");
//                            if(FrameCaptureEnabled) {
//
//                                CurrentImg = image;
//
//                            }
//                            if (null != image && null != image.getPlanes()) {
//
//                                Image.Plane[] planes = image.getPlanes();
//
//                                int widthLuminance = image.getWidth();
//                                int heightLuminance = image.getHeight();
//
//                                // 4:2:0 format -> chroma planes have half the width and half the height of the luma plane
//                                int widthChrominance = widthLuminance / 2;
//                                int heightChrominance = heightLuminance / 2;
//
//                                int pixelStrideLuminance = planes[0].getPixelStride();
//                                int rowStrideLuminance = planes[0].getRowStride();
//
//                                int pixelStrideBlue = planes[1].getPixelStride();
//                                int rowStrideBlue = planes[1].getRowStride();
//
//                                int pixelStrideRed = planes[2].getPixelStride();
//                                int rowStrideRed = planes[2].getRowStride();
//                                Log.d("CustomCameraActivity","onPrev");
//
//                                notifyNewCameraFrame(
//
//                                        widthLuminance,
//                                        heightLuminance,
//                                        getPlanePixelPointer(planes[0].getBuffer()),
//                                        pixelStrideLuminance,
//                                        rowStrideLuminance,
//                                        widthChrominance,
//                                        heightChrominance,
//                                        getPlanePixelPointer(planes[1].getBuffer()),
//                                        pixelStrideBlue,
//                                        rowStrideBlue,
//                                        getPlanePixelPointer(planes[2].getBuffer()),
//                                        pixelStrideRed,
//                                        rowStrideRed
//                                );
//
//                                image.close();
//                            }
//                        }
//                    });
//                }
//                else
                //{
                    mWikitudeCamera.start(new Camera.PreviewCallback() {

                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            Log.d("CustomCameraActivity","onPrev");


                            if(FrameCaptureEnabled) {
//
                                CurrentBuff = data;
//
                            }
                            notifyNewCameraFrameN21(data);
                        }
                    });
                    setCameraFieldOfView(mWikitudeCamera.getCameraFieldOfView());
                //}
            }
        });
    }

    public void onInputPluginDestroyed() {
        Log.v(TAG, "onInputPluginDestroyed");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//                    mWikitudeCamera2.close();
//                }
//                else
                //{
                    mWikitudeCamera.close();
               // }
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

    public boolean isCameraLandscape(){
        final Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
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