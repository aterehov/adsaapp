package ru.anoadsa.adsaapp.ui.menufragments.map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.mapsforge.core.model.LatLong;

import ru.anoadsa.adsaapp.Geo;
import ru.anoadsa.adsaapp.databinding.DialogMapGeoBinding;
import ru.anoadsa.adsaapp.databinding.DialogSosGeoBinding;
import ru.anoadsa.adsaapp.ui.menufragments.sos.SosViewModel;
import ru.anoadsa.adsaapp.ui.views.TitleTextInputView;

public class MapGeoDialog extends DialogFragment {
    private @NonNull DialogMapGeoBinding binding;
    private MapViewModel viewModel;

    private TitleTextInputView latTTI;
    private TitleTextInputView lonTTI;
    private TitleTextInputView addrTTI;
    private TextView nominatimText;
    private TextView addrHelp;
    private Button fillGeoButton;

    private boolean editable = false;

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    private void searchAddressByCoordinates() {
        if (latTTI.getInputText() != null
                && lonTTI.getInputText() != null
//            && incidentSearchRadiusTTIV.getInputText() != null
                && !latTTI.getInputText().isEmpty()
                && !lonTTI.getInputText().isEmpty()
//            && !incidentSearchRadiusTTIV.getInputText().isEmpty()
        ) {
            float lat;
            float lon;


            try {
                lat = Float.parseFloat(latTTI.getInputText());
                lon = Float.parseFloat(lonTTI.getInputText());
//                radius = Float.parseFloat(incidentSearchRadiusTTIV.getInputText());
            } catch (NumberFormatException e) {
                return;
            }

            viewModel.getAddressByCoordinates(lat, lon);
        }
    }

    private void searchCoordinatesByAddress() {
        if (addrTTI.getInputText() != null
                && !addrTTI.getInputText().isEmpty()) {
            viewModel.getCoordinatesByAddress(addrTTI.getInputText());
        }
    }

    private void refreshLocation() {
//        geoUpdateType = "geolocation";
//        incidentLocationLatitudeTTIV.setDoWhenUserTypesEnabled(false);
//        incidentLocationLatitudeTTIV.setDoWhenUserTypesEnabled(false);
//        incidentLocationAddressTTIV.setDoWhenUserTypesEnabled(false);
//        incidentLocationLatitudeTTIV.setEnabled(false);
//        incidentLocationLongitudeTTIV.setEnabled(false);
//        incidentLocationAddressTTIV.setEnabled(false);

//        latitudeBeforeRefresh = incidentLocationLatitudeTTIV.getInputText();
//        longitudeBeforeRefresh = incidentLocationLongitudeTTIV.getInputText();
//        addressBeforeRefresh = incidentLocationAddressTTIV.getInputText();

        latTTI.setTTIEnabled(false);
        lonTTI.setTTIEnabled(false);
        addrTTI.setTTIEnabled(false);
        latTTI.setInputText("Подождите...");
        lonTTI.setInputText("Подождите...");
        addrTTI.setInputText("Подождите...");
        viewModel.prepareForLocationUpdate();
        Geo.refreshLocation(getContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogMapGeoBinding.inflate(getActivity().getLayoutInflater(), null, false);
        viewModel = new ViewModelProvider(getActivity()).get(MapViewModel.class);

        latTTI = binding.incidentLocationLatitudeTTIV;
        lonTTI = binding.incidentLocationLongitudeTTIV;
        addrTTI = binding.incidentLocationAddressTTIV;
        addrHelp = binding.incidentLocationAddressHint;
        fillGeoButton = binding.incidentFillGeolocationButton;

//        nominatimText = binding.incidentNominatimMentionText;

        latTTI.setOnFocusChangeListenerTTI(
                TitleTextInputView.doOnTextChangedOnlyIfHasFocus
        );
        lonTTI.setOnFocusChangeListenerTTI(
                TitleTextInputView.doOnTextChangedOnlyIfHasFocus
        );
        addrTTI.setOnFocusChangeListenerTTI(
                TitleTextInputView.doOnTextChangedOnlyIfHasFocus
        );


        latTTI.doOnTextChanged(
                null,
                2500,
                new Runnable() {
                    @Override
                    public void run() {
                        searchAddressByCoordinates();
//                        return false;
                    }
                }
        );
        lonTTI.doOnTextChanged(
                null,
                2500,
                new Runnable() {
                    @Override
                    public void run() {
                        searchAddressByCoordinates();
//                        return false;
                    }
                }
        );
        addrTTI.doOnTextChanged(
                null,
                2500,
                new Runnable() {
                    @Override
                    public void run() {
                        searchCoordinatesByAddress();
//                        return false;
                    }
                }
        );

        latTTI.setDoOnTextChangedEnabled(false);
        lonTTI.setDoOnTextChangedEnabled(false);
        addrTTI.setDoOnTextChangedEnabled(false);

//        nominatimText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("https://www.openstreetmap.org/copyright")
//                ));
//            }
//        });


//        viewModel.getLatitude().observe(
////                getViewLifecycleOwner(),
//                this,
//                new Observer<Float>() {
//            @Override
//            public void onChanged(Float lat) {
//                if (lat == null) {
////                    if (viewModel.getFinishedGeoUpdate().getValue()) {
////                        latTTI.setInputText("");
////                    }
//                    return;
//                }
//                latTTI.setInputText(String.valueOf(lat));
//                latTTI.setTTIEnabled(true);
//            }
//        });

//        viewModel.getLongitude().observe(
////                getViewLifecycleOwner(),
//                this,
//                new Observer<Float>() {
//            @Override
//            public void onChanged(Float lon) {
//                if (lon == null) {
////                    if (viewModel.getFinishedGeoUpdate().getValue()) {
////                        lonTTI.setInputText("");
////                    }
//                    return;
//                }
//                lonTTI.setInputText(String.valueOf(lon));
//                lonTTI.setTTIEnabled(true);
//            }
//        });

        viewModel.getTypedLocation().observe(
//                getViewLifecycleOwner(),
                this,
                new Observer<LatLong>() {
                    @Override
                    public void onChanged(LatLong location) {
                        if (location == null) {
//                    if (viewModel.getFinishedGeoUpdate().getValue()) {
//                        latTTI.setInputText("");
//                    }
                            return;
                        }
                        latTTI.setInputText(String.valueOf(location.getLatitude()));
                        lonTTI.setInputText(String.valueOf(location.getLongitude()));
                        if (editable) {
                            latTTI.setTTIEnabled(true);
                            lonTTI.setTTIEnabled(true);
                        }
                    }
                });

        viewModel.getTypedAddress().observe(
//                getViewLifecycleOwner(),
                this,
                new Observer<String>() {
            @Override
            public void onChanged(String a) {
                if (a == null) {
                    return;
                }
                addrTTI.setInputText(a);
                if (editable) {
                    addrTTI.setTTIEnabled(true);
                }
            }
        });

//        viewModel.getUserAddress().observe(
////                getViewLifecycleOwner(),
//                this,
//                new Observer<String>() {
//                    @Override
//                    public void onChanged(String a) {
//                        if (a == null) {
//                            return;
//                        }
//                        addrTTI.setInputText(a);
//                        if (editable) {
////                            addrTTI.setTTIEnabled(true);
//                        }
//                    }
//                });

        viewModel.getLocationUpdated().observe(
//                getViewLifecycleOwner(),
                this,
                new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean finished) {
                if (finished) {
//                    if (viewModel.getLatitude().getValue() != null) {
//                        latTTI.setInputText(viewModel.getLatitude().getValue().toString());
//                    } else {
//                        latTTI.setInputText("");
//                    }
//                    if (viewModel.getLongitude().getValue() != null) {
//                        lonTTI.setInputText(viewModel.getLongitude().getValue().toString());
//                    } else {
//                        lonTTI.setInputText("");
//                    }
                    if (viewModel.getTypedLocation().getValue() != null) {
                        latTTI.setInputText(String.valueOf(viewModel.getTypedLocation().getValue().getLatitude()));
                        lonTTI.setInputText(String.valueOf(viewModel.getTypedLocation().getValue().getLongitude()));
                        searchAddressByCoordinates();
                    } else {
                        latTTI.setInputText("");
                        lonTTI.setInputText("");
                        addrTTI.setInputText("");
                        if (editable) {
                            addrTTI.setTTIEnabled(true);
                        }
                    }
//                    if (viewModel.getLatitude().getValue() != null && viewModel.getLongitude().getValue() != null) {
//                        searchAddressByCoordinates();
//                    } else {
//                        addrTTI.setInputText("");
//                        addrTTI.setTTIEnabled(true);
//                    }
                    if (editable) {
                        latTTI.setTTIEnabled(true);
                        lonTTI.setTTIEnabled(true);
                    }
////                    addrTTI.setTTIEnabled(true);
//                    if (!latTTI.getInputText().isEmpty() && !lonTTI.getInputText().isEmpty()) {
//                        searchAddressByCoordinates();
//                    } else {
//                        addrTTI.setInputText("");
//                        addrTTI.setTTIEnabled(true);
//                    }
                }
            }
        });

        viewModel.subscribeOnGeoUpdates(
//                getViewLifecycleOwner()
                this
        );

//        refreshLocation();

        if (!editable) {
            fillGeoButton.setVisibility(View.GONE);
            addrHelp.setVisibility(View.GONE);
            latTTI.setTTIEnabled(false);
            lonTTI.setTTIEnabled(false);
            addrTTI.setTTIEnabled(false);
        }
        latTTI.setInputText("Подождите...");
        lonTTI.setInputText("Подождите...");
        addrTTI.setInputText("Подождите...");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setView(
//                        getActivity().getLayoutInflater().inflate(R.layout.dialog_sos_geo, null)
                    binding.getRoot()
            )
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // TODO pass geolocation to activity
                    if (!editable) {
                        return;
                    }
                    if (!latTTI.isTTIEnabled()
                            || !lonTTI.isTTIEnabled()
                            || !addrTTI.isTTIEnabled()) {
                        return;
                    }
                    if (latTTI.getInputText() == null || latTTI.getInputText().isEmpty()
                        || lonTTI.getInputText() == null || lonTTI.getInputText().isEmpty()) {
//                            viewModel.setLatitude(null);
                        viewModel.setUserLocation(null);
                    } else {
//                            viewModel.setLatitude(Float.parseFloat(latTTI.getInputText()));
                        viewModel.setUserLocation(
                                new LatLong(
                                        Double.parseDouble(latTTI.getInputText()),
                                        Double.parseDouble(lonTTI.getInputText())
                                )
                        );
                    }
//                        if (lonTTI.getInputText() == null || lonTTI.getInputText().isEmpty()) {
//                            viewModel.setLongitude(null);
//                        } else {
//                            viewModel.setLongitude(Float.parseFloat(lonTTI.getInputText()));
//                        }
                    if (addrTTI.getInputText() == null || addrTTI.getInputText().isEmpty()) {
                        viewModel.setUserAddress(null);
                    } else {
                        viewModel.setUserAddress(addrTTI.getInputText());
                    }
                    viewModel.setGeoDialogFinished(true);
                    dialog.cancel();
                }
            });
        if (editable) {
            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // TODO pass cancellation to activity
                    dialog.cancel();
                }
            });
        }
        return builder.create();
//                .create()
//            ;
    }

    @Override
    public void onDestroyView() {
        viewModel.setTypedLocation(null);
        viewModel.setTypedAddress(null);
        super.onDestroyView();
    }

    @Override
    public void onStart() {


        super.onStart();
    }
}
