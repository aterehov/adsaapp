package ru.anoadsa.adsaapp.ui.activities.menu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.models.data.User;

public class MenuSharedViewModel extends ViewModel {
    private MutableLiveData<Boolean> enableBackOnNavigateUp;

    private MutableLiveData<String> userName;
    private MutableLiveData<String> userPhone;

    public MenuSharedViewModel() {
        enableBackOnNavigateUp = new MutableLiveData<Boolean>(false);
        userName = new MutableLiveData<String>(null);
        userPhone = new MutableLiveData<String>(null);
    }

    public LiveData<Boolean> getEnableBackOnNavigateUp() {
        return enableBackOnNavigateUp;
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<String> getUserPhone() {
        return userPhone;
    }

    public void loadUserDataFromDb() {
        Static.db.userDao().getUser().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Throwable {
//                        String fullName = "";
//                        if (user.getSurname() != null) {
//                            fullName += user.getSurname();
//                        }
//                        if (user.getName() != null) {
//                            if (!fullName.isEmpty()) {
//                                fullName += " ";
//                            }
//                            fullName += user.getName();
//                        }
//                        if (user.getMidname() != null) {
//                            if (!fullName.isEmpty()) {
//                                fullName += " ";
//                            }
//                            fullName += user.getMidname();
//                        }
//                        if (fullName.isEmpty()) {
//                            fullName = "Пользователь";
//                        }
                        String fullName = Static.makeFullName(user.getSurname(), user.getName(),
                                user.getMidname());
                        userName.setValue(fullName);
                        userPhone.setValue(user.getPhone());
                    }
                });
    }

    public void setUserName(String fullName) {
        userName.setValue(fullName);
    }

    public void setUserPhone(String phone) {
        userPhone.setValue(phone);
    }

    public void setEnableBackOnNavigateUp(boolean enableBackOnNavigateUp) {
        this.enableBackOnNavigateUp.setValue(enableBackOnNavigateUp);
    }
}
