package zju.homework.augmentedstudio.Activities;

import android.app.Activity;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.vuforia.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import zju.homework.augmentedstudio.ARAppRenderer;
import zju.homework.augmentedstudio.ARApplicationSession;
import zju.homework.augmentedstudio.GL.ARGLView;
import zju.homework.augmentedstudio.Models.CubeObject;
import zju.homework.augmentedstudio.Models.Texture;
import zju.homework.augmentedstudio.Interfaces.ARApplicationControl;
import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Utils.ARApplicationException;

public class ARSceneActivity extends Activity implements ARApplicationControl, AdapterView.OnItemSelectedListener{

    private static final String LOGTAG = ARSceneActivity.class.getName();

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

        appSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_USER);

        spinnerArray = new ArrayList<>();
        mTextures = new Vector<Texture>();
        loadTextures();
//        mGestureDetector = new GestureDetector(this, new GestureListen)

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        mRenderer.handleTouchEvent(event);
        mGLView.requestRender();
        return true;
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

        setContentView(mGLView);
//        Log.i(LOGTAG, String.format("GLView width: %d, height: %d", mGLView.getWidth(), mGLView.getHeight()));

        initLayouts();

        mRenderer.getModels().add(new CubeObject());
//        spinnerArray.add("Cube");
//        spinnerArray.add("Camera");
//        addContentView(mGLView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void initApplicationAR(){

        mGLView = new ARGLView(this);
//        mGLView.init()

        mRenderer = new ARAppRenderer(this, appSession);
        mRenderer.setTextures(mTextures);

        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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


    Spinner spinner = null;
    private ArrayList<String> spinnerArray = null;

    private void initLayouts(){
        String[] buttonTexts = new String[]{ "Rotate", "Transform" };
        LinearLayout ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

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

        this.addContentView(ll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
        mGLView.getRenderer().changeSelection((int)id);
        mGLView.requestRender();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        mGLView.getRenderer().changeSelection(-1);
        mGLView.requestRender();
    }

}