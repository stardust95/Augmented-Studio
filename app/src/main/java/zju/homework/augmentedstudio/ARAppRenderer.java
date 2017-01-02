package zju.homework.augmentedstudio;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.ObjectTracker;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vuforia;

import java.nio.ByteOrder;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import zju.homework.augmentedstudio.Activities.ARSceneActivity;
import zju.homework.augmentedstudio.Models.ModelObject;
import zju.homework.augmentedstudio.Shaders.ObjectShader;
import zju.homework.augmentedstudio.Models.ModelObject;
import zju.homework.augmentedstudio.Models.Texture;
import zju.homework.augmentedstudio.Interfaces.ARAppRendererControl;
import zju.homework.augmentedstudio.GL.ARBaseRenderer;
import zju.homework.augmentedstudio.Utils.ARMath;
import zju.homework.augmentedstudio.Utils.Util;

/**
 * Created by stardust on 2016/12/12.
 */

public class ARAppRenderer implements GLSurfaceView.Renderer, ARAppRendererControl{


    private static final String LOGTAG = "ARAppRenderer";


    public enum TrackerMode{
        OBJECT_TRACKER, ROTATION_TRACKER
    }

    private TrackerMode trackerMode = TrackerMode.OBJECT_TRACKER;


    private ARApplicationSession vuforiaAppSession;
    private ARBaseRenderer arBaseRenderer;

    private boolean mIsActive = false;

    private Vector<ModelObject> models;

    private Vector<Texture> mTextures = null;
    private int shaderProgramID;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;
    Texture texture = null;

    // Constants:
    static final float kObjectScale = 3.f;
    private float nearPlane = 0.1f;
    private float farPlane = 1000f;
//    private Teapot mTeapot;

    // Reference to main activity
    private ARSceneActivity mActivity;

    ModelObject cube;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mLookatMatrix = new float[]{
            0, 0, 10f,
            0f, 0, 0,
            0f, 1.0f, 0f
    };

    public ARAppRenderer(ARSceneActivity activity, ARApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;

        arBaseRenderer = new ARBaseRenderer(this, mActivity, Device.MODE.MODE_AR,
                false, nearPlane, farPlane);

        models = new Vector<>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        vuforiaAppSession.onSurfaceCreated();

        arBaseRenderer.onSurfaceCreated();
    }

    void initRendering(){
        Log.i(LOGTAG, "initRendering");

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);

        for(Texture t : mTextures){
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = Util.createProgramFromShaderSrc(ObjectShader.CUBE_MESH_VERTEX_SHADER,
                ObjectShader.CUBE_MESH_FRAGMENT_SHADER);
        if( shaderProgramID > 0 ){
            GLES20.glUseProgram(shaderProgramID);
            texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                    "texSampler2D");
            vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                    "vertexPosition");
            textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                    "vertexTexCoord");
            mvpMatrixHandle =GLES20.glGetUniformLocation(shaderProgramID,
                    "modelViewProjectionMatrix");
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        if(height == 0) { 						//Prevent A Divide By Zero By
            height = 1; 						//Making Height Equal One
        }

        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged: " + String.format("Width:%d, Height:%d", width, height));

        // Call function to update rendering when render surface
        // parameters have changed:
        mActivity.updateRendering();

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        arBaseRenderer.onConfigurationChanged(mIsActive);

        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / (float) height;

        Matrix.perspectiveM(mProjectionMatrix, 0, 45.0f, ratio, nearPlane, farPlane);
//        Util.printMatrix(mProjectionMatrix, 4);
        this.width = width;
        this.height = height;

        // Call function to initialize rendering:
        initRendering();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if( !mIsActive)
            return;
        arBaseRenderer.render();
//        this.renderFrame(null, null);
    }

    @Override
    public void renderFrame(State state, float[] projectionMatrix) {

        arBaseRenderer.renderVideoBackground();
//        Log.i(LOGTAG, "renderFrame");

//        if( true ){
//            return;
//        }
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        float[] modelViewMatrix = new float[16];
        float[] mvpMatrix = new float[16];

//        this.selectObject(100, 100);

        for(int tldx=0; tldx < state.getNumTrackableResults(); tldx++){
            TrackableResult result = state.getTrackableResult(tldx);

            if( !result.isOfType(ObjectTracker.getClassType()) ){
//                Log.i(LOGTAG, result.getType().toString());   // meaningless output
//                continue;
            }

            Trackable trackable = result.getTrackable();

//            printUserData(trackable);
//
            Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(result.getPose());

            if( trackerMode == TrackerMode.OBJECT_TRACKER ){
                modelViewMatrix = modelViewMatrix_Vuforia.getData();
            }else{
                modelViewMatrix = ARMath.Matrix44FTranspose(ARMath.Matrix44FInverse(modelViewMatrix_Vuforia)).getData();
            }

            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
//            Util.printMatrix(mvpMatrix, 4);

            for(int i=0; i<models.size(); i++){

                ModelObject model = models.get(i);
                float scale = model.getScale();
                float[] position = model.getPosition();
                float[] rotation = model.getRotation();

                float[] tmpMvpMatrix = mvpMatrix.clone();

                Matrix.scaleM(tmpMvpMatrix, 0, scale, scale, scale);
                Matrix.rotateM(tmpMvpMatrix, 0, rotation[1], 0, 1, 0);
                Matrix.rotateM(tmpMvpMatrix, 0, rotation[0], 1, 0, 0);
                Matrix.translateM(tmpMvpMatrix, 0, position[0], position[1], position[2]);

                GLES20.glUseProgram(shaderProgramID);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                        false, 0, model.getVertices());

                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT,
                        false, 0, model.getTexCoords());

                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                        mTextures.get(i).mTextureID[0]);
//                    model.getTextureID());

                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                        tmpMvpMatrix, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);

                if( model instanceof ModelObject ){
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                            model.getNumObjectVertex());
                }else{
                    GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                            model.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                            model.getIndices());
                }

                GLES20.glDisableVertexAttribArray(vertexHandle);
                GLES20.glDisableVertexAttribArray(textureCoordHandle);

            }
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

    }

    public boolean getActive() {
        return mIsActive;
    }

    public void setActive(boolean ismIsActive) {
        this.mIsActive = ismIsActive;

        if ( this.mIsActive ){
            arBaseRenderer.configureVideoBackground();
        }
    }

    public Vector<Texture> getTextures() {
        return mTextures;
    }

    public void setTextures(Vector<Texture> mTextures) {
        this.mTextures = mTextures;
    }


    private float oldX;
    private float oldY;
    private final float ROTATE_SPEED = 0.04f;		//Proved to be good for normal rotation ( NEW )
    private final float TRANSFORM_SPEED = 0.01f;
    private boolean isTouching = false;
    private String mode = "Trans";
//    private String mode = "Trans";
    private int width, height;

    private int selectIndex = 0;


    public boolean handleTouchEvent(MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        //If a touch is moved on the screen
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            //Calculate the change
            isTouching = true;
            float dx = x - oldX;
            float dy = y - oldY;
            //Define an upper area of 10% on the screen
            int leftArea = width / 4;

            //Zoom in/out if the touch move has been made in the upper
            if( selectIndex >= 0 && selectIndex < models.size() ){

                if( mode.equals("Rotate") ) {
                    float[] rotation = models.get(selectIndex).getRotation();
                    rotation[0] -= dy * ROTATE_SPEED;
                    rotation[1] += dx * ROTATE_SPEED;
                }else {
                    if(x < leftArea) {
                        float[] position = models.get(selectIndex).getPosition();
                        position[2] -= dy * TRANSFORM_SPEED / 2;
                        //Rotate around the axis otherwise
                    } else {
                        float[] position = models.get(selectIndex).getPosition();
                        position[0] += dx * TRANSFORM_SPEED;
                        position[1] -= dy * TRANSFORM_SPEED;
                    }
                }

            }else if ( selectIndex == models.size() ){		// camera

                if( mode.equals("Rotate") ) {

//                    if( mLookatMatrix[3] < 90 && (mLookatMatrix[3]+dx * ROTATE_SPEED) > 0 )
                        mLookatMatrix[3] += dx * ROTATE_SPEED;
//                    if( mLookatMatrix[4] < 90 && (mLookatMatrix[4]+dy * ROTATE_SPEED) > 0 )
                        mLookatMatrix[4] += dy * ROTATE_SPEED;

                }else {

                    if( x < leftArea ){
                        mLookatMatrix[2] -= dy * TRANSFORM_SPEED;
                        mLookatMatrix[5] -= dy * TRANSFORM_SPEED;
                    }else{
                        mLookatMatrix[0] -= dx * TRANSFORM_SPEED;
                        mLookatMatrix[3] -= dx * TRANSFORM_SPEED;

                        mLookatMatrix[1] += dy * TRANSFORM_SPEED;
                        mLookatMatrix[4] += dy * TRANSFORM_SPEED;
                    }
                }
            }

            if( selectIndex == 0 ){
//                updateLight();
            }

            //A press on the screen
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
//            mRenderer.isTouching = false;


            isTouching = false;
        }

        //Remember the values
        oldX = x;
        oldY = y;

        return true;
    }


    public void changeSelection(int id){
        selectIndex = id;
    }


    private static float ScaleFactor = 0.1f;
    public void changeScale(boolean scaleUp){

        if( selectIndex >= 0 && selectIndex < models.size() ){
            float scale = models.get(selectIndex).getScale();

            if ( scaleUp && scale < 10 )
                scale += ScaleFactor;
            else if( !scaleUp && scale > 1 )
                scale -= ScaleFactor;
            models.get(selectIndex).setScale(scale);

        }

    }

    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }

    public void updateConfiguration()
    {
        arBaseRenderer.onConfigurationChanged(mIsActive);
    }

    public void changeMode(String s) {
        mode = s;
    }

    public Vector<ModelObject> getModels() {
        return models;
    }

    public TrackerMode getTrackerMode() {
        return trackerMode;
    }

    public void setTrackerMode(TrackerMode trackerMode) {
        this.trackerMode = trackerMode;
    }
}
