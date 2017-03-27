package zju.homework.augmentedstudio.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import zju.homework.augmentedstudio.Activities.LoginActivity;
import zju.homework.augmentedstudio.Activities.MainActivity;

import static android.R.id.message;

/**
 * Created by stardust on 2016/12/12.
 */

public class Util {

    private static final String LOGTAG = Util.class.getSimpleName();

    private static int BUFFER_SIZE = 1024;

    private static final String HOST = "http://115.159.110.103:3000";
    public static final String URL_ACCOUNT = HOST + "/accounts";
    public static final String URL_OBJECTS = HOST + "/objects";
    public static final String URL_DOWNLOAD = URL_OBJECTS + "/download/";
    public static final String URL_OBJECTLIST = URL_OBJECTS+ "/list";
    public static final String URL_GROUP = HOST + "/groups";

    private static ObjectMapper mapper;

    private static String cacheDir;

    private static AssetManager asset;

    private static Resources resources;

    static {
        mapper = new ObjectMapper();
        mapper.addMixIn(ByteBuffer.class, ByteBufferMixin.class);
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static final int REQUEST_OPEN_OBJ = 1;
    public static final int REQUEST_ASK_FOR_CAMERA = 2;
    public static final int REQUEST_LOGIN = 3;
    public static final int REQUEST_CREATE_GROUP = 4;

    public static final int RESULT_DENIED = 11;
    public static final int RESULT_REQUESTING = 11;
    public static final int RESULT_GRANTED = 11;

    public static int requestPermission(Activity activity, String permission, int requestCode){
        if(ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED ){
            if( ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) ){
                return RESULT_DENIED;
            }else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                return RESULT_REQUESTING;
            }
        }
        return RESULT_GRANTED;
    }

    public static void showOpenFileDialog(@NonNull Activity activity, int requestCode){
        Intent intent = new Intent(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                Intent.ACTION_OPEN_DOCUMENT : Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        activity.startActivityForResult(intent, requestCode);

    }



    public static byte[] getBytesFromInputStream(InputStream is) throws IOException{
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public static String getStringFromInputStream(InputStream is) throws IOException{

        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;

        while ( (line = br.readLine()) != null ){
            sb.append(line);
        }

        is.close();
        br.close();
        return sb.toString();
    }
    public static boolean uriToFile(Context ctx,Uri uri, String path){
        boolean res = true;
        int len;
        byte[] buffer = new byte[BUFFER_SIZE];
        try{
            InputStream is = ctx.getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(path);
            while ( (len = is.read(buffer)) > 0 ){
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
        }catch (FileNotFoundException ex){
            ex.printStackTrace();
            return false;
        }catch (IOException ex){
            ex.printStackTrace();
            return false;
        }

        return res;
    }
    public static InputStream stringToInputStream(String str){
        return new ByteArrayInputStream(str.getBytes());
    }

    public static String inputStreamToBase64(InputStream is){
        byte[] bytes = null;
        try{
            bytes = getBytesFromInputStream(is);
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return new String(Base64.encode(bytes, Base64.DEFAULT));

    }

    public static Uri base64ToFile(String base64str, File tmpFile){
        try{
            FileOutputStream fout = new FileOutputStream(tmpFile);
            fout.write(Base64.decode(base64str, Base64.DEFAULT));
            fout.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return Uri.fromFile(tmpFile);
    }

    //获取当前时间
    public static String getTime() {
        Date now = new Date();
        return getTimeSimple(now);
    }

    public static String getTimeSimple(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String objectToJson(Object obj){
        try{
            return mapper.writeValueAsString(obj);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Object jsonToObject(String json, Class cls){
        try{
            Object obj = mapper.readValue(json, cls);
//            Log.v(LOG_TAG, "content = " + json);
            return obj;
        }
        catch (IOException ex){
            ex.printStackTrace();
            return null;
        }
    }


    public static Object jsonToObject(String json, TypeReference type){
        try{
            Object obj = mapper.readValue(json, type);
            Log.v(LOGTAG, "content = " + json);
            return obj;
        }
        catch (IOException ex){
            ex.printStackTrace();
            return null;
        }
    }
    public static int randInt(int min, int max) {

        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /********************************* GL utils *************************************/

    static int initShader(int shaderType, String source)
    {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0)
        {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);

            int[] glStatusVar = { GLES20.GL_FALSE };
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, glStatusVar,
                    0);
            if (glStatusVar[0] == GLES20.GL_FALSE)
            {
                Log.e(LOGTAG, "Could NOT compile shader " + shaderType + " : "
                        + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }

        }

        return shader;
    }


    public static int createProgramFromShaderSrc(String vertexShaderSrc,
                                                 String fragmentShaderSrc)
    {
        int vertShader = initShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc);
        int fragShader = initShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderSrc);

        if (vertShader == 0 || fragShader == 0)
            return 0;

        int program = GLES20.glCreateProgram();
        if (program != 0)
        {
            GLES20.glAttachShader(program, vertShader);
            checkGLError("glAttchShader(vert)");

            GLES20.glAttachShader(program, fragShader);
            checkGLError("glAttchShader(frag)");

            GLES20.glLinkProgram(program);
            int[] glStatusVar = { GLES20.GL_FALSE };
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, glStatusVar,
                    0);
            if (glStatusVar[0] == GLES20.GL_FALSE)
            {
                Log.e(
                        LOGTAG,
                        "Could NOT link program : "
                                + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }

        return program;
    }


    public static void checkGLError(String op)
    {
        for (int error = GLES20.glGetError(); error != 0; error = GLES20
                .glGetError())
            Log.e(
                    LOGTAG,
                    "After operation " + op + " got glError 0x"
                            + Integer.toHexString(error));
    }


    // Transforms a screen pixel to a pixel onto the camera image,
    // taking into account e.g. cropping of camera image to fit different aspect
    // ratio screen.
    // for the camera dimensions, the width is always bigger than the height
    // (always landscape orientation)
    // Top left of screen/camera is origin
    public static void screenCoordToCameraCoord(int screenX, int screenY,
                                                int screenDX, int screenDY, int screenWidth, int screenHeight,
                                                int cameraWidth, int cameraHeight, int[] cameraX, int[] cameraY,
                                                int[] cameraDX, int[] cameraDY, int displayRotation, int cameraRotation)
    {
        float videoWidth, videoHeight;
        videoWidth = (float) cameraWidth;
        videoHeight = (float) cameraHeight;

        // Compute the angle by which the camera image should be rotated clockwise so that it is
        // shown correctly on the display given its current orientation.
        int correctedRotation = ((((displayRotation*90)-cameraRotation)+360)%360)/90;

        switch (correctedRotation) {

            case 0:
                break;

            case 1:

                int tmp = screenX;
                screenX = screenHeight - screenY;
                screenY = tmp;

                tmp = screenDX;
                screenDX = screenDY;
                screenDY = tmp;

                tmp = screenWidth;
                screenWidth = screenHeight;
                screenHeight = tmp;

                break;

            case 2:
                screenX = screenWidth - screenX;
                screenY = screenHeight - screenY;
                break;

            case 3:

                tmp = screenX;
                screenX = screenY;
                screenY = screenWidth - tmp;

                tmp = screenDX;
                screenDX = screenDY;
                screenDY = tmp;

                tmp = screenWidth;
                screenWidth = screenHeight;
                screenHeight = tmp;

                break;
        }

        float videoAspectRatio = videoHeight / videoWidth;
        float screenAspectRatio = (float) screenHeight / (float) screenWidth;

        float scaledUpX;
        float scaledUpY;
        float scaledUpVideoWidth;
        float scaledUpVideoHeight;

        if (videoAspectRatio < screenAspectRatio)
        {
            // the video height will fit in the screen height
            scaledUpVideoWidth = (float) screenHeight / videoAspectRatio;
            scaledUpVideoHeight = screenHeight;
            scaledUpX = (float) screenX
                    + ((scaledUpVideoWidth - (float) screenWidth) / 2.0f);
            scaledUpY = (float) screenY;
        } else
        {
            // the video width will fit in the screen width
            scaledUpVideoHeight = (float) screenWidth * videoAspectRatio;
            scaledUpVideoWidth = screenWidth;
            scaledUpY = (float) screenY
                    + ((scaledUpVideoHeight - (float) screenHeight) / 2.0f);
            scaledUpX = (float) screenX;
        }

        if (cameraX != null && cameraX.length > 0)
        {
            cameraX[0] = (int) ((scaledUpX / (float) scaledUpVideoWidth) * videoWidth);
        }

        if (cameraY != null && cameraY.length > 0)
        {
            cameraY[0] = (int) ((scaledUpY / (float) scaledUpVideoHeight) * videoHeight);
        }

        if (cameraDX != null && cameraDX.length > 0)
        {
            cameraDX[0] = (int) (((float) screenDX / (float) scaledUpVideoWidth) * videoWidth);
        }

        if (cameraDY != null && cameraDY.length > 0)
        {
            cameraDY[0] = (int) (((float) screenDY / (float) scaledUpVideoHeight) * videoHeight);
        }
    }


    public static float[] getOrthoMatrix(float nLeft, float nRight,
                                         float nBottom, float nTop, float nNear, float nFar)
    {
        float[] nProjMatrix = new float[16];

        int i;
        for (i = 0; i < 16; i++)
            nProjMatrix[i] = 0.0f;

        nProjMatrix[0] = 2.0f / (nRight - nLeft);
        nProjMatrix[5] = 2.0f / (nTop - nBottom);
        nProjMatrix[10] = 2.0f / (nNear - nFar);
        nProjMatrix[12] = -(nRight + nLeft) / (nRight - nLeft);
        nProjMatrix[13] = -(nTop + nBottom) / (nTop - nBottom);
        nProjMatrix[14] = (nFar + nNear) / (nFar - nNear);
        nProjMatrix[15] = 1.0f;

        return nProjMatrix;
    }


    public static void printMatrix(float[] matrix, int n){
        String tmp = "";
        for(int i=1; i<=n*n; i++){
            tmp += String.format("\t%.2f ", matrix[i-1]);
            if( i % n == 0 ){
                Log.i(LOGTAG, tmp);
                tmp = "";
            }
        }
        Log.i(LOGTAG, tmp);
        return;
    }


    /**
     * 用户登录
     * @param context
     */
    public static void userLogin(@NonNull Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);

        activity.startActivityForResult(intent, REQUEST_LOGIN);
    }


    public static void showDialogWithText(Context context, String text) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setMessage(text);
        dialog.setPositiveButton("OK", null);
        dialog.show();
    }

    public static void createAndShowDialog(Context context, String message, final String title) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }


    public static void unzip(String zipFile, String location) throws IOException {
        int size;
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            if ( !location.endsWith("/") ) {
                location += "/";
            }
            File f = new File(location);
            if(!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), BUFFER_SIZE));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();
                    File unzipFile = new File(path);

                    if (ze.isDirectory()) {
                        if(!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        // check for and create parent directories if they don't exist
                        File parentDir = unzipFile.getParentFile();
                        if ( null != parentDir ) {
                            if ( !parentDir.isDirectory() ) {
                                parentDir.mkdirs();
                            }
                        }

                        // unzip the file
                        FileOutputStream out = new FileOutputStream(unzipFile, false);
                        BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
                        try {
                            while ( (size = zin.read(buffer, 0, BUFFER_SIZE)) != -1 ) {
                                fout.write(buffer, 0, size);
                            }

                            zin.closeEntry();
                        }
                        finally {
                            fout.flush();
                            fout.close();
                        }
                    }
                }
            }
            finally {
                zin.close();
            }
        }
        catch (Exception e) {
            Log.e(LOGTAG, "Unzip exception", e);
        }
    }

    public static String getCacheDir() {
        return cacheDir;
    }

    public static void setCacheDir(String cacheDir) {
        Util.cacheDir = cacheDir;
    }

    public static Resources getResource() {
        return resources;
    }

    public static void setResource(Resources assetManager) {
        Util.resources = assetManager;
    }

    public static AssetManager getAsset() {
        return asset;
    }

    public static void setAsset(AssetManager asset) {
        Util.asset = asset;
    }
}
