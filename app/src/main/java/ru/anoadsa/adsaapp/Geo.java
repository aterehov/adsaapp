package ru.anoadsa.adsaapp;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Response;

public class Geo {
    public static double a = 6378137.0; // equatorial radius
    public static double f = 1 / 298.257223563; // flattening
//    public static double b = getB(); // polar radius
//    public static double e = getE(); // eccentricity

    private static LocationManager locationManager;

    private static boolean hasGps;
    private static boolean hasNetwork;

    private static MutableLiveData<Location> locationByGps = new MutableLiveData<>(null);
    private static MutableLiveData<Location> locationByNetwork = new MutableLiveData<>(null);

    private static MutableLiveData<Location> location = new MutableLiveData<>(null);

    private static boolean usedGps;
    private static boolean usedNetwork;

    private static boolean autoselectBetterLocationInitialized = false;

    public static boolean getUsedGps() {
        return usedGps || !hasGps;
    }

    public static boolean getUsedNetwork() {
        return usedNetwork || !hasNetwork;
    }

    public static LiveData<Location> getLocation() {
        return location;
    }

//    private static Executor executor;

    private static LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
//            usedGps = true;
            locationByGps.postValue(location);
        }

        // This method is needed for compatibility reasons
        /** @noinspection RedundantSuppression*/
        @SuppressWarnings("deprecation")
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                LocationListener.super.onStatusChanged(provider, status, extras);
            }
        }
    };

    private static LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
//            usedNetwork = true;
            locationByNetwork.postValue(location);
        }

        // This method is needed for compatibility reasons
        /** @noinspection RedundantSuppression*/
        @SuppressWarnings("deprecation")
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                LocationListener.super.onStatusChanged(provider, status, extras);
            }
        }
    };

    public static void initLocationManager(@NonNull Context context) {
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public static void checkAvailability() {
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

//    public static void initExecutor() {
//        executor = Executors.newSingleThreadExecutor();
//    }

    private static void updateLocation(Context context) {
        usedGps = false;
        usedNetwork = false;
        if (hasGps
                && Static.checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && Static.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Static.initExecutor();
                locationManager.getCurrentLocation(
                        LocationManager.GPS_PROVIDER,
                        null,
                        Static.executor,
                        new Consumer<Location>() {
                            @Override
                            public void accept(Location location) {
//                                usedGps = true;
                                locationByGps.postValue(location);
                            }
                        }
                );
            } else {
                Static.initAndStartHandlerThread();
                locationManager.requestSingleUpdate(
                        LocationManager.GPS_PROVIDER,
                        gpsLocationListener,
                        Static.handlerThread.getLooper()
//                        Looper.myLooper()
                );
            }
        } else {
//            locationByGps = null;
            usedGps = true;
            locationByGps.setValue(null);
        }
        if (hasNetwork
                && Static.checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Static.initExecutor();
                locationManager.getCurrentLocation(
                        LocationManager.NETWORK_PROVIDER,
                        null,
                        Static.executor,
                        new Consumer<Location>() {
                            @Override
                            public void accept(Location location) {
//                                usedNetwork = true;
                                locationByNetwork.postValue(location);
                            }
                        }
                );
            } else {
                Static.initAndStartHandlerThread();
                locationManager.requestSingleUpdate(
                        LocationManager.NETWORK_PROVIDER,
                        networkLocationListener,
                        Static.handlerThread.getLooper()
                );
            }
        } else {
//            locationByNetwork = null;
            usedNetwork = true;
            locationByNetwork.setValue(null);
        }
    }

    @Nullable
    public static Location getBetterLocation() {
//        if (Static.checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
//                && Static.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
//            Location lastKnownLocationByGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if (lastKnownLocationByGps != null) {
//                locationByGps = lastKnownLocationByGps;
//            }
//        }
//
//        if (Static.checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
//            Location lastKnownLocationByNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            if (lastKnownLocationByNetwork != null) {
//                locationByNetwork = lastKnownLocationByNetwork;
//            }
//        }

        if (locationByGps.getValue() != null && locationByNetwork.getValue() != null) {
            if (locationByGps.getValue().getAccuracy() < locationByNetwork.getValue().getAccuracy()) {
                return locationByGps.getValue();
            } else {
                return locationByNetwork.getValue();
            }
        } else if (locationByGps.getValue() != null) {
            return locationByGps.getValue();
        } else if (locationByNetwork.getValue() != null) {
            return locationByNetwork.getValue();
        } else {
            return null;
        }
    }

    public static void refreshLocation(Context context) {
        initLocationManager(context);
        checkAvailability();
        updateLocation(context);
    }

    public static void initAutoselectBetterLocation() {
        if (autoselectBetterLocationInitialized) {
            return;
        }

        locationByGps.observeForever(new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                usedGps = true;
                Geo.location.setValue(getBetterLocation());
            }
        });

        locationByNetwork.observeForever(new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                usedNetwork = true;
                Geo.location.setValue(getBetterLocation());
            }
        });

        autoselectBetterLocationInitialized = true;
    }

    public static Single<Pair<Boolean, String>> coordinatesToAddress(float lat, float lon) {
        HashMap<String, String> queries = new HashMap<String, String>();
        queries.put("format", "geocodejson");
        queries.put("addressdetails", "1");
        queries.put("lat", String.valueOf(lat));
        queries.put("lon", String.valueOf(lon));

        return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> call() throws Exception {
                Response response;
                try {
                    response = Net.request(
                            Net.OSM_NOMINATIM + "/reverse",
                            "GET",
                            null,
                            queries,
                            null
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
//                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                boolean ok = response != null && response.isSuccessful() && response.body() != null;
                if (ok) {
                    JsonObject body = new Gson().fromJson(response.body().string(), JsonObject.class);
                    JsonObject geocoding = body
                            .getAsJsonArray("features")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("properties")
                            .getAsJsonObject("geocoding");
//                    String address = "";
                    ArrayList<String> addressFragments = new ArrayList<String>();
                    if (geocoding.get("postcode") != null) {
                        addressFragments.add(geocoding.get("postcode").getAsString());
                    }
                    if (geocoding.get("city") != null) {
                        addressFragments.add(geocoding.get("city").getAsString());
                        if (geocoding.get("district") != null) {
                            addressFragments.add(geocoding.get("district").getAsString());
                        }
                    } else if (geocoding.get("state") != null) {
                        addressFragments.add(geocoding.get("state").getAsString());
                        if (geocoding.get("county") != null) {
                            addressFragments.add(geocoding.get("county").getAsString());
                        }
                        if (geocoding.get("district") != null) {
                            addressFragments.add(geocoding.get("district").getAsString());
                        }
                        if (geocoding.get("locality") != null) {
                            addressFragments.add(geocoding.get("locality").getAsString());
                        }
                    }
                    if (geocoding.get("street") != null) {
                        addressFragments.add(geocoding.get("street").getAsString());
                    }
                    if (geocoding.get("housenumber") != null) {
                        addressFragments.add(geocoding.get("housenumber").getAsString());
                    }
                    if (geocoding.get("name") != null) {
                        addressFragments.add(geocoding.get("name").getAsString());
                    }

                    return new Pair<Boolean, String>(true, String.join(", ", addressFragments));
                } else {
                    return new Pair<Boolean, String>(false, "Ошибка при получении адреса");
                }
            }
        });
    }

    public static Single<Pair<Float, Float>> addressToCoordinates(String address) {
        HashMap<String, String> queries = new HashMap<String, String>();
        queries.put("format", "geocodejson");
        queries.put("addressdetails", "1");
        queries.put("limit", "1");
        queries.put("countrycodes", "ru");
//        queries.put("lat", String.valueOf(lat));
//        queries.put("lon", String.valueOf(lon));
        queries.put("q", address);

        return Single.fromCallable(new Callable<Pair<Float, Float>>() {
            @Override
            public Pair<Float, Float> call() throws Exception {
                Response response;
                try {
                    response = Net.request(
                            Net.OSM_NOMINATIM + "/search",
                            "GET",
                            null,
                            queries,
                            null
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Float, Float>(null, null);
                }
//                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                boolean ok = response != null && response.isSuccessful() && response.body() != null;
                if (ok) {
                    JsonElement body = new Gson().fromJson(response.body().string(), JsonElement.class);
                    if (body.isJsonArray()) {
                        return new Pair<Float, Float>(null, null);
                    } else {
                        JsonArray features = body
                                .getAsJsonObject()
                                .getAsJsonArray("features");
                        if (features.isEmpty()) {
                            return new Pair<Float, Float>(null, null);
                        }
                        JsonArray coordinates = features
                                .get(0)
                                .getAsJsonObject()
                                .getAsJsonObject("geometry")
                                .getAsJsonArray("coordinates");

                        // OSM Nominatim returns longitude first, latitude second
                        return new Pair<Float, Float>(
                                coordinates.get(1).getAsFloat(),
                                coordinates.get(0).getAsFloat()
                        );
                    }
//                    JsonObject geocoding = body
//                            .getAsJsonArray("features")
//                            .get(0)
//                            .getAsJsonObject()
//                            .getAsJsonObject("properties")
//                            .getAsJsonObject("geocoding");
////                    String address = "";
//                    ArrayList<String> addressFragments = new ArrayList<String>();
//                    if (geocoding.get("postcode") != null) {
//                        addressFragments.add(geocoding.get("postcode").getAsString());
//                    }
//                    if (geocoding.get("city") != null) {
//                        addressFragments.add(geocoding.get("city").getAsString());
//                        if (geocoding.get("district") != null) {
//                            addressFragments.add(geocoding.get("district").getAsString());
//                        }
//                    } else if (geocoding.get("state") != null) {
//                        addressFragments.add(geocoding.get("state").getAsString());
//                        if (geocoding.get("county") != null) {
//                            addressFragments.add(geocoding.get("county").getAsString());
//                        }
//                        if (geocoding.get("district") != null) {
//                            addressFragments.add(geocoding.get("district").getAsString());
//                        }
//                        if (geocoding.get("locality") != null) {
//                            addressFragments.add(geocoding.get("locality").getAsString());
//                        }
//                    }
//                    if (geocoding.get("street") != null) {
//                        addressFragments.add(geocoding.get("street").getAsString());
//                    }
//                    if (geocoding.get("housenumber") != null) {
//                        addressFragments.add(geocoding.get("housenumber").getAsString());
//                    }
//                    if (geocoding.get("name") != null) {
//                        addressFragments.add(geocoding.get("name").getAsString());
//                    }
//
//                    return new Pair<Boolean, String>(true, String.join(", ", addressFragments));
                } else {
                    return new Pair<Float, Float>(null, null);
                }
            }
        });
    }

    /** Get equatorial radius **/
    public static double getA() {
        return a;
    }

    /** Get flattening **/
    public static double getF() {
        return f;
    }

    /** Get polar radius **/
    public static double getB() {
        return a * (1 - f);
    }

    /** Get eccentricity **/
    public static double getE() {
        return 2 * f - Math.pow(f, 2);
    }

    /** Get reduced latitude from geographic latitude **/
    public static double getBeta(double phi) {
        return Math.atan((1 - f) * Math.tan(phi));
    }

    public static double getReducedLatitude(double lat) {
        return getBeta(lat);
    }

    /** Get central angle between two points **/
    public static double getRo(double lon1, double lon2, double lat1, double lat2) {
        return Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
    }

    public static double getCentralAngle(double lat1, double lat2, double lon1, double lon2) {
        return getRo(lon1, lon2, lat1, lat2);
    }

    public static double getGeoDistance(double lat1, double lat2, double lon1, double lon2) {
        // coordinates should be converted from degrees to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);

        double beta1 = getReducedLatitude(lat1);
        double beta2 = getReducedLatitude(lat2);

        double ro = getCentralAngle(lat1, lat2, lon1, lon2);

        double p = (beta1 + beta2) / 2;
        double q = (beta2 - beta1) / 2;

        double x = (ro - Math.sin(ro)) * Math.pow(Math.sin(p), 2) * Math.pow(Math.cos(q), 2) / Math.pow(Math.cos(ro / 2), 2);
        double y = (ro + Math.sin(ro)) * Math.pow(Math.cos(p), 2) * Math.pow(Math.sin(q), 2) / Math.pow(Math.sin(ro / 2), 2);

        return a * (ro - f * (x + y) / 2);
    }


}
