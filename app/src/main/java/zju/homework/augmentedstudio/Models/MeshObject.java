package zju.homework.augmentedstudio.Models;

/**
 * Created by stardust on 2016/12/24.
 */

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import zju.homework.augmentedstudio.Container.TransformData;



public abstract class MeshObject
{

    public final float[] defaultAmbient = new float[]{ 0.2f, 0.2f, 0.2f, 0.2f };
    public final float[] defaultSpecular = new float[]{ 0, 0, 0 };
    public final float[] defaultDiffuse = new float[]{ 0.8f, 0.8f, 0.8f };


    protected String modelName;

    protected List<Material> materials;
    protected float height, width;

    protected float[] color = new float[]{ 0.5f, 0.5f, 0.5f, 1f};

    protected float[] position = new float[3];
    protected float[] rotation = new float[3];
    protected float scale = 30f;
    public final float MAXSCALE = scale * 20;
    public final float MINSCALE = 1;

    protected float[] materialAmbient = defaultAmbient.clone();
    protected float[] materialDiffuse = defaultDiffuse.clone();
    protected float[] materialSpecular = defaultSpecular.clone();
    protected float shine = 50.0f;
    protected float alpha = 1.0f;

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

    @JsonIgnore
    public Buffer getVertices()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_VERTEX);
    }


    @JsonIgnore
    public Buffer getTexCoords()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_TEXTURE_COORD);
    }

    @JsonIgnore
    public Buffer getNormals()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_NORMALS);
    }

    @JsonIgnore
    public Buffer getIndices()
    {
        return getBuffer(BUFFER_TYPE.BUFFER_TYPE_INDICES);
    }


    protected DoubleBuffer fillBuffer(double[] array)
    {
        // Convert to floats because OpenGL doesn't work on doubles, and manually
        // casting each input value would take too much time.
        // Each float takes 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (double d : array)
            bb.putFloat((float) d);
        bb.rewind();

        return bb.asDoubleBuffer();

    }


    protected FloatBuffer fillBuffer(float[] array)
    {
        // Each float takes 4 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (float d : array)
            bb.putFloat(d);
        bb.rewind();

        return bb.asFloatBuffer();

    }


    protected ShortBuffer fillBuffer(short[] array)
    {
        // Each short takes 2 bytes
        ByteBuffer bb = ByteBuffer.allocateDirect(2 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (short s : array)
            bb.putShort(s);
        bb.rewind();

        return bb.asShortBuffer();

    }

    public abstract Buffer getBuffer(BUFFER_TYPE bufferType);

    public abstract int getNumObjectVertex();

    public abstract int getNumObjectIndex();

    public final TransformData getTransform(){
        return new TransformData(this.modelName, position, rotation, scale);
    }

    public void setTransform(TransformData transform){
//        if( !transform.getModelName().equals(this.modelName) )

        position = transform.position.clone();
        rotation = transform.rotation.clone();
        scale = transform.scale;
    }

    public String getModelName() {
        return modelName;
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    public float[] getMaterialAmbient() {
        return materialAmbient;
    }

    public void setMaterialAmbient(float[] materialAmbient) {
        this.materialAmbient = materialAmbient;
    }

    public float[] getMaterialDiffuse() {
        return materialDiffuse;
    }

    public void setMaterialDiffuse(float[] materialDiffuse) {
        this.materialDiffuse = materialDiffuse;
    }

    public float[] getMaterialSpecular() {
        return materialSpecular;
    }

    public void setMaterialSpecular(float[] materialSpecular) {
        this.materialSpecular = materialSpecular;
    }

    public float getShine() {
        return shine;
    }

    public void setShine(float shine) {
        this.shine = shine;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
