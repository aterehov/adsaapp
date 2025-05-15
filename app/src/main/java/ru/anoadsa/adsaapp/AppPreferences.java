package ru.anoadsa.adsaapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;

public class AppPreferences {
    private static RxDataStore<Preferences> dataStoreNoBackup;

    private static Preferences.Key<Boolean> isFirstRun = PreferencesKeys.booleanKey("isFirstRun");

    public static void initDataStore(Context context) {
        if (dataStoreNoBackup == null) {
            dataStoreNoBackup = new RxPreferenceDataStoreBuilder(context, "adsapreferences_nobackup").build();
        }
    }

    public static Single<Boolean> getIsFirstRun() {
        return dataStoreNoBackup.data().map(new Function<Preferences, Boolean>() {
            @Override
            public Boolean apply(Preferences prefs) throws Throwable {
                Boolean isFirstRun = prefs.get(AppPreferences.isFirstRun);
                if (isFirstRun == null) {
                    return true;
                }
                return isFirstRun;
            }
        }).first(true);
    }

    @NonNull
    public static Single<Preferences> setIsFirstRun(boolean isFirstRun) {
        return dataStoreNoBackup.updateDataAsync(new Function<Preferences, Single<Preferences>>() {
            @Override
            public Single<Preferences> apply(Preferences prefsIn) throws Throwable {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.set(AppPreferences.isFirstRun, isFirstRun);
                return Single.just(mutablePreferences);
            }
        });
    }
}
