package zju.homework.augmentedstudio.Java;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.Cube;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by stardust on 2016/11/27.
 */

public class SimpleRenderer extends ARRenderer{

    private int markerID = -1;          // need to be initialize in configureARScene

    private Cube cube = new Cube(40.0f, 0.0f, 0.0f, 20.0f);
    private float angle = 0.0f;
    private boolean spinning = false;

    @Override
    public boolean configureARScene() {

        markerID = ARToolKit.getInstance().addMarker("single;Data/patt.hiro;80");       // whats the marker rule?
        if (markerID < 0) return false;

        return true;

    }

    public void click() {
        spinning = !spinning;
    }

    public void draw(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadMatrixf(ARToolKit.getInstance().getProjectionMatrix(), 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glFrontFace(GL10.GL_CW);

        gl.glMatrixMode(GL10.GL_MODELVIEW);

        if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {

            gl.glLoadMatrixf(ARToolKit.getInstance().queryMarkerTransformation(markerID), 0);

            gl.glPushMatrix();
            gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
            cube.draw(gl);
            gl.glPopMatrix();

            if (spinning) angle += 5.0f;
        }
    }

}
