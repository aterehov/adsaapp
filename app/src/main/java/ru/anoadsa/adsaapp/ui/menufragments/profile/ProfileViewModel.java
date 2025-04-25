package ru.anoadsa.adsaapp.ui.menufragments.profile;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.models.data.IncidentDisabilityCategorySprav;
import ru.anoadsa.adsaapp.models.data.User;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<String> errorMessage;

    private MutableLiveData<User> user;

    private MutableLiveData<Boolean> resetMenuValues;

    private MutableLiveData<Boolean> enableChangePassword;

    private MutableLiveData<Boolean> passwordSuccessfullyChanged;

    private MutableLiveData<Boolean> logoutSuccessful;

    private MutableLiveData<IncidentDisabilityCategorySprav> disabilityCategorySprav;

    public ProfileViewModel() {
        errorMessage = new MutableLiveData<String>(null);
        user = new MutableLiveData<User>(null);
        resetMenuValues = new MutableLiveData<Boolean>(false);
        enableChangePassword = new MutableLiveData<Boolean>(true);
        passwordSuccessfullyChanged = new MutableLiveData<Boolean>(false);
        logoutSuccessful = new MutableLiveData<Boolean>(false);
        disabilityCategorySprav = new MutableLiveData<IncidentDisabilityCategorySprav>(null);
    }

    public LiveData<IncidentDisabilityCategorySprav> getDisabilityCategorySprav() {
        return disabilityCategorySprav;
    }

    public LiveData<Boolean> getPasswordSuccessfullyChanged() {
        return passwordSuccessfullyChanged;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<Boolean> getResetMenuValues() {
        return resetMenuValues;
    }

    public LiveData<Boolean> getEnableChangePassword() {
        return enableChangePassword;
    }

    public LiveData<Boolean> getLogoutSuccessful() {
        return logoutSuccessful;
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void loadDisabilityCategorySprav() {
        IncidentDisabilityCategorySprav d = new IncidentDisabilityCategorySprav();
        d.updateFromServer().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            disabilityCategorySprav.setValue(d);
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void loadUserFromDb() {
        Static.db.userDao().getUser().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Throwable {
                        ProfileViewModel.this.user.setValue(user);
                    }
                });
    }

    public void updateUserFromServer() {
        user.getValue().updateFromServer().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        // Rerun user observers
                        user.setValue(user.getValue());
                        if (pair.first) {
//                            // Rerun user observers
//                            user.setValue(user.getValue());
                            resetMenuValues.setValue(true);
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void sendUserToServer(String surname, String name, String patronymic, String email,
                                String carBrand, String carNumber, String osago, String snils,
                                String medPolis, LocalDate birthday, String disabilityCategory,
                                 boolean onlySMS) {
        user.getValue().setSurname(surname);
        user.getValue().setName(name);
        user.getValue().setMidname(patronymic);
        user.getValue().setEmail(email);
        user.getValue().setCarBrand(carBrand);
        user.getValue().setCarNumber(carNumber);
        user.getValue().setOsago(osago);
        user.getValue().setSnils(snils);
        user.getValue().setMedPolis(medPolis);
        user.getValue().setBirthdayFromLocalDate(birthday);
        user.getValue().setDisabilityCategory(disabilityCategory);
        user.getValue().setOnlySMS(onlySMS);
        user.getValue().saveChanges().concatMap(new Function<Boolean, Single<Pair<Boolean, String>>>() {
            @Override
            public Single<Pair<Boolean, String>> apply(Boolean success) throws Throwable {
                if (success) {
                    return user.getValue().saveToServer();
                } else {
                    return Single.just(new Pair<Boolean, String>(false, "Ошибка при сохранении пользователя в базу данных"));
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
            @Override
            public void accept(Pair<Boolean, String> pair) throws Throwable {
                if (pair.first) {
                    resetMenuValues.setValue(true);
                } else {
                    errorMessage.setValue(pair.second);
                }
            }
        });
    }

    public void changeUserPassword(String newPassword) {
        user.getValue().changePassword(newPassword).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            errorMessage.setValue("Пароль успешно изменен");
                            passwordSuccessfullyChanged.setValue(true);
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                        enableChangePassword.setValue(true);
                    }
                });
    }

    public void logout() {
        Static.db.userDao().clearUser().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Throwable {
                        if (success) {

                        } else {
                            errorMessage.setValue("Ошибка при выходе из системы");
                        }
                        logoutSuccessful.setValue(success);
                    }
                });
    }
}
