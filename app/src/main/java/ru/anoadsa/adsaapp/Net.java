package ru.anoadsa.adsaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Net {
    public static final String ADSA_SERVER = "https://dev.anoadsa.ru:9900";
    public static final String OSM_NOMINATIM = "https://nominatim.openstreetmap.org";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType MEDIA_TYPE_JSON =
            MediaType.parse("application/json; charset=utf-8");

    @FunctionalInterface
    public interface IOnResponse {
        void onResponse(@NonNull Call call, @NonNull Response response);
    }

    @FunctionalInterface
    public interface IOnFailure {
        void onFailure(@NonNull Call call, @NonNull IOException e);
    }

    @NonNull
    private static Request formRequest(
            @NonNull String url, @NonNull String method, @Nullable Map<String, String> headers,
            @Nullable Map<String, String> queries, @Nullable RequestBody body) {
        HttpUrl.Builder queried_url = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (queries != null) {
            for (String key : queries.keySet()) {
                queried_url.addQueryParameter(key, queries.get(key));
            }
        }

        Request.Builder request = new Request.Builder()
                .url(queried_url.build())
                .method(
                        method,
//                        RequestBody.create(body == null ? "" : body, MEDIA_TYPE_JSON)
                        body
                );
        if (headers != null) {
            for (String key : headers.keySet()) {
                if (headers.get(key) != null) {
                    // This warning can be ignored because if statement checks for get() return not
                    // being null
                    //noinspection DataFlowIssue
                    request.addHeader(key, headers.get(key));
                }
            }
        }

        request.addHeader(
                "User-Agent",
                DevSettings.APP_NAME
                        + "/"
                        + DevSettings.APP_VERSION
                        + " "
                        + System.getProperty("http.agent")
        );

        return request.build();
    }

    @NonNull
    public static Response request(
            @NonNull String url, @NonNull String method, @Nullable Map<String, String> headers,
            @Nullable Map<String, String> queries, @Nullable RequestBody body) throws IOException {
        return client.newCall(formRequest(url, method, headers, queries, body)).execute();
    }

    public static void requestAsync(
            @NonNull String url, @NonNull String method, @Nullable Map<String, String> headers,
            @Nullable Map<String, String> queries, @Nullable RequestBody body,
            @NonNull IOnResponse _onResponse, @Nullable IOnFailure _onFailure) {
        client.newCall(formRequest(url, method, headers, queries, body)).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (_onFailure == null) {
                    e.printStackTrace();
                } else {
                    _onFailure.onFailure(call, e);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                _onResponse.onResponse(call, response);
            }
        });
    }
}
