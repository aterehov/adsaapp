package ru.anoadsa.adsaapp.ui.activities.login;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Response;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.models.data.User;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<Boolean> loginSuccessful;
    private final MutableLiveData<String> loginErrorMessage;
    private final MutableLiveData<Boolean> userLoadedFromServer;

    private boolean running;

    public LoginViewModel() {
        loginSuccessful = new MutableLiveData<>(null);
        loginErrorMessage = new MutableLiveData<>(null);
        userLoadedFromServer = new MutableLiveData<>(false);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean r) {
        running = r;
    }

    public LiveData<Boolean> getLoginSuccessful() {
        return loginSuccessful;
    }

    public LiveData<String> getLoginErrorMessage() {
        return loginErrorMessage;
    }

    public LiveData<Boolean> getUserLoadedFromServer() {
        return userLoadedFromServer;
    }

//    public void

    public void clearIncidents() {
        Static.db.incidentDao().clearIncidents().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Throwable {
                        if (success) {

                        } else {
                            loginErrorMessage.setValue("Не удалось очистить таблицу обращений в базе данных");
                        }
                    }
                });
    }

    public void loadUserDataFromServer() {
        Static.db.userDao().getUser().concatMap(new Function<User, Single<Pair<Boolean, String>>>() {
            @Override
            public Single<Pair<Boolean, String>> apply(User user) throws Throwable {
                if (user != null) {
                    return user.updateFromServer();
                } else {
                    return Single.just(new Pair<Boolean, String>(false, "Не удалось загрузить пользователя из базы данных"));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
            @Override
            public void accept(Pair<Boolean, String> pair) throws Throwable {
                userLoadedFromServer.setValue(true);
            }
        });
    }

    public void login(String phone, String password) {
        if (!Static.checkPhone(phone)) {
            loginErrorMessage.setValue("Неверный формат телефона");
            return;
        }
        Static.db.userDao().getUser().subscribeOn(Schedulers.io()).concatMap(new Function<User, SingleSource<Pair<Boolean, String>>>() {
            @Override
            public SingleSource<Pair<Boolean, String>> apply(User user) throws Throwable {
                return user.login(phone, password);
            }
        })
//                .map(new Function<Response, Pair<Boolean, String>>() {
//            @Override
//            public Pair<Boolean, String> apply(Response response) throws Throwable {
//                Boolean ok = response != null && response.isSuccessful() && response.body() != null;
//                String msg = null;
//                if (!ok) {
//                    msg = Static.getResponseErrorMessage(response);
//                }
//                return new Pair<>(ok, msg);
//            }
//        })
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
            @Override
            public void accept(Pair<Boolean, String> status) throws Throwable {
                if (!status.first) {
                    loginSuccessful.setValue(false);
                    loginErrorMessage.setValue(status.second);
                } else {
                    loginSuccessful.setValue(true);
                }
            }
        });
//                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Response>() {
//            @Override
//            public void accept(Response response) throws Throwable {
//                if (response == null || response.body() == null) {
//                    loginSuccessful.setValue(false);
//                    loginErrorMessage.setValue("Неизвестная ошибка");
//                } else if (!response.isSuccessful()) {
//                    loginSuccessful.setValue(false);
//                    loginErrorMessage.setValue(
//                            new Gson()
//                                    .fromJson(response.body().string(), JsonObject.class)
//                                    .get("message")
//                                    .getAsString()
//                    );
//                } else {
//                    loginSuccessful.setValue(true);
//                }
//            }
//        });
    }
}