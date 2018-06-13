package xd.servicetesting;

import android.app.Application;
import android.content.Context;

public class Application_Manager extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        Application_Manager.context = getApplicationContext();

    }

    public static Context getAppContext() {
        return Application_Manager.context;
    }
}