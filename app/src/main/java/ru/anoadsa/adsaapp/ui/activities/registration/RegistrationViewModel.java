package ru.anoadsa.adsaapp.ui.activities.registration;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.Async;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.models.data.User;

public class RegistrationViewModel extends ViewModel {
    private MutableLiveData<Boolean> registeredSuccessful;
    private MutableLiveData<String> errorMessage;

    public RegistrationViewModel() {
        registeredSuccessful = new MutableLiveData<Boolean>(false);
        errorMessage = new MutableLiveData<String>(null);
    }

    public LiveData<Boolean> getRegisteredSuccessful() {
        return registeredSuccessful;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        errorMessage.setValue(message);
    }

    public void register(String phone, String password, String surname, String name,
                         String patronymic, String email) {
        // TODO configure phone format check
        if (phone == null || phone.isEmpty()) {
            errorMessage.setValue("Введите телефон");
            registeredSuccessful.setValue(false);
            return;
        }
        if (!Static.checkPhone(phone)) {
            errorMessage.setValue("Неверный формат номера телефона");
            registeredSuccessful.setValue(false);
            return;
        }
        if (password == null || password.length() < 10) {
            errorMessage.setValue("Пароль должен содержать не менее 10 символов");
            registeredSuccessful.setValue(false);
            return;
        }
        if (surname == null || surname.isEmpty()) {
            errorMessage.setValue("Введите фамилию");
            registeredSuccessful.setValue(false);
            return;
        }
        if (email != null && !email.isEmpty() && !email.contains("@")) {
            errorMessage.setValue("Неверный формат e-mail");
            registeredSuccessful.setValue(false);
            return;
        }
        User.register(phone, password, surname, name, patronymic, email)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
//                            registeredSuccessful.setValue(true);
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                        registeredSuccessful.setValue(pair.first);
                    }
                });
    }
}
