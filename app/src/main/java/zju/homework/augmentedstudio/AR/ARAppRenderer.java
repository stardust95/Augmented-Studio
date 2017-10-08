package zju.homework.augmentedstudio.AR;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vuforia;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import zju.homework.augmentedstudio.Activities.ARSceneActivity;
import zju.homework.augmentedstudio.Models.CubeObject;
import zju.homework.augmentedstudio.Models.Material;
import zju.homework.augmentedstudio.Models.MeshObject;
import zju.homework.augmentedstudio.Models.ModelObject;
import zju.homework.augmentedstudio.Models.ObjObject;
import zju.homework.augmentedstudio.Models.Texture;
import zju.homework.augmentedstudio.Interfaces.ARAppRendererControl;
import zju.homework.augmentedstudio.GL.ARBaseRenderer;
import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Shaders.IShader;
import zju.homework.augmentedstudio.Shaders.LambertShader;
import zju.homework.augmentedstudio.Utils.ARMath;
import zju.homework.augmentedstudio.Utils.Util;

/**
 * Created by stardust on 2016/12/12.
 */

public class ARAppRenderer implements GLSurfaceView.Renderer, ARAppRendererControl{

    private static final String LOGTAG = ARAppRenderer.class.getSimpleName();

    private IShader shader = new LambertShader();

    public enum TrackerMode{
        OBJECT_TRACKER, ROTATION_TRACKER
    }

    private TrackerMode trackerMode = TrackerMode.OBJECT_TRACKER;

    private ARApplicationSession vuforiaAppSession;
    private ARBaseRenderer arBaseRenderer;

    private boolean mIsActive = false;

    private static Vector<MeshObject> models;

    Texture texture = null;

    // Constants:
    private float nearPlane = 0.1f;
    private float farPlane = 10000f;

    private boolean isColorPicking = false;
//    private Teapot mTeapot;

    // Reference to main activity
    private ARSceneActivity mActivity;

    MeshObject cube;

    private float[] lightPos = {30, 30, 30};
    private float[] lightColor = { 0.5f, 0.5f, 0.5f, 1 };

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mLookatMatrix = new float[]{
            0, 0, 10f,
            0f, 0, 0,
            0f, 1.0f, 0f
    };

    private int[] touchPosition = new int[2];

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

    public void initRendering(){
        Log.i(LOGTAG, "initRendering");

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);

        for(MeshObject object : this.getModels()){
            if( object != null && object.getMaterials() != null )
                for(Material material : object.getMaterials()){
                    material.loadTexture();
                }
        }
//        try{
//            String vertex = Util.getStringFromInputStream(Util.getResource().openRawResource(R.raw.lambert_vert_shader));
//            String frag = Util.getStringFromInputStream(Util.getResource().openRawResource(R.raw.lambert_frag_shader));
//            shaderProgramID = Util.createProgramFromShaderSrc(vertex, frag);
//        }catch (IOException ex){
//            ex.printStackTrace();
//        }
        shaderProgramID = Util.createProgramFromShaderSrc(shader.VERTEX_SHADER(), shader.FRAGMENT_SHADER());
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

    private int shaderProgramID;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int colorHandle;
    private int normalHandle;
    private int lightPosHandle;
    private int mvMatrixHandle;
    private int texSampler2DHandle;
    private int isColorPickingHandle;
    private int diffuseHandle;
    private int eyePosHandle;
    private int specularHandle;
    private int shinesHandle;
    private int ambientHandle;
    private int texEnableHandle;

    private void getShaderHandles(){

        if( shaderProgramID > 0 ){
            GLES20.glUseProgram(shaderProgramID);

            mvMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_MVMatrix");

            lightPosHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_LightPos");

            vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "a_Position");

            normalHandle = GLES20.glGetAttribLocation(shaderProgramID, "a_Normal");

            colorHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_Color");

            texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_Texture");

            textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID, "a_TexCoordinate");

            mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_MVPMatrix");

            isColorPickingHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_isColorPicking");

            diffuseHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_Diffuse");

            eyePosHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_EyePos");

            specularHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_Specular");

            shinesHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_Shines");

            ambientHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_Ambient");

            texEnableHandle = GLES20.glGetUniformLocation(shaderProgramID, "u_TextureEnable");

        }

    }

    @Override
    public void renderFrame(State state, float[] projectionMatrix) {

//        Log.i(LOGTAG, "renderFrame");

//        if( true ){
//            return;
//        }

        if( isColorPicking ){
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        }else{
            arBaseRenderer.renderVideoBackground();
        }

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        mActivity.refFreeFrame.render();

        float[] modelViewMatrix = new float[16];
        float[] modelViewMatrixInverse = new float[16];
        float[] mvpMatrix = new float[16];
        float[] cameraPosMatrix = new float[16];
        float[] cameraLookAtMatrix = new float[16];
        float [] position = {0, 0, 0, 1};
        float [] lookAt = {0, 0, 1, 0};
        float [] eyePos = new float[3];
//        this.selectObject(100, 100);

        for(int tldx=0; tldx < state.getNumTrackableResults(); tldx++){
            TrackableResult result = state.getTrackableResult(tldx);

            getShaderHandles();

            if( isColorPickingHandle >= 0 )
                GLES20.glUniform1i(isColorPickingHandle, isColorPicking ? 1 : 0);

//            Trackable trackable = result.getTrackable();
            Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(result.getPose());

            if( trackerMode == TrackerMode.OBJECT_TRACKER ){
                modelViewMatrix = modelViewMatrix_Vuforia.getData();
            }else{
                modelViewMatrix = ARMath.Matrix44FTranspose(ARMath.Matrix44FInverse(modelViewMatrix_Vuforia)).getData();
            }
            Matrix.invertM(modelViewMatrixInverse, 0, modelViewMatrix, 0);
//            Util.printMatrix(modelViewMatrix, 4);
            Matrix.multiplyMV(cameraPosMatrix, 0, modelViewMatrixInverse, 0, position, 0);
            Matrix.multiplyMV(cameraLookAtMatrix, 0, modelViewMatrixInverse, 0, lookAt, 0);
//            Util.printMatrix(cameraPosMatrix, 4);

            eyePos[0] = cameraPosMatrix[0]; eyePos[1] = cameraPosMatrix[1]; eyePos[2] = cameraPosMatrix[2];
            if( eyePosHandle >= 0 )
                GLES20.glUniform3fv(eyePosHandle, 1, eyePos, 0);

            if( lightPosHandle >= 0)
                GLES20.glUniform3fv(lightPosHandle, 1, lightPos, 0);
//                GLES20.glUniform3fv(lightPosHandle, 1, eyePos, 0);

            if( mvMatrixHandle >= 0 )
                GLES20.glUniformMatrix4fv(mvMatrixHandle, 1, false, modelViewMatrix, 0);
//            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

            for(int i=0; i<models.size(); i++){

                MeshObject model = models.get(i);

                if( isColorPicking ){
                    model.setColor(new float[]{ (i & 0x04) >> 2, (i & 0x02) >> 1, i & 0x01, 1 });
                }

                float scale = model.getScale();
                float[] modelPosition = model.getPosition();
                float[] modelRotation = model.getRotation();

                float[] tmpMVMatrix = modelViewMatrix.clone();

                Matrix.translateM(tmpMVMatrix, 0, modelPosition[0], modelPosition[1], modelPosition[2]);
                Matrix.rotateM(tmpMVMatrix, 0, modelRotation[1], 0, 1, 0);
                Matrix.rotateM(tmpMVMatrix, 0, modelRotation[0], 1, 0, 0);
                Matrix.scaleM(tmpMVMatrix, 0, scale, scale, scale);
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tmpMVMatrix, 0);

                GLES20.glUseProgram(shaderProgramID);

                if( texEnableHandle >= 0 )
                    GLES20.glUniform1i(texEnableHandle, 0);

                if( diffuseHandle >= 0 )
                    GLES20.glUniform3fv(diffuseHandle, 1, model.getMaterialDiffuse(), 0);
                if( ambientHandle >= 0 )
                    GLES20.glUniform3fv(ambientHandle, 1, model.getMaterialAmbient(), 0);

                if( specularHandle >= 0 )
                    GLES20.glUniform3fv(specularHandle, 1, model.getMaterialSpecular(), 0);
                if( colorHandle >= 0 )
                    GLES20.glUniform4fv(colorHandle, 1, isColorPicking ? model.getColor() : lightColor, 0);
                if( shinesHandle >= 0 )
                    GLES20.glUniform1f(shinesHandle, model.getShine());

                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                        false, 0, model.getVertices());

                if( textureCoordHandle >= 0 )
                    GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT,
                        false, 0, model.getTexCoords());

                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                        false, 0, model.getNormals());

                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                if( textureCoordHandle >= 0 )
                    GLES20.glEnableVertexAttribArray(textureCoordHandle);

                List<Material> materials = model.getMaterials();
                if( materials!= null && materials.size() > 0 && texSampler2DHandle >= 0 ){
                    for (Material material : materials){
                        if( texEnableHandle >= 0 ) {
                            GLES20.glUniform1i(texEnableHandle, 1);
                            Log.i(LOGTAG, "texture is enabled");
                        }

                        material.loadTexture();
                        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                                material.getGlTexture());
                        GLES20.glUniform1i(texSampler2DHandle, 0);
                    }
                }

                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                        mvpMatrix, 0);

                if( model instanceof ModelObject){
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                            model.getNumObjectVertex());
                }else{
                    GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                            model.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                            model.getIndices());
                }

                GLES20.glDisableVertexAttribArray(vertexHandle);
                if( textureCoordHandle >= 0 )
                    GLES20.glDisableVertexAttribArray(textureCoordHandle);
                GLES20.glDisableVertexAttribArray(normalHandle);

            }
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        if( isColorPicking ){
            int color = ARSceneActivity.getColor(width, height, touchPosition[0], touchPosition[1]);
            this.setColorPicking(false);
            int i = 0;
            for(i=1; i<models.size(); i++){
                int[] icolor = new int[]{(i & 0x04) >> 2, (i & 0x02) >> 1, i & 0x01};
                if( ( icolor[0] == (color & 0xff) / 0xff)
                        && ( icolor[1] == ((color & 0xff00) >> 8) / 0xff)
                        && ( icolor[2] == ((color & 0xff0000) >> 16) / 0xff ) ){
                    changeSelection(i);
                    break;
                }
            }
            if( i == models.size() )
                changeSelection(0);
            renderFrame(state, projectionMatrix);
        }

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

    private float oldX;
    private float oldY;
    private final float ROTATE_SPEED = 0.04f;		//Proved to be good for normal rotation ( NEW )
    private final float TRANSFORM_SPEED = 0.1f;
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
            int rightArea = ( width / 4 ) * 3;

            //Zoom in/out if the touch move has been made in the upper
            if( selectIndex >= 0 && selectIndex < models.size() ){

                if( mode.equals("Rotate") ) {
                    float[] rotation = models.get(selectIndex).getRotation();
                    rotation[0] -= dy * ROTATE_SPEED;
                    rotation[1] += dx * ROTATE_SPEED;
                }else {
                    if(x > rightArea) {
                        float[] position = models.get(selectIndex).getPosition();
                        position[2] -= dy * TRANSFORM_SPEED / 2;
                        //Rotate around the axis otherwise
                    } else {
                        float[] position = models.get(selectIndex).getPosition().clone();
                        position[0] += dy * TRANSFORM_SPEED;
                        position[1] += dx * TRANSFORM_SPEED;
                        if( !checkCollision(selectIndex, position) ){
                            models.get(selectIndex).setPosition(position);
                        }else {
                            Log.i(LOGTAG, "Collided");
                        }

                    }

                }

            }else if ( selectIndex == models.size() ){		// camera

                if( mode.equals("Rotate") ) {

//                    if( mLookatMatrix[3] < 90 && (mLookatMatrix[3]+dx * ROTATE_SPEED) > 0 )
                        mLookatMatrix[3] += dx * ROTATE_SPEED;
//                    if( mLookatMatrix[4] < 90 && (mLookatMatrix[4]+dy * ROTATE_SPEED) > 0 )
                        mLookatMatrix[4] += dy * ROTATE_SPEED;

                }else {

                    if( x > rightArea ){
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

            //A press on the screen
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            isTouching = false;
        }

        //Remember the values
        oldX = x;
        oldY = y;

        return true;
    }


    public void changeSelection(int id){
        selectIndex = id;
        Log.i(LOGTAG, "change selection to " + id);
    }


    private static float ScaleFactor = 1f;
    public void changeScale(boolean scaleUp){
        if( selectIndex >= 0 && selectIndex < models.size() ){
            MeshObject meshObject = models.get(selectIndex);
            float scale = meshObject.getScale();

            if ( scaleUp && scale+ScaleFactor < meshObject.MAXSCALE )
                scale += ScaleFactor;
            else if( !scaleUp && scale-ScaleFactor > meshObject.MINSCALE )
                scale -= ScaleFactor;
            meshObject.setScale(scale);

        }

    }

    private boolean checkCollision(int cubeIndex, float[] position){
//        boolean result = false;
        if( this.getModels().get(cubeIndex) instanceof CubeObject == false )
            return false;
        CubeObject cube = (CubeObject) this.getModels().get(cubeIndex);

        float minX =  position[0] - cube.getScale();
        float maxX = position[0] + cube.getScale();
        float minY =  position[1] - cube.getScale();
        float maxY = position[1] + cube.getScale();
        float minZ =  position[2] - cube.getScale();
        float maxZ = position[2] + cube.getScale();

        for(int i=1; i<models.size(); i++ ){
            if( i == cubeIndex )
                continue;
            if( models.get(i) instanceof CubeObject == false )
                continue;
            CubeObject thisCube = (CubeObject)models.get(i);
            float scale = thisCube.getScale();
            float[] thisPosition = thisCube.getPosition();
            float[] vertices = CubeObject.verticesCoords;
            for(int j=0; j<vertices.length; j+=3){
                double x, y, z;
                x = vertices[j]; y = vertices[j+1]; z = vertices[j+2];
                x = x * scale + thisPosition[0];
                y = y * scale + thisPosition[1];
                z = z * scale + thisPosition[2];
                if( (x >= minX) && (x <= maxX) && (y >= minY) && (y <= maxY) && (z >= minZ) && (z <= maxZ) )
                    return true;
            }
        }
        return false;
    }

    public boolean isColorPicking() {
        return isColorPicking;
    }

    public void setColorPicking(boolean colorPicking) {
        isColorPicking = colorPicking;
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

    public Vector<MeshObject> getModels() {
        return models;
    }

    public TrackerMode getTrackerMode() {
        return trackerMode;
    }

    public void setTrackerMode(TrackerMode trackerMode) {
        this.trackerMode = trackerMode;
    }

    public int[] getTouchPosition() {
        return touchPosition;
    }

    public void setTouchPosition(int[] touchPosition) {
        this.touchPosition = touchPosition;
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public float[] getLightPos() {
        return lightPos;
    }

    public void setLightPos(float[] lightPos) {
        this.lightPos = lightPos;
    }

    public float[] getLightColor() {
        return lightColor;
    }

    public void setLightColor(float[] lightColor) {
        this.lightColor = lightColor;
    }
}
