package ru.anoadsa.adsaapp.ui.menufragments.incidents;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.models.data.IncidentMap;

public class IncidentsViewModel extends ViewModel {
    private MutableLiveData<List<Incident>> sortedIncidentsList;
    private MutableLiveData<String> errorMessage;

    private MutableLiveData<Boolean> incidentFullInfoLoaded;

    private IncidentMap ibm;

    private Disposable incidentRefresher;

    public IncidentsViewModel() {
        sortedIncidentsList = new MutableLiveData<>(null);
        errorMessage = new MutableLiveData<>(null);
        incidentFullInfoLoaded = new MutableLiveData<Boolean>(false);
        ibm = new IncidentMap();
    }

    public LiveData<Boolean> getIncidentFullInfoLoaded() {
        return incidentFullInfoLoaded;
    }

    public Map<String, Incident> getIncidentHashMap() {
        return ibm.getMap();
    }

    public LiveData<List<Incident>> getSortedIncidentsList() {
        return sortedIncidentsList;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refresh() {
        ibm.updateFromServer().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                        new Consumer<Pair<Boolean, String>>() {
                            @Override
                            public void accept(Pair<Boolean, String> status) throws Throwable {
                                if (!status.first) {
                                    errorMessage.setValue(status.second);
                                } else {
                                    sortedIncidentsList.setValue(ibm.getSortedByCreationTime());
                                }
                            }
                        }
                );
    }

    public void startAutoRefresh() {
        incidentRefresher = Observable.interval(30, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long count) throws Throwable {
                        refresh();
                    }
                })
//                .doOnEach(new Observer<Long>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(@NonNull Long aLong) {
//                        refresh();
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                })
//                .map(new Function<Long, Object>() {
//            @Override
//            public Object apply(Long count) throws Throwable {
//                refresh();
//                return new Object();
//            }
//        })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    public void stopAutoRefresh() {
        if (incidentRefresher != null) {
            incidentRefresher.dispose();
        }
    }

    public void loadFullIncidentInfo(int incidentIndex) {
        sortedIncidentsList.getValue().get(incidentIndex).loadFullInfo().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
//                            ldIncident.setValue(incident);
                            incidentFullInfoLoaded.setValue(true);
                        } else {
                            errorMessage.setValue(pair.second);
                            incidentFullInfoLoaded.setValue(false);
                        }
                    }
                });
    }
}
