package ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;

public class ChatNotificationManager {
    private static HashMap<String, Integer> incidentIdToNotificationId = new HashMap<>();
    private static HashMap<String, Integer> incidentIdToMessageCount = new HashMap<>();
    private static MutableLiveData<HashMap<String, Integer>> ldIncidentIdToMessageCount = new MutableLiveData<HashMap<String, Integer>>(new HashMap<>());
    private static int nextNotificationId = 0;

//    public static void init() {
//        if (incidentIdToNotificationId == null) {
//            incidentIdToNotificationId = new HashMap<>();
//        }
//    }

    public static LiveData<HashMap<String, Integer>> getAllNotificationCountLiveData() {
        return ldIncidentIdToMessageCount;
    }

    public static Integer getNotificationCount(String incidentId) {
        if (!ldIncidentIdToMessageCount.getValue().containsKey(incidentId)) {
            return 0;
        } else {
            return ldIncidentIdToMessageCount.getValue().get(incidentId);
        }
    }

    public static void setNotificationCount(String incidentId, int count) {
        ldIncidentIdToMessageCount.getValue().put(incidentId, count);
        ldIncidentIdToMessageCount.postValue(ldIncidentIdToMessageCount.getValue());
    }

//    @Nullable
    public static Integer getNotificationId(String incidentId) {
//        if (incidentIdToNotificationId == null) {
//            return null;
//        }
        return incidentIdToNotificationId.get(incidentId);
    }

//    @Nullable
    public static Integer addNotificationId(String incidentId) {
//        if (incidentIdToNotificationId == null) {
//            return null;
//        }
        if (incidentIdToNotificationId.containsKey(incidentId)) {
            return incidentIdToNotificationId.get(incidentId);
        }
        incidentIdToNotificationId.put(incidentId, nextNotificationId);
        return nextNotificationId++;
    }
}
