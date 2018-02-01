package com.example.danielphillips.ecng3020ar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.camera.CameraSettings;

import java.io.IOException;

/**
 * Created by danielphillips on 1/29/18.
 */
public class ArActivity extends AppCompatActivity {

    public static final String INTENT_EXTRAS_KEY_SAMPLE = "sampleData";

    private static final String TAG = ArActivity.class.getSimpleName();

    /** Root directory of the sample AR-Experiences in the assets dir. */
    private static final String SAMPLES_ROOT = "files/";

    /**
     * The ArchitectView is the core of the AR functionality, it is the main
     * interface to the Wikitude SDK.
     * The ArchitectView has its own lifecycle which is very similar to the
     * Activity lifecycle.
     * To ensure that the ArchitectView is functioning properly the following
     * methods have to be called:
     *      - onCreate(ArchitectStartupConfiguration)
     *      - onPostCreate()
     *      - onResume()
     *      - onPause()
     *      - onDestroy()
     * Those methods are preferably called in the corresponding Activity lifecycle callbacks.
     */
    protected ArchitectView architectView;

    /** The path to the AR-Experience. This is usually the path to its index.html. */
    private String arExperience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Used to enabled remote debugging of the ArExperience with google chrome https://developers.google.com/web/tools/chrome-devtools/remote-debugging
        WebView.setWebContentsDebuggingEnabled(true);

        /*
         * The following code is used to run different configurations of the SimpleArActivity,
         * it is not required to use the ArchitectView but is used to simplify the Sample App.
         *
         * Because of this the Activity has to be started with correct intent extras.
         * e.g.:
         *  SampleData sampleData = new SampleData.Builder("SAMPLE_NAME", "PATH_TO_AR_EXPERIENCE")
         *              .arFeatures(ArchitectStartupConfiguration.Features.ImageTracking)
         *              .cameraFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS)
         *              .cameraPosition(CameraSettings.CameraPosition.BACK)
         *              .cameraResolution(CameraSettings.CameraResolution.HD_1280x720)
         *              .camera2Enabled(false)
         *              .build();
         *
         * Intent intent = new Intent(this, SimpleArActivity.class);
         * intent.putExtra(UrlLauncherStorageActivity.URL_LAUNCHER_SAMPLE_CATEGORY, category);
         * startActivity(intent);
         */
//        final Intent intent = getIntent();
//        if (!intent.hasExtra(INTENT_EXTRAS_KEY_SAMPLE)) {
//            throw new IllegalStateException(getClass().getSimpleName() +
//                    " can not be created without valid SampleData as intent extra for key " + INTENT_EXTRAS_KEY_SAMPLE + ".");
//        }

        arExperience = "05_InstantTracking_3_Interactivity/index.html";

        /*
         * The ArchitectStartupConfiguration is required to call architectView.onCreate.
         * It controls the startup of the ArchitectView which includes camera settings,
         * the required device features to run the ArchitectView and the LicenseKey which
         * has to be set to enable an AR-Experience.
         */
        final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration(); // Creates a config with its default values.
        config.setLicenseKey("FUDotVu1MepQn+u7cWDCF5XTWZYgPf+HPtxQziTymEg57zuej3G+CTXNkXo/SweVkj8VBn2dFpd1hyeyDbdAh6duljIFTEqTnMOSJB/srVQmuSOICb+Pvig9dEOBZzNYuqt+YmbqfH0ROSqOpxo0wVf+9EfjoUm1V0OnzsPULnBTYWx0ZWRfX0Dc2DZF0xM5rgcsGWnx9pi0KnbIoHBgLXS1VCVGAzfvhAztIuQll6a257ievtpdn4ZoKnU5PfQzPefGeBgNgJcTWDClZb/K4u7gG8J6rRm/0SLLnPb53m0uYzWgf5FBKwdIpjuzyJBIwHrWK5m53b9AnIKRPev8vCmcxhLHfitfSWwRIq/aPO/lC859Dt/HQeG/Imf3ct1yP9HY9WCpN+aUjwWzuqbcUFVuetpM3sfxlZ8qmxP5eQtLBOM+IDF+l0UYimJ8Qz2qyGxLGFw87ct3Dl83rw4/FEwPr69Dg1s0eF6pf5EpkP3jOuuwAYq6713KstBHPktTDWiqmQZxAxHov1lUdn8W0Hut2ZjuI20CGMvxYy2dXiKb+ea2l47Ch4+1y7yTMqdH0mcJgRk6U0UXD0ohgCAkmr7UGXhof1wd9EEYiWbhxwC9qi9VhWS8LvoRUWRKykT0vFxpQCY3zSXfbCMX3A06Q3QvbE+uR+paCf75edPi5gA="); // Has to be set, to get a trial license key visit http://www.wikitude.com/developer/licenses.
        config.setCameraPosition(CameraSettings.CameraPosition.BACK);       // The default camera is the first camera available for the system.
        config.setCameraResolution(CameraSettings.CameraResolution.AUTO);   // The default resolution is 640x480.
        config.setCameraFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS);     // The default focus mode is continuous focusing.
        config.setCamera2Enabled(false);        // The camera2 api is disabled by default (old camera api is used).
        config.setFeatures(15);                 // This tells the ArchitectView which AR-features it is going to use, the default is all of them.

        architectView = new ArchitectView(this);
        architectView.onCreate(config); // create ArchitectView with configuration

        setContentView(architectView);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        architectView.onPostCreate();

        try {
            /*
             * Loads the AR-Experience, it may be a relative path from assets,
             * an absolute path (file://) or a server url.
             *
             * To get notified once the AR-Experience is fully loaded,
             * an ArchitectWorldLoadedListener can be registered.
             */
            architectView.load(SAMPLES_ROOT + arExperience);
        } catch (IOException e) {
            Toast.makeText(this, "Could Not Load - IOException", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception while loading arExperience " + arExperience + ".", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        architectView.onResume(); // Mandatory ArchitectView lifecycle call
    }

    @Override
    protected void onPause() {
        super.onPause();
        architectView.onPause(); // Mandatory ArchitectView lifecycle call
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * Deletes all cached files of this instance of the ArchitectView.
         * This guarantees that internal storage for this instance of the ArchitectView
         * is cleaned and app-memory does not grow each session.
         *
         * This should be called before architectView.onDestroy
         */
        architectView.clearCache();
        architectView.onDestroy(); // Mandatory ArchitectView lifecycle call
    }

}