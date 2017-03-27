package zju.homework.augmentedstudio.Container;

/**
 * Created by stardust on 2017/1/2.
 */

public class TransformData {

    public String modelName;

    public float[] position = new float[3];
    public float[] rotation = new float[3];
    public float scale = 5f;

    public TransformData() {
    }

    public TransformData(String name, float[] position, float[] rotation, float scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.modelName = name;
    }

    public float[] getPosition() {
        return position;
    }

    public void setPosition(float[] position) {
        this.position = position;
    }

    public float[] getRotation() {
        return rotation;
    }

    public void setRotation(float[] rotation) {
        this.rotation = rotation;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
