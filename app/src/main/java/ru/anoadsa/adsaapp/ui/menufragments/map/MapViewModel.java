package ru.anoadsa.adsaapp.ui.menufragments.map;

import android.location.Location;
import android.util.Pair;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import org.mapsforge.core.model.LatLong;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.Geo;
import ru.anoadsa.adsaapp.models.data.AbonentsNearbyMap;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.models.data.IncidentAbonentCategorySprav;
import ru.anoadsa.adsaapp.models.data.Video;

public class MapViewModel extends ViewModel {
    private MutableLiveData<Incident> incident;
    private MutableLiveData<Boolean> hasIncident;
    private MutableLiveData<Video> video;

    private MutableLiveData<String> errorMessage;

//    private MutableLiveData<Location> location;
    private MutableLiveData<Boolean> locationUpdated;

    private MutableLiveData<LatLong> userLocation;

//    private MutableLiveData<String> address;

    private MutableLiveData<LatLong> typedLocation;

    private MutableLiveData<String> userAddress;
    private MutableLiveData<String> typedAddress;

    private MutableLiveData<Boolean> geoDialogFinished;
//    private MutableLiveData<Boolean> categoryDialogFinished;

    private MutableLiveData<IncidentAbonentCategorySprav> categorySprav;
    private MutableLiveData<String> selectedCategoryName;

    private MutableLiveData<AbonentsNearbyMap> abonentsNearbyMap;

    private boolean allowLocationUpdate = false;
    private String locationUpdateType;

    public MapViewModel() {
        incident = new MutableLiveData<Incident>(null);
        hasIncident = new MutableLiveData<Boolean>(null);
        video = new MutableLiveData<Video>(null);
        errorMessage = new MutableLiveData<String>(null);
//        location = new MutableLiveData<Location>(null);
        locationUpdated = new MutableLiveData<Boolean>(false);

        userLocation = new MutableLiveData<LatLong>(null);
//        address = new MutableLiveData<String>(null);
        typedLocation = new MutableLiveData<LatLong>(null);
        userAddress = new MutableLiveData<String>(null);
        typedAddress = new MutableLiveData<String>(null);
        geoDialogFinished = new MutableLiveData<Boolean>(false);
        categorySprav = new MutableLiveData<IncidentAbonentCategorySprav>(null);
        selectedCategoryName = new MutableLiveData<String>(null);
        abonentsNearbyMap = new MutableLiveData<AbonentsNearbyMap>(null);
    }

    public LiveData<AbonentsNearbyMap> getAbonentsNearbyMap() {
        return abonentsNearbyMap;
    }

    public void searchAbonents(String category, float radius, float lat, float lon) {
        AbonentsNearbyMap map = abonentsNearbyMap.getValue();
        if (map == null) {
            map = new AbonentsNearbyMap();
        }
//        if (abonentsNearbyMap == null) {
//            abonentsNearbyMap = new AbonentsNearbyMap();
//        }
        AbonentsNearbyMap finalMap = map;
        finalMap.findAbonentsNearby(categorySprav.getValue().convertNameToId(category),
                        radius, lat, lon)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (!pair.first) {
                            errorMessage.setValue(pair.second);
                        } else {
                            abonentsNearbyMap.setValue(finalMap);
                        }
                    }
                });
    }

    public LiveData<String> getSelectedCategoryName() {
        return selectedCategoryName;
    }

    public void setSelectedCategoryName(String name) {
        selectedCategoryName.setValue(name);
    }

    public LiveData<IncidentAbonentCategorySprav> getCategorySprav() {
        return categorySprav;
    }

    public void loadCategorySprav() {
        IncidentAbonentCategorySprav sprav = categorySprav.getValue();
        if (sprav == null) {
            sprav = new IncidentAbonentCategorySprav();
        }
        IncidentAbonentCategorySprav finalSprav = sprav;
        finalSprav.updateFromServer().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            categorySprav.setValue(finalSprav);
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void setUserAddress(String addr) {
        userAddress.setValue(addr);
    }

    public void setTypedAddress(String addr) {
        typedAddress.setValue(addr);
    }

    public LiveData<Boolean> getGeoDialogFinished() {
        return geoDialogFinished;
    }

    public void setGeoDialogFinished(boolean finished) {
        geoDialogFinished.setValue(finished);
    }

    public void setUserLocation(LatLong location) {
        userLocation.setValue(location);
    }

    public LiveData<LatLong> getTypedLocation() {
        return typedLocation;
    }

//    public LiveData<String> getAddress() {
//        return address;
//    }

    public LiveData<LatLong> getUserLocation() {
        return userLocation;
    }

    public LiveData<Incident> getIncident() {
        return incident;
    }

    public LiveData<Boolean> getHasIncident() {
        return hasIncident;
    }

    public LiveData<Video> getVideo() {
        return video;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

//    public LiveData<Location> getLocation() {
//        return location;
//    }

    public LiveData<Boolean> getLocationUpdated() {
        return locationUpdated;
    }

    public void prepareForLocationUpdate() {
        allowLocationUpdate = true;
    }

    public void setLocationUpdateType(String type) {
        locationUpdateType = type;
    }

    public LiveData<String> getUserAddress() {
        return userAddress;
    }

    public LiveData<String> getTypedAddress() {
        return typedAddress;
    }

    public void setTypedLocation(LatLong location) {
        typedLocation.setValue(location);
    }

    public void subscribeOnGeoUpdates(LifecycleOwner owner) {
        Geo.getLocation().observe(owner, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (!allowLocationUpdate) {
                    return;
                }
                if (!Geo.getUsedGps() || !Geo.getUsedNetwork()) {
                    return;
                }
                if (locationUpdateType == null) {
                    allowLocationUpdate = false;
                    return;
                }
//                MapViewModel.this.location.setValue(location);

                if (location == null) {
                    errorMessage.setValue("Не удалось определить местоположение");
                    if (locationUpdateType.equals("typed")) {
                        typedLocation.setValue(null);
                    }
                } else {
                    if (locationUpdateType.equals("user")) {
                        userLocation.setValue(new LatLong(location.getLatitude(), location.getLongitude()));
                    } else if (locationUpdateType.equals("typed")) {
                        typedLocation.setValue(new LatLong(location.getLatitude(), location.getLongitude()));
                    }
                }
                allowLocationUpdate = false;
                locationUpdated.setValue(true);
            }
        });
    }

    public void getAddressByCoordinates(float lat, float lon) {
        Geo.coordinatesToAddress(lat, lon).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            typedAddress.setValue(pair.second);
//                            addressRefreshNeeded.setValue(false);
                        } else {
                            typedAddress.setValue("");
                            errorMessage.setValue(pair.second);
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
                            errorMessage.setValue("Не удалось получить координаты по адресу");
                        } else {
//                            latitude.setValue(pair.first);
//                            longitude.setValue(pair.second);
                            typedLocation.setValue(new LatLong(pair.first, pair.second));
//                            addressRefreshNeeded.setValue(false);
                        }
                    }
                });
    }

    public void setErrorMessage(String error) {
        errorMessage.setValue(error);
    }

//    public void getVideoFromServer() {
//        Video newVideo = new Video();
//        if (hasIncident.getValue() != null && hasIncident.getValue()) {
//            newVideo.setId(incident.getValue().getId());
//            newVideo.setCompanion("abonent");
//        } else {
//            newVideo.setCompanion("operator");
//        }
//        if (location.getValue() != null) {
//            newVideo.setLatitude((float) location.getValue().getLatitude());
//            newVideo.setLongitude((float) location.getValue().getLongitude());
//        }
//        newVideo.getVideoFromServer().subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
//                    @Override
//                    public void accept(Pair<Boolean, String> pair) throws Throwable {
//                        if (pair.first) {
//                            video.setValue(newVideo);
//                        } else {
//                            errorMessage.setValue(pair.second);
//                        }
//                    }
//                });
//    }

    public void setIncident(Incident incident) {
        this.incident.setValue(incident);
    }

    public void setHasIncident(boolean hasIncident) {
        this.hasIncident.setValue(hasIncident);
    }
}
