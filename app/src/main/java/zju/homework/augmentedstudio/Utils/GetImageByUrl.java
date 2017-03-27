package zju.homework.augmentedstudio.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 马啸远 on 2017/1/7.
 */

/**
 * 通过一个URL获取图片
 */
public class GetImageByUrl {

    private PicHandler pic_hdl;
    private ImageView imgView;
    private String url;
    private NetworkManager networkManager;


    public GetImageByUrl() {
        networkManager = new NetworkManager();
    }

    /**
     * 通过图片url路径获取图片并显示到对应控件上
     *
     * @param imgView
     * @param url
     */



    public void setImage(ImageView imgView, String url) {
        this.url = url;
        this.imgView = imgView;
        pic_hdl = new PicHandler();
        Thread t = new LoadPicThread();
        t.start();
    }


    class LoadPicThread extends Thread {
        @Override
        public void run() {
            Bitmap img = getUrlImage(url);
//            System.out.println(img + "---");
            Message msg = pic_hdl.obtainMessage();
            msg.what = 0;
            msg.obj = img;
            pic_hdl.sendMessage(msg);
        }
    }

    class PicHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bitmap myimg = (Bitmap) msg.obj;
            imgView.setImageBitmap(myimg);
        }

    }

    public Bitmap getUrlImage(String url) {
        Bitmap img ;
        String filepath = Util.getCacheDir() + url.substring(url.lastIndexOf('/'));
        if( (new File(filepath)).exists() == false )
            networkManager.getArchiveFile(url, filepath);
        img = BitmapFactory.decodeFile(filepath);
        return img;
    }
}
