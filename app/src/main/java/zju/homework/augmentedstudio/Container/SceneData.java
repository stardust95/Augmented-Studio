package zju.homework.augmentedstudio.Container;

import com.vuforia.Mesh;

import zju.homework.augmentedstudio.Models.MeshObject;
import zju.homework.augmentedstudio.Models.ModelObject;

/**
 * Created by stardust on 2017/1/2.
 */

public class SceneData {
    private String group;

    private String user;

    private ImageTargetData target;

    public SceneData() {
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ImageTargetData getTarget() {
        return target;
    }

    public void setTarget(ImageTargetData target) {
        this.target = target;
    }

    public SceneData(String group, String user, ImageTargetData target) {
        this.group = group;
        this.user = user;
        this.target = target;
    }
}
