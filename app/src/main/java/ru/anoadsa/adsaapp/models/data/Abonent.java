package ru.anoadsa.adsaapp.models.data;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

public class Abonent {
    private String id;
    private String name;
    private String address;
    private float distance;
    private float radius;
    private float latitude;
    private float longitude;

    public Abonent(String id, String name, String address, float distance, float radius,
                   float latitude, float longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.distance = distance;
        this.radius = radius;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Abonent(@NonNull JsonObject json) {
        this.id = json.get("id").getAsString();
        this.name = json.get("name").getAsString();
        this.address = json.get("address").getAsString();
        this.distance = json.get("distance").getAsFloat();
        this.radius = json.get("radius").getAsFloat();
        this.latitude = json.get("latitude").getAsFloat();
        this.longitude = json.get("longitude").getAsFloat();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public float getDistance() {
        return distance;
    }

    public float getRadius() {
        return radius;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }
}
