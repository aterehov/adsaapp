package ru.anoadsa.adsaapp.models.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.anoadsa.adsaapp.Net;
import ru.anoadsa.adsaapp.Static;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AbonentsNearbyMap {
    private HashMap<String, Abonent> nameToAbonent;

    public AbonentsNearbyMap() {
        nameToAbonent = new HashMap<>();
    }

    public Abonent convertNameToAbonent(String name) {
        return nameToAbonent.get(name);
    }

    public List<String> getNames() {
        ArrayList<String> r = new ArrayList<String>(nameToAbonent.keySet());
        r.sort(String::compareToIgnoreCase);
        return r;
    }

    public Single<Pair<Boolean, String>> findAbonentsNearby(String categoryId, float radius,
                                                            float lat, float lon) {
        RequestBody body = new FormBody.Builder()
                .add("category", categoryId)
                .add("radius", String.valueOf(radius))
                .add("lat", String.valueOf(lat))
                .add("lon", String.valueOf(lon))
                .build();
        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/query/firms/find",
                            "POST",
                            headers,
                            null,
                            body
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {
                    nameToAbonent.clear();
                    JsonArray list = new Gson().fromJson(response.body().string(), JsonArray.class);
                    for (JsonElement i: list) {
                        JsonObject iobj = i.getAsJsonObject();
                        Abonent a = new Abonent(iobj);
                        nameToAbonent.put(a.getName(), a);
                    }
                }
                return pair;
            }
        });
    }
}
