package zju.homework.augmentedstudio.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import zju.homework.augmentedstudio.Container.ObjectInfoData;
import zju.homework.augmentedstudio.Java.Account;
import zju.homework.augmentedstudio.Java.ImageAdapter;
import zju.homework.augmentedstudio.R;
import zju.homework.augmentedstudio.Utils.ActivityCollector;
import zju.homework.augmentedstudio.Utils.NetworkManager;
import zju.homework.augmentedstudio.Utils.Util;

public class MainActivity extends AppCompatActivity {

    private static String LOGTAG = MainActivity.class.getSimpleName();

    private Account mAccount = null;    //当前用户

    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Util.setCacheDir(getExternalCacheDir().getAbsolutePath());
        Util.setResource(getResources());
        Util.setAsset(getAssets());

        networkManager = new NetworkManager();

        ActivityCollector.addActivity(this);

        setButton();

        mAccount = new Account("admin");
        //Util.userLogin(MainActivity.this);

        //loadARScene();
    }

    /**
     * 加载AR场景
     */
    private void loadARScene(){
        Intent intent = new Intent(MainActivity.this, ARSceneActivity.class);
        Bundle extras = new Bundle();
        if( mAccount != null ){
            if( mAccount.getGroup() != null )
                extras.putString(ARSceneActivity.BUNDLE_GROUP, mAccount.getGroup().getId());
            extras.putString(ARSceneActivity.BUNDLE_USER, mAccount.getID());
        }
        ArrayList<String> datasets = new ArrayList<String>();
        datasets.add("StonesAndChips.xml");
        extras.putStringArrayList(ARSceneActivity.BUNDLE_DATASET, datasets);
        extras.putParcelableArrayList(ARSceneActivity.BUNDLE_OBJECTS, objectInfoList);

        intent.putExtras(extras);
        MainActivity.this.startActivity(intent);
    }


    private ArrayList<ObjectInfoData> objectInfoList = new ArrayList<>();
    private ImageAdapter mAdapter;

    private ListView mObjList;
    private LinearLayout mLocal;
    private LinearLayout mUrl;
    private LinearLayout mOpenAR;
    private Button mClear;

    private void downloadModel(final ObjectInfoData objectInfoData){
        final String modelName = objectInfoData.getName();
        final String filepath = getExternalCacheDir() + "/" + modelName + ".zip";

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Boolean result = false;
                makeToast("downloading model " + modelName);
                if( (new File(filepath)).exists() ){
                    result = true;
                }else{
                    Log.i(LOGTAG, "downloading model " + modelName);
                    result = networkManager.getArchiveFile(Util.URL_DOWNLOAD + modelName, filepath);
                }
                return result;
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
                if( result == Boolean.FALSE ){
                    return;
                }
                try{
                    String folder = getExternalCacheDir() + "/" + modelName;
                    Util.unzip(filepath, folder);
//                    Log.i(LOGTAG, /);
                    File[] files = (new File(folder)).listFiles();
                    for(File file : files){
                        Log.i(LOGTAG, "unzipped file: " + file.getName());
                        if( file.getName().contains(".obj") ){
                            ObjectInfoData obj = new ObjectInfoData();
                            obj.setName(modelName);
                            obj.setFilename(file.getAbsolutePath());
                            obj.setImageUrl(objectInfoData.getImageUrl());
                            objectInfoList.add(obj);
                            mAdapter.notifyDataSetChanged();
                            makeToast("download files saved in " + folder);
                        }
                    }
                }catch (IOException ex){
                    ex.printStackTrace();
                }

            }
        };
        task.execute();
    }

    private void setButton() {
        mAdapter = new ImageAdapter(MainActivity.this, R.layout.image_item, objectInfoList);
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

                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {

                        String result = networkManager.getJson(Util.URL_OBJECTLIST);
                        return result;
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        super.onPostExecute(result);
                        if( result == null ){
                            Log.i(LOGTAG, "download list is null");
                            return;
                        }
                        final ArrayList<ObjectInfoData> allObjectList = (ArrayList<ObjectInfoData>)Util.
                                jsonToObject((String) result, new TypeReference<ArrayList<ObjectInfoData>>() {});

                        ImageAdapter imageAdapter = new ImageAdapter(MainActivity.this, R.layout.image_item, allObjectList);
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
                                ObjectInfoData obj = allObjectList.get(position);
                                downloadModel(allObjectList.get(position));
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }
                };

                task.execute();

            }
        });

        mOpenAR = (LinearLayout) findViewById(R.id.open_ar);
        mOpenAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = Util.requestPermission(MainActivity.this, Manifest.permission.CAMERA, Util.REQUEST_ASK_FOR_CAMERA);
                if( result == Util.RESULT_GRANTED){
                    loadARScene();
                }else if( result == Util.RESULT_DENIED ){
                    makeToast("No camera permissions");
                }
            }
        });

        mClear = (Button)findViewById(R.id.clear_button);
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                objectInfoList.clear();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Util.REQUEST_ASK_FOR_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadARScene();
                } else {
                    makeToast("No camera permissions");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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

                objectInfoList.add(obj);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void makeToast(final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, content, Toast.LENGTH_LONG).show();
            }
        });
    }
}
