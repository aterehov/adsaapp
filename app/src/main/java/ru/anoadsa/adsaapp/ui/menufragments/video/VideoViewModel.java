//package ru.anoadsa.adsaapp.ui.menufragments.video;
//
//import android.location.Location;
//import android.util.Pair;
//
//import androidx.lifecycle.LifecycleOwner;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModel;
//
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
//import io.reactivex.rxjava3.functions.Consumer;
//import io.reactivex.rxjava3.schedulers.Schedulers;
//import ru.anoadsa.adsaapp.Geo;
//import ru.anoadsa.adsaapp.models.data.Incident;
//import ru.anoadsa.adsaapp.models.data.Video;
//
//public class VideoViewModel extends ViewModel {
//    private MutableLiveData<Incident> incident;
//    private MutableLiveData<Boolean> hasIncident;
//    private MutableLiveData<Video> video;
//
//    private MutableLiveData<String> errorMessage;
//
//    private MutableLiveData<Location> location;
//    private MutableLiveData<Boolean> locationUpdated;
//
//    private boolean allowLocationUpdate = false;
//
//    public VideoViewModel() {
//        incident = new MutableLiveData<Incident>(null);
//        hasIncident = new MutableLiveData<Boolean>(null);
//        video = new MutableLiveData<Video>(null);
//        errorMessage = new MutableLiveData<String>(null);
//        location = new MutableLiveData<Location>(null);
//        locationUpdated = new MutableLiveData<Boolean>(false);
//    }
//
//    public LiveData<Incident> getIncident() {
//        return incident;
//    }
//
//    public LiveData<Boolean> getHasIncident() {
//        return hasIncident;
//    }
//
//    public LiveData<Video> getVideo() {
//        return video;
//    }
//
//    public LiveData<String> getErrorMessage() {
//        return errorMessage;
//    }
//
//    public LiveData<Location> getLocation() {
//        return location;
//    }
//
//    public LiveData<Boolean> getLocationUpdated() {
//        return locationUpdated;
//    }
//
//    public void prepareForLocationUpdate() {
//        allowLocationUpdate = true;
//    }
//
//    public void subscribeOnGeoUpdates(LifecycleOwner owner) {
//        Geo.getLocation().observe(owner, new Observer<Location>() {
//            @Override
//            public void onChanged(Location location) {
//                if (!allowLocationUpdate) {
//                    return;
//                }
//                if (!Geo.getUsedGps() || !Geo.getUsedNetwork()) {
//                    return;
//                }
//                VideoViewModel.this.location.setValue(location);
//                if (location == null) {
//                    errorMessage.setValue("Не удалось определить местоположение");
//                }
//                allowLocationUpdate = false;
//                locationUpdated.setValue(true);
//            }
//        });
//    }
//
//    public void setErrorMessage(String error) {
//        errorMessage.setValue(error);
//    }
//
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
//
//    public void setIncident(Incident incident) {
//        this.incident.setValue(incident);
//    }
//
//    public void setHasIncident(boolean hasIncident) {
//        this.hasIncident.setValue(hasIncident);
//    }
//}
