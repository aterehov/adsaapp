package ru.anoadsa.adsaapp.ui.activities.menu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MenuViewModel extends ViewModel {
    private MutableLiveData<Boolean> firstRun;

    public MenuViewModel() {
        firstRun = new MutableLiveData<Boolean>(true);
    }

    public LiveData<Boolean> getFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun.setValue(firstRun);
    }
}
