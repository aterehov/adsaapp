package ru.anoadsa.adsaapp.ui.menufragments.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.Contract;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.input.MapZoomControls;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.map.scalebar.DefaultMapScaleBar;
import org.mapsforge.map.view.InputListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import ru.anoadsa.adsaapp.DevSettings;
import ru.anoadsa.adsaapp.Geo;
import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.databinding.FragmentMapBinding;
import ru.anoadsa.adsaapp.models.data.Abonent;
import ru.anoadsa.adsaapp.models.data.AbonentsNearbyMap;
import ru.anoadsa.adsaapp.ui.abstracts.UiMenuFragment;
import ru.anoadsa.adsaapp.ui.InDevelopmentDialog;
import ru.anoadsa.adsaapp.ui.activities.permission.PermissionActivity;

public class MapFragment extends UiMenuFragment<MapViewModel, FragmentMapBinding> {
    private MapView map;
    private ExtendedFloatingActionButton mapFab;
    private TextView mapOSMMention;
    private EditText searchEditText;

    private TileCache tileCache;
    private OnlineTileSource onlineTileSource;
    private OpenStreetMapMapnik tileSource;
    private TileDownloadLayer downloadLayer;

    private FixedPixelCircle locationCircle;

    private Snackbar locationMessage;

    private boolean searchAbonentsAllowed = true;

    private String updateLocationType;

    private ArrayList<Layer> abonentsMarkers = new ArrayList<Layer>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private ActivityResultLauncher<String> locationFineLauncher;
    private ActivityResultLauncher<String> locationCoarseLauncher;

    private ActivityResultCallback<Boolean> locationFineCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(@NonNull Boolean success) {
            if (success) {
                updateLocation();
            } else if (!Static.checkPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                locationCoarseLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
            } else {
                updateLocation();
            }
        }
    };

    private ActivityResultCallback<Boolean> locationCoarseCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean success) {
//            if (success) {
            updateLocation();
//            } else {
//
//            }
        }
    };

    private void findAbonentsDelayed() {
        mainHandler.removeCallbacks(findAbonentsRunnable);
        mainHandler.postDelayed(findAbonentsRunnable, 2500);
    }

    private Runnable findAbonentsRunnable = new Runnable() {
        @Override
        public void run() {
            findAbonents();
        }
    };

    @NonNull
    private Paint createPaint(int color, int strokeWidth, Style style) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }


    @NonNull
    @Contract("_, _, _, _ -> new")
    private ModifiableActionsMarker createModifiableActionsMarker(@NonNull Context c, int resourceIdentifier, LatLong latLong, MapView mapView) {
        Bitmap bitmap = new AndroidBitmap(BitmapFactory.decodeResource(c.getResources(), resourceIdentifier));
        bitmap.incrementRefCount();
        return new ModifiableActionsMarker(latLong, bitmap, 0, -bitmap.getHeight() / 2);
    }

    @Override
    protected void setViewBinding(Class<FragmentMapBinding> vbclass) {
        super.setViewBinding(vbclass);
    }

    @Override
    protected void getRoot(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.getRoot(inflater, container);
    }

    @Override
    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.initUi(inflater, container);
        AndroidGraphicFactory.createInstance(getActivity().getApplication());

        map = binding.map;
        mapFab = binding.mapFab;
        mapOSMMention = binding.mapOSMMention;
        searchEditText = binding.searchEditText;
    }

    @Override
    protected void configureUiState() {

        map.setBuiltInZoomControls(true);
        map.getMapZoomControls()
                .setZoomControlsOrientation(MapZoomControls.Orientation.VERTICAL_IN_OUT);
        map.getMapZoomControls().setZoomInResource(R.drawable.zoom_control_in);
        map.getMapZoomControls().setZoomOutResource(R.drawable.zoom_control_out);
        map.getMapZoomControls().setZoomControlsGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        map.getMapZoomControls().setMarginHorizontal(Static.dpToPixels(8, getContext()));


        map.getModel().displayModel.setUserScaleFactor(2f);

        map.setMapScaleBar(new DefaultMapScaleBar(
                map.getModel().mapViewPosition,
                map.getModel().mapViewDimension,
                AndroidGraphicFactory.INSTANCE,
                map.getModel().displayModel,
                (float) 3
        ));
        map.getMapScaleBar().setVisible(true);
//        map.getModel().displayModel.setFixedTileSize(256);
//        map.getModel().mapViewPosition.setScaleFactor(2);
//        map.getModel().mapViewPosition.setScaleFactorAdjustment(2);
//        map.getModel().frameBufferModel.setScaleEnabled(true);

        tileCache = AndroidUtil.createTileCache(
                getContext(),
                "mapcache",
                map.getModel().displayModel.getTileSize(),
                1f,
                map.getModel().frameBufferModel.getOverdrawFactor()
            );

        tileSource = OpenStreetMapMapnik.INSTANCE;
        tileSource.setUserAgent(DevSettings.getUserAgent());
//        onlineTileSource = new OnlineTileSource(
//                new String[]{
//                        "a.tile.openstreetmap.org",
//                        "b.tile.openstreetmap.org",
//                        "c.tile.openstreetmap.org"
//                },
//                443
//        );
//        onlineTileSource
//                .setName("ANO ADSA Map")
//                .setAlpha(false)
//                .setParallelRequestsLimit(8)
//                .setProtocol("https")
//                .setTileSize(256)
//                .setZoomLevelMax((byte) 18)
//                .setZoomLevelMin((byte) 0)
//                .setUserAgent(DevSettings.getUserAgent());

        downloadLayer = new TileDownloadLayer(
                tileCache,
                map.getModel().mapViewPosition,
                tileSource,
//                onlineTileSource,
                AndroidGraphicFactory.INSTANCE
            );

        map.getLayerManager().getLayers().add(downloadLayer);

        map.setZoomLevelMin(
                tileSource
//                onlineTileSource
                        .getZoomLevelMin()
        );
        map.setZoomLevelMax(
                tileSource
//                onlineTileSource
                        .getZoomLevelMax()
        );

        // TODO replace with geolocation
//        map.setCenter(new LatLong(56.130077, 40.396767));
        map.setZoomLevel((byte) 16);

//        locationCircle = new FixedPixelCircle(
//                new LatLong(56.130077, 40.396767),
//                5,
//                createPaint(AndroidGraphicFactory.INSTANCE.createColor(Color.RED), 0, Style.FILL),
//                null
//            );

//        map.getLayerManager().getLayers().add(locationCircle);
    }

    @Override
    protected void setViewModel(Class<MapViewModel> vmclass) {
        super.setViewModel(vmclass);
    }

    @Override
    protected void configureViewModel() {
        if(vmclass != null) {
            viewModel = new ViewModelProvider(getActivity()).get(vmclass);
        }
    }

    @Override
    protected void configureUiActions() {
        mapOSMMention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.openstreetmap.org/copyright")
                ));
            }
        });

        mapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MapCategoryDialog().show(
                        getActivity().getSupportFragmentManager(),
                        "MAP_CATEGORY_DIALOG"
                );
            }
        });

        map.addInputListener(new InputListener() {
            @Override
            public void onMoveEvent() {
                System.out.println("MAPSFORGE MOVE EVENT");
                findAbonentsDelayed();
            }

            @Override
            public void onZoomEvent() {
                System.out.println("MAPSFORGE ZOOM EVENT");
                findAbonentsDelayed();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            private Timer timer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        applySearchFilter();
                    }
                }, 2500);
            }
        });
    }

    private void applySearchFilter() {
        if (searchEditText.getText() == null
                || searchEditText.getText().length() == 0
                || viewModel.getAbonentsNearbyMap().getValue() == null) {
            for (Layer marker: abonentsMarkers) {
                marker.setVisible(true);
            }
            return;
        }
        // Prevent updating abonentsmap during filter applying
//        mapFab.setEnabled(false);
//        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mapFab.setEnabled(false);
            }
        });
        searchAbonentsAllowed = false;

        String text = searchEditText.getText().toString();
//        String splitChars = "";
//        String allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZабвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ0123456789-";
//        for (char c: text.toCharArray()) {
//            if (allowedChars.indexOf(c) == -1 && splitChars.indexOf(c) == -1) {
//                splitChars += c;
//            }
//        }
//        ArrayList<String> words = new ArrayList<String>();
//        words.add(text);
//        for (char c: splitChars.toCharArray()) {
////            ArrayList<String> newWords = new ArrayList<String>(words);
//            int len = words.size();
//            for (int i = 0; i < len; ++i) {
//                words.addAll(Arrays.asList(words.get(0).split(String.valueOf(c))));
//                words.remove(0);
//            }
//        }
        ArrayList<String> words = Static.stringToWords(text);
        Collection<Abonent> abonents = viewModel.getAbonentsNearbyMap().getValue().getAbonents();
        int i = 0;
        for (Abonent abonent: abonents) {
            ArrayList<String> nameWords = Static.stringToWords(abonent.getName());
            ArrayList<String> addressWords = Static.stringToWords(abonent.getAddress());
            boolean finish = false;

            for (String word: words) {
                for (String nameWord: nameWords) {
                    if (nameWord.toLowerCase().contains(word.toLowerCase())) {
                        abonentsMarkers.get(i).setVisible(true);
                        finish = true;
                        break;
                    }
                }
                if (finish) {
                    break;
                }
                for (String addressWord: addressWords) {
                    if (addressWord.toLowerCase().contains(word.toLowerCase())) {
                        abonentsMarkers.get(i).setVisible(true);
                        finish = true;
                        break;
                    }
                }
                if (finish) {
                    break;
                }
            }

            if (!finish) {
                abonentsMarkers.get(i).setVisible(false);
            }
            ++i;
        }

//        mapFab.setEnabled(true);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mapFab.setEnabled(true);
            }
        });
        searchAbonentsAllowed = true;
    }

    private void updateLocation() {
        viewModel.prepareForLocationUpdate();
        viewModel.setLocationUpdateType(updateLocationType);
        Geo.refreshLocation(getContext());
    }

    private void updateLocationWithPermissionsCheck() {
        if (Static.checkPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            updateLocation();
        } else {
            locationFineLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void configureViewModelActions() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            private boolean ran;

            @Override
            public void onChanged(String error) {
                if (wasRestored && !ran) {
                    ran = true;
                    return;
                }
                if (error != null && !error.isEmpty()) {
                    if (locationMessage != null && locationMessage.isShown()) {
                        locationMessage.dismiss();
                    }
                    Snackbar.make(map, error, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getUserLocation().observe(getViewLifecycleOwner(), new Observer<LatLong>() {
            private boolean ran;

            @Override
            public void onChanged(LatLong location) {
                if (location == null) {
                    if (!ran) {
                        ran = true;
                        locationMessage = Snackbar.make(map, "Определение местоположения...",
                                Snackbar.LENGTH_INDEFINITE);
                        locationMessage.show();

                        updateLocationType = "user";
                        updateLocationWithPermissionsCheck();
                    } else {
                        if (locationMessage != null && locationMessage.isShown()) {
                            locationMessage.dismiss();
                        }
                        Snackbar.make(map, "Не удалось определить местоположение",
                                Snackbar.LENGTH_LONG).show();
                    }
                    return;
                }
                if (locationMessage != null && locationMessage.isShown()) {
                    locationMessage.dismiss();
                }
                if (!wasRestored) {
                    map.setCenter(location);
                }
                locationCircle = new FixedPixelCircle(
                        location,
                        5,
                        createPaint(AndroidGraphicFactory.INSTANCE.createColor(Color.RED), 0, Style.FILL),
                        null
                ) {
                    @Override
                    public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
                        if (!this.contains(layerXY, tapXY, map)) {
                            return false;
                        }
                        viewModel.setTypedLocation(location);
                        viewModel.getAddressByCoordinates((float) location.getLatitude(), (float) location.getLongitude());
                        MapGeoDialog dialog = new MapGeoDialog();
                        dialog.setEditable(false);
                        dialog.show(getActivity().getSupportFragmentManager(), "MAP_GEO_DIALOG");
                        return true;
                    }
                };

                map.getLayerManager().getLayers().add(locationCircle);
            }
        });

        viewModel.getSelectedCategoryName().observe(getViewLifecycleOwner(), new Observer<String>() {
            private boolean ran;

            @Override
            public void onChanged(String categoryName) {
                if (categoryName == null) {
                    // TODO: tell user to choose category
                    ran = true;
                    return;
                }
                mapFab.setText(categoryName);
                if (wasRestored && !ran) {
                    ran = true;
                    return;
                }
                findAbonents();
            }
        });

        viewModel.getAbonentsNearbyMap().observe(getViewLifecycleOwner(), new Observer<AbonentsNearbyMap>() {
            @Override
            public void onChanged(AbonentsNearbyMap abonentsNearbyMap) {
                if (abonentsNearbyMap == null) {
                    return;
                }
                map.getLayerManager().getLayers().removeAll(abonentsMarkers);
                abonentsMarkers.clear();
                for (Abonent abonent: abonentsNearbyMap.getAbonents()) {
                    ModifiableActionsMarker marker = createModifiableActionsMarker(
                            getContext(),
                            R.drawable.marker_green,
                            new LatLong(abonent.getLatitude(), abonent.getLongitude()),
                            map
                    );
                    // applySearchFilter will show the necessary markers
                    marker.setVisible(false);
                    // TODO add actions to markers
                    marker.setOnTapRunnable(new MarkerInnerRunnable() {
                        @Override
                        public boolean run() {
                            if (!parentMarker.contains(layerXY, tapXY, map)) {
                                return false;
                            }
                            MapAbonentDialog dialog = new MapAbonentDialog();
                            dialog.setName(abonent.getName());
                            dialog.setAddress(abonent.getAddress());
                            if (viewModel.getUserLocation().getValue() != null) {
                                dialog.setDistance(Geo.getGeoDistance(
                                        viewModel.getUserLocation().getValue().getLatitude(),
                                        abonent.getLatitude(),
                                        viewModel.getUserLocation().getValue().getLongitude(),
                                        abonent.getLongitude()
                                ));
                            }
                            dialog.show(getActivity().getSupportFragmentManager(), "MAP_ABONENT_DIALOG");
                            return true;
                        }
                    });

                    abonentsMarkers.add(marker);
                }
                map.getLayerManager().getLayers().addAll(abonentsMarkers);
                applySearchFilter();
            }
        });
    }

    private void findAbonents() {
        System.out.println("!!! TRIED SEARCHING ABONENTS !!!");
        if (!searchAbonentsAllowed) {
            return;
        }
        if (viewModel.getSelectedCategoryName().getValue() == null) {
            return;
        }
        double r = 0;
        LatLong[] points = new LatLong[] {
                new LatLong(map.getBoundingBox().minLatitude, map.getBoundingBox().minLongitude),
                new LatLong(map.getBoundingBox().minLatitude, map.getBoundingBox().maxLongitude),
                new LatLong(map.getBoundingBox().maxLatitude, map.getBoundingBox().minLongitude),
                new LatLong(map.getBoundingBox().maxLatitude, map.getBoundingBox().maxLongitude)
        };
        for (LatLong point: points) {
            double rr = Geo.getGeoDistance(
                    map.getBoundingBox().getCenterPoint().getLatitude(),
                    point.getLatitude(),
                    map.getBoundingBox().getCenterPoint().getLongitude(),
                    point.getLongitude()
            );
            if (rr > r) {
                r = rr;
            }
        }

        viewModel.searchAbonents(
                viewModel.getSelectedCategoryName().getValue(),
                ((float) r / 1000), // from meters to kilometers
                (float) map.getBoundingBox().getCenterPoint().getLatitude(),
                (float) map.getBoundingBox().getCenterPoint().getLongitude()
        );
    }

    protected void configureSharedViewModelActions() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DevSettings.APP_VERSION_CODE < 2 && !DevSettings.ENABLE_ALL_FEATURES) {
            new InDevelopmentDialog().show(getActivity().getSupportFragmentManager(), "IN_DEVELOPMENT_MAP");
            return null;
        }
        setViewModel(MapViewModel.class);
        setViewBinding(FragmentMapBinding.class);
        configureSharedViewModelActions();
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel.subscribeOnGeoUpdates(getViewLifecycleOwner());
        locationFineLauncher = registerForActivityResult(PermissionActivity.getARC(), locationFineCallback);
        locationCoarseLauncher = registerForActivityResult(PermissionActivity.getARC(), locationCoarseCallback);
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (map == null) {
            return;
        }
        outState.putByte("mapZoomLevel", map.getModel().mapViewPosition.getZoomLevel());
        outState.putDouble("mapCenterLatitude", map.getModel().mapViewPosition.getCenter().getLatitude());
        outState.putDouble("mapCenterLongitude", map.getModel().mapViewPosition.getCenter().getLongitude());
        outState.putString("searchText", searchEditText.getText().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        map.setZoomLevel(savedInstanceState.getByte("mapZoomLevel"));
        map.setCenter(new LatLong(
                savedInstanceState.getDouble("mapCenterLatitude"),
                savedInstanceState.getDouble("mapCenterLongitude")
        ));
        searchEditText.setText(savedInstanceState.getString("searchText"));
    }

    @Override
    public void onDestroyView() {
        if (map != null) {
            map.destroyAll();
        }
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        if (downloadLayer != null) {
            downloadLayer.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (downloadLayer != null) {
            downloadLayer.onResume();
        }
    }
}
