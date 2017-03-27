package zju.homework.augmentedstudio.AR;

import android.app.Application;


/**
 * Created by stardust on 2016/12/6.
 */

public class ARStudioApplication extends Application {

    private static Application sInstance;

    // Anywhere in the application where an instance is required, this method
    // can be used to retrieve it.
    public static Application getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

}
