package ru.anoadsa.adsaapp.models.data;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.Response;
import ru.anoadsa.adsaapp.Net;
import ru.anoadsa.adsaapp.Static;

public class IncidentMap {
    private HashMap<String, Incident> idToIncident;
    private boolean allFullInfoLoaded;

    private String type;

    public IncidentMap() {
        idToIncident = new HashMap<>();
    }

    public List<Incident> getSortedByCreationTime() {
        ArrayList<Incident> list = new ArrayList<Incident>(idToIncident.values());
        list.sort(Incident::compareRecentlyCreatedFirst);
        return list;
    }

    public boolean getAllFullInfoLoaded() {
        return allFullInfoLoaded;
    }

    public String getType() {
        return type;
    }

    public Map<String, Incident> getMap() {
        return idToIncident;
    }

    public Collection<Incident> getIncidents() {
        return idToIncident.values();
    }

    public boolean containsId(String id) {
        return idToIncident.containsKey(id);
    }

    public Set<String> getAllIds() {
        return idToIncident.keySet();
    }

    public Single<Pair<Boolean, String>> loadAllMessageCount() {
        Single<Pair<Boolean, String>> task = Single.just(new Pair<Boolean, String>(true, null));
        for (Incident i: idToIncident.values()) {
            task = task.concatMap(new Function<Pair<Boolean, String>, Single<Pair<Boolean, String>>>() {
                @Override
                public Single<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                    if (pair.first) {
//                        return i.loadChatMessagesCount();
                    } else {
//                        return Single.just(pair);
                    }
                    // Errors are ignored, if failed messageCount = -1
                    return i.loadChatMessagesCount();
                }
            });
        }
        return task;
    }

    public Single<Boolean> loadAllFromDb() {
        return Static.db.incidentDao().getAllIncidents().map(new Function<List<Incident>, Boolean>() {
            @Override
            public Boolean apply(List<Incident> incidents) throws Throwable {
                idToIncident.clear();
                for (Incident i: incidents) {
                    idToIncident.put(i.getId(), i);
                }
                type = "chatCountOnly";
                return true;
            }
        });
    }

    public Single<Boolean> saveAllToDb() {
        if (type.equals("basicInfo")) {
            return Single.just(false);
        }
        Single<Boolean> task = Single.just(true);
        for (Incident i: idToIncident.values()) {
            task = task.concatMap(new Function<Boolean, Single<Boolean>>() {
                @Override
                public Single<Boolean> apply(Boolean success) throws Throwable {
                    if (success) {
                        return i.saveToDb();
                    } else {
                        return Single.just(success);
                    }
                }
            });
        }
        return task;
    }

    public Single<Pair<Boolean, String>> loadAllFullInfo() {
        Single<Pair<Boolean, String>> task = Single.just(new Pair<Boolean, String>(true, null));
        for (Incident i: idToIncident.values()) {
            task = task.concatMap(new Function<Pair<Boolean, String>, Single<Pair<Boolean, String>>>() {
                @Override
                public Single<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                    if (pair.first) {
                        return i.loadFullInfo();
                    } else {
                        return Single.just(pair);
                    }
                }
            });
        }
        task = task.map(new Function<Pair<Boolean, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Pair<Boolean, String> pair) throws Throwable {
                if (pair.first) {
//                    allFullInfoLoaded = true;
                    type = "fullInfo";
                }
                return pair;
            }
        });
        return task;
    };

    public Single<Pair<Boolean, String>> updateFromServer() {
        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/queries/list",
                            "GET",
                            headers,
                            null,
                            null
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {
//                    allFullInfoLoaded = false;
                    type = "basicInfo";
                    idToIncident.clear();
                    JsonArray list = new Gson().fromJson(response.body().string(), JsonArray.class);
                    for (JsonElement i: list) {
                        Incident incident = new Incident(i.getAsJsonObject());
                        idToIncident.put(incident.getId(), incident);
                    }
                }
                return pair;
            }
        });

//        return Static.getAuthorizedHeadersMap().concatMap(new Function<Map<String, String>, SingleSource<Pair<Boolean, String>>>() {
//            @Override
//            public SingleSource<Pair<Boolean, String>> apply(Map<String, String> headers) throws Throwable {
//                Response response = Net.request(
//                        Net.ADSA_SERVER + "/api/queries/list",
//                        "GET",
//                        headers,
//                        null,
//                        null
//                );
//                Boolean ok = response != null && response.isSuccessful() && response.body() != null;
//                String errorMessage = null;
//                if (!ok) {
//                    errorMessage = Static.getResponseErrorMessage(response);
//                } else {
//                    JsonArray list = new Gson().fromJson(response.body().string(), JsonArray.class);
//                    for (JsonElement i: list) {
//                        Incident incident = new Incident(i.getAsJsonObject());
//                        idToIncident.put(incident.getId(), incident);
//                    }
//                }
//                return Single.just(new Pair<>(ok, errorMessage));
//            }
//        });
    }
}
