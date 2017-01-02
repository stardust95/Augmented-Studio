package zju.homework.augmentedstudio.Container;

/**
 * Created by stardust on 2017/1/2.
 */

public class ImageTargetData {

    public String xml;

    public String dat;      // base64 encoded

    public ImageTargetData(){

    }

    public ImageTargetData(String xml, String dat) {
        this.xml = xml;
        this.dat = dat;
    }

}
