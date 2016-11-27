package zju.homework.augmentedstudio.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;


import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import zju.homework.augmentedstudio.R;



public class ARSceneActivity extends ARActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arscene);
    }

    @Override
    protected FrameLayout supplyFrameLayout() {
        return null;
    }

    @Override
    protected ARRenderer supplyRenderer() {
        return null;
    }
}
