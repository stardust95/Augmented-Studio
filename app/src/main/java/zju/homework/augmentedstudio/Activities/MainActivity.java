package zju.homework.augmentedstudio.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import zju.homework.augmentedstudio.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadARScene();
    }

    private void loadARScene(){
        Intent intent = new Intent(MainActivity.this, ARSceneActivity.class);
        MainActivity.this.startActivity(intent);
    }
}
