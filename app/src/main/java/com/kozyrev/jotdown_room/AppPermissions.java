package com.kozyrev.jotdown_room;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class AppPermissions {

    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 100;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 101;
    public static final int REQUEST_RECORD_AUDIO = 102;

    private Context context;
    private Activity activity;

    public AppPermissions(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean getNeedPermissions(String[] permissions, int requestCode){
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                if (activity.shouldShowRequestPermissionRationale(permission)){
                    Toast.makeText(context, "Для корректной работы приложения предоставьте необходимые разрешения", Toast.LENGTH_SHORT).show();
                }
                activity.requestPermissions(permissions, requestCode);
                return false;
            }
        }
        return true;
    }
}
