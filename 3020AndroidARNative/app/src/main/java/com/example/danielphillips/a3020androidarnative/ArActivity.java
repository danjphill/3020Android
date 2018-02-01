package com.example.danielphillips.a3020androidarnative;

/**
 * Created by danielphillips on 1/30/18.
 */

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.HiddenCameraActivity;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;
import com.wikitude.WikitudeSDK;
import com.wikitude.NativeStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class ArActivity extends  HiddenCameraActivity implements InstantTrackerListener, ExternalRendering {

    private static final String TAG = "InstantScenePicking";

    private static int cubeID = 0;

    private WikitudeSDK mWikitudeSDK;
    private CustomSurfaceView mSurfaceView;
    private Driver mDriver;
    private GLRenderer mGLRenderer;
    private ImageView ARView;


    private InstantTracker mInstantTracker;

    private InstantTrackingState mCurrentTrackingState = InstantTrackingState.Initializing;
    private InstantTrackingState mRequestedTrackingState = InstantTrackingState.Initializing;

    private LinearLayout mHeightSettingsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);

        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        mInstantTracker = mWikitudeSDK.getTrackerManager().createInstantTracker(this, null);

        this.mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    Vector2<Float> screenCoordinates = new Vector2<>();
                    screenCoordinates.x = event.getX();
                    screenCoordinates.y = event.getY();

                    mInstantTracker.convertScreenCoordinateToPointCloudCoordinate(screenCoordinates, new InstantTracker.ScenePickingCallback() {
                        @Override
                        public void onCompletion(boolean success, Vector3<Float> result) {
                            if (success) {
                                StrokedCube strokedCube = new StrokedCube();
                                strokedCube.setXScale(0.05f);
                                strokedCube.setYScale(0.05f);
                                strokedCube.setZScale(0.05f);
                                strokedCube.setXTranslate(result.x);
                                strokedCube.setYTranslate(result.y);
                                strokedCube.setZTranslate(result.z);
                                mGLRenderer.setRenderablesForKey("" + cubeID++, strokedCube, null);
                            }
                        }
                    });
                }

                return true;
            }
        });
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
        ARView = (ImageView)findViewById(R.id.img_arview);

        final Button changeStateButton = (Button) findViewById(R.id.on_change_tracker_state);
        changeStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
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
                ARView.setImageBitmap(GLRenderer.Current);
//stuff that updates ui

            }
        });


        //Bitmap capturedScreen = Bitmap.createBitmap(mSurfaceView.getDrawingCache());
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.RGB_565;

//        File mydir = new File(Environment.getExternalStorageDirectory() + "/ECNG3020Temp/");
//        if(!mydir.exists()) {
//            mydir.mkdirs();
//        }
//        else {
//            Log.d("error", "dir. already exists");
//        }
//        File image = new File(mydir, "test.png");
//
//
//
//        FileOutputStream out = null;
//        try {
//            out = new FileOutputStream(image);
//            GLRenderer.Current.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
//            // PNG is a lossless format, the compression factor (100) is ignored
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (out != null) {
//                    out.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        //}


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

    Bitmap save(View v)
    {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }
    @Override
    public void onTracked(InstantTracker tracker, InstantTarget target) {
        StrokedRectangle strokedRectangle = (StrokedRectangle)mGLRenderer.getRenderableForKey("");
        if (strokedRectangle == null) {
            strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);
        }

        strokedRectangle.projectionMatrix = target.getProjectionMatrix();
        strokedRectangle.viewMatrix = target.getViewMatrix();

        mGLRenderer.setRenderablesForKey("", strokedRectangle, null);

        for (int i = 0; i < cubeID; ++i) {
            StrokedCube strokedCube = (StrokedCube)mGLRenderer.getRenderableForKey("" + i);
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
            StrokedCube strokedCube = (StrokedCube)mGLRenderer.getRenderableForKey("" + i);
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

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        Log.d("ImageCaptured ","ImageCaptured");
        mWikitudeSDK.onResume();
        mSurfaceView.onResume();
        mDriver.start();
    }

    @Override
    public void onCameraError(int errorCode) {
        mWikitudeSDK.onResume();
        mSurfaceView.onResume();
        mDriver.start();
    }
}