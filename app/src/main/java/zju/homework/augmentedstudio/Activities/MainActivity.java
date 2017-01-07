package zju.homework.augmentedstudio.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import java.util.ArrayList;

import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Utils.ActivityCollector;
import zju.homework.augmentedstudio.Utils.Util;

import zju.homework.augmentedstudio.Java.Account;

public class MainActivity extends AppCompatActivity {

    private Account mAccount = null;    //当前用户

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        ActivityCollector.addActivity(this);

        //Util.userLogin(MainActivity.this);

        loadARScene();
    }

    /**
     * 加载AR场景
     */
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == Util.REQUEST_LOGIN ) {
            if( resultCode == Activity.RESULT_OK && data != null) {
                String email = data.getStringExtra(LoginActivity.GET_EMAIL_KEY);
                mAccount = new Account(email);
            }
        }
    }
}
