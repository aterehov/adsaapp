package ru.anoadsa.adsaapp.ui.menufragments.sos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import ru.anoadsa.adsaapp.Geo;
import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.databinding.DialogSosGeoBinding;
import ru.anoadsa.adsaapp.ui.views.TitleTextInputView;

public class SelectGeoDialog extends DialogFragment {
    private DialogSosGeoBinding binding;
    private SosViewModel viewModel;

    private TitleTextInputView latTTI;
    private TitleTextInputView lonTTI;
    private TitleTextInputView addrTTI;
    private TextView nominatimText;

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
        binding = DialogSosGeoBinding.inflate(getActivity().getLayoutInflater(), null, false);
        viewModel = new ViewModelProvider(getActivity()).get(SosViewModel.class);

        latTTI = binding.incidentLocationLatitudeTTIV;
        lonTTI = binding.incidentLocationLongitudeTTIV;
        addrTTI = binding.incidentLocationAddressTTIV;
        nominatimText = binding.incidentNominatimMentionText;

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
                    }
                }
        );

        latTTI.setDoOnTextChangedEnabled(false);
        lonTTI.setDoOnTextChangedEnabled(false);
        addrTTI.setDoOnTextChangedEnabled(false);


        viewModel.getLatitude().observe(
//                getViewLifecycleOwner(),
                this,
                new Observer<Float>() {
            @Override
            public void onChanged(Float lat) {
                if (lat == null) {
//                    if (viewModel.getFinishedGeoUpdate().getValue()) {
//                        latTTI.setInputText("");
//                    }
                    return;
                }
                latTTI.setInputText(String.valueOf(lat));
                latTTI.setTTIEnabled(true);
            }
        });

        viewModel.getLongitude().observe(
//                getViewLifecycleOwner(),
                this,
                new Observer<Float>() {
            @Override
            public void onChanged(Float lon) {
                if (lon == null) {
//                    if (viewModel.getFinishedGeoUpdate().getValue()) {
//                        lonTTI.setInputText("");
//                    }
                    return;
                }
                lonTTI.setInputText(String.valueOf(lon));
                lonTTI.setTTIEnabled(true);
            }
        });

        viewModel.getAddress().observe(
//                getViewLifecycleOwner(),
                this,
                new Observer<String>() {
            @Override
            public void onChanged(String a) {
                if (a == null) {
                    return;
                }
                addrTTI.setInputText(a);
                addrTTI.setTTIEnabled(true);
            }
        });

        viewModel.getFinishedGeoUpdate().observe(
//                getViewLifecycleOwner(),
                this,
                new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean finished) {
                if (finished) {
                    if (viewModel.getLatitude().getValue() != null) {
                        latTTI.setInputText(viewModel.getLatitude().getValue().toString());
                    } else {
                        latTTI.setInputText("");
                    }
                    if (viewModel.getLongitude().getValue() != null) {
                        lonTTI.setInputText(viewModel.getLongitude().getValue().toString());
                    } else {
                        lonTTI.setInputText("");
                    }
                    if (viewModel.getLatitude().getValue() != null && viewModel.getLongitude().getValue() != null) {
                        searchAddressByCoordinates();
                    } else {
                        addrTTI.setInputText("");
                        addrTTI.setTTIEnabled(true);
                    }

                    latTTI.setTTIEnabled(true);
                    lonTTI.setTTIEnabled(true);
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

        viewModel.subscribeOnLocationUpdates(
//                getViewLifecycleOwner()
                this
        );

        refreshLocation();


        return new AlertDialog.Builder(getActivity())
                .setView(
//                        getActivity().getLayoutInflater().inflate(R.layout.dialog_sos_geo, null)
                        binding.getRoot()
                )
                .setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO pass geolocation to activity
                        if (!latTTI.isTTIEnabled()
                                || !lonTTI.isTTIEnabled()
                                || !addrTTI.isTTIEnabled()) {
                            return;
                        }
                        if (latTTI.getInputText() == null || latTTI.getInputText().isEmpty()) {
                            viewModel.setLatitude(null);
                        } else {
                            viewModel.setLatitude(Float.parseFloat(latTTI.getInputText()));
                        }
                        if (lonTTI.getInputText() == null || lonTTI.getInputText().isEmpty()) {
                            viewModel.setLongitude(null);
                        } else {
                            viewModel.setLongitude(Float.parseFloat(lonTTI.getInputText()));
                        }
                        if (addrTTI.getInputText() == null || addrTTI.getInputText().isEmpty()) {
                            viewModel.setAddress(null);
                        } else {
                            viewModel.setAddress(addrTTI.getInputText());
                        }
                        viewModel.setNext(true);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO pass cancellation to activity
                        dialog.cancel();
                    }
                })
                .create();
    }

    @Override
    public void onStart() {


        super.onStart();
    }
}
