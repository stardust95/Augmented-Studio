package zju.homework.augmentedstudio.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import zju.homework.augmentedstudio.Container.ObjectInfoData;
import zju.homework.augmentedstudio.Java.Account;
import zju.homework.augmentedstudio.Java.ImageAdapter;
import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Utils.ActivityCollector;
import zju.homework.augmentedstudio.Utils.Util;

public class MainActivity extends AppCompatActivity {

    private Account mAccount = null;    //当前用户

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        ActivityCollector.addActivity(this);

        setButton();

        //Util.userLogin(MainActivity.this);

        //loadARScene();
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


    private ArrayList<ObjectInfoData> mList = new ArrayList<>();
    private ImageAdapter mAdapter;

    private ListView mObjList;
    private LinearLayout mLocal;
    private LinearLayout mUrl;
    private LinearLayout mOpenAR;
    private Button mClear;

    private void setButton() {
        mAdapter = new ImageAdapter(MainActivity.this, R.layout.image_item, mList);
        mObjList = (ListView)findViewById(R.id.obj_list_view);
        mObjList.setAdapter(mAdapter);

        mLocal = (LinearLayout) findViewById(R.id.open_local_obj);
        mUrl = (LinearLayout) findViewById(R.id.open_url_obj);

        //本地
        mLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showOpenFileDialog(MainActivity.this, Util.REQUEST_OPEN_OBJ);
            }
        });

        //在线
        mUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<ObjectInfoData> tmp = new ArrayList<ObjectInfoData>();

                ImageAdapter imageAdapter = new ImageAdapter(MainActivity.this, R.layout.image_item, tmp);
                ListView listView = new ListView(MainActivity.this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(30, 30, 30, 30);
                listView.setLayoutParams(layoutParams);

                listView.setAdapter(imageAdapter);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(listView);
                builder.setTitle("Choose the Model");

                final AlertDialog alertDialog = builder.create();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ObjectInfoData obj = tmp.get(position);
                        mList.add(obj);
                        mAdapter.notifyDataSetChanged();

                        Util.showDialogWithText(MainActivity.this, "正在加载" + obj.getFilename());

                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        mOpenAR = (LinearLayout) findViewById(R.id.open_ar);
        mOpenAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadARScene();      //在这里打开新的活动
            }
        });

        mClear = (Button)findViewById(R.id.clear_button);
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mList.clear();
                mAdapter.notifyDataSetChanged();
            }
        });
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

        else if(requestCode == Util.REQUEST_OPEN_OBJ) {
            if(resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                ObjectInfoData obj = new ObjectInfoData();
                String filename = uri.getPath();
                obj.setName(filename.substring(filename.lastIndexOf('/')+1));
                obj.setFilename(filename);

                mList.add(obj);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
