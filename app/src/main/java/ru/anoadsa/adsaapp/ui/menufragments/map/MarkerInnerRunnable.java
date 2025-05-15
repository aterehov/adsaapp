package ru.anoadsa.adsaapp.ui.menufragments.map;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.layer.overlay.Marker;

public abstract class MarkerInnerRunnable {
    protected Marker parentMarker;
    protected LatLong tapLatLong;
    protected Point layerXY;
    protected Point tapXY;

    public Marker getParentMarker() {
        return parentMarker;
    }

    public void setParentMarker(Marker parentMarker) {
        this.parentMarker = parentMarker;
    }

    public LatLong getTapLatLong() {
        return tapLatLong;
    }

    public void setTapLatLong(LatLong tapLatLong) {
        this.tapLatLong = tapLatLong;
    }

    public Point getLayerXY() {
        return layerXY;
    }

    public void setLayerXY(Point layerXY) {
        this.layerXY = layerXY;
    }

    public Point getTapXY() {
        return tapXY;
    }

    public void setTapXY(Point tapXY) {
        this.tapXY = tapXY;
    }

    public abstract boolean run();
}
