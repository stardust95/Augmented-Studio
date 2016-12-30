package zju.homework.augmentedstudio.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import zju.homework.augmentedstudio.ARAppRenderer;
import zju.homework.augmentedstudio.ARApplicationSession;
import zju.homework.augmentedstudio.GL.ARGLView;
import zju.homework.augmentedstudio.Models.CubeObject;
import zju.homework.augmentedstudio.Models.ModelObject;
import zju.homework.augmentedstudio.Models.Texture;
import zju.homework.augmentedstudio.Interfaces.ARApplicationControl;
import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Utils.ARApplicationException;

public class ARSceneActivity extends Activity implements ARApplicationControl, AdapterView.OnItemSelectedListener{

    private static final String LOGTAG = ARSceneActivity.class.getName();

    private ARGLView mGLView;

//    private GestureDetector mGestureDetector;

    private ARApplicationSession appSession;

    // View overlays to be displayed in the Augmented View
    private RelativeLayout mUILayout;
    private View mBottomBar;
    private View mCameraButton;

    // Our OpenGL view:
//    private SampleApplicationGLView mGlView;

    // Our renderer:
    private ARAppRenderer mRenderer;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private int mCurrentDatasetSelectionIndex = 0;
    private DataSet mCurrentDataset;

    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    private boolean mSwitchDatasetAsap = false;
    private boolean mContAutoFocus = false;

    private ScaleGestureDetector scaleListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatasetStrings.add("StonesAndChips.xml");
        mDatasetStrings.add("Tarmac.xml");

        appSession = new ARApplicationSession(this);

        appSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        spinnerArray = new ArrayList<>();
        mTextures = new Vector<Texture>();
        loadTextures();

        scaleListener = new ScaleGestureDetector(this.getApplicationContext(), new ScaleGestureListener());

        spinnerArray.add("Cube");
        spinnerArray.add("Buildings");
//        spinnerArray.add("Camera");
    }

    public class ScaleGestureListener implements  ScaleGestureDetector.OnScaleGestureListener{
        private float curSpan;
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.i(LOGTAG, "ON SCALE");
            float span = detector.getCurrentSpan();
            if( span > curSpan ){       // scale up
                mGLView.getRenderer().changeScale(true);
            }else {             // scale down
                mGLView.getRenderer().changeScale(false);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.i(LOGTAG, "OnTouchEvent");
        super.onTouchEvent(event);
        scaleListener.onTouchEvent(event);
        mRenderer.handleTouchEvent(event);

        mGLView.requestRender();
        return true;
    }

    private void loadTextures(){
        mTextures.add(Texture.loadTextureFromApk("texture.bmp", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("Buildings.jpeg", getAssets()));
    }

    private void testLoadModel(){

        ModelObject object = new ModelObject();
        try {
            object.loadTextModel(this.getAssets(), "Buildings.txt");
            mRenderer.getModels().add(new CubeObject());
            mRenderer.getModels().add(object);
        }catch (IOException ex){
            ex.printStackTrace();
        }


        return;
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
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

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return result;
    }

    @Override
    public boolean doStopTrackers() {

        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }


    @Override
    public boolean doDeinitTrackers() {

        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    @Override
    public void onInitARDone(ARApplicationException e) {

        if( e == null ){

            initApplicationAR();

            mRenderer.setActive(true);

            setContentView(mGLView);

            initLayouts();

//        mRenderer.getModels().add(new CubeObject());
            testLoadModel();

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
        mRenderer.setTextures(mTextures);
        mGLView.setRenderer(mRenderer);

        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLView.setZOrderMediaOverlay(true);

//        mGestureDetector = new GestureDetector(this, new GestureListen)
    }

    // Adds the Overlay view to the GLView
    private void addOverlayView(boolean initLayout)
    {
        // Inflates the Overlay Layout to be displayed above the Camera View
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(
                R.layout.activity_arscene, null, false);

        mUILayout.setVisibility(View.VISIBLE);

        // If this is the first time that the application runs then the
        // uiLayout background is set to BLACK color, will be set to
        // transparent once the SDK is initialized and camera ready to draw
        if (initLayout)
        {
            mUILayout.setBackgroundColor(Color.BLACK);
        }

        // Adds the inflated layout to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Gets a reference to the bottom navigation bar
//        mBottomBar = mUILayout.findViewById(R.id.bottom_bar);

        // Gets a reference to the Camera button
//        mCameraButton = mUILayout.findViewById(R.id.camera_button);

        // Gets a reference to the loading dialog container
//        loadingDialogHandler.mLoadingDialogContainer = mUILayout
//                .findViewById(R.id.loading_layout);

//        startUserDefinedTargets();
//        initializeBuildTargetModeViews();

        mUILayout.bringToFront();
    }

    @Override
    public void onVuforiaUpdate(State state) {
        if (mSwitchDatasetAsap)
        {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                    .getClassType());
            if (ot == null || mCurrentDataset == null
                    || ot.getActiveDataSet() == null)
            {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
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
        String[] buttonTexts = new String[]{ "Rotate", "Transform" };
        LinearLayout ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        ll.setBackgroundColor(Color.WHITE);

        final Button[] buttons = new Button[2];
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
                    mGLView.getRenderer().changeMode(text);
                    mGLView.requestRender();
                }
            });
            ll.addView(buttons[i]);
        }
        buttons[1].setEnabled(false);       // default is transform mode

        // init spinner
        spinner = new Spinner(this);
        spinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
        spinner.setAdapter(new ArrayAdapter<String>(this, android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item, spinnerArray));

        spinner.setOnItemSelectedListener(this);

        ll.addView(spinner);

        this.addContentView(ll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
        mGLView.getRenderer().changeSelection((int)id);
        mGLView.requestRender();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        mGLView.getRenderer().changeSelection(-1);
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

        mTextures.clear();
        mTextures = null;

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


    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}