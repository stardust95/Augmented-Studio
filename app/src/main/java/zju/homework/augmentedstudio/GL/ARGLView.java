package zju.homework.augmentedstudio.GL;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by stardust on 2016/12/24.
 */

public class ARGLView extends GLSurfaceView{

    private String LOG_TAG = ARGLView.class.getName();

    public ARGLView(Context context){
        super(context);
        setEGLContextClientVersion(2);
    }



}
