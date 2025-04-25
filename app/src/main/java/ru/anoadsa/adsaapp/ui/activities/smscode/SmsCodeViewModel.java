package ru.anoadsa.adsaapp.ui.activities.smscode;

import android.util.Pair;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.models.data.User;

public class SmsCodeViewModel extends ViewModel {
    private MutableLiveData<String> errorMessage;

    private MutableLiveData<Boolean> actionSuccessful;

    private MutableLiveData<Boolean> resendSuccessful;

    public SmsCodeViewModel() {
        errorMessage = new MutableLiveData<String>(null);
        actionSuccessful = new MutableLiveData<Boolean>(false);
        resendSuccessful = new MutableLiveData<Boolean>(false);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getActionSuccessful() {
        return actionSuccessful;
    }

    public LiveData<Boolean> getResendSuccessful() {
        return resendSuccessful;
    }

    public void resendRegistrationCode(String phone) {
        // TODO check phone format
        if (phone == null || phone.isEmpty()) {
            errorMessage.setValue("Введите номер телефона");
            resendSuccessful.setValue(false);
            return;
        }
        User.resendRegistrationSms(phone).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            errorMessage.setValue("Код подтверждения выслан");
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                        resendSuccessful.setValue(pair.first);
                    }
                });
    }

    public void confirmRegistration(String phone, String code) {
        // TODO check phone format
        if (phone == null || phone.isEmpty()) {
            errorMessage.setValue("Введите номер телефона");
            actionSuccessful.setValue(false);
            return;
        }
        if (code == null || code.isEmpty()) {
            errorMessage.setValue("Введите код подтверждения");
            actionSuccessful.setValue(false);
            return;
        }
        User.confirmRegistration(phone, code).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {

                        } else {
                            errorMessage.setValue(pair.second);
                        }
                        actionSuccessful.setValue(pair.first);
                    }
                });
    }

    public void sendPasswordResetCode(String phone) {
        if (phone == null || phone.isEmpty()) {
            errorMessage.setValue("Введите номер телефона");
            resendSuccessful.setValue(false);
            return;
        }
        User.requestResetPasswordCode(phone).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            errorMessage.setValue("Код подтверждения выслан");
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                        resendSuccessful.setValue(pair.first);
                    }
                });
    }

    public void resetPassword(String phone, String password, String code) {
        if (phone == null || phone.isEmpty()) {
            errorMessage.setValue("Введите номер телефона");
            actionSuccessful.setValue(false);
            return;
        }
        if (password == null || password.length() < 10) {
            errorMessage.setValue("Пароль должен содержать не менее 10 символов");
            actionSuccessful.setValue(false);
            return;
        }
        if (code == null || code.isEmpty()) {
            errorMessage.setValue("Введите код подтверждения");
            actionSuccessful.setValue(false);
            return;
        }

        User.resetPassword(phone, password, code).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {

                        } else {
                            errorMessage.setValue(pair.second);
                        }
                        actionSuccessful.setValue(pair.first);
                    }
                });
    }
}
