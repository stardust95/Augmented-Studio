package zju.homework.augmentedstudio.Activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vuforia.State;

import java.util.Vector;

import zju.homework.augmentedstudio.ARAppRenderer;
import zju.homework.augmentedstudio.ARApplicationSession;
import zju.homework.augmentedstudio.GL.ARGLView;
import zju.homework.augmentedstudio.Models.Texture;
import zju.homework.augmentedstudio.Interfaces.ARApplicationControl;
import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Utils.ARApplicationException;

public class ARSceneActivity extends Activity implements ARApplicationControl{

    private ARGLView mGLView;

    private GestureDetector mGestureDetector;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appSession = new ARApplicationSession(this);

        appSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mTextures = new Vector<Texture>();
        loadTextures();
//        mGestureDetector = new GestureDetector(this, new GestureListen)

    }

    private void loadTextures(){
        mTextures.add(Texture.loadTextureFromApk("texture.bmp", getAssets()));

    }

    @Override
    public boolean doInitTrackers() {
        return false;
    }

    @Override
    public boolean doLoadTrackersData() {
        return false;
    }

    @Override
    public boolean doStartTrackers() {
        return false;
    }

    @Override
    public boolean doStopTrackers() {
        return false;
    }

    @Override
    public boolean doUnloadTrackersData() {
        return false;
    }

    @Override
    public boolean doDeinitTrackers() {
        return false;
    }

    @Override
    public void onInitARDone(ARApplicationException e) {
        initApplicationAR();

        mRenderer.setActive(true);

        addContentView(mGLView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void initApplicationAR(){

        mGLView = new ARGLView(this);
//        mGLView.init()

        mRenderer = new ARAppRenderer(this, appSession);
        mRenderer.setTextures(mTextures);
        mGLView.setRenderer(mRenderer);
        mGLView.setZOrderMediaOverlay(true);
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

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        appSession.onConfigurationChanged();
    }

    public void updateRendering()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        refFreeFrame.initGL(metrics.widthPixels, metrics.heightPixels);
    }

}