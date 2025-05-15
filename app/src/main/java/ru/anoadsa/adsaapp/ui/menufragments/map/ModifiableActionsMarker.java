package ru.anoadsa.adsaapp.ui.menufragments.map;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.layer.overlay.Marker;

public class ModifiableActionsMarker extends Marker {
    /**
     * @param latLong          the initial geographical coordinates of this marker (may be null).
     * @param bitmap           the initial {@code Bitmap} of this marker (may be null).
     * @param horizontalOffset the horizontal marker offset.
     * @param verticalOffset   the vertical marker offset.
     */
    public ModifiableActionsMarker(LatLong latLong, Bitmap bitmap, int horizontalOffset, int verticalOffset) {
        super(latLong, bitmap, horizontalOffset, verticalOffset);
    }

    private MarkerInnerRunnable onTapRunnable;
    private MarkerInnerRunnable onLongPressRunnable;

    public void setOnTapRunnable(MarkerInnerRunnable r) {
        onTapRunnable = r;
    }

    public void setOnLongPressRunnable(MarkerInnerRunnable r) {
        onLongPressRunnable = r;
    }

    @Override
    public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
//                return super.onTap(tapLatLong, layerXY, tapXY);
        if (onTapRunnable != null) {
            onTapRunnable.setParentMarker(this);
            onTapRunnable.setTapLatLong(tapLatLong);
            onTapRunnable.setLayerXY(layerXY);
            onTapRunnable.setTapXY(tapXY);
            return onTapRunnable.run();
        }
        return false;
    }

    @Override
    public boolean onLongPress(LatLong tapLatLong, Point layerXY, Point tapXY) {
//                return super.onLongPress(tapLatLong, layerXY, tapXY);
        if (onLongPressRunnable != null) {
            onLongPressRunnable.setParentMarker(this);
            onLongPressRunnable.setTapLatLong(tapLatLong);
            onLongPressRunnable.setLayerXY(layerXY);
            onLongPressRunnable.setTapXY(tapXY);
            return onLongPressRunnable.run();
        }
        return false;
    }
}
