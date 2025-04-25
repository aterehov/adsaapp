package ru.anoadsa.adsaapp.models.data;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.Response;
import ru.anoadsa.adsaapp.Net;
import ru.anoadsa.adsaapp.Static;

public class IncidentStatusSprav {
    private HashMap<String, String> nameToId;

    public IncidentStatusSprav() {
        nameToId = new HashMap<>();
    }

    public List<String> getNames() {
        ArrayList<String> r = new ArrayList<String>(nameToId.keySet());
        r.sort(String::compareToIgnoreCase);
        return r;
    }

    public String convertNameToId(String name) {
        return nameToId.get(name);
    }

    public Single<Pair<Boolean, String>> updateFromServer() {
        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/sprav/order-statuses",
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
                    JsonArray list = new Gson().fromJson(response.body().string(), JsonArray.class);
                    for (JsonElement i: list) {
                        JsonObject iobj = i.getAsJsonObject();
                        nameToId.put(iobj.get("name").getAsString(), iobj.get("id").getAsString());
                    }
                }
                return pair;
            }
        });
    }
}
