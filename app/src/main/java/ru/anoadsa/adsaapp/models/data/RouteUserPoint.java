package ru.anoadsa.adsaapp.models.data;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import ru.anoadsa.adsaapp.ui.views.RoutePointView;

@Entity(foreignKeys =
    @ForeignKey(entity = Route.class, parentColumns = "id", childColumns = "route_id", onDelete = CASCADE)
)
public class RouteUserPoint {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "route_id")
    private int routeId;

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "order")
    private int order;

    @NonNull
    @ColumnInfo(name = "latitude")
    private float latitude;

    @NonNull
    @ColumnInfo(name = "longitude")
    private float longitude;

    @ColumnInfo(name = "address")
    private String address;

    public static int sortByOrder(@NonNull RouteUserPoint rup1, @NonNull RouteUserPoint rup2) {
        if (rup1.getOrder() < rup2.getOrder()) {
            return -1;
        } else if (rup1.getOrder() > rup2.getOrder()) {
            return 1;
        } else {
            return 0;
        }
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
