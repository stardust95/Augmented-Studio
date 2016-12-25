package zju.homework.augmentedstudio.GL;

import android.content.Context;
import android.opengl.GLSurfaceView;

import zju.homework.augmentedstudio.ARAppRenderer;

/**
 * Created by stardust on 2016/12/24.
 */

public class ARGLView extends GLSurfaceView{

    private String LOG_TAG = ARGLView.class.getName();

    private ARAppRenderer renderer;

    public ARGLView(Context context){
        super(context);
        setEGLContextClientVersion(2);

    }

    @Override
    public void setRenderer(Renderer renderer) {
        super.setRenderer(renderer);
        this.renderer = (ARAppRenderer) renderer;
    }

    public ARAppRenderer getRenderer(){
        return this.renderer;
    }


}
