package ru.anoadsa.adsaapp.ui.activities.video;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ru.anoadsa.adsaapp.models.data.Incident;

public class VideoSharedViewModel extends ViewModel {
    private MutableLiveData<Incident> incident;
    private MutableLiveData<Boolean> hasIncident;

    public VideoSharedViewModel() {
        incident = new MutableLiveData<Incident>(null);
        hasIncident = new MutableLiveData<Boolean>(null);
    }

    public LiveData<Incident> getIncident() {
        return incident;
    }

    public LiveData<Boolean> getHasIncident() {
        return hasIncident;
    }

    public void setIncident(Incident incident) {
        this.incident.setValue(incident);
    }

    public void setHasIncident(boolean hasIncident) {
        this.hasIncident.setValue(hasIncident);
    }
}
