/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.PostItStory;
import com.vuforia.TrackableResult;
import com.vuforia.samples.VuforiaSamples.app.PostItNote.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vuforia.CameraDevice;
import com.vuforia.Marker;
import com.vuforia.MarkerTracker;
import com.vuforia.State;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vec2F;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.vuforia.samples.SampleApplication.SampleApplicationException;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleApplicationGLView;
import com.vuforia.samples.SampleApplication.utils.Texture;
import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenu;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuGroup;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuInterface;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


// The main activity for the FrameMarkers sample.
public class PostItStory extends Activity implements SampleApplicationControl,
        SampleAppMenuInterface
{
    private static PostItStory pis;
    private static final String LOGTAG = "Post-It story";

    SampleApplicationSession vuforiaAppSession;

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private PostItStoryRenderer mRenderer;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private RelativeLayout mUILayout;

    private Marker dataSet[];

    private GestureDetector mGestureDetector;

    private SampleAppMenu mSampleAppMenu;

    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    boolean mIsDroidDevice = false;

    public static final int NUM_TARGETS = 10;

    private List<Parola> parolas=new ArrayList();

    private int selectedIndex = -1;

    FrameLayout overlay= null;

    //utility for unzip lezione folder
    private boolean unpackZip(String path, String zipname){
        InputStream is;
        ZipInputStream zis;
        try{
            String filename;
            is = new FileInputStream(path+zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));

            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();
                //Creo la directory se non esiste altrimenti lancio eccezione
                if(ze.isDirectory()){
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path+filename);

                while ((count = zis.read(buffer)) != -1){
                    fout.write(buffer, 0, count);
                }
                fout.close();
                zis.closeEntry();
            }
            zis.close();
        }
        catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("Creato:","ok");
        Bundle extras = getIntent().getExtras();
        final String directory = extras.getString("story");

            //this.unpackZip("/storage/emulated/0/","lezione.zip");
        if(!Updater.getInstance().isLoaded()){
            Updater.getInstance().loadStoryResources(directory);
        }

        parolas = Updater.getInstance().getWords();




        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);



        vuforiaAppSession = new SampleApplicationSession(this);

        startLoadingAnimation();

        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGestureDetector = new GestureDetector(this, new GestureListener());

        // Load any sample specific textures:
        mTextures = new Vector<Texture>(PostItNote.NUM_TARGETS);
        loadTextures();

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
                "droid");
        pis=this;



    }



    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {


        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e)
        {

            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {

            //Integer ind;
            //Imposto lettura file audio
            for(int i = 0; i< mTextures.size();i++) {
                int index = getIndexFromMarkerId(i);
                switch (mRenderer.isTapOnScreenInsideTarget(i, e.getX(), e.getY())) {
                    case 1:
                        Updater.getInstance().playAudio(parolas.get(index).getPathfl());
                        break;
                    case 2:
                        Updater.getInstance().playAudio(parolas.get(index).getPathsl());
                        break;
                    default:
                        break;
                }
            }

            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);

            return true;
        }

    }

    // We want to load specific textures from the APK, which we will later use
    // for rendering.
    private void loadTextures()
    {
        if(parolas.size() == 0){
            return;
        }

        int maxIndex = getMaxId() + 1;

        for(int i = 0; i < maxIndex ; i++){
            mTextures.add(Texture.loadTextureFromPath(parolas.get(0).getPathTexture()));
        }

        for(int i = 0 ; i < parolas.size() ; i++) {
            int index = parolas.get(i).getId_marker();
            Texture t = Texture.loadTextureFromPath(parolas.get(i).getPathTexture());
            mTextures.set(index, t);
            //mTextures.add(t);
        }
    }

    private void returnAnswer(){
        Intent data = new Intent();
        data.putExtra("answer", selectedIndex);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private int getMaxId(){
        int maxIndex = -1;
        for(int i = 0; i < parolas.size(); i++){
            if(parolas.get(i).getId_marker() > maxIndex){
                maxIndex = parolas.get(i).getId_marker();
            }
        }

        return maxIndex;
    }

    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

    }


    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        if (mSampleAppMenu != null && mSampleAppMenu.processEvent(event))
            return true;

        return mGestureDetector.onTouchEvent(event);
    }


    private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
                null, false);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }


    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new PostItStoryRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);

    }


    @Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        // Initialize the marker tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker trackerBase = trackerManager.initTracker(MarkerTracker
                .getClassType());
        MarkerTracker markerTracker = (MarkerTracker) (trackerBase);



        if (markerTracker == null)
        {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }

        return result;

    }


    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager.getTracker(MarkerTracker.getClassType());
        if (markerTracker == null)
            return false;

        int maxIndex = getMaxId() + 1;
        dataSet = new Marker[maxIndex];
        for(int i = 0; i< maxIndex ; i++){
            dataSet[i] = markerTracker.createFrameMarker(i, "Marker"+i, new Vec2F(40, 40));
            if (dataSet[i] == null)
            {
                Log.e(LOGTAG, "Failed to create frame marker "+i);
                return false;
            }
        }

        Log.i(LOGTAG, "Successfully initialized MarkerTracker.");

        return true;
    }


    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager
                .getTracker(MarkerTracker.getClassType());

        int markers = markerTracker.getNumMarkers();
        if (markerTracker != null)
            markerTracker.start();

        return result;
    }


    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager
                .getTracker(MarkerTracker.getClassType());
        if (markerTracker != null)
            markerTracker.stop();

        return result;
    }


    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        return result;
    }


    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(MarkerTracker.getClassType());

        return result;
    }


    @Override
    public void onInitARDone(SampleApplicationException exception)
    {

        if (exception == null)
        {
            initApplicationAR();

            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.

            // [davide] aggiungo un pulsante
            FrameLayout layout = new FrameLayout(this);
            layout.addView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

             overlay = new FrameLayout(this);
            ImageButton select = new ImageButton(this);
            select.setImageResource(R.drawable.camera);
            final float scale = getResources().getDisplayMetrics().density;
            int dp150 = (int) (120 * scale + 0.5f);

            FrameLayout.LayoutParams selectParams = new FrameLayout.LayoutParams(
                    dp150,
                    dp150
            );
            selectParams.gravity = Gravity.CENTER;
            select.setBackgroundColor(Color.TRANSPARENT);
            select.setScaleType(ImageView.ScaleType.FIT_XY);

            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    returnAnswer();
                }
            });

            overlay.addView(select, selectParams);


            FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    dp150
            );
            overlayParams.gravity = Gravity.BOTTOM;
            overlay.setBackgroundColor(Color.TRANSPARENT);
            layout.addView(overlay, overlayParams);

            addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Hides the Loading Dialog
            loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (!result)
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

            mSampleAppMenu = new SampleAppMenu(this, this, "Post-it Note",
                    mGlView, mUILayout, null);
            setSampleAppMenuSettings();
        } else
        {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }


    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message)
    {
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        PostItStory.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    @Override
    public void onVuforiaUpdate(State state)
    {
        int tr = state.getNumTrackableResults();
        if(tr == 1){
            TrackableResult result = state.getTrackableResult(0);
            selectedIndex = getIdFromTextureIndex(result.getTrackable().getId());
            overlay.setVisibility(View.VISIBLE);
        }else{
            selectedIndex = -1;
            overlay.setVisibility(View.INVISIBLE);
        }
    }

    final public static int CMD_BACK = -1;

    // This method sets the menu's settings
    private void setSampleAppMenuSettings()
    {
        SampleAppMenuGroup group;

        group = mSampleAppMenu.addGroup("", false);
        group.addTextItem(getString(R.string.menu_back), -1);

        mSampleAppMenu.attachMenu();
    }

    private int getIdFromTextureIndex(int textureIndex){
        for(int i = 0; i < parolas.size(); i++){
            if(textureIndex == parolas.get(i).getId_marker()){
                return  parolas.get(i).getId_marker();
            }
        }
        return -1;
    }

    private int getIndexFromMarkerId(int markerId){
        for(int i = 0; i < parolas.size(); i++){
            if(markerId == parolas.get(i).getId_marker()){
                return  i;
            }
        }
        return -1;
    }


    @Override
    public boolean menuProcess(int command)
    {
        boolean result = true;

        switch (command)
        {
            case CMD_BACK:
                finish();
                break;

        }

        return result;
    }

}
