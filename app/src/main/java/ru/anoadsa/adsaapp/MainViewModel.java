package ru.anoadsa.adsaapp;

import androidx.datastore.preferences.core.Preferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.models.data.User;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Boolean> loggedIn;
    private final MutableLiveData<Boolean> isFirstRun;

    // This is static so its value could persist across multiple MainActivity launches
    private static MutableLiveData<Boolean> appIsLaunching = new MutableLiveData<>(true);;

    public MainViewModel() {
        loggedIn = new MutableLiveData<>(null);
        isFirstRun = new MutableLiveData<>(null);
//        appIsLaunching = new MutableLiveData<>(true);
    }

    public LiveData<Boolean> getLoggedIn() {
        return loggedIn;
    }

    public LiveData<Boolean> getIsFirstRun() {
        return isFirstRun;
    }

    public LiveData<Boolean> getAppIsLaunching() {
        return appIsLaunching;
    }

    public void checkFirstRun() {
        AppPreferences.getIsFirstRun().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isFirstRun) throws Throwable {
                        if (isFirstRun == null) {
                            MainViewModel.this.isFirstRun.setValue(true);
                        } else {
                            MainViewModel.this.isFirstRun.setValue(isFirstRun);
                        }
                    }
                });
    }

    public void setFirstRun(boolean isFirstRun) {
        AppPreferences.setIsFirstRun(isFirstRun).subscribeOn(Schedulers.io()).subscribe();
    }

    public void finishAppLaunching() {
        appIsLaunching.setValue(false);
        // Rerun other observers
        loggedIn.setValue(null);
        isFirstRun.setValue(null);
    }

    public void checkIfLoggedIn() {
        Static.db.userDao().getUser().subscribeOn(Schedulers.io()).concatMap(
            new Function<User, SingleSource<Boolean>>() {
                @Override
                public SingleSource<Boolean> apply(User user) throws Throwable {
                    if (user == null) {
                        return Static.db.userDao().newUser().map(new Function<Boolean, Boolean>() {
                            @Override
                            public Boolean apply(Boolean success) throws Throwable {
                                return false;
                            }
                        });
                    } else {
                        return Single.just(user.getAuthToken() != null && !user.getAuthToken().isEmpty());
                    }
                }
            }
        ).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isLoggedIn) throws Throwable {
                loggedIn.setValue(isLoggedIn);
            }
        });
    }
}