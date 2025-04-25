package ru.anoadsa.adsaapp.ui.menufragments.incident;

import android.content.Context;
import android.location.Location;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.Geo;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount.CheckMessageCountWorker;
import ru.anoadsa.adsaapp.models.data.AbonentsNearbyMap;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.models.data.IncidentAbonentCategorySprav;
import ru.anoadsa.adsaapp.models.data.IncidentDisabilityCategorySprav;
import ru.anoadsa.adsaapp.models.data.IncidentMediaFile;
import ru.anoadsa.adsaapp.models.data.IncidentStatusSprav;
import ru.anoadsa.adsaapp.models.data.IncidentTypeSprav;
import ru.anoadsa.adsaapp.models.data.User;
import ru.anoadsa.adsaapp.ui.menufragments.profile.ProfileViewModel;
import ru.anoadsa.adsaapp.ui.views.AttachedImageView;

public class IncidentViewModel extends ViewModel {
    private MutableLiveData<String> mode;
    private MutableLiveData<Incident> ldIncident;
    private MutableLiveData<String> errorMessage;

    private MutableLiveData<IncidentStatusSprav> ldStatusSprav;
    private MutableLiveData<IncidentTypeSprav> ldTypeSprav;
    private MutableLiveData<IncidentDisabilityCategorySprav> ldDisabilityCategorySprav;
    private MutableLiveData<IncidentAbonentCategorySprav> ldAbonentCategorySprav;
    private MutableLiveData<AbonentsNearbyMap> ldAbonentsNearbyMap;

    private MutableLiveData<Location> location;
    private boolean allowLocationUpdate = false;

    private MutableLiveData<String> address;

    private boolean enableGeolocationErrorMessage;
    private MutableLiveData<Float> latitude;
    private MutableLiveData<Float> longitude;

    private MutableLiveData<Boolean> addressRefreshNeeded;

    public MutableLiveData<User> user;

    private IncidentStatusSprav statusSprav;
    private IncidentTypeSprav typeSprav;
    private IncidentDisabilityCategorySprav disabilityCategorySprav;
    private IncidentAbonentCategorySprav abonentCategorySprav;
    private AbonentsNearbyMap abonentsNearbyMap;
    private Incident incident;

    private boolean neededInfoLoaded;

    private ArrayList<IncidentMediaFile> previousPhotoList;
    private ArrayList<IncidentMediaFile> previousDocumentList;

    public ArrayList<IncidentMediaFile> getPreviousPhotoList() {
        return previousPhotoList;
    }

    public void setPreviousPhotoList(ArrayList<IncidentMediaFile> list) {
        previousPhotoList = list;
    }

    public ArrayList<IncidentMediaFile> getPreviousDocumentList() {
        return previousDocumentList;
    }

    public void setPreviousDocumentList(ArrayList<IncidentMediaFile> list) {
        previousDocumentList = list;
    }

    public IncidentViewModel() {
        mode = new MutableLiveData<String>(null);
        ldIncident = new MutableLiveData<Incident>(null);
        errorMessage = new MutableLiveData<String>(null);
        ldStatusSprav = new MutableLiveData<IncidentStatusSprav>(null);
        ldTypeSprav = new MutableLiveData<IncidentTypeSprav>(null);
        ldDisabilityCategorySprav = new MutableLiveData<IncidentDisabilityCategorySprav>(null);
        ldAbonentCategorySprav = new MutableLiveData<IncidentAbonentCategorySprav>(null);
        ldAbonentsNearbyMap = new MutableLiveData<AbonentsNearbyMap>(null);
        location = new MutableLiveData<Location>(null);
        address = new MutableLiveData<String>(null);
        latitude = new MutableLiveData<Float>(null);
        longitude = new MutableLiveData<Float>(null);
        addressRefreshNeeded = new MutableLiveData<Boolean>(false);
        user = new MutableLiveData<User>(null);
    }

    public LiveData<User> getUser() {
        return user;
    }

    public boolean isNeededInfoLoaded() {
        return neededInfoLoaded;
    }

    public LiveData<Boolean> getAddressRefreshNeeded() {
        return addressRefreshNeeded;
    }

    public LiveData<Float> getLatitude() {
        return latitude;
    }

    public LiveData<Float> getLongitude() {
        return longitude;
    }

    public void loadUserFromDb() {
        Static.db.userDao().getUser().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Throwable {
                        IncidentViewModel.this.user.setValue(user);
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
//                            resetMenuValues.setValue(true);
                        } else {
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
                            latitude.setValue(pair.first);
                            longitude.setValue(pair.second);
                            addressRefreshNeeded.setValue(false);
                        }
                    }
                });
    }

    public LiveData<String> getAddress() {
        return address;
    }

    public void getAddressByCoordinates(float lat, float lon) {
        Geo.coordinatesToAddress(lat, lon).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            address.setValue(pair.second);
                            addressRefreshNeeded.setValue(false);
                        } else {
                            address.setValue("");
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void subscribeToLocationUpdates(LifecycleOwner owner) {
        Geo.getLocation().observe(owner, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (allowLocationUpdate) {
                    if (Geo.getUsedGps() && Geo.getUsedNetwork()) {
//                        IncidentViewModel.this.location.setValue(location);
                        if (location != null) {
                            latitude.setValue((float) location.getLatitude());
                            longitude.setValue((float) location.getLongitude());
//                            allowLocationUpdate = false;
                            addressRefreshNeeded.setValue(true);
                        } else
//                            if (enableGeolocationErrorMessage)
                            {
                            errorMessage.setValue("Не удалось определить местоположение");
                            // Rerun observers so that field values were set back
                            latitude.setValue(latitude.getValue());
                            longitude.setValue(longitude.getValue());
                            address.setValue(address.getValue());
//                        } else {
//                            enableGeolocationErrorMessage = true;
                        }
                        allowLocationUpdate = false;
                    }
                }
            }
        });
    }

    public void prepareForLocationUpdate() {
        allowLocationUpdate = true;
    }

    public LiveData<Location> getLocation() {
        return location;
    }

    public LiveData<IncidentStatusSprav> getStatusSprav() {
        return ldStatusSprav;
    }

    public LiveData<IncidentTypeSprav> getTypeSprav() {
        return ldTypeSprav;
    }

    public LiveData<IncidentDisabilityCategorySprav> getDisabilityCategorySprav() {
        return ldDisabilityCategorySprav;
    }

    public LiveData<IncidentAbonentCategorySprav> getAbonentCategorySprav() {
        return ldAbonentCategorySprav;
    }

    public LiveData<AbonentsNearbyMap> getAbonentsNearbyMap() {
        return ldAbonentsNearbyMap;
    }

    public LiveData<String> getMode() {
        return mode;
    }

    public LiveData<Incident> getIncident() {
        return ldIncident;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setIncident(Incident incident) {
        this.ldIncident.setValue(incident);
        this.incident = incident;
    }

    public void setMode(String mode) {
        this.mode.setValue(mode);
    }

//    private Single<Pair<Boolean, String>> getFileSingle(@NonNull IncidentMediaFile imf, Context context) {
//        if (imf.isDownloadedToCache(context)) {
//            return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
//                @Override
//                public Pair<Boolean, String> call() throws Exception {
//                    return new Pair<Boolean, String>(imf.openFileFromCache(context), null);
//                }
//            });
//        } else {
//            return imf.downloadToCache(context);
//        }
//    }

    public void copyFileToGallery(@NonNull IncidentMediaFile imf, String dir, Context context) {
        imf.copyToGallery(context).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            errorMessage.setValue("Файл сохранен в галерею в альбоме " + Static.ADSA_PICTURES_FOLDER);
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void copyFileToDocuments(@NonNull IncidentMediaFile imf, Context context) {
        imf.copyToDocuments(context).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            errorMessage.setValue("Файл сохранен в папку документов в папке " + Static.ADSA_DOCUMENTS_FOLDER);
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void getFileAndSetAttachedImageViewUri(@NonNull IncidentMediaFile imf, AttachedImageView aiv,
                                                  Context context) {
        imf.getFileSingle(context).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            aiv.setUri(imf.getUri());
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void resetIncident() {
        ldIncident.setValue(incident);
    }

    public void loadFullIncidentInfo() {
        incident.loadFullInfo().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            ldIncident.setValue(incident);
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void searchAbonents(String category, float radius, float lat, float lon) {
        if (abonentsNearbyMap == null) {
            abonentsNearbyMap = new AbonentsNearbyMap();
        }
        abonentsNearbyMap.findAbonentsNearby(abonentCategorySprav.convertNameToId(category),
                        radius, lat, lon)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (!pair.first) {
                            errorMessage.setValue(pair.second);
                        } else {
                            ldAbonentsNearbyMap.setValue(abonentsNearbyMap);
                        }
                    }
                });
    }

    public  void rerunIncidentObserver() {
        ldIncident.setValue(ldIncident.getValue());
    }

    public void loadNeededInfo() {
        if (statusSprav == null) {
            statusSprav = new IncidentStatusSprav();
        }
        if (typeSprav == null) {
            typeSprav = new IncidentTypeSprav();
        }
        if (disabilityCategorySprav == null) {
            disabilityCategorySprav = new IncidentDisabilityCategorySprav();
        }
        if (abonentCategorySprav == null) {
            abonentCategorySprav = new IncidentAbonentCategorySprav();
        }
        statusSprav.updateFromServer().concatMap(new Function<Pair<Boolean, String>, SingleSource<Pair<Boolean, String>>>() {
            @Override
            public SingleSource<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                if (!pair.first) {
                    return Single.just(pair);
                } else {
                    return typeSprav.updateFromServer();
                }
            }
        }).concatMap(new Function<Pair<Boolean, String>, SingleSource<Pair<Boolean, String>>>() {
            @Override
            public SingleSource<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                if (!pair.first) {
                    return Single.just(pair);
                } else {
                    return disabilityCategorySprav.updateFromServer();
                }
            }
        }).concatMap(new Function<Pair<Boolean, String>, SingleSource<Pair<Boolean, String>>>() {
            @Override
            public SingleSource<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                if (!pair.first) {
                    return Single.just(pair);
                } else {
                    return abonentCategorySprav.updateFromServer();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
            @Override
            public void accept(Pair<Boolean, String> pair) throws Throwable {
                if (!pair.first) {
                    errorMessage.setValue(pair.second);
                } else {
                    ldStatusSprav.setValue(statusSprav);
                    ldTypeSprav.setValue(typeSprav);
                    ldDisabilityCategorySprav.setValue(disabilityCategorySprav);
                    ldAbonentCategorySprav.setValue(abonentCategorySprav);
                    neededInfoLoaded = true;
                    // Rerun mode observer
                    mode.setValue(new String(mode.getValue()));
                    // Rerun incident observer
//                    ldIncident.setValue(incident);
                }
            }
        });
    }

    public void saveIncident(String id, String status, String incidentType, boolean isHurt,
                             String abonent, String category, float radius, float latitude,
                             float longitude, String address, boolean isEyewitness,
                             String clientSurname, String clientName, String clientPatronymic,
                             String carBrand, String carNumber, String osago, String medPolis,
                             String snils, LocalDate birthday, String disabilityCategory,
                             String description, ArrayList<IncidentMediaFile> orderMediaFile,
                             Context context) {
        if (incident == null) {
            incident = new Incident();
        }
        incident.editAll(
                incident.getId(),
                incident.getStatus() == null
                        ? statusSprav.convertNameToId("Новая")
                        : incident.getStatus(),
                typeSprav.convertNameToId(incidentType),
                isHurt,
                abonentsNearbyMap.convertNameToAbonent(abonent).getId(),
                abonentCategorySprav.convertNameToId(category),
                radius,
                latitude,
                longitude,
                address,
                isEyewitness,
                clientSurname,
                clientName,
                clientPatronymic,
                carBrand,
                carNumber,
                osago,
                medPolis,
                snils,
                birthday,
                disabilityCategorySprav.convertNameToId(disabilityCategory),
                description,
                orderMediaFile
        );
        incident.setCategoryText(category);
        incident.setAbonentText(abonent);
        incident.setDisabilityCategoryText(disabilityCategory);
        incident.setIncidentTypeText(incidentType);
        Static.db.userDao().getUser().concatMap(new Function<User, SingleSource<Pair<Boolean, String>>>() {
            @Override
            public SingleSource<Pair<Boolean, String>> apply(User user) throws Throwable {
                if (user != null) {
                    incident.setPhone(user.getPhone());
                    return incident.sendToServer(context);
                } else {
                    return Single.just(new Pair<Boolean, String>(false, "Не удалось загрузить номер телефона"));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (!pair.first) {
                            errorMessage.setValue(pair.second);
                        } else if (mode.getValue().equals("create")) {
                            CheckMessageCountWorker.requestIncidentMapUpdate();
                            CheckMessageCountWorker.scheduleNextWorkRequest(true, context);
                            mode.setValue("finish");
                        } else {
                            mode.setValue("view");
                            // TODO check if it is necessary to set incident on every update
//                            ldIncident.setValue(incident);
                        }
                    }
                });
    }

    public void requestOperator() {
        incident.requestOperator().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            errorMessage.setValue("Оператору отправлен запрос");
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }
}
