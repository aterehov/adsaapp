package ru.anoadsa.adsaapp.ui.menufragments.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ru.anoadsa.adsaapp.models.data.Incident;

public class ChatSharedViewModel extends ViewModel {
    private MutableLiveData<Incident> incident;

    public ChatSharedViewModel() {
        incident = new MutableLiveData<Incident>(null);
    }

    public void setIncident(Incident incident) {
        this.incident.setValue(incident);
    }

    public LiveData<Incident> getIncident() {
        return incident;
    }
}
