package ru.anoadsa.adsaapp.ui.menufragments.incident;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ru.anoadsa.adsaapp.models.data.Incident;

public class IncidentSharedViewModel extends ViewModel {
    private MutableLiveData<String> mode;
    private MutableLiveData<Incident> incident;

    public IncidentSharedViewModel() {
        mode = new MutableLiveData<String>(null);
        incident = new MutableLiveData<Incident>(null);
    }

    public LiveData<String> getMode() {
        return mode;
    }

    public LiveData<Incident> getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident.setValue(incident);
    }

    public void setMode(String mode) {
        this.mode.setValue(mode);
    }
}
