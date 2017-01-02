package zju.homework.augmentedstudio.Container;

import zju.homework.augmentedstudio.Models.ModelObject;

/**
 * Created by stardust on 2017/1/2.
 */

public class ModelsData {

    private String group;

    private String[] modelName;
    private String[] modelData;
    private TransformData[] transforms;

    public ModelsData() {
    }

    public ModelsData(String group, String[] modelName, String[] modelData, TransformData[] transforms) {
        this.group = group;
        this.modelName = modelName;
        this.modelData = modelData;
        this.transforms = transforms;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String[] getModelName() {
        return modelName;
    }

    public void setModelName(String[] modelName) {
        this.modelName = modelName;
    }

    public String[] getModelData() {
        return modelData;
    }

    public void setModelData(String[] modelData) {
        this.modelData = modelData;
    }

    public TransformData[] getTransforms() {
        return transforms;
    }

    public void setTransforms(TransformData[] transforms) {
        this.transforms = transforms;
    }
}
