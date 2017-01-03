//package zju.homework.augmentedstudio.Utils;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.opengl.GLES20;
//import android.opengl.GLUtils;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.IntBuffer;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.microedition.khronos.opengles.GL;
//
//import ddsutil.DDSUtil;
//import model.DDSFile;
//
///**
// * Created by stardust on 2017/1/3.
// */
//
//public class TextureLoader {
//
//    protected static Map<String, Integer> textureCache = new HashMap<>();
//
//
//    /**
//     * Loads the specified bitmap object as GL texture.
//     *
//     * @param bitmap The bitmap to load.
//     * @return Texture's handle (0 if load failed).
//     */
//    public static int loadTexture(Bitmap bitmap) {
//        int[] texture = new int[1];
//
//        // allocate a texture object
//        GLES20.glGenTextures(1, texture, 0);
//
//        if (texture[0] != 0) {
//            // bind to the texture
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
//
//            // load the bitmap into the bound texture
//            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//        }
//
//        return texture[0];
//    }
//
//    /**
//     * Loads texture from a bitmap resource but stores it into the local cache using the specified
//     * key.
//     *
//     * @param bitmap The bitmap to load. Can be null to force retrieval from cache (if it exists).
//     * @param cacheKey The cache key to prevent loading it multiple times.
//     * @return Texture's handle (0 if load failed).
//     */
//    @SuppressWarnings("unused")
//    public static int loadTextureCached(Bitmap bitmap, String cacheKey) {
//        if (textureCache.containsKey(cacheKey)) {
//            return textureCache.get(cacheKey);
//        }
//
//        // load the texture
//        if (bitmap == null)
//            return 0;
//
//        int texture = loadTexture(bitmap);
//        if (texture != 0) {
//            // add it to cache
//            textureCache.put(cacheKey, texture);
//        }
//        return texture;
//    }
//
//    /**
//     * Unloads the specified texture from GPU memory.
//     *
//     * @param texture The texture's handle to remove.
//     */
////    public static void unloadTexture(int texture) {
////        // remove the texture from the cache, if it exists
////        String cacheKey = null;
////        for (Map.Entry<String, Integer> textureEntry: textureCache.entrySet()) {
////            if (textureEntry.getValue().equals(texture))
////                cacheKey = textureEntry.getKey();
////        }
////        if (cacheKey != null)
////            textureCache.remove(cacheKey);
////
////        IntBuffer texToDelete = BufferUtils.asBuffer(new int[]{ texture });
////        GLES20.glDeleteTextures(1, texToDelete);
////    }
//
//    /**
//     * Reads and loads a texture bitmap from the specified Android resource ID.
//     *
//     * @param resourceId The ID of the resource to read.
//     * @return Texture's handle (0 if load failed).
//     */
////    @SuppressWarnings("unused")
////    public static int loadTextureFromResource(int resourceId) {
////        // check the cache if the texture was already loaded
////        String cacheKey = "res_" + resourceId;
////        if (textureCache.containsKey(cacheKey)) {
////            return textureCache.get(cacheKey);
////        }
////
////        // disable pre-scaling
////        final BitmapFactory.Options options = new BitmapFactory.Options();
////        options.inScaled = false;
////
////        // open the resource
////        final Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.getAppContext().getResources(), resourceId, options);
////
////        if (bitmap == null)
////            return 0;
////
////        // load the texture
////        int texture = loadTexture(bitmap);
////        if (texture != 0) {
////            // add it to cache
////            textureCache.put(cacheKey, texture);
////        }
////        return texture;
////    }
//
//    /**
//     * Reads and loads a texture bitmap from the specified Android asset.
//     *
//     * @param path The path of the image asset to load.
//     * @return Texture's handle (0 if load failed).
//     */
//    @SuppressWarnings("unused")
//    public static int loadTextureFromStorage(String path) {
//        // check the cache if the texture was already loaded
//        String cacheKey = "storage_" + path.lastIndexOf('/');
//        if (textureCache.containsKey(cacheKey)) {
//            return textureCache.get(cacheKey);
//        }
//
////        AssetManager assetManager = MainActivity.getAppContext().getAssets();
//
//        Bitmap bitmap;
//        try {
//            InputStream assetStream = new FileInputStream(path);
//            bitmap = BitmapFactory.decodeStream(assetStream);
//
//
//        } catch (IOException e) {
//            return 0;
//        }
//
//        // load the texture
//        int texture = loadTexture(bitmap);
//        if (texture != 0) {
//            // add it to cache
//            textureCache.put(cacheKey, texture);
//        }
//        return texture;
//    }
//
//    public static int loadDDSFromStorage(String path) {
//        // check the cache if the texture was already loaded
//        String cacheKey = "storage_" + path.lastIndexOf('/');
//        if (textureCache.containsKey(cacheKey)) {
//            return textureCache.get(cacheKey);
//        }
//
////        AssetManager assetManager = MainActivity.getAppContext().getAssets();
//
//        Bitmap bitmap;
//        try {
//            InputStream is = new FileInputStream(path);
//            byte [] buffer = new byte[is.available()];
//            is.read(buffer);
//            is.close();
//
//            int [] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
//            int width = DDSReader.getWidth(buffer);
//            int height = DDSReader.getHeight(buffer);
//
//            bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return 0;
//        }
//
//        // load the texture
//        int texture = loadTexture(bitmap);
//        if (texture != 0) {
//            // add it to cache
//            textureCache.put(cacheKey, texture);
//        }
//        return texture;
//    }
//
//
////
////    public static int loadDDSTexture(String path){
////        int texture = 0;
////        String cacheKey = "storage_" + path.lastIndexOf('/');
////        if (textureCache.containsKey(cacheKey)) {
////            return textureCache.get(cacheKey);
////        }
////
////        try {
////            FileInputStream fis = new FileInputStream(path);
////            byte [] buffer = new byte[fis.available()];
////            fis.read(buffer);
////            fis.close();
////
////            int [] pixels = DDSReader.read(buffer, DDSReader.ABGR, 0);
////            int width = DDSReader.getWidth(buffer);
////            int height = DDSReader.getHeight(buffer);
////            int mipmap = DDSReader.getMipmap(buffer);
////
////            int [] textures = new int[1];
////             GLES20.glGenTextures(1, textures, 0);
////
////             GLES20.glEnable(GLES20.GL_TEXTURE_2D);
////             GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
////             GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4);
////
////            if(mipmap > 0) {
////                // mipmaps
////                for(int i=0; (width > 0) || (height > 0); i++) {
////                    if(width <= 0) width = 1;
////                    if(height <= 0) height = 1;
////                    int [] mm_pixels = DDSReader.read(buffer, DDSReader.ABGR, i);
////
////                    IntBuffer texBuffer = IntBuffer.wrap(mm_pixels);
////                    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, i, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, texBuffer);
////
////                    width /= 2;
////                    height /= 2;
////                }
////
////                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
////                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
////                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
////                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
////            }
////            else {
////                // no mipmaps
////                int [] mm_pixels = DDSReader.read(buffer, DDSReader.ABGR, 0);
////
////                IntBuffer texBuffer = IntBuffer.wrap(mm_pixels);
////                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, texBuffer);
////
////                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
////                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
////                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
////                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
////
////            }
////
////            texture = textures[0];
////        }
////        catch(Exception e) {
////            e.printStackTrace();
////        }
////
////        return texture;
////
////    }
//
//    /**
//     * Cleans up the internal state of the object.
//     * To be called when the EGL context has been reset.
//     */
//    public static void clear() {
//        textureCache.clear();
//    }
//
//
//}
