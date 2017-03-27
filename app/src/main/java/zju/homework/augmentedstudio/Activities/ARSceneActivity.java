package zju.homework.augmentedstudio.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ImageTargetBuilder;
import com.vuforia.ObjectTracker;
import com.vuforia.RotationalDeviceTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import zju.homework.augmentedstudio.AR.ARAppRenderer;
import zju.homework.augmentedstudio.AR.ARApplicationSession;
import zju.homework.augmentedstudio.AR.RefFreeFrame;
import zju.homework.augmentedstudio.Container.ImageTargetData;
import zju.homework.augmentedstudio.Container.ModelsData;
import zju.homework.augmentedstudio.Container.ObjectInfoData;
import zju.homework.augmentedstudio.Container.SceneData;
import zju.homework.augmentedstudio.Container.TransformData;
import zju.homework.augmentedstudio.GL.ARGLView;
import zju.homework.augmentedstudio.Interfaces.ARApplicationControl;
import zju.homework.augmentedstudio.Java.Account;
import zju.homework.augmentedstudio.Java.ImageAdapter;
import zju.homework.augmentedstudio.Models.CubeObject;
import zju.homework.augmentedstudio.Models.Material;
import zju.homework.augmentedstudio.Models.MeshObject;
import zju.homework.augmentedstudio.Models.ModelObject;
import zju.homework.augmentedstudio.Models.ObjObject;
import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Tasks.JoinGroupTask;
import zju.homework.augmentedstudio.UI.AppMenu;
import zju.homework.augmentedstudio.UI.AppMenuGroup;
import zju.homework.augmentedstudio.UI.AppMenuInterface;
import zju.homework.augmentedstudio.Utils.ARApplicationException;
import zju.homework.augmentedstudio.Utils.ActivityCollector;
import zju.homework.augmentedstudio.Utils.LoadingDialogHandler;
import zju.homework.augmentedstudio.Utils.NetworkManager;
import zju.homework.augmentedstudio.Utils.Tools.ResourceLoader;
import zju.homework.augmentedstudio.Utils.Util;

public class ARSceneActivity extends Activity implements ARApplicationControl,
        AdapterView.OnItemSelectedListener, AppMenuInterface {

    private static final String LOGTAG = ARSceneActivity.class.getSimpleName();

    public final static String BUNDLE_GROUP = "group";
    public final static String BUNDLE_USER = "user";
    public final static String BUNDLE_DATASET = "dataset";
    public final static String BUNDLE_OBJECTS = "objects";
    public final static String BUNDLE_TRACKER = "tracker";

    private ARGLView mGLView;

//    private GestureDetector mGestureDetector;

    private ARApplicationSession appSession;

    private GestureDetector mGestureDetector ;

    private NetworkManager networkManager = new NetworkManager();
    // View overlays to be displayed in the Augmented View
    private RelativeLayout mUILayout;
    private View mBottomBar;
    private View mCameraButton;
    private boolean useLightSensor = true;

    int targetBuilderCounter = 1;

    DataSet dataSetUserDef = null;

    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    public RefFreeFrame refFreeFrame;

    // Our OpenGL view:
//    private ApplicationGLView mGlView;

    // Our renderer:
    private ARAppRenderer mRenderer;


    private boolean mContAutoFocus = false;

    private ScaleGestureDetector mScaleDetector;
    private ArrayList<ObjectInfoData> objectInfoDataArrayList = null;

    private String userid;
    private Account mAccount = null;

    private String groupId;

    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    private View mFlashOptionView;

    private boolean isRotationTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setCacheDir(getExternalCacheDir().getAbsolutePath());

        startLoadingAnimation();

        Bundle bundle = getIntent().getExtras();

        isRotationTracker = bundle.getBoolean(BUNDLE_TRACKER);
//        isRotationTracker = true;
        mDatasetStrings = bundle.getStringArrayList(BUNDLE_DATASET);
        groupId = bundle.getString(BUNDLE_GROUP);
        userid = bundle.getString(BUNDLE_USER);
        objectInfoDataArrayList = bundle.getParcelableArrayList(BUNDLE_OBJECTS);

        if(userid == null)
            userid = "Unknown User";

        mAccount = new Account(userid);

        appSession = new ARApplicationSession(this);

        appSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGestureDetector = new GestureDetector(this.getApplicationContext(), new GestureListener());

        mScaleDetector = new ScaleGestureDetector(this.getApplicationContext(), new ScaleGestureListener());;

        initSensor();
    }

    private void initSensor(){

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        SensorEventListener LightSensorListener = new SensorEventListener() {

            private float maxLight = 80;
            @Override
            public void onSensorChanged(final SensorEvent event) {
                if( event.sensor.getType() == Sensor.TYPE_LIGHT  && useLightSensor ){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(LOGTAG, "light value:" + event.values[0]);
                            maxLight = Math.max(event.values[0], maxLight);
                            float lightColor = Math.max( event.values[0] / maxLight, 0.2f );
                            if( mRenderer != null ){
                                mRenderer.getModels().get(0).setColor(new float[]{ lightColor, lightColor, lightColor, 1.0f });
                                mRenderer.setLightColor(new float[]{ lightColor, lightColor, lightColor, 1.0f });
                            }
                        }
                    });
                }
            }


            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.i(LOGTAG, "accuracy changed: " + accuracy);

            }
        };
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if( lightSensor != null ){
//            makeToast("Light Sensor is available");
            sensorManager.registerListener(
                    LightSensorListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        }else {
            makeToast("Light Sensor is not available");
        }
    }

    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
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

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            selectObject((int) e.getX(), (int) e.getY());
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }
    }




    public class ScaleGestureListener implements  ScaleGestureDetector.OnScaleGestureListener{
        private float curSpan;
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
//            Log.i(LOGTAG, "ON SCALE");
            float span = detector.getCurrentSpan();
            if( span > curSpan ){       // scale up
                mRenderer.changeScale(true);
            }else {             // scale down
                mRenderer.changeScale(false);
            }
            curSpan = span;
            mGLView.requestRender();
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            curSpan = detector.getCurrentSpan();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    private float touchDownX;
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
//        Log.i(LOGTAG, "OnTouchEvent");
        super.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        boolean result = false;

        if( event.getAction() == MotionEvent.ACTION_DOWN ){
            touchDownX = event.getX();
//            return true;
        }

        if( mAppMenu != null && (mAppMenu.isMenuDisplaying() || touchDownX < mGLView.getWidth() / 4) ){
            // Process the Gestures
            if (mAppMenu != null && mAppMenu.processEvent(event))
                return true;
        }else {
            mScaleDetector.onTouchEvent(event);

            mRenderer.handleTouchEvent(event);
            mGLView.requestRender();
        }

        return true;
    }


    public static int getColor(int width, int height, int x, int y) {
        int screenshotSize = width * height;
        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb);
        int pixelsBuffer[] = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);

        return pixelsBuffer[(height - y -1) * width + x];
    }

    private void selectObject(final int x, final int y){
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.setColorPicking(true);
                int[] position = mRenderer.getTouchPosition();
                position[0] = x;
                position[1] = y;
            }
        });
    }
//
//    private String buildingFilename = "/storage/emulated/0/Buildings.txt";
//    private String objFilename = "/storage/emulated/0/APK/armchair.obj";
    private void loadObjModel(final String objName, final String objPath){
//        Log.i(LOGTAG, objFilename.substring(0, objFilename.lastIndexOf('/')));
        final ResourceLoader loader = ResourceLoader.getResourceLoader();
        Log.i(LOGTAG, objName);
        spinnerArray.add(objName);
        ObjObject objObject = loader.loadObjObject(objName, objPath);
        if( objObject != null )
            mRenderer.getModels().add(objObject);
        return;
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker otracker;
        RotationalDeviceTracker rtracker;

        // Trying to initialize the trackers
        otracker = tManager.initTracker(ObjectTracker.getClassType());
        rtracker = (RotationalDeviceTracker) tManager.initTracker(RotationalDeviceTracker.getClassType());
        rtracker.setPosePrediction(true);
        rtracker.setModelCorrection(rtracker.getDefaultHandheldModel());

        if (otracker == null || rtracker == null )
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
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker otracker, rtracker;
        otracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        rtracker = TrackerManager.getInstance().getTracker(RotationalDeviceTracker.getClassType());

        if ( mRenderer.getTrackerMode() == ARAppRenderer.TrackerMode.OBJECT_TRACKER  && otracker != null)
            otracker.start();
        if ( mRenderer.getTrackerMode() == ARAppRenderer.TrackerMode.ROTATION_TRACKER  && rtracker != null)
            rtracker.start();

        return result;
    }

    @Override
    public boolean doStopTrackers() {

        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker otracker = null, rtracker = null;
//        if( trackerMode == TrackerMode.OBJECT_TRACKER )
        otracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        rtracker = TrackerManager.getInstance().getTracker(RotationalDeviceTracker.getClassType());

        if ( mRenderer.getTrackerMode() == ARAppRenderer.TrackerMode.ROTATION_TRACKER  && otracker != null)
            otracker.stop();
        if ( mRenderer.getTrackerMode() == ARAppRenderer.TrackerMode.OBJECT_TRACKER  && rtracker != null)
            rtracker.stop();

        return result;
    }


    @Override
    public boolean doDeinitTrackers() {

        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        if (refFreeFrame != null)
            refFreeFrame.deInit();

        TrackerManager tManager = TrackerManager.getInstance();
//        if( trackerMode == TrackerMode.OBJECT_TRACKER )
        tManager.deinitTracker(ObjectTracker.getClassType());
//        else
        tManager.deinitTracker(RotationalDeviceTracker.getClassType());

        return result;
    }


    @Override
    public void onInitARDone(ARApplicationException e) {

        if( e == null ){

            initApplicationAR();

            mRenderer.setActive(true);

            addContentView(mGLView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

            mUILayout.bringToFront();

            // Hides the Loading Dialog
            loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

//            loadObjModel("armchair", "/sdcard/APK/armchair.obj");

            CubeObject lightCube = new CubeObject();       // light cube
            lightCube.setPosition(mRenderer.getLightPos());
            lightCube.setScale(10f);

            mRenderer.getModels().add(lightCube);
            spinnerArray.add("Light");

            initLayouts();

            try{
                appSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);

            }catch (ARApplicationException ex){
                ex.printStackTrace();
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO
            );

            if( result )
                mContAutoFocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

            Log.i(LOGTAG, "The thread id = " + android.os.Process.myPid());
//        addContentView(mGLView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));

            mAppMenu = new AppMenu(ARSceneActivity.this, ARSceneActivity.this, "Image Targets",
                    mGLView, mUILayout, null,
                    userid);
            setAppMenuSettings();
        }else {
            e.printStackTrace();
        }
    }

    private void initApplicationAR(){
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        // Do application initialization
        refFreeFrame = new RefFreeFrame(this, appSession);
        refFreeFrame.init();

        mGLView = new ARGLView(this);
        mGLView.init(translucent, depthSize, stencilSize);

        mRenderer = new ARAppRenderer(this, appSession);
        mRenderer.setTrackerMode( this.isRotationTracker ? ARAppRenderer.TrackerMode.ROTATION_TRACKER
                                                               : ARAppRenderer.TrackerMode.OBJECT_TRACKER );

        mGLView.setRenderer(mRenderer);

//        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLView.setZOrderMediaOverlay(true);

//        mGestureDetector = new GestureDetector(this, new GestureListen)
    }

    // Adds the Overlay view to the GLView
    private void startLoadingAnimation()
    {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.loading_overlay,
                null);

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


    boolean isUserDefinedTargetsRunning()
    {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        if (objectTracker != null)
        {
            ImageTargetBuilder targetBuilder = objectTracker
                    .getImageTargetBuilder();
            if (targetBuilder != null)
            {
                Log.e(LOGTAG, "Quality> " + targetBuilder.getFrameQuality());
                return (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE) ? true
                        : false;
            }
        }

        return false;
    }

    public void onCameraClick(View v)
    {
        if (isUserDefinedTargetsRunning())
        {
            // Shows the loading dialog
            loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

            // Builds the new target
            startBuild();
        }
    }

    @Override
    public void onVuforiaUpdate(State state) {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        if (refFreeFrame.hasNewTrackableSource())
        {
            Log.d(LOGTAG,
                    "Attempting to transfer the trackable source to the dataset");

            // Deactivate current dataset
            objectTracker.deactivateDataSet(objectTracker.getActiveDataSet());

            // Clear the oldest target if the dataset is full or the dataset
            // already contains five user-defined targets.
            if (dataSetUserDef.hasReachedTrackableLimit()
                    || dataSetUserDef.getNumTrackables() >= 5)
                dataSetUserDef.destroy(dataSetUserDef.getTrackable(0));

            if (mExtendedTracking && dataSetUserDef.getNumTrackables() > 0)
            {
                // We need to stop the extended tracking for the previous target
                // so we can enable it for the new one
                int previousCreatedTrackableIndex =
                        dataSetUserDef.getNumTrackables() - 1;

                objectTracker.resetExtendedTracking();
                dataSetUserDef.getTrackable(previousCreatedTrackableIndex)
                        .stopExtendedTracking();
            }

            // Add new trackable source
            Trackable trackable = dataSetUserDef
                    .createTrackable(refFreeFrame.getNewTrackableSource());

            // Reactivate current dataset
            objectTracker.activateDataSet(dataSetUserDef);

            if (mExtendedTracking)
            {
                trackable.startExtendedTracking();
            }

        }

        int select = mRenderer.getSelectIndex();

        if( select <= 0 ){
            spinner.setSelection(0);
        }else{
            spinner.setSelection(select);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        appSession.onConfigurationChanged();
    }

    public void updateRendering()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        refFreeFrame.initGL(metrics.widthPixels, metrics.heightPixels);
    }

    ArrayAdapter spinnerAdapter = null;
    Spinner spinner = null;
    private ArrayList<String> spinnerArray = new ArrayList<>();;          // must use this theme

    private void initLayouts(){
        String[] buttonTexts = new String[]{ "Rotate", "Transform" };
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay, null,false);

        mUILayout.setVisibility(View.VISIBLE);
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        mBottomBar = mUILayout.findViewById(R.id.bottom_bar);

        // Gets a reference to the Camera button
        mCameraButton = mUILayout.findViewById(R.id.camera_button);

        // Gets a reference to the loading dialog container
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_layout);

        startUserDefinedTargets();
        initializeBuildTargetModeViews();

        mUILayout.bringToFront();

        LinearLayout ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        ll.setBackgroundColor(Color.WHITE);

        final Button[] buttons = new Button[buttonTexts.length];
        for(int i=0; i<buttons.length; i++){
            buttons[i] = new Button(this);
            buttons[i].setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
            buttons[i].setText(buttonTexts[i]);
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button button = (Button) v;
                    String text = button.getText().toString();
                    if( text.equals("Rotate") ){
                        buttons[1].setEnabled(true);
                        buttons[0].setEnabled(false);
                    }else{
                        buttons[0].setEnabled(true);
                        buttons[1].setEnabled(false);
                    }
                    mRenderer.changeMode(text);
                    mGLView.requestRender();
                }
            });
            ll.addView(buttons[i]);
        }
        buttons[1].setEnabled(false);       // default is transform mode

        // init spinner
        spinner = new Spinner(this);
        spinnerAdapter = new ArrayAdapter<String>(this, android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item, spinnerArray);
        spinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(this);

        ll.addView(spinner);

        addContentView(ll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
        mRenderer.changeSelection((int)id);
        mGLView.requestRender();
}

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        mRenderer.changeSelection(0);
        mGLView.requestRender();
    }

    private void initializeBuildTargetModeViews()
    {
        // Shows the bottom bar
        mBottomBar.setVisibility(View.VISIBLE);
        mCameraButton.setVisibility(View.VISIBLE);
    }

    boolean startUserDefinedTargets()           // 开始对UDT跟踪, 只在初始化执行一次
    {
        Log.d(LOGTAG, "startUserDefinedTargets");

        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) (trackerManager
                .getTracker(ObjectTracker.getClassType()));
        if (objectTracker != null)
        {
            ImageTargetBuilder targetBuilder = objectTracker
                    .getImageTargetBuilder();

            if (targetBuilder != null)
            {
                // if needed, stop the target builder
                if (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE)
                    targetBuilder.stopScan();

                objectTracker.stop();

                targetBuilder.startScan();

            }
        } else
            return false;

        return true;
    }

    void startBuild()                   // 使用ImageTargetBuilder拍照并生成TargetSource(更新追踪的Iamge)
    {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        if (objectTracker != null)
        {
            ImageTargetBuilder targetBuilder = objectTracker
                    .getImageTargetBuilder();
            if (targetBuilder != null)
            {
                if (targetBuilder.getFrameQuality() == ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_LOW)
                {
                    showErrorDialogInUIThread();
                }

                String name;
                do
                {
                    name = "UserTarget-" + targetBuilderCounter;
                    Log.d(LOGTAG, "TRYING " + name);
                    targetBuilderCounter++;
                } while (!targetBuilder.build(name, 320.0f));

                refFreeFrame.setCreating();
            }
        }
    }


    @Override
    protected void onResume() {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        try{
            appSession.resumeAR();
        }catch (ARApplicationException ex){
            ex.printStackTrace();
        }

        if( mGLView != null){
            mGLView.setVisibility(View.VISIBLE);
            mGLView.onResume();
        }
    }

    @Override
    protected void onPause() {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if( mGLView != null ){
            mGLView.setVisibility(View.INVISIBLE);
            mGLView.onPause();
        }

        try{
            appSession.pauseAR();
        }catch (ARApplicationException ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try{
            appSession.stopAR();
        }catch (ARApplicationException ex){
            ex.printStackTrace();
        }

        for(MeshObject object : mRenderer.getModels()){
            for(Material material : object.getMaterials()){
                object.setMaterials(null);
            }
        }

        System.gc();
    }


    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        dataSetUserDef = objectTracker.createDataSet();

        if (dataSetUserDef == null ){
            Log.d(LOGTAG, "Failed to create a new tracking data.");
            return false;
        }

        if (!objectTracker.activateDataSet(dataSetUserDef))
        {
            Log.d(LOGTAG, "Failed to activate data set.");
            return false;
        }

        Log.d(LOGTAG, "Successfully loaded and activated data set.");
        return true;
    }


    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return true;
        if (objectTracker == null)
        {
            result = false;
            Log.d(
                    LOGTAG,
                    "Failed to destroy the tracking data set because the ObjectTracker has not been initialized.");
        }

        if (dataSetUserDef != null)
        {
            if (objectTracker.getActiveDataSet() != null
                    && !objectTracker.deactivateDataSet(dataSetUserDef))
            {
                Log.d(
                        LOGTAG,
                        "Failed to destroy the tracking data set because the data set could not be deactivated.");
                result = false;
            }

            if (!objectTracker.destroyDataSet(dataSetUserDef))
            {
                Log.d(LOGTAG, "Failed to destroy the tracking data set.");
                result = false;
            }

            Log.d(LOGTAG, "Successfully destroyed the data set.");
            dataSetUserDef = null;
        }


        return result;
    }



    private AlertDialog mErrorDialog = null;
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
                        ARSceneActivity.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_ok),
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

    private HashSet<MeshObject> hasUploaded = new HashSet<>();
    private void uploadModels() throws IOException{
        ModelsData modelsData = null;
        List<String> names = new ArrayList<>();
        List<TransformData> transforms = new ArrayList<>();

        for(MeshObject object : mRenderer.getModels()){
            if( !hasUploaded.contains(object) ){
                names.add(object.getModelName());
                transforms.add(object.getTransform());
                hasUploaded.add(object);
            }
        }

        modelsData = new ModelsData(groupId,
                names.toArray(new String[0]),
                transforms.toArray(new TransformData[0]));
        try{
            String filepath = this.getCacheDir() + "/" + "time" + ".models";
            FileOutputStream saveFile = new FileOutputStream(filepath);

            saveFile.write(Util.objectToJson(modelsData).getBytes());

            Log.i(LOGTAG, "saved scene in " + filepath);
        }catch (IOException ex){
            ex.printStackTrace();
        }

    }
//
//    private void uploadScene(){ // upload track image(xml) and models with their transforms to server
//
//        SceneData sceneData = null;
//
////        String imageName = mDatasetStrings.get(mCurrentDatasetSelectionIndex);
////        String xmlName = "StonesAndChips.xml";
////        String datName = "StonesAndChips.dat";
//
////        mRenderer.getModels().toArray(objectsArray);        // fill array
//
//        try{
//            sceneData = new SceneData(groupId, "demouser",
//                    new ImageTargetData(Util.getStringFromInputStream(getAssets().open(xmlName)),
//                            Util.inputStreamToBase64(getAssets().open(datName))));
//
//            String filepath = this.getCacheDir() + "/" + "time" + ".scene";
//            FileOutputStream saveFile = new FileOutputStream(filepath);
//            saveFile.write(Util.objectToJson(sceneData).getBytes());
//            Log.i(LOGTAG, "saved scene in " + filepath);
////            String result = networkManager.postJson(Util.URL_GROUP, Util.objectToJson(sceneData));
////
////            if( result != null ){
////                Log.i(LOGTAG, result);
////            }
//
//        }catch (IOException ex){
//            ex.printStackTrace();
//        }
//
//    }
//
//    private void extractModels(String filename){
//        try{
//            String jsondata = Util.getStringFromInputStream(new FileInputStream(filename));
//            ModelsData modelsData = (ModelsData) Util.jsonToObject(jsondata, ModelsData.class);
//
////            String[] modelNames = modelsData.getModelName();
//
//            for(int i=0; i<modelsData.getModelName().length; i++){       // if has any new models
//                ModelObject object = new ModelObject();
////                object.loadTextModel(Util.stringToInputStream(modelsData.getModelData()[i]));.
//                hasUploaded.add(object);
//                mRenderer.getModels().add(object);
//            }
//
//            for(TransformData transform : modelsData.getTransforms()){
//                for(MeshObject object : mRenderer.getModels()){
//                    if( transform.getModelName().equals(object.getModelName()) ){
//                        object.setTransform(transform);
//                        Log.i(LOGTAG, "upload transform of model " + object.getModelName());
//                        break;
//                    }
//                }
//            }
//
//            Log.i(LOGTAG, "Updated Models from server");
//        }catch (IOException ex){
//            ex.printStackTrace();
//        }
//    }


    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    final public static int CMD_AUTOFOCUS = 2;
    final public static int CMD_FLASH = 3;
    final public static int CMD_CAMERA_FRONT = 4;
    final public static int CMD_CAMERA_REAR = 5;
    final public static int CMD_DATASET_START_INDEX = 6;

    final public static int CMD_JOIN_GROUP = 7;
    final public static int CMD_CREATE_GROUP = 8;
    final public static int CMD_EXIT_GROUP = 9;

    final public static int CMD_ONLINE_MODEL = 10;
    final public static int CMD_LOCAL_MODEL = 11;

    final public static int CMD_SHARE_ONLINE = 12;
    final public static int CMD_CREATE_CUBE = 13;

    final public static int CMD_LIGHT_SENSOR = 14;

    private AppMenu mAppMenu;

//    private boolean mSwitchDatasetAsap = false;
    private boolean mFlash = false;
    private boolean mContAutofocus = false;
    private boolean mExtendedTracking = false;


    // 设置菜单
    private void setAppMenuSettings()
    {
        AppMenuGroup group;

        group = mAppMenu.addGroup("", false);
        group.addTextItem(getString(R.string.menu_back), -1);

        group = mAppMenu.addGroup("Group", true);
        group.addTextItem(getString(R.string.create_group), CMD_CREATE_GROUP);
        group.addTextItem(getString(R.string.join_group), CMD_JOIN_GROUP);
        group.addTextItem(getString(R.string.exit_group), CMD_EXIT_GROUP);

        group = mAppMenu.addGroup("", true);
        group.addSelectionItem("Lightsensor", CMD_LIGHT_SENSOR, useLightSensor);
        group.addSelectionItem(getString(R.string.menu_extended_tracking),
                CMD_EXTENDED_TRACKING, false);
        group.addSelectionItem(getString(R.string.menu_contAutofocus),
                CMD_AUTOFOCUS, mContAutofocus);
        mFlashOptionView = group.addSelectionItem(
                getString(R.string.menu_flash), CMD_FLASH, false);

        Camera.CameraInfo ci = new Camera.CameraInfo();
        boolean deviceHasFrontCamera = false;
        boolean deviceHasBackCamera = false;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++)
        {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                deviceHasFrontCamera = true;
            else if (ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                deviceHasBackCamera = true;
        }

        if (deviceHasBackCamera && deviceHasFrontCamera)
        {
            group = mAppMenu.addGroup(getString(R.string.menu_camera),
                    true);
            group.addRadioItem(getString(R.string.menu_camera_front),
                    CMD_CAMERA_FRONT, false);
            group.addRadioItem(getString(R.string.menu_camera_back),
                    CMD_CAMERA_REAR, true);
        }

        group = mAppMenu.addGroup("Others", true);
        group.addTextItem("Create Cube", CMD_CREATE_CUBE);
        group.addTextItem("ScreenShot Share", CMD_SHARE_ONLINE);

        mAppMenu.attachMenu();
    }

    ArrayList<ObjectInfoData> imageUrlList = new ArrayList<>();

    @Override
    public boolean menuProcess(int command)
    {

        boolean result = true;

        switch (command)
        {
            case CMD_BACK:
                finish();
                break;

            case CMD_FLASH:
                result = CameraDevice.getInstance().setFlashTorchMode(!mFlash);

                if (result)
                {
                    mFlash = !mFlash;
                } else
                {
                    showToast(getString(mFlash ? R.string.menu_flash_error_off
                            : R.string.menu_flash_error_on));
                    Log.e(LOGTAG,
                            getString(mFlash ? R.string.menu_flash_error_off
                                    : R.string.menu_flash_error_on));
                }
                break;

            case CMD_AUTOFOCUS:

                if (mContAutofocus)
                {
                    result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);

                    if (result)
                    {
                        mContAutofocus = false;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_off));
                        Log.e(LOGTAG,
                                getString(R.string.menu_contAutofocus_error_off));
                    }
                } else
                {
                    result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

                    if (result)
                    {
                        mContAutofocus = true;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_on));
                        Log.e(LOGTAG,
                                getString(R.string.menu_contAutofocus_error_on));
                    }
                }

                break;

            case CMD_CAMERA_FRONT:
            case CMD_CAMERA_REAR:

                // Turn off the flash
                if (mFlashOptionView != null && mFlash)
                {
                    // OnCheckedChangeListener is called upon changing the checked state
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    {
                        ((Switch) mFlashOptionView).setChecked(false);
                    } else
                    {
                        ((CheckBox) mFlashOptionView).setChecked(false);
                    }
                }

                appSession.stopCamera();

                try
                {
                    appSession
                            .startAR(command == CMD_CAMERA_FRONT ? CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_FRONT
                                    : CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);

                    mRenderer.updateConfiguration();

                } catch (ARApplicationException e)
                {
                    showToast(e.getString());
                    Log.e(LOGTAG, e.getString());
                    result = false;
                }
                doStartTrackers();
                break;
            case CMD_LIGHT_SENSOR:
                useLightSensor = !useLightSensor;
                if( !useLightSensor ){
                    float color = 0.5f;
                    mRenderer.setLightColor(new float[]{ color, color, color, 1.0f });
                }
                break;
            case CMD_EXTENDED_TRACKING:
                if (dataSetUserDef.getNumTrackables() > 0)
                {
                    int lastTrackableCreatedIndex =
                            dataSetUserDef.getNumTrackables() - 1;

                    Trackable trackable = dataSetUserDef
                            .getTrackable(lastTrackableCreatedIndex);

                    if (!mExtendedTracking)
                    {
                        if (!trackable.startExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                    "Failed to start extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                    "Successfully started extended tracking target");
                        }
                    } else
                    {
                        if (!trackable.stopExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                    "Failed to stop extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                    "Successfully stopped extended tracking target");
                        }
                    }
                }

                if (result)
                    mExtendedTracking = !mExtendedTracking;

                break;


            //小组
            case CMD_JOIN_GROUP:
                //加入小组
                if(mAccount.getGroup() != null) {
                    Util.showDialogWithText(ARSceneActivity.this, "You have had a group!");
                    break;
                }

                final EditText et = new EditText(ARSceneActivity.this);

                new AlertDialog.Builder(ARSceneActivity.this).setTitle("Input group id")
//                            .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(et)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog1, int which) {
                                String input = et.getText().toString();
                                if (input.equals("")) {
                                    Toast.makeText(getApplicationContext(), "input can't be vacant" + input, Toast.LENGTH_LONG).show();
                                }
                                else {

                                    JoinGroupTask joinGroupTask = new JoinGroupTask(){
                                        @Override
                                        protected void onPostExecute(Object res) {
                                            super.onPostExecute(res);
                                            Group group = (Group) res;
                                            //showProgress(false, "");
                                            if( res == null ){
                                                Util.createAndShowDialog(ARSceneActivity.this, "Join Group Failed", "msg");
                                                return;
                                            }
                                        }
                                    };
                                    joinGroupTask.execute(input);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                break;

            case CMD_CREATE_GROUP:
                //创建小组
                if( mAccount.hasGroup() ) {
                    Util.showDialogWithText(ARSceneActivity.this, "You have had a group!");
                }
                else {
                    Util.showOpenFileDialog(ARSceneActivity.this, Util.REQUEST_CREATE_GROUP);     // 先选择需要协作编辑的文件
                }
                break;

            case CMD_EXIT_GROUP:
                //退出小组
                break;

            //浏览模型
            case CMD_ONLINE_MODEL:
                //获取模型图片URL数组

                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        String result = networkManager.getJson(Util.URL_OBJECTLIST);
                        return result;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        super.onPostExecute(result);
                        if( result == null )
                            return;
                        imageUrlList = (ArrayList<ObjectInfoData>)Util.
                                jsonToObject((String) result, new TypeReference<ArrayList<ObjectInfoData>>() {});

                        ImageAdapter imageAdapter = new ImageAdapter(ARSceneActivity.this, R.layout.image_item, imageUrlList);
                        ListView listView = new ListView(ARSceneActivity.this);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(15, 15, 15, 15);
                        listView.setLayoutParams(layoutParams);

                        listView.setAdapter(imageAdapter);

                        AlertDialog.Builder builder = new AlertDialog.Builder(ARSceneActivity.this);
                        builder.setView(listView);
                        builder.setTitle("Choose the Model");

                        final AlertDialog alertDialog = builder.create();

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String urlString = imageUrlList.get(position).getImageUrl();
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.show();

                    }
                };
                task.execute();
                break;

            case CMD_LOCAL_MODEL:
                Util.showOpenFileDialog(ARSceneActivity.this, Util.REQUEST_OPEN_OBJ);
                break;

            case CMD_SHARE_ONLINE:
                mGLView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        int width = mGLView.getWidth();
                        int height = mGLView.getHeight();
                        int screenshotSize = width * height;
                        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
                        bb.order(ByteOrder.nativeOrder());
                        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb);
                        int pixelsBuffer[] = new int[screenshotSize];
                        bb.asIntBuffer().get(pixelsBuffer);
                        bb = null;

                        for (int i = 0; i < screenshotSize; ++i) {
                            // The alpha and green channels' positions are preserved while the  red and blue are swapped
                            pixelsBuffer[i] = ((pixelsBuffer[i] & 0xff00ff00)) | ((pixelsBuffer[i] & 0x000000ff) << 16) | ((pixelsBuffer[i] & 0x00ff0000) >> 16);
                        }

                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);

                        String file = Util.getCacheDir() + Util.randInt(0, Short.MAX_VALUE) + ".jpg";
                        try{
                            FileOutputStream fos = new FileOutputStream(file);
                            if( fos != null ){
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                fos.flush();
                                fos.close();
                            }
                        }catch (FileNotFoundException ex){
                            ex.printStackTrace();
                            return;
                        }catch (IOException ex){
                            ex.printStackTrace();
                            return;
                        }

                        Log.i(LOGTAG, "screen shot saved in " + file);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(file)));
                        shareIntent.setType("image/png");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share With");
                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ARSceneActivity.this.startActivity(Intent.createChooser(shareIntent, "ARStudio"));
                    }
                });

                break;
            case CMD_CREATE_CUBE:
                mRenderer.getModels().add(new CubeObject());
                int i=0;
                while( spinnerArray.contains("Cube"+i) )
                    i++;
                spinnerAdapter.add("Cube"+i);
                break;
        }

        return result;
    }

    public void targetCreated()
    {
        // Hides the loading dialog
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

        if (refFreeFrame != null)
        {
            refFreeFrame.reset();
        }

    }

    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    // from other answer in this question
    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h) {

        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        GLES20.glReadPixels(x, y, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer);
        int offset1, offset2;
        for (int i = 0; i < h; i++) {
            offset1 = i * w;
            offset2 = (h - i - 1) * w;
            for (int j = 0; j < w; j++) {
                int texturePixel = bitmapBuffer[offset1 + j];
                int blue = (texturePixel >> 16) & 0xff;
                int red = (texturePixel << 16) & 0x00ff0000;
                int pixel = (texturePixel & 0xff00ff00) | red | blue;
                bitmapSource[offset2 + j] = pixel;
            }
        }


        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }


    void showErrorDialog()
    {
        if (mErrorDialog != null && mErrorDialog.isShowing())
            mErrorDialog.dismiss();

        mErrorDialog = new AlertDialog.Builder(ARSceneActivity.this).create();
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        };

        mErrorDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.button_OK), clickListener);

        mErrorDialog.setTitle(getString(R.string.target_quality_error_title));

        String message = getString(R.string.target_quality_error_desc);

        // Show dialog box with error message:
        mErrorDialog.setMessage(message);
        mErrorDialog.show();
    }

    void showErrorDialogInUIThread()
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                showErrorDialog();
            }
        });
    }

    public void makeToast(final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ARSceneActivity.this, content, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        ActivityCollector.finishAll();
    }
}

