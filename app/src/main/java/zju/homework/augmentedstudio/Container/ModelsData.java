package zju.homework.augmentedstudio.Container;

import zju.homework.augmentedstudio.Models.ModelObject;

/**
 * Created by stardust on 2017/1/2.
 */

public class ModelsData {

    private String group;

    private String[] modelName;
    private TransformData[] transforms;

    public ModelsData() { }

    public ModelsData(String group, String[] modelName, TransformData[] transforms) {
        this.group = group;
        this.modelName = modelName;
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

    public TransformData[] getTransforms() {
        return transforms;
    }

    public void setTransforms(TransformData[] transforms) {
        this.transforms = transforms;
    }
}
