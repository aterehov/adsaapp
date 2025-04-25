package ru.anoadsa.adsaapp.ui.menufragments.sos;

import android.location.Location;
import android.util.Pair;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.Geo;

public class SosViewModel extends ViewModel {
    private MutableLiveData<Float> latitude;
    private MutableLiveData<Float> longitude;
    private MutableLiveData<String> address;
    private MutableLiveData<Boolean> next;

    private MutableLiveData<Boolean> finishedGeoUpdate;

    private boolean updateLocation = false;

    public SosViewModel() {
        latitude = new MutableLiveData<Float>(null);
        longitude = new MutableLiveData<Float>(null);
        address = new MutableLiveData<String>(null);
        next = new MutableLiveData<Boolean>(false);
        finishedGeoUpdate = new MutableLiveData<Boolean>(false);
    }

    public LiveData<Boolean> getFinishedGeoUpdate() {
        return finishedGeoUpdate;
    }

    public void prepareForLocationUpdate() {
        updateLocation = true;
    }

    public void subscribeOnLocationUpdates(LifecycleOwner owner) {
        Geo.getLocation().observe(owner, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (!updateLocation) {
                    return;
                }
                if (!Geo.getUsedGps() || !Geo.getUsedNetwork()) {
                    return;
                }
                updateLocation = false;
                if (location == null) {
                    latitude.setValue(null);
                    longitude.setValue(null);
                } else {
                    latitude.setValue((float) location.getLatitude());
                    longitude.setValue((float) location.getLongitude());
                }
                finishedGeoUpdate.setValue(true);
            }
        });
    }

    public LiveData<Float> getLatitude() {
        return latitude;
    }

    public LiveData<Float> getLongitude() {
        return longitude;
    }

    public LiveData<Boolean> getNext() {
        return next;
    }

    public LiveData<String> getAddress() {
        return address;
    }


    public void setLatitude(Float lat) {
        latitude.setValue(lat);
    }

    public void setLongitude(Float lon) {
        longitude.setValue(lon);
    }

    public void setAddress(String a) {
        address.setValue(a);
    }

    public void setNext(boolean n) {
        next.setValue(n);
    }

    public void resetAll() {
        latitude.setValue(null);
        longitude.setValue(null);
        address.setValue(null);
        next.setValue(false);
        finishedGeoUpdate.setValue(false);
    }

    public void getAddressByCoordinates(float lat, float lon) {
        Geo.coordinatesToAddress(lat, lon).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            address.setValue(pair.second);
//                            addressRefreshNeeded.setValue(false);
                        } else {
                            address.setValue("");
//                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void getCoordinatesByAddress(String address) {
        Geo.addressToCoordinates(address).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Float, Float>>() {
                    @Override
                    public void accept(Pair<Float, Float> pair) throws Throwable {
                        if (pair.first == null) {
//                            errorMessage.setValue("Не удалось получить координаты по адресу");
                        } else {
                            latitude.setValue(pair.first);
                            longitude.setValue(pair.second);
//                            addressRefreshNeeded.setValue(false);
                        }
                    }
                });
    }
}
