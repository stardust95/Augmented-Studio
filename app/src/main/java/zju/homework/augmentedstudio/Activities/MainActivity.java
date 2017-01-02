package zju.homework.augmentedstudio.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

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
        Bundle extras = new Bundle();
        extras.putString("group", "123");
        extras.putString("user", "demouser");
        ArrayList<String> datasets = new ArrayList<String>();
        datasets.add("StonesAndChips.xml");
        extras.putStringArrayList("dataset", datasets);

        intent.putExtras(extras);
        MainActivity.this.startActivity(intent);
    }
}
