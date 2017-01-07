package zju.homework.augmentedstudio.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
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

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.RotationalDeviceTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import zju.homework.augmentedstudio.AR.ARAppRenderer;
import zju.homework.augmentedstudio.AR.ARApplicationSession;
import zju.homework.augmentedstudio.Container.ImageTargetData;
import zju.homework.augmentedstudio.Container.ModelsData;
import zju.homework.augmentedstudio.Container.SceneData;
import zju.homework.augmentedstudio.Container.TransformData;
import zju.homework.augmentedstudio.GL.ARGLView;
import zju.homework.augmentedstudio.Interfaces.ARApplicationControl;
import zju.homework.augmentedstudio.Java.Account;
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

    private static final String LOGTAG = ARSceneActivity.class.getName();

    private ARGLView mGLView;

//    private GestureDetector mGestureDetector;

    private ARApplicationSession appSession;

    //private GestureDetector mGestureDetector = new GestureDetector(this, new GestureListener());

    private NetworkManager networkManager = new NetworkManager();
    // View overlays to be displayed in the Augmented View
    private RelativeLayout mUILayout;
    private View mBottomBar;
    private View mCameraButton;


    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Our OpenGL view:
//    private ApplicationGLView mGlView;

    // Our renderer:
    private ARAppRenderer mRenderer;


    private boolean mContAutoFocus = false;

    private ScaleGestureDetector scaleListener;

    private String userid;
    private Account mAccount = null;

    private String groupId;

    private ArrayList<String> mDatasetStrings = new ArrayList<String>();


    private View mFlashOptionView;

    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private int mStartDatasetsIndex = 0;
    private int mDatasetsNumber = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mDatasetStrings.add("target_images.xml");
//        mDatasetStrings.add("StonesAndChips.xml");
//        mDatasetStrings.add("Tarmac.xml");
        startLoadingAnimation();

        Bundle bundle = getIntent().getExtras();

        mDatasetStrings = bundle.getStringArrayList("dataset");
        groupId = bundle.getString("group");
        userid = bundle.getString("user");

        if(userid == null)
            userid = "Unknown User";

        mAccount = new Account(userid);

        appSession = new ARApplicationSession(this);

        appSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        spinnerArray = new ArrayList<>();

        scaleListener = new ScaleGestureDetector(this.getApplicationContext(), new ScaleGestureListener());

//        spinnerArray.add("Cube");
//        spinnerArray.add("Buildings");
//        spinnerArray.add("Camera");
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
        boolean result = false;
        if( event.getAction() == MotionEvent.ACTION_DOWN ){
            touchDownX = event.getX();
//            return true;
        }

        if( mAppMenu.isMenuDisplaying() || touchDownX < mGLView.getWidth() / 4 ){
            // Process the Gestures
            if (mAppMenu != null && mAppMenu.processEvent(event))
                return true;
        }else {
            scaleListener.onTouchEvent(event);

            mRenderer.handleTouchEvent(event);
            mGLView.requestRender();
        }

//        return mGestureDetector.onTouchEvent(event);
        return true;
    }

//    private String buildingFilename = "/storage/emulated/0/Objs/Buildings.txt";
    private String objFilename = "/storage/emulated/0/APK/armchair.obj";
    private void loadObjModel(final String objFilename){
//        Log.i(LOGTAG, objFilename.substring(0, objFilename.lastIndexOf('/')));
        final ResourceLoader loader = ResourceLoader.getResourceLoader();
        final String objName = objFilename.substring(objFilename.lastIndexOf('/'));
        Log.i(LOGTAG, objName);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                loader.loadObjObject(objName, objFilename);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                ObjObject objObject = loader.getObjObjectByName(objName);
                mRenderer.getModels().add(objObject);
            }
        };
        task.execute();
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

            initLayouts();

//        mRenderer.getModels().add(new CubeObject());
            loadObjModel(objFilename);

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

        mGLView = new ARGLView(this);
        mGLView.init(translucent, depthSize, stencilSize);

        mRenderer = new ARAppRenderer(this, appSession);

        mGLView.setRenderer(mRenderer);

        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLView.setZOrderMediaOverlay(true);

//        mGestureDetector = new GestureDetector(this, new GestureListen)
    }

    // Adds the Overlay view to the GLView
    private void startLoadingAnimation()
    {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,
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

    @Override
    public void onVuforiaUpdate(State state) {
        if (mSwitchDatasetAsap)
        {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
//            if( trackerMode == TrackerMode.OBJECT_TRACKER ){
                ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker.getClassType());
                if (ot == null || mCurrentDataset == null
                        || ot.getActiveDataSet() == null)
                {
                    Log.d(LOGTAG, "Failed to swap datasets");
                    return;
                }
                doUnloadTrackersData();
                doLoadTrackersData();
//            }

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
//        refFreeFrame.initGL(metrics.widthPixels, metrics.heightPixels);
    }


    Spinner spinner = null;
    private ArrayList<String> spinnerArray = null;          // must use this theme

    private void initLayouts(){
        String[] buttonTexts = new String[]{ "Rotate", "TransforM", "Test" };
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
        buttons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                uploadScene();
                try{
                    uploadModels();
//                    extractModels("/data/data/zju.homework.augmentedstudio/cache/time.models");
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        });
        // init spinner
        spinner = new Spinner(this);
        spinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
        spinner.setAdapter(new ArrayAdapter<String>(this, android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item, spinnerArray));

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
        mRenderer.changeSelection(-1);
        mGLView.requestRender();
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

        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();

        if (mCurrentDataset == null )
            return false;

        if (!mCurrentDataset.load(
                mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
//            if(isExtendedTrackingActive())
//            {
//                trackable.startExtendedTracking();
//            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                    + (String) trackable.getUserData());
        }

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
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (objectTracker.getActiveDataSet().equals(mCurrentDataset)
                    && !objectTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }

            mCurrentDataset = null;
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

    private void uploadScene(){ // upload track image(xml) and models with their transforms to server

        SceneData sceneData = null;

//        String imageName = mDatasetStrings.get(mCurrentDatasetSelectionIndex);
        String xmlName = "StonesAndChips.xml";
        String datName = "StonesAndChips.dat";

//        mRenderer.getModels().toArray(objectsArray);        // fill array

        try{
            sceneData = new SceneData(groupId, "demouser",
                    new ImageTargetData(Util.getStringFromInputStream(getAssets().open(xmlName)),
                            Util.inputStreamToBase64(getAssets().open(datName))));

            String filepath = this.getCacheDir() + "/" + "time" + ".scene";
            FileOutputStream saveFile = new FileOutputStream(filepath);
            saveFile.write(Util.objectToJson(sceneData).getBytes());
            Log.i(LOGTAG, "saved scene in " + filepath);
//            String result = networkManager.postJson(Util.URL_GROUP, Util.objectToJson(sceneData));
//
//            if( result != null ){
//                Log.i(LOGTAG, result);
//            }

        }catch (IOException ex){
            ex.printStackTrace();
        }

    }

    private void downloadModel(final String modelName){
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String filepath = getCacheDir() + modelName + ".zip";
                boolean result = networkManager.getArchiveFile(Util.URL_DOWNLOAD + modelName, filepath);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
            }
        };
        task.execute();

    }

    private void extractModels(String filename){
        try{
            String jsondata = Util.getStringFromInputStream(new FileInputStream(filename));
            ModelsData modelsData = (ModelsData) Util.jsonToObject(jsondata, ModelsData.class);

//            String[] modelNames = modelsData.getModelName();

            for(int i=0; i<modelsData.getModelName().length; i++){       // if has any new models
                ModelObject object = new ModelObject();
//                object.loadTextModel(Util.stringToInputStream(modelsData.getModelData()[i]));.
                hasUploaded.add(object);
                mRenderer.getModels().add(object);
            }

            for(TransformData transform : modelsData.getTransforms()){
                for(MeshObject object : mRenderer.getModels()){
                    if( transform.getModelName().equals(object.getModelName()) ){
                        object.setTransform(transform);
                        Log.i(LOGTAG, "upload transform of model " + object.getModelName());
                        break;
                    }
                }
            }

            Log.i(LOGTAG, "Updated Models from server");
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void uploadTransforms(){ // only upload transforms to server

    }


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

    private AppMenu mAppMenu;

    private boolean mSwitchDatasetAsap = false;
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

        group = mAppMenu
                .addGroup(getString(R.string.menu_datasets), true);

        group.addTextItem("View Online Models", CMD_ONLINE_MODEL);
        group.addTextItem("View Local Models", CMD_LOCAL_MODEL);

        mAppMenu.attachMenu();
    }

    ArrayList<String> list = new ArrayList<>();

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

            case CMD_EXTENDED_TRACKING:
                for (int tIdx = 0; tIdx < mCurrentDataset.getNumTrackables(); tIdx++)
                {
                    Trackable trackable = mCurrentDataset.getTrackable(tIdx);

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
                                    "Successfully started extended tracking target");
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
                                            /*try{
                                                String filename = group.getId() + "-" + group.getFileName();
                                                File tmpFile = File.createTempFile(filename, ".pdf", getExternalCacheDir());
                                                Log.i(LOG_TAG, tmpFile.getAbsolutePath());
                                                Uri uri = Util.base64ToFile(group.getPdfData(), tmpFile);
                                                Intent intent = new Intent(MainActivity.this, PDFViewActivity.class);
                                                intent.putExtra(PDFViewActivity.EXTRA_URI, uri);
                                                intent.putExtra(PDFViewActivity.EXTRA_ACCOUNT, mAccount.getID());
                                                intent.putExtra(PDFViewActivity.EXTRA_GROUP, group.getId());
                                                ARSceneActivity.this.startActivity(intent);
                                            }catch (IOException ex){
                                                ex.printStackTrace();
                                            }*/
                                        }
                                    };
                                    joinGroupTask.execute(input);
                                    //showProgress(true, "Joining Group");
//                                        if(!mAccount.setGroup(input)) {
//                                            showDialogWithText("Group id wrong!");
//                                            return;
//                                        }
//                                        else {
//                                            showDialogWithText("Successfully joined!");
//
//                                            mDatas[0] = "Your group id:" + mAccount.getGroup().getId() + "\n"
//                                                    + "Your PDF: " + mAccount.getGroup().getFileName();
//
//                                            adapter.notifyDataSetChanged();
//                                        }
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

                list.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=214931719,1608091472&fm=116&gp=0.jpg");
                list.add("http://img06.tooopen.com/images/20161204/tooopen_sl_188713338136.jpg");
                list.add("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2290757533,426974567&fm=116&gp=0.jpg");

                ImageAdapter imageAdapter = new ImageAdapter(this, R.layout.image_item, list);
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
                        String urlString = list.get(position);
                        Util.showDialogWithText(ARSceneActivity.this, "正在加载" + urlString);

                        /*用这个URL得到模型然后显示......*/

                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();

                break;

            case CMD_LOCAL_MODEL:
                Util.showOpenFileDialog(ARSceneActivity.this, Util.REQUEST_OPEN_OBJ);
                break;


            default:
                if (command >= mStartDatasetsIndex
                        && command < mStartDatasetsIndex + mDatasetsNumber)
                {
                    mSwitchDatasetAsap = true;
                    mCurrentDatasetSelectionIndex = command
                            - mStartDatasetsIndex;
                }
                break;
        }

        return result;
    }

    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        loadObjModel(objFilename);
        if(requestCode == Util.REQUEST_OPEN_OBJ && data != null) {
            /*打开OBJ*/
            Uri uri = data.getData();
            Log.i(LOGTAG, uri.getPath());
            this.loadObjModel(uri.getPath());
        }
    }


    @Override
    public void onBackPressed() {
        ActivityCollector.finishAll();
    }
}

