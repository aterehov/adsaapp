package ru.anoadsa.adsaapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

public class DevSettings {
    public static boolean FILE_REMOVAL_ENABLED = false;
    public static boolean HIDE_DUMMY_EMAIL = true;
    public static String APP_NAME = "ANO ADSA App";
    public static String APP_VERSION_OLD = "1.00";
    public static String APP_VERSION;
    public static long APP_VERSION_CODE;
    public static long maxJWTValidity = 15 * 60 * 1000; // 15 minutes

    public static boolean ENABLE_ALL_FEATURES = true;

    public static void loadAppVersion(@NonNull Context appContext) throws PackageManager.NameNotFoundException {
        APP_VERSION = appContext.getPackageManager().getPackageInfo("ru.anoadsa.adsaapp", 0).versionName;
    }

    public static void loadAppVersionCode(Context appContext) throws PackageManager.NameNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            APP_VERSION_CODE = appContext.getPackageManager().getPackageInfo("ru.anoadsa.adsaapp", 0).getLongVersionCode();
        } else {
            APP_VERSION_CODE = appContext.getPackageManager().getPackageInfo("ru.anoadsa.adsaapp", 0).versionCode;
        }
    }

    @NonNull
    public static String getUserAgent() {
        return DevSettings.APP_NAME
                + "/"
                + DevSettings.APP_VERSION
                + " "
                + System.getProperty("http.agent");
    }
}
