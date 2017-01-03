package zju.homework.augmentedstudio.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stardust on 2017/1/3.
 */

public class TextureLoader {

    protected static Map<String, Integer> textureCache = new HashMap<>();


    /**
     * Loads the specified bitmap object as GL texture.
     *
     * @param bitmap The bitmap to load.
     * @return Texture's handle (0 if load failed).
     */
    public static int loadTexture(Bitmap bitmap) {
        int[] texture = new int[] { 0 };

        // allocate a texture object
        GLES20.glGenTextures(1, texture, 0);

        if (texture[0] != 0) {
            // bind to the texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

            // load the bitmap into the bound texture
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        }

        return texture[0];
    }

    /**
     * Loads texture from a bitmap resource but stores it into the local cache using the specified
     * key.
     *
     * @param bitmap The bitmap to load. Can be null to force retrieval from cache (if it exists).
     * @param cacheKey The cache key to prevent loading it multiple times.
     * @return Texture's handle (0 if load failed).
     */
    @SuppressWarnings("unused")
    public static int loadTextureCached(Bitmap bitmap, String cacheKey) {
        if (textureCache.containsKey(cacheKey)) {
            return textureCache.get(cacheKey);
        }

        // load the texture
        if (bitmap == null)
            return 0;

        int texture = loadTexture(bitmap);
        if (texture != 0) {
            // add it to cache
            textureCache.put(cacheKey, texture);
        }
        return texture;
    }

    /**
     * Unloads the specified texture from GPU memory.
     *
     * @param texture The texture's handle to remove.
     */
//    public static void unloadTexture(int texture) {
//        // remove the texture from the cache, if it exists
//        String cacheKey = null;
//        for (Map.Entry<String, Integer> textureEntry: textureCache.entrySet()) {
//            if (textureEntry.getValue().equals(texture))
//                cacheKey = textureEntry.getKey();
//        }
//        if (cacheKey != null)
//            textureCache.remove(cacheKey);
//
//        IntBuffer texToDelete = BufferUtils.asBuffer(new int[]{ texture });
//        GLES20.glDeleteTextures(1, texToDelete);
//    }

    /**
     * Reads and loads a texture bitmap from the specified Android resource ID.
     *
     * @param resourceId The ID of the resource to read.
     * @return Texture's handle (0 if load failed).
     */
//    @SuppressWarnings("unused")
//    public static int loadTextureFromResource(int resourceId) {
//        // check the cache if the texture was already loaded
//        String cacheKey = "res_" + resourceId;
//        if (textureCache.containsKey(cacheKey)) {
//            return textureCache.get(cacheKey);
//        }
//
//        // disable pre-scaling
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inScaled = false;
//
//        // open the resource
//        final Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.getAppContext().getResources(), resourceId, options);
//
//        if (bitmap == null)
//            return 0;
//
//        // load the texture
//        int texture = loadTexture(bitmap);
//        if (texture != 0) {
//            // add it to cache
//            textureCache.put(cacheKey, texture);
//        }
//        return texture;
//    }

    /**
     * Reads and loads a texture bitmap from the specified Android asset.
     *
     * @param path The path of the image asset to load.
     * @return Texture's handle (0 if load failed).
     */
    @SuppressWarnings("unused")
    public static int loadTextureFromStorage(String path) {
        // check the cache if the texture was already loaded
        String cacheKey = "storage_" + path;
        if (textureCache.containsKey(cacheKey)) {
            return textureCache.get(cacheKey);
        }

//        AssetManager assetManager = MainActivity.getAppContext().getAssets();

        Bitmap bitmap;
        try {
            InputStream assetStream = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(assetStream);

        } catch (IOException e) {
            return 0;
        }

        // load the texture
        int texture = loadTexture(bitmap);
        if (texture != 0) {
            // add it to cache
            textureCache.put(cacheKey, texture);
        }
        return texture;
    }

    /**
     * Cleans up the internal state of the object.
     * To be called when the EGL context has been reset.
     */
    public static void clear() {
        textureCache.clear();
    }


}
