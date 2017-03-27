package zju.homework.augmentedstudio.Interfaces;

import com.vuforia.State;

/**
 * Created by stardust on 2016/12/12.
 */

public interface ARAppRendererControl {

    // This method has to be implemented by the Renderer class which handles the content rendering
    // of the sample, this one is called from SampleAppRendering class for each view inside a loop
    void renderFrame(State state, float[] projectionMatrix);

}
