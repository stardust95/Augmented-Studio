package zju.homework.augmentedstudio.Models;

/**
 * Created by stardust on 2016/12/24.
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;

import zju.homework.augmentedstudio.Utils.Tools.DDSReader;
import zju.homework.augmentedstudio.Utils.Tools.TGAReader;


// Support class for the Vuforia samples applications.
// Exposes functionality for loading a texture from the APK.
public class Texture
{

    protected static Map<String, Integer> textureCache = new HashMap<>();

    private static final String LOGTAG = "Vuforia_Texture";

    public int mWidth;          // The width of the texture.
    public int mHeight;         // The height of the texture.
    public int mChannels;       // The number of channels.
    public ByteBuffer mData;    // The pixel data.
    public int[] mTextureID = new int[1];
    public boolean mSuccess = false;


    public static int loadTGAFromStorage(String path){
        int texture = 0;
        try {

            InputStream is = new FileInputStream(path);
            byte [] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            int [] pixels = TGAReader.read(buffer, TGAReader.ABGR);
            int width = TGAReader.getWidth(buffer);
            int height = TGAReader.getHeight(buffer);

            int [] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4);

            IntBuffer texBuffer = IntBuffer.wrap(pixels);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, texBuffer);

//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

            texture = textures[0];

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return texture;
    }

    public static int loadDDSFromStorage(String path) {
        // check the cache if the texture was already loaded
        String cacheKey = "storage_" + path.substring(path.lastIndexOf('/'));
        if (textureCache.containsKey(cacheKey)
                && GLES20.glIsTexture(textureCache.get(cacheKey))) {
            return textureCache.get(cacheKey);
        }

//        AssetManager assetManager = MainActivity.getAppContext().getAssets();

        Bitmap bitmap;
        try {
            InputStream is = new FileInputStream(path);
            byte [] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            int [] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
            int width = DDSReader.getWidth(buffer);
            int height = DDSReader.getHeight(buffer);

            bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        // load the texture
        int texture = loadTextureFromBitmap(bitmap);
        if (texture != 0) {
            // add it to cache
            textureCache.put(cacheKey, texture);
        }
        return texture;
    }

    public static int loadTextureFromStorage(String filepath)
    {

        String cacheKey = "storage_" + filepath.substring(filepath.lastIndexOf('/'));
        if (textureCache.containsKey(cacheKey)
                && GLES20.glIsTexture(textureCache.get(cacheKey))) {
                return textureCache.get(cacheKey);
        }

        Bitmap bitmap = null;
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(filepath);
            BufferedInputStream bufferedStream = new BufferedInputStream(
                    inputStream);

            bitmap = BitmapFactory.decodeStream(bufferedStream);


        } catch (IOException e)
        {
            Log.e(LOGTAG, "Failed to log texture '" + filepath + "' from APK");
            Log.i(LOGTAG, e.getMessage());
            return 0;
        }

        int texture = loadTextureFromBitmap(bitmap);
        if (texture != 0) {
            // add it to cache
            textureCache.put(cacheKey, texture);
        }
        return texture;
    }

    /* Factory function to load a texture from the APK. */
    public static int loadTextureFromApk(String fileName,
                                             AssetManager assets)
    {

        String cacheKey = "asset_" + fileName;
        if (textureCache.containsKey(cacheKey)
            && GLES20.glIsTexture(textureCache.get(cacheKey))) {
            return textureCache.get(cacheKey);
        }
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try
        {
            inputStream = assets.open(fileName, AssetManager.ACCESS_BUFFER);

            BufferedInputStream bufferedStream = new BufferedInputStream(
                    inputStream);

            bitmap = BitmapFactory.decodeStream(bufferedStream);


        } catch (IOException e)
        {
            Log.e(LOGTAG, "Failed to log texture '" + fileName + "' from APK");
            Log.i(LOGTAG, e.getMessage());
            return 0;
        }

        int texture = loadTextureFromBitmap(bitmap);
        if (texture != 0) {
            // add it to cache
            textureCache.put(cacheKey, texture);
        }
        return texture;
    }

    private static int loadTextureFromBitmap(Bitmap bitMap){

        int[] data = new int[bitMap.getWidth() * bitMap.getHeight()];
        bitMap.getPixels(data, 0, bitMap.getWidth(), 0, 0,
                bitMap.getWidth(), bitMap.getHeight());

        return loadTextureFromIntBuffer(data, bitMap.getWidth(),
                bitMap.getHeight());
    }

    private static int loadTextureFromIntBuffer(int[] data, int width,
                                                   int height)
    {
        // Convert:
        int numPixels = width * height;
        byte[] dataBytes = new byte[numPixels * 4];

        for (int p = 0; p < numPixels; ++p)
        {
            int colour = data[p];
            dataBytes[p * 4] = (byte) (colour >>> 16); // R
            dataBytes[p * 4 + 1] = (byte) (colour >>> 8); // G
            dataBytes[p * 4 + 2] = (byte) colour; // B
            dataBytes[p * 4 + 3] = (byte) (colour >>> 24); // A
        }

        Texture texture = new Texture();
        texture.mWidth = width;
        texture.mHeight = height;
        texture.mChannels = 4;

        texture.mData = ByteBuffer.allocateDirect(dataBytes.length).order(
                ByteOrder.nativeOrder());
        int rowSize = texture.mWidth * texture.mChannels;
        for (int r = 0; r < texture.mHeight; r++)
            texture.mData.put(dataBytes, rowSize * (texture.mHeight - 1 - r),
                    rowSize);

        texture.mData.rewind();

        // Cleans variables
        dataBytes = null;
        data = null;

        texture.mSuccess = true;


        GLES20.glGenTextures(1, texture.mTextureID, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.mTextureID[0]);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                texture.mWidth, texture.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, texture.mData);

        return texture.mTextureID[0];
    }
}
