package zju.homework.augmentedstudio.Models;

import android.opengl.GLES20;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Utils.ObjLoader;

/**
 * Created by stardust on 2017/1/2.
 */
public class ObjObject extends MeshObject{

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private ShortBuffer mIndexBuffer;
    private float vertices[];
    private float textures[];
    private short indices[];

    private int textureId;
    private int[] buffers = new int[3];

    private void init() {

        GLES20.glGenBuffers(3, buffers, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, (vertices.length*4), mVertexBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, (textures.length*4), mTextureBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[2]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, (indices.length*2), mIndexBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void loadFromFile(String filename) throws IOException{
        ObjLoader loader = new ObjLoader();
        loader.loadObj(filename);
//        loader.(filename);

        mVertexBuffer = loader.getVertsBuffer();
//        mTextureBuffer = fillBuffer(textures);
        mIndexBuffer = loader.getIndicesBuffer();

        init();
    }


    public void draw(float[] matrix, int i, int j) {

//        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[2]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
    }


    public void preDraw() {

        GLES20.glFrontFace(GLES20.GL_CCW);

        // Draw with indices
    }


    public void postDraw() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertexBuffer;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTextureBuffer;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndexBuffer;
                break;
            case BUFFER_TYPE_NORMALS:
                result = null;
            default:
                break;
        }
        return result;
    }


    @Override
    public int getNumObjectVertex() {
        return 0;
    }

    @Override
    public int getNumObjectIndex() {
        return 0;
    }
}