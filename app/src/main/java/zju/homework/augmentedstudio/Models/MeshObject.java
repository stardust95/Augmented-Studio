package zju.homework.augmentedstudio.Models;

/**
 * Created by stardust on 2016/12/24.
 */

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public abstract class MeshObject
{
    private final float[] defaultAmbient = new float[]{ 0.2f, 0.2f, 0.2f };
    private final float[] defaultSpecular = new float[]{ 0, 0, 0 };
    private final float[] defaultDiffuse = new float[]{ 0.8f, 0.8f, 0.8f };

    private final float[] selectedAmbient = new float[]{ 1f, 0f, 0f };
    private final float[] selectedSpecular = new float[]{ 1f, 0f, 0f };
    private final float[] selectedDiffuse = new float[]{ 1f, 0f, 0f };

    protected float height, width;

    protected float[] position = new float[3];
    protected float[] rotation = new float[3];
    protected float scale = 5f;

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }


    public float[] getPosition() {
        return position;
    }

    public void setPosition(float[] position) {
        this.position = position;
    }

    public void setPosition(float x, float y, float z){
        this.position[0] = x;
        this.position[1] = y;
        this.position[2] = z;
    }


    public float[] getRotation() {
        return rotation;
    }

    public void setRotation(float[] rotation) {
        this.rotation = rotation;
    }

    public void setRotation(float x, float y, float z){
        this.rotation[0] = x;
        this.rotation[1] = y;
        this.rotation[2] = z;
    }

    public enum BUFFER_TYPE{
        BUFFER_TYPE_VERTEX, BUFFER_TYPE_TEXTURE_COORD, BUFFER_TYPE_NORMALS, BUFFER_TYPE_INDICES
    }


    public Buffer getVertices()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_VERTEX);
    }


    public Buffer getTexCoords()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_TEXTURE_COORD);
    }


    public Buffer getNormals()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_NORMALS);
    }


    public Buffer getIndices()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_INDICES);
    }


    protected Buffer fillBuffer(double[] array)
    {
        // Convert to floats because OpenGL doesn't work on doubles, and manually
        // casting each input value would take too much time.
        // Each float takes 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (double d : array)
            bb.putFloat((float) d);
        bb.rewind();

        return bb;

    }


    protected Buffer fillBuffer(float[] array)
    {
        // Each float takes 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (float d : array)
            bb.putFloat(d);
        bb.rewind();

        return bb;

    }


    protected Buffer fillBuffer(short[] array)
    {
        // Each short takes 2 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(2 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : array)
            bb.putShort(s);
        bb.rewind();

        return bb;

    }


    public abstract Buffer getBuffer(BUFFER_TYPE bufferType);


    public abstract int getNumObjectVertex();


    public abstract int getNumObjectIndex();

}
