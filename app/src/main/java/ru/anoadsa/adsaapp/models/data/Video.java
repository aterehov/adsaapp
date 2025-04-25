package ru.anoadsa.adsaapp.models.data;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.FormBody;
import okhttp3.Response;
import ru.anoadsa.adsaapp.Net;
import ru.anoadsa.adsaapp.Static;

public class Video {
    private String id;
    private String companion;
    private Float latitude;
    private Float longitude;

    private String confomeetLink;
    private String channelId;

    public String getConfomeetLink() {
        return confomeetLink;
    }

    public String getChannelId() {
        return channelId;
    }

    public Single<Pair<Boolean, String>> getVideoFromServer() {
        FormBody.Builder bodyBuilder = new FormBody.Builder();

        if (id != null && !id.isEmpty()) {
            bodyBuilder.add("id", id);
        }

        bodyBuilder.add("companion", companion);

        if (latitude != null) {
            bodyBuilder.add("latitude", String.valueOf(latitude));
        }
        if (longitude != null) {
            bodyBuilder.add("longitude", String.valueOf(longitude));
        }

        FormBody body = bodyBuilder.build();

        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/video/request",
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
                    JsonObject json = new Gson().fromJson(response.body().string(), JsonObject.class);
                    confomeetLink = json.get("confomeetLink").getAsString();
                    channelId = json.get("channelID").getAsString();
                }
                return pair;
            }
        });
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanion() {
        return companion;
    }

    public void setCompanion(String companion) {
        this.companion = companion;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}
