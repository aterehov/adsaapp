package ru.anoadsa.adsaapp.ui.menufragments.incident;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import ru.anoadsa.adsaapp.DevSettings;
import ru.anoadsa.adsaapp.Geo;
import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.databinding.FragmentIncidentBinding;
import ru.anoadsa.adsaapp.models.data.AbonentsNearbyMap;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.models.data.IncidentAbonentCategorySprav;
import ru.anoadsa.adsaapp.models.data.IncidentDisabilityCategorySprav;
import ru.anoadsa.adsaapp.models.data.IncidentMediaFile;
import ru.anoadsa.adsaapp.models.data.IncidentStatusSprav;
import ru.anoadsa.adsaapp.models.data.IncidentTypeSprav;
import ru.anoadsa.adsaapp.models.data.User;
import ru.anoadsa.adsaapp.ui.abstracts.UiMenuFragment;
import ru.anoadsa.adsaapp.ui.activities.menu.MenuSharedViewModel;
import ru.anoadsa.adsaapp.ui.activities.permission.PermissionActivity;
import ru.anoadsa.adsaapp.ui.activities.video.VideoActivity;
import ru.anoadsa.adsaapp.ui.menufragments.chat.ChatSharedViewModel;
//import ru.anoadsa.adsaapp.ui.menufragments.video.VideoSharedViewModel;
import ru.anoadsa.adsaapp.ui.views.AttachedDocumentView;
import ru.anoadsa.adsaapp.ui.views.AttachedImageView;
import ru.anoadsa.adsaapp.ui.views.DividerTitleView;
import ru.anoadsa.adsaapp.ui.views.TitleSpinnerView;
import ru.anoadsa.adsaapp.ui.views.TitleTextInputView;

public class IncidentFragment extends UiMenuFragment<IncidentViewModel, FragmentIncidentBinding> {
    private IncidentSharedViewModel sharedVM;

    private NestedScrollView incidentNSV;
    private int scrollYRestored;
    private ConstraintLayout incidentInnerCL;

    private TextView incidentDocnumNewLabel;
    private TextView incidentDocnumLabel;
    private TextView incidentDocnumValue;
    private TableLayout incidentBasicInfoTL;
    private TextView createdLabel;
    private TextView createdValue;
    private TextView editedLabel;
    private TextView editedValue;
    private TextView statusLabel;
    private TextView statusValue;
    private TextView operatorLabel;
    private TextView operatorValue;
    private Button incidentChatButton;
    private ConstraintLayout incidentSecondaryButtonsCL;
    private Button incidentEditButton;
    private Button incidentRequestOperatorButton;
    private ConstraintLayout incidentMainCL;
    private DividerTitleView incidentReceiverDTV;
    private TitleSpinnerView incidentOrganisationCategoryTSV;
    private TitleSpinnerView incidentOrganisationTSV;
    private TitleTextInputView incidentSearchRadiusTTIV;
    private DividerTitleView incidentLocationDTV;
    private ConstraintLayout incidentLocationCoordinatesCL;
    private TitleTextInputView incidentLocationLatitudeTTIV;
    private TitleTextInputView incidentLocationLongitudeTTIV;
    private TitleTextInputView incidentLocationAddressTTIV;
    private TextView incidentLocationAddressHint;
    private DividerTitleView incidentDTV;
    private TitleSpinnerView incidentCategoryTSV;
    private CheckBox incidentIsHurt;
    private CheckBox incidentIsEyewitness;
    private TitleTextInputView incidentDescriptionTTIV;
    private TextView incidentAddFilesLabel;
    private Button incidentAddPhotoButton;
    private Button incidentAddDocumentButton;
    private ConstraintLayout incidentFilesListCL;
    private ConstraintLayout profileMainConstraintLayout;
    private DividerTitleView profilePersonalDataDT;
    private TitleTextInputView profileSurnameTTI;
    private TitleTextInputView profileNameTTI;
    private TitleTextInputView profileMidnameTTI;
    private DividerTitleView profileCommunicationMethodsDT;
    private TitleTextInputView profilePhoneTTI;
    private DividerTitleView profileCarInfoDT;
    private TitleTextInputView profileCarBrandTTI;
    private TitleTextInputView profileCarNumberTTI;
    private DividerTitleView profileDocumentsDT;
    private TitleTextInputView profileOsagoTTI;
    private TitleTextInputView profileSnilsTTI;
    private TitleTextInputView profileMedPolisTTI;
    private DividerTitleView profileOtherDT;
    private TitleTextInputView profileBirthdayTTI;
    private TitleSpinnerView incidentDisabilityCategoryTSV;
    private Button incidentSendButton;

    private ConstraintLayout incidentTopCL;
    private Button incidentRadiusSearchButton;

    private Button incidentFillGeolocationButton;
    private boolean enableGeolocationErrorSnackbar;

    private Button incidentFillAddressButton;

    private Button incidentFillCoordinatesByAddressButton;

    private MenuSharedViewModel menuSharedVM;

    private ConstraintLayout incidentImagesListCL;

    private TextView incidentNominatimMentionText;

    private boolean userUpdatedFromServer = false;

    private ChatSharedViewModel chatSharedVM;

    private Button incidentVideoButton;

    private String latitudeBeforeRefresh;
    private String longitudeBeforeRefresh;
    private String addressBeforeRefresh;

    private boolean modeObserverFinished;
//    private String geoUpdateType = null;

    private boolean modeWasChanged;

    private ActivityResultLauncher<String> videoLauncher;

    private ActivityResultCallback<Boolean> videoCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean success) {
            // Do nothing
        }
    };

    private ActivityResultCallback<Boolean> getLocationPermissionCoarseCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean success) {
            checkLocationPermissionFine();
        }
    };

    private ActivityResultCallback<Boolean> getLocationPermissionFineCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean success) {
            refreshLocation();
        }
    };

    private ArrayList<IncidentMediaFile> photoList = new ArrayList<IncidentMediaFile>();
    private ArrayList<AttachedImageView> aivList = new ArrayList<AttachedImageView>();

    private ArrayList<IncidentMediaFile> documentList = new ArrayList<IncidentMediaFile>();
    private ArrayList<AttachedDocumentView> advList = new ArrayList<AttachedDocumentView>();

    private ActivityResultLauncher<String[]> pickDocumentLauncher;
    private ActivityResultCallback<Uri> pickDocumentCallback = new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri uri) {
            // TODO replace with proper action
//            System.out.println(uri);
            if (uri != null) {
                IncidentMediaFile imf = new IncidentMediaFile();
                imf.setAllInfoByUri(getContext(), uri);
                if (!imf.getType().equals("application/pdf")) {
                    Snackbar.make(incidentTopCL, "Неподдерживаемый тип файла",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }
                imf.setAddedByUser(true);

//                AttachedDocumentView adv = addAdv();
//                adv.setFileName(imf.getName());
//                adv.showDownloadButton(false);
////                 TODO
//                adv.setOnDeleteListener(onDeleteListener);
//
//                documentList.add(imf);
//                advList.add(adv);
                addDocument(imf);
            } else {
                Snackbar.make(incidentTopCL, "Файл не выбран",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    };

    private ActivityResultCallback<Uri> pickPhotoCallback = new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri uri) {
            // TODO replace with proper action
            if (uri != null) {
//                System.out.println("Selected URI: " + uri);
//                File f = new File(uri.getPath());
//                System.out.println(f);
                IncidentMediaFile imf = new IncidentMediaFile();
//                imf.setUri(uri);
//                imf.detectTypeByUri(getContext());
//                imf.createFileFromUri();
//                imf.detectNameFromFile();
                imf.setAllInfoByUri(getContext(), uri);
//                imf.readFileContent(getContext());
                if (!imf.getType().equals("image/png") && !imf.getType().equals("image/jpeg")) {
                    Snackbar.make(incidentTopCL, "Неподдерживаемый формат изображения",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }
                imf.setAddedByUser(true);
//                AttachedImageView aiv = addAiv();

//                AttachedImageView aiv = new AttachedImageView(getContext());
//                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
//                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
//                        ConstraintLayout.LayoutParams.WRAP_CONTENT
//                );
//
//                aiv.setId(View.generateViewId());
//
//                lp.topToTop = incidentImagesListCL.getId();
//                lp.bottomToBottom = incidentImagesListCL.getId();
//                if (aivList.isEmpty()) {
//                    lp.startToStart = incidentImagesListCL.getId();
//                } else {
//                    lp.startToEnd = aivList.get(aivList.size() - 1).getId();
//                }
//
//                // lp.setMargins?
//
//                aiv.setLayoutParams(lp);
//
//                incidentImagesListCL.addView(aiv);

//                aiv.setUri(uri);
//                aiv.setFileName(imf.getName());
//                aiv.showDownloadButton(false);
//                aiv.showDeleteButton(true);
//                aiv.setOnDeleteListener(onDeleteListener);

//                photoList.add(imf);
//                aivList.add(aiv);

//                imf.createFileFromUri();
//                imf.uploadToServer().subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
//                            @Override
//                            public void accept(Pair<Boolean, String> pair) throws Throwable {
//                                System.out.println(pair.first);
//                                System.out.println(pair.second);
//                            }
//                        });
                addImage(imf);
            } else {
//                System.out.println("No media selected");
                Snackbar.make(incidentTopCL, "Файл не выбран",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    };


    private View viewToDownload;

    private View.OnClickListener onDownloadListener = new View.OnClickListener() {
        @Override
        public void onClick(@NonNull View view) {
            // view arg is button, we convert it to AttachedImageView
//            AttachedImageView aiv = (AttachedImageView) view.getParent().getParent().getParent().getParent();
//            viewModel.copyFile(
//                    photoList.get(aivList.indexOf(aiv)),
//                    Static.ADSA_PICTURES_FOLDER,
//                    getContext()
//            );
            view = (View) view.getParent().getParent().getParent().getParent();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P || Static.checkPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                if (view instanceof AttachedImageView) {
//                    aivOnDownloadFunc(view);
//                } else if (view instanceof AttachedDocumentView) {
//                    advOnDownloadFunc(view);
//                }
                onDownloadFunc(view);
            } else {
                viewToDownload = view;
                downloadLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    };

    private ActivityResultLauncher<String> downloadLauncher;
    private ActivityResultCallback<Boolean> downloadCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(@NonNull Boolean success) {
            if (success) {
//                if (viewToDownload instanceof AttachedImageView) {
//                    aivOnDownloadFunc(viewToDownload);
//                } else if (viewToDownload instanceof AttachedDocumentView) {
//                    advOnDownloadFunc(viewToDownload);
//                }
                onDownloadFunc(viewToDownload);
            } else {
                Snackbar.make(
                        incidentTopCL,
                        "Нет доступа к записи данных за пределами приложения",
                        Snackbar.LENGTH_LONG
                ).show();
            }
        }
    };

//    private View.OnClickListener advOnDownloadListener = new View.OnClickListener() {
//        @Override
//        public void onClick(@NonNull View view) {
//            // view arg is button, we convert it to AttachedImageView
////            AttachedImageView aiv = (AttachedImageView) view.getParent().getParent().getParent().getParent();
////            viewModel.copyFile(
////                    photoList.get(aivList.indexOf(aiv)),
////                    Static.ADSA_PICTURES_FOLDER,
////                    getContext()
////            );
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P || Static.checkPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                advOnDownloadFunc(view);
//            } else {
//                viewToDownload = view;
//                documentDownloadLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            }
//        }
//    };

//    private ActivityResultLauncher<String> imageDownloadLauncher;
//    private ActivityResultCallback<Boolean> imageDownloadCallback = new ActivityResultCallback<Boolean>() {
//        @Override
//        public void onActivityResult(@NonNull Boolean success) {
//            if (success) {
//                aivOnDownloadFunc(viewToDownload);
//            } else {
//                Snackbar.make(
//                        incidentTopCL,
//                        "Нет доступа к записи данных за пределами приложения",
//                        Snackbar.LENGTH_LONG
//                ).show();
//            }
//        }
//    };

    private void aivOnDownloadFunc(@NonNull View view) {
        AttachedImageView aiv = (AttachedImageView) view
//                .getParent().getParent().getParent().getParent()
                ;
        viewModel.copyFileToGallery(
                photoList.get(aivList.indexOf(aiv)),
                Static.ADSA_PICTURES_FOLDER,
                getContext()
        );
    }

    private void advOnDownloadFunc(@NonNull View view) {
        AttachedDocumentView adv = (AttachedDocumentView) view
//                .getParent().getParent().getParent().getParent()
                ;
        viewModel.copyFileToDocuments(
                documentList.get(advList.indexOf(adv)),
//                Static.ADSA_DOCUMENTS_FOLDER,
                getContext()
        );
    }

    private void onDownloadFunc(View view) {
        if (view instanceof AttachedImageView) {
            viewModel.copyFileToGallery(
                    photoList.get(aivList.indexOf((AttachedImageView) view)),
                    Static.ADSA_PICTURES_FOLDER,
                    getContext()
            );
        } else if (view instanceof AttachedDocumentView) {
            viewModel.copyFileToDocuments(
                    documentList.get(advList.indexOf((AttachedDocumentView) view)),
//                    Static.ADSA_DOCUMENTS_FOLDER,
                    getContext()
            );
        }
    }

    private View.OnClickListener onDeleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view = (View) view.getParent().getParent().getParent().getParent();
            removeAttachedFile(view);
        }
    };

    private View.OnClickListener advOnDeleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view = (View) view.getParent().getParent().getParent().getParent();
            removeDocument(view);
        }
    };

    private View.OnClickListener aivOnDeleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // view arg is button, we convert it to AttachedImageView
            view = (View) view.getParent().getParent().getParent().getParent();
            removeImage(view);
//            int index = aivList.indexOf((AttachedImageView) view);
//            if (index != aivList.size() - 1) {
//                ConstraintLayout.LayoutParams lp =
//                        (ConstraintLayout.LayoutParams) aivList.get(index + 1).getLayoutParams();
//                if (index == 0) {
//                    lp.startToEnd = ConstraintLayout.LayoutParams.UNSET;
//                    lp.startToStart = incidentImagesListCL.getId();
//                } else {
//                    lp.startToEnd = aivList.get(index - 1).getId();
//                }
//                aivList.get(index + 1).setLayoutParams(lp);
//            }
//            incidentImagesListCL.removeView(view);
//            aivList.remove(index);
//            photoList.remove(index);
        }
    };

    private void removeAttachedFile(View view) {
        if (view instanceof AttachedImageView) {
//            int index = aivList.indexOf((AttachedImageView) view);
//            if (index != aivList.size() - 1) {
//                ConstraintLayout.LayoutParams lp =
//                        (ConstraintLayout.LayoutParams) aivList.get(index + 1).getLayoutParams();
//                if (index == 0) {
//                    lp.startToEnd = ConstraintLayout.LayoutParams.UNSET;
//                    lp.startToStart = incidentImagesListCL.getId();
//                } else {
//                    lp.startToEnd = aivList.get(index - 1).getId();
//                }
//                aivList.get(index + 1).setLayoutParams(lp);
//            }
//            incidentImagesListCL.removeView(view);
//            aivList.remove(index);
//            photoList.remove(index);
            removeImage(view);
        } else if (view instanceof AttachedDocumentView) {
//            int index = advList.indexOf((AttachedDocumentView) view);
//            if (index != advList.size() - 1) {
//                ConstraintLayout.LayoutParams lp =
//                        (ConstraintLayout.LayoutParams) advList.get(index + 1).getLayoutParams();
//                if (index == 0) {
//                    lp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
//                    lp.topToTop = incidentFilesListCL.getId();
//                } else {
//                    lp.topToBottom = advList.get(index - 1).getId();
//                }
//                advList.get(index + 1).setLayoutParams(lp);
//            }
//            incidentFilesListCL.removeView(view);
//            advList.remove(index);
//            documentList.remove(index);
            removeDocument(view);
        }
    }

    private void removeDocument(View view) {
        int index = advList.indexOf((AttachedDocumentView) view);
        if (index != advList.size() - 1) {
            ConstraintLayout.LayoutParams lp =
                    (ConstraintLayout.LayoutParams) advList.get(index + 1).getLayoutParams();
            if (index == 0) {
                lp.topToBottom = ConstraintLayout.LayoutParams.UNSET;
                lp.topToTop = incidentFilesListCL.getId();
            } else {
                lp.topToBottom = advList.get(index - 1).getId();
            }
            advList.get(index + 1).setLayoutParams(lp);
        }
        incidentFilesListCL.removeView(view);
        advList.remove(index);
        documentList.remove(index);
    }

    private void removeImage(View view) {
        int index = aivList.indexOf((AttachedImageView) view);
        if (index != aivList.size() - 1) {
            ConstraintLayout.LayoutParams lp =
                    (ConstraintLayout.LayoutParams) aivList.get(index + 1).getLayoutParams();
            if (index == 0) {
                lp.startToEnd = ConstraintLayout.LayoutParams.UNSET;
                lp.startToStart = incidentImagesListCL.getId();
            } else {
                lp.startToEnd = aivList.get(index - 1).getId();
            }
            aivList.get(index + 1).setLayoutParams(lp);
        }
        incidentImagesListCL.removeView(view);
        aivList.remove(index);
        photoList.remove(index);
    }

    @NonNull
    private AttachedImageView addAiv() {
        AttachedImageView aiv = new AttachedImageView(getContext());
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

        aiv.setId(View.generateViewId());

        lp.topToTop = incidentImagesListCL.getId();
//        lp.bottomToBottom = incidentImagesListCL.getId();
        if (aivList.isEmpty()) {
            lp.startToStart = incidentImagesListCL.getId();
        } else {
            lp.startToEnd = aivList.get(aivList.size() - 1).getId();
        }

        // lp.setMargins?

        aiv.setLayoutParams(lp);

        incidentImagesListCL.addView(aiv);

        return aiv;
    };

    @NonNull
    private AttachedDocumentView addAdv() {
        AttachedDocumentView adv = new AttachedDocumentView(getContext());
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

        adv.setId(View.generateViewId());

        lp.startToStart = incidentFilesListCL.getId();
        lp.endToEnd = incidentFilesListCL.getId();
//        lp.bottomToBottom = incidentImagesListCL.getId();
        if (advList.isEmpty()) {
            lp.topToTop = incidentFilesListCL.getId();
        } else {
            lp.topToBottom = advList.get(advList.size() - 1).getId();
        }

        // lp.setMargins?

        adv.setLayoutParams(lp);

        incidentFilesListCL.addView(adv);

        return adv;
    };

    private ActivityResultLauncher<String> getLocationPermissionCoarseLauncher;
    private ActivityResultLauncher<String> getLocationPermissionFineLauncher;

    private ActivityResultLauncher<PickVisualMediaRequest> pickPhotoLauncher;

    private Runnable disableIncidentOrganisationTSV = new Runnable() {
        @Override
        public void run() {
            incidentOrganisationTSV.setEnabledTSV(false);
//            return false;
        }
    };

    @Override
    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.initUi(inflater, container);

        incidentNSV = binding.incidentNSV;
        incidentInnerCL = binding.incidentInnerCL;

        incidentDocnumNewLabel = binding.incidentDocnumNewLabel;
        incidentDocnumLabel = binding.incidentDocnumLabel;
        incidentDocnumValue = binding.incidentDocnumValue;
        incidentBasicInfoTL = binding.incidentBasicInfoTL;
        createdLabel = binding.createdLabel;
        createdValue = binding.createdValue;
        editedLabel = binding.editedLabel;
        editedValue = binding.editedValue;
        statusLabel = binding.statusLabel;
        statusValue = binding.statusValue;
        operatorLabel = binding.operatorLabel;
        operatorValue = binding.operatorValue;
        incidentChatButton = binding.incidentChatButton;
        incidentSecondaryButtonsCL = binding.incidentSecondaryButtonsCL;
        incidentEditButton = binding.incidentEditButton;
        incidentRequestOperatorButton = binding.incidentRequestOperatorButton;
        incidentMainCL = binding.incidentMainCL;
        incidentReceiverDTV = binding.incidentReceiverDTV;
        incidentOrganisationCategoryTSV = binding.incidentOrganisationCategoryTSV;
        incidentOrganisationTSV = binding.incidentOrganisationTSV;
        incidentSearchRadiusTTIV = binding.incidentSearchRadiusTTIV;
        incidentLocationDTV = binding.incidentLocationDTV;
        incidentLocationCoordinatesCL = binding.incidentLocationCoordinatesCL;
        incidentLocationLatitudeTTIV = binding.incidentLocationLatitudeTTIV;
        incidentLocationLongitudeTTIV = binding.incidentLocationLongitudeTTIV;
        incidentLocationAddressTTIV = binding.incidentLocationAddressTTIV;
        incidentLocationAddressHint = binding.incidentLocationAddressHint;
        incidentDTV = binding.incidentDTV;
        incidentCategoryTSV = binding.incidentCategoryTSV;
        incidentIsHurt = binding.incidentIsHurt;
        incidentIsEyewitness = binding.incidentIsEyewitness;
        incidentDescriptionTTIV = binding.incidentDescriptionTTIV;
        incidentAddFilesLabel = binding.incidentAddFilesLabel;
        incidentAddPhotoButton = binding.incidentAddPhotoButton;
        incidentAddDocumentButton = binding.incidentAddDocumentButton;
        incidentFilesListCL = binding.incidentFilesListCL;
        profileMainConstraintLayout = binding.profileMainConstraintLayout;
        profilePersonalDataDT = binding.profilePersonalDataDT;
        profileSurnameTTI = binding.profileSurnameTTI;
        profileNameTTI = binding.profileNameTTI;
        profileMidnameTTI = binding.profileMidnameTTI;
        profileCommunicationMethodsDT = binding.profileCommunicationMethodsDT;
        profilePhoneTTI = binding.profilePhoneTTI;
        profileCarInfoDT = binding.profileCarInfoDT;
        // TODO!!! Check this variable name mismatch!
        profileCarBrandTTI = binding.profileArBrandTTI;
        profileCarNumberTTI = binding.profileCarNumberTTI;
        profileDocumentsDT = binding.profileDocumentsDT;
        profileOsagoTTI = binding.profileOsagoTTI;
        profileSnilsTTI = binding.profileSnilsTTI;
        profileMedPolisTTI = binding.profileMedPolisTTI;
        profileOtherDT = binding.profileOtherDT;
        profileBirthdayTTI = binding.profileBirthdayTTI;
        incidentDisabilityCategoryTSV = binding.incidentDisabilityCategoryTSV;
        incidentSendButton = binding.incidentSendButton;

        incidentTopCL = binding.incidentTopCL;
        incidentRadiusSearchButton = binding.incidentRadiusSearchButton;

        incidentFillGeolocationButton = binding.incidentFillGeolocationButton;

//        incidentFillAddressButton = binding.incidentFillAddressButton;

//        incidentFillCoordinatesByAddressButton = binding.incidentFillCoordinatesByAddressButton;

        incidentImagesListCL = binding.incidentImagesListCL;

//        incidentNominatimMentionText = binding.incidentNominatimMentionText;

        incidentVideoButton = binding.incidentVideoButton;
    }

    @Override
    protected void configureUiState() {
        profileCarNumberTTI.setInputFilter(Static.carNumberInputFilter);
        profileOsagoTTI.setInputFilter(Static.osagoInputFilter);
        profileSnilsTTI.setInputFilter(Static.snilsInputFilter);
        profileMedPolisTTI.setInputFilter(Static.medPolisInputFilter);
        profileBirthdayTTI.setInputFilter(Static.birthdayInputFilter);
    }

    @Override
    protected void configureUiActions() {

        requireActivity().getOnBackPressedDispatcher().addCallback(editBack);

        incidentSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (incidentLocationLatitudeTTIV.getInputText().isEmpty()
                    || !incidentLocationLatitudeTTIV.isTTIEnabled()) {
                    Snackbar.make(incidentTopCL, "Введите широту",
                            Snackbar.LENGTH_LONG).show();
                    return;
                } else if (!Static.checkLatitude(Float.parseFloat(incidentLocationLatitudeTTIV.getInputText()))) {
                    Snackbar.make(incidentTopCL, "Введите корректное значение широты",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (incidentLocationLongitudeTTIV.getInputText().isEmpty()
                    || !incidentLocationLongitudeTTIV.isTTIEnabled()) {
                    Snackbar.make(incidentTopCL, "Введите долготу",
                            Snackbar.LENGTH_LONG).show();
                    return;
                } else if (!Static.checkLongitude(Float.parseFloat(incidentLocationLongitudeTTIV.getInputText()))) {
                    Snackbar.make(incidentTopCL, "Введите корректное значение долготы",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (incidentLocationAddressTTIV.getInputText() == null
                    || incidentLocationAddressTTIV.getInputText().isEmpty()
                    || !incidentLocationAddressTTIV.isTTIEnabled()) {
                    Snackbar.make(incidentTopCL, "Введите адрес",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (incidentSearchRadiusTTIV.getInputText().isEmpty()) {
                    Snackbar.make(incidentTopCL, "Введите радиус поиска",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }



                if (!profileCarNumberTTI.getInputText().isEmpty()
                        && !Static.checkCarNumber(profileCarNumberTTI.getInputText())) {
                    Snackbar.make(incidentTopCL, "Формат номера транспортного средства: а123бв45",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (!profileBirthdayTTI.getInputText().isEmpty()
                        && !Static.checkDateString(profileBirthdayTTI.getInputText())) {
                    Snackbar.make(incidentTopCL, "Формат даты рождения: ДД.ММ.ГГГГ",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (!profileOsagoTTI.getInputText().isEmpty()
                        && !Static.checkOsago(profileOsagoTTI.getInputText())) {
                    Snackbar.make(incidentTopCL, "Формат ОСАГО: АБВ 1234567890",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (!profileSnilsTTI.getInputText().isEmpty()
                        && !Static.checkSnils(profileSnilsTTI.getInputText())) {
                    Snackbar.make(incidentTopCL, "Формат СНИЛС: 123-456-789 01",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (!profileMedPolisTTI.getInputText().isEmpty()
                        && !Static.checkMedPolis(profileMedPolisTTI.getInputText())) {
                    Snackbar.make(incidentTopCL,
                            "Формат медицинского полиса: 1234567890123456",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (incidentOrganisationTSV.getSelection() == null
                        || incidentOrganisationTSV.getSelection().isEmpty()) {
                    Snackbar.make(incidentTopCL,
                            "Выберите получателя обращения",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (incidentOrganisationCategoryTSV.getSelection() == null
                        || incidentOrganisationCategoryTSV.getSelection().isEmpty()) {
                    Snackbar.make(incidentTopCL,
                            "Выберите категорию получателя обращения",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                for (IncidentMediaFile i: photoList) {
                    if (!i.getType().equals("image/png") && !i.getType().equals("image/jpeg")) {
                        Snackbar.make(incidentTopCL,
                                "Неподдерживаемый формат фото",
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }

                for (IncidentMediaFile i: documentList) {
                    if (!i.getType().equals("application/pdf")) {
                        Snackbar.make(incidentTopCL,
                                "Неподдерживаемый формат документа",
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }

                incidentSendButton.setEnabled(false);
                // TODO validate data
                // merging photo and document lists
                ArrayList<IncidentMediaFile> filesList = new ArrayList<IncidentMediaFile>(photoList);
                filesList.addAll(documentList);
                viewModel.saveIncident(
                        null,
                        null,
                        incidentCategoryTSV.getSelection(),
                        incidentIsHurt.isChecked(),
                        incidentOrganisationTSV.getSelection(),
                        incidentOrganisationCategoryTSV.getSelection(),
                        Float.parseFloat(incidentSearchRadiusTTIV.getInputText()),
                        Float.parseFloat(incidentLocationLatitudeTTIV.getInputText()),
                        Float.parseFloat(incidentLocationLongitudeTTIV.getInputText()),
                        incidentLocationAddressTTIV.getInputText(),
                        incidentIsEyewitness.isChecked(),
                        profileSurnameTTI.getInputText(),
                        profileNameTTI.getInputText(),
                        profileMidnameTTI.getInputText(),
                        profileCarBrandTTI.getInputText(),
                        profileCarNumberTTI.getInputText(),
                        profileOsagoTTI.getInputText(),
                        profileMedPolisTTI.getInputText(),
                        profileSnilsTTI.getInputText(),

                        Static.makeLocalDate(profileBirthdayTTI.getInputText()),
                        incidentDisabilityCategoryTSV.getSelection(),
                        incidentDescriptionTTIV.getInputText(),
                        // TODO add file support
//                        (ArrayList<IncidentMediaFile>) photoList.clone(),
//                        new ArrayList<IncidentMediaFile>(photoList),
                        filesList,
                        getContext()
                );
            }
        });

        incidentRadiusSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (incidentLocationLatitudeTTIV.getInputText() == null
                        || incidentLocationLongitudeTTIV.getInputText() == null
                        || incidentLocationLatitudeTTIV.getInputText().isEmpty()
                        || incidentLocationLongitudeTTIV.getInputText().isEmpty()) {
                    Snackbar.make(incidentTopCL, "Укажите широту и долготу",
                            Snackbar.LENGTH_LONG).show();
                } else if (incidentSearchRadiusTTIV.getInputText() == null ||
                        incidentSearchRadiusTTIV.getInputText().isEmpty()) {
                    Snackbar.make(incidentTopCL, "Укажите радиус поиска",
                            Snackbar.LENGTH_LONG).show();
                } else {
                    viewModel.searchAbonents(
                            incidentOrganisationCategoryTSV.getSelection(),
                            Float.parseFloat(incidentSearchRadiusTTIV.getInputText()),
                            Float.parseFloat(incidentLocationLatitudeTTIV.getInputText()),
                            Float.parseFloat(incidentLocationLongitudeTTIV.getInputText())
                    );
                }
            }
        });

        incidentFillGeolocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshLocationWithPermissionsCheck();
            }
        });

//        incidentLocationLatitudeTTIV.

//        incidentFillAddressButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                float lat;
//                float lon;
//
//                if (incidentLocationLatitudeTTIV.getInputText() == null
//                    || incidentLocationLatitudeTTIV.getInputText().isEmpty()) {
//                    Snackbar.make(incidentTopCL, "Введите широту",
//                            Snackbar.LENGTH_LONG).show();
//                } else if (incidentLocationLongitudeTTIV.getInputText() == null
//                    || incidentLocationLongitudeTTIV.getInputText().isEmpty()) {
//                    Snackbar.make(incidentTopCL, "Введите долготу",
//                            Snackbar.LENGTH_LONG).show();
//                } else {
//                    try {
//                        lat = Float.parseFloat(incidentLocationLatitudeTTIV.getInputText());
//                    } catch (NumberFormatException e) {
//                        Snackbar.make(incidentTopCL, "Проверьте формат широты",
//                                Snackbar.LENGTH_LONG).show();
//                        return;
//                    }
//
//                    try {
//                        lon = Float.parseFloat(incidentLocationLongitudeTTIV.getInputText());
//                    } catch (NumberFormatException e) {
//                        Snackbar.make(incidentTopCL, "Проверьте формат долготы",
//                                Snackbar.LENGTH_LONG).show();
//                        return;
//                    }
//
////                    geoUpdateType = "addressByCoordinates";
//                    viewModel.getAddressByCoordinates(lat, lon);
//                }
//            }
//        });

//        incidentFillCoordinatesByAddressButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (incidentLocationAddressTTIV.getInputText() == null
//                    || incidentLocationAddressTTIV.getInputText().isEmpty()) {
//                    Snackbar.make(incidentTopCL, "Введите адрес",
//                            Snackbar.LENGTH_LONG).show();
//                } else {
////                    geoUpdateType = "coordinatesByAddress";
//                    viewModel.getCoordinatesByAddress(incidentLocationAddressTTIV.getInputText());
//                }
//            }
//        });

        incidentEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeWasChanged = true;
                viewModel.setMode("edit");
            }
        });

        incidentOrganisationCategoryTSV.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean ran;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (wasRestored && !ran) {
                    ran = true;
                    return;
                }
                if (incidentOrganisationCategoryTSV.isEnabledTSV()) {
                    incidentOrganisationTSV.setEnabledTSV(false);
                    // Refresh organisations list
                    searchOrganisations();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        incidentSearchRadiusTTIV.setOnFocusChangeListenerTTI(
                TitleTextInputView.doOnTextChangedOnlyIfHasFocus
//                new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                //                    incidentOrganisationTSV.setEnabledTSV(false);
//                //                    searchOrganisations();
//                if (incidentSearchRadiusTTIV.isTTIEnabled()) {
//                    incidentSearchRadiusTTIV.setDoOnTextChangedEnabled(hasFocus);
//                }
//            }
//        }
        );

        incidentSearchRadiusTTIV.doOnTextChanged(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        incidentOrganisationTSV.setEnabledTSV(false);
//                    }
//                }
                disableIncidentOrganisationTSV
                ,
                2500,
                new Runnable() {
                    @Override
                    public void run() {
                        searchOrganisations();
//                        return false;
                    }
                }
//                new TimerTask() {
//            @Override
//            public void run() {
//                searchOrganisations();
//            }
//        }
        );
        incidentSearchRadiusTTIV.setDoOnTextChangedEnabled(false);

        incidentLocationLatitudeTTIV.setOnFocusChangeListenerTTI(
                TitleTextInputView.doOnTextChangedOnlyIfHasFocus
//                new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if (incidentLocationLatitudeTTIV.isTTIEnabled()) {
////                    if (hasFocus) {
////                    geoUpdateType = "addressByCoordinates";
//                    incidentLocationLatitudeTTIV.setDoOnTextChangedEnabled(hasFocus);
////                    }
//                }
//            }
//        }
        );

        incidentLocationLatitudeTTIV.doOnTextChanged(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        incidentOrganisationTSV.setEnabledTSV(false);
//                    }
//                }
                disableIncidentOrganisationTSV
                ,
                2500,
                new Runnable() {
                    @Override
                    public void run() {
                        searchOrganisations();
//                        if (!geoUpdateType.equals("coordinatesByAddress")) {
//                            geoUpdateType = "addressByCoordinates";
//                        searchOrganisations();
                        searchAddressByCoordinates();
//                        }
//                        return false;
                    }
                }
        );
        incidentLocationLatitudeTTIV.setDoOnTextChangedEnabled(false);

        incidentLocationLongitudeTTIV.setOnFocusChangeListenerTTI(
                TitleTextInputView.doOnTextChangedOnlyIfHasFocus
//                new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if (incidentLocationLongitudeTTIV.isTTIEnabled()) {
////                    if (hasFocus) {
////                    geoUpdateType = "addressByCoordinates";
//                    incidentLocationLongitudeTTIV.setDoOnTextChangedEnabled(hasFocus);
////                    }
//                }
//            }
//        }
        );

        incidentLocationLongitudeTTIV.doOnTextChanged(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        incidentOrganisationTSV.setEnabledTSV(false);
//                    }
//                }
                disableIncidentOrganisationTSV
                ,
                2500,
                new Runnable() {
                    @Override
                    public void run() {
                        // Latitude will update organisations when geolocation is refreshed
//                        if (!geoUpdateType.equals("coordinatesByAddress")) {
//                            geoUpdateType = "addressByCoordinates";
                        searchOrganisations();
                        searchAddressByCoordinates();
//                        }
//                        return false;
                    }
                }
        );
        incidentLocationLongitudeTTIV.setDoOnTextChangedEnabled(false);

        incidentLocationAddressTTIV.setOnFocusChangeListenerTTI(
                TitleTextInputView.doOnTextChangedOnlyIfHasFocus
//                new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if (incidentLocationAddressTTIV.isTTIEnabled()) {
////                    if (hasFocus) {
////                    geoUpdateType = "coordinatesByAddress";
//                    incidentLocationAddressTTIV.setDoOnTextChangedEnabled(hasFocus);
////                    }
//                }
//            }
//        }
        );

        incidentLocationAddressTTIV.doOnTextChanged(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        incidentOrganisationTSV.setEnabledTSV(false);
//                    }
//                }
                disableIncidentOrganisationTSV
                ,
                2500,
                new Runnable() {
                    @Override
                    public void run() {
//                        geoUpdateType = "coordinatesByAddress";
                        searchCoordinatesByAddress();
                        // Lat and long will be updated by viewmodel and will auto call organisation
                        // search
//                        return false;
                    }
                }
        );
        incidentLocationAddressTTIV.setDoOnTextChangedEnabled(false);

        incidentAddPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(requireContext())) {
                    pickPhotoLauncher.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                } else {
                    Snackbar.make(incidentTopCL, "Выбор фото недоступен",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });

        incidentAddDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int PICK_PDF_FILE = 2;
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("application/pdf");

//                startActivityForResult(intent, PICK_PDF_FILE);
//                ActivityResultContracts.OpenDocument
                pickDocumentLauncher.launch(new String[]{"application/pdf"});
            }
        });

//        incidentNominatimMentionText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("https://www.openstreetmap.org/copyright")
//                ));
//            }
//        });

        incidentChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatSharedVM = new ViewModelProvider(requireActivity()).get(ChatSharedViewModel.class);
                chatSharedVM.setIncident(viewModel.getIncident().getValue());
                Navigation
                        .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_incident_to_chat);
            }
        });

        incidentRequestOperatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incidentRequestOperatorButton.setEnabled(false);
                viewModel.requestOperator();
            }
        });

        incidentVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                VideoSharedViewModel videoSharedVM = new ViewModelProvider(getActivity()).get(VideoSharedViewModel.class);
//                videoSharedVM.setHasIncident(true);
//                videoSharedVM.setIncident(viewModel.getIncident().getValue());
//                Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main)
//                        .navigate(R.id.action_incident_to_video);
                videoLauncher.launch(viewModel.getIncident().getValue().getId());
            }
        });

        incidentNSV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean ran;

            @Override
            public void onGlobalLayout() {
                if (wasRestored && !ran) {
                    ran = true;
                    incidentNSV.scrollTo(
                            0,
                            Integer.max(
                                    0,
                                    Integer.min(
                                            scrollYRestored,
                                            incidentInnerCL.getHeight() - incidentNSV.getHeight()
                                    )
                            )
                    );
                }
            }
        });
    }

    private void searchCoordinatesByAddress() {
        if (incidentLocationAddressTTIV.getInputText() != null
            && !incidentLocationAddressTTIV.getInputText().isEmpty()) {
            viewModel.getCoordinatesByAddress(incidentLocationAddressTTIV.getInputText());
        }
    }

    private void refreshLocationWithPermissionsCheck() {
        checkLocationPermissionCoarse();
    }

    private void checkLocationPermissionCoarse() {
        if (!Static.checkPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            getLocationPermissionCoarseLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            checkLocationPermissionFine();
        }
    }

    private void checkLocationPermissionFine() {
        if (!Static.checkPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            getLocationPermissionFineLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            refreshLocation();
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
        latitudeBeforeRefresh = incidentLocationLatitudeTTIV.getInputText();
        longitudeBeforeRefresh = incidentLocationLongitudeTTIV.getInputText();
        addressBeforeRefresh = incidentLocationAddressTTIV.getInputText();

        incidentLocationLatitudeTTIV.setTTIEnabled(false);
        incidentLocationLongitudeTTIV.setTTIEnabled(false);
        incidentLocationAddressTTIV.setTTIEnabled(false);
        incidentLocationLatitudeTTIV.setInputText("Подождите...");
        incidentLocationLongitudeTTIV.setInputText("Подождите...");
        incidentLocationAddressTTIV.setInputText("Подождите...");
        viewModel.prepareForLocationUpdate();
        Geo.refreshLocation(getContext());
    }

    protected void configureSharedViewModelActions() {
        sharedVM.getMode().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String mode) {
                if (!wasRestored) {
                    viewModel.setMode(mode);
                }
            }
        });

        sharedVM.getIncident().observe(getViewLifecycleOwner(), new Observer<Incident>() {
            @Override
            public void onChanged(Incident incident) {
                if (!wasRestored) {
                    viewModel.setIncident(incident);
                }
            }
        });
    }

    private OnBackPressedCallback editBack = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            modeWasChanged = true;
            viewModel.setMode("view");
            viewModel.resetIncident();
        }
    };

//    public boolean onOptionsItemSelected(MenuItem item) {
//
//    }

    private void addImage(@NonNull IncidentMediaFile imf) {
        AttachedImageView view = addAiv();

        if (imf.getUri() != null) {
            view.setUri(imf.getUri());
        }

        view.setFileName(imf.getName());
//            if (!imf.isAddedByUser()) {
        view.showDownloadButton(!imf.isAddedByUser());
//            }
        if (viewModel.getMode().getValue().equals("view")) {
            view.showDeleteButton(false);
        }

        view.setOnDeleteListener(onDeleteListener);
        view.setOnDownloadListener(onDownloadListener);

        photoList.add(imf);
        aivList.add(view);

        if (imf.getUri() == null) {
            viewModel.getFileAndSetAttachedImageViewUri(imf, view, getContext());
        }
    }

    private void addDocument(@NonNull IncidentMediaFile imf) {
        AttachedDocumentView view = addAdv();

        view.setFileName(imf.getName());
        view.showDownloadButton(!imf.isAddedByUser());
        if (viewModel.getMode().getValue().equals("view")) {
            view.showDeleteButton(false);
        }

        view.setOnDeleteListener(onDeleteListener);
        // TODO download listener
        view.setOnDownloadListener(onDownloadListener);

        documentList.add(imf);
        advList.add(view);
    }

    private void addAttachedFile(@NonNull IncidentMediaFile imf) {
//        View view;
        if (imf.getType().equals("image/jpeg") || imf.getType().equals("image/png")) {
//            AttachedImageView view = addAiv();
//
//            view.setFileName(imf.getName());
////            if (!imf.isAddedByUser()) {
//            view.showDownloadButton(!imf.isAddedByUser());
////            }
//            if (viewModel.getMode().getValue().equals("view")) {
//                view.showDeleteButton(false);
//            }
//
//            view.setOnDeleteListener(onDeleteListener);
//            view.setOnDownloadListener(onDownloadListener);
//
//            photoList.add(imf);
//            aivList.add(view);
//
//            viewModel.getFileAndSetAttachedImageViewUri(imf, view, getContext());
            addImage(imf);
        } else if (imf.getType().equals("application/pdf")) {
//            AttachedDocumentView view = addAdv();
//
//            view.setFileName(imf.getName());
//            view.showDownloadButton(!imf.isAddedByUser());
//            if (viewModel.getMode().getValue().equals("view")) {
//                view.showDeleteButton(false);
//            }
//
//            view.setOnDeleteListener(onDeleteListener);
//            // TODO download listener
//            view.setOnDownloadListener(onDownloadListener);
//
//            documentList.add(imf);
//            advList.add(view);
            addDocument(imf);
        } else {
            Snackbar.make(incidentTopCL, "Неподдерживаемый тип файла",
                    Snackbar.LENGTH_LONG).show();
//            continue;
        }
    }

    @Override
    protected void configureViewModelActions() {
        viewModel.getMode().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String mode) {
                if (mode == null) {
                    return;
//                    getActivity().getActionBar().set
//                    Navigation
//                            .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
//                            .
                }
//                if ((mode.equals("create")
////                        || mode.equals("edit")
//                        || mode.equals("view"))
//                        && !viewModel.isNeededInfoLoaded()
//                ) {
//                    viewModel.loadNeededInfo();
//                    return;
//                }
                if (mode.equals("create")) {
                    incidentDocnumValue.setVisibility(View.GONE);
                    incidentBasicInfoTL.setVisibility(View.GONE);
                    incidentChatButton.setVisibility(View.GONE);
                    incidentSecondaryButtonsCL.setVisibility(View.GONE);
                    incidentVideoButton.setVisibility(View.GONE);

                    if (!wasRestored) {
                        refreshLocationWithPermissionsCheck();
                    }

                    editBack.setEnabled(false);
//                    ((MenuActivity) requireActivity()).setBackOnNavigateUp(false);
                    menuSharedVM.setEnableBackOnNavigateUp(false);
                } else if (mode.equals("finish")) {
                    // TODO finish
                    Navigation
                            .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                            .popBackStack();
//                    getParentFragmentManager().popBackStack();
                } else if (mode.equals("view")) {

                    incidentDocnumNewLabel.setVisibility(View.GONE);
                    incidentDocnumLabel.setText(
                            Character.toUpperCase(incidentDocnumLabel.getText().charAt(0))
                                    + incidentDocnumLabel.getText().toString().substring(1)
                    );
                    incidentRadiusSearchButton.setVisibility(View.GONE);
                    incidentFillGeolocationButton.setVisibility(View.GONE);
//                    incidentFillCoordinatesByAddressButton.setVisibility(View.GONE);
                    incidentLocationAddressHint.setVisibility(View.GONE);
//                    incidentFillAddressButton.setVisibility(View.GONE);
                    incidentAddPhotoButton.setVisibility(View.GONE);
                    incidentAddDocumentButton.setVisibility(View.GONE);
                    incidentSendButton.setVisibility(View.GONE);

                    incidentChatButton.setVisibility(View.VISIBLE);
                    incidentSecondaryButtonsCL.setVisibility(View.VISIBLE);
                    incidentVideoButton.setVisibility(View.VISIBLE);

                    incidentOrganisationCategoryTSV.setEnabledTSV(false);
                    incidentOrganisationTSV.setEnabledTSV(false);
                    incidentSearchRadiusTTIV.setTTIEnabled(false);
                    incidentLocationLatitudeTTIV.setTTIEnabled(false);
                    incidentLocationLongitudeTTIV.setTTIEnabled(false);
                    incidentLocationAddressTTIV.setTTIEnabled(false);
                    incidentCategoryTSV.setEnabledTSV(false);
                    incidentIsHurt.setEnabled(false);
                    incidentIsEyewitness.setEnabled(false);
                    incidentDescriptionTTIV.setTTIEnabled(false);

                    profileSurnameTTI.setTTIEnabled(false);
                    profileNameTTI.setTTIEnabled(false);
                    profileMidnameTTI.setTTIEnabled(false);
                    profileCarBrandTTI.setTTIEnabled(false);
                    profileCarNumberTTI.setTTIEnabled(false);
                    profileOsagoTTI.setTTIEnabled(false);
                    profileSnilsTTI.setTTIEnabled(false);
                    profileMedPolisTTI.setTTIEnabled(false);
                    profileBirthdayTTI.setTTIEnabled(false);

                    incidentDisabilityCategoryTSV.setEnabledTSV(false);

                    incidentSearchRadiusTTIV.setDoOnTextChangedEnabled(false);
                    incidentLocationLatitudeTTIV.setDoOnTextChangedEnabled(false);
                    incidentLocationLongitudeTTIV.setDoOnTextChangedEnabled(false);
                    incidentLocationAddressTTIV.setDoOnTextChangedEnabled(false);

                    for (AttachedImageView aiv: aivList) {
                        aiv.showDownloadButton(true);
                        aiv.showDeleteButton(false);
                    }

                    editBack.setEnabled(false);
//                    ((MenuActivity) requireActivity()).setBackOnNavigateUp(false);
                    menuSharedVM.setEnableBackOnNavigateUp(false);
                } else if (mode.equals("edit")) {
                    incidentDocnumNewLabel.setVisibility(View.GONE);
                    incidentDocnumLabel.setText(
                            Character.toUpperCase(incidentDocnumLabel.getText().charAt(0))
                                    + incidentDocnumLabel.getText().toString().substring(1)
                    );

                    incidentChatButton.setVisibility(View.GONE);
                    incidentSecondaryButtonsCL.setVisibility(View.GONE);
                    incidentVideoButton.setVisibility(View.GONE);

                    incidentRadiusSearchButton.setVisibility(View.VISIBLE);
                    incidentFillGeolocationButton.setVisibility(View.VISIBLE);
//                    incidentFillAddressButton.setVisibility(View.VISIBLE);
//                    incidentFillCoordinatesByAddressButton.setVisibility(View.VISIBLE);
                    incidentSendButton.setVisibility(View.VISIBLE);

                    incidentAddPhotoButton.setVisibility(View.VISIBLE);
                    incidentAddDocumentButton.setVisibility(View.VISIBLE);

                    incidentLocationAddressHint.setVisibility(View.VISIBLE);

                    incidentOrganisationCategoryTSV.setEnabledTSV(true);
//                    incidentOrganisationTSV.setEnabledTSV(true);
                    incidentSearchRadiusTTIV.setTTIEnabled(true);
                    incidentLocationLatitudeTTIV.setTTIEnabled(true);
                    incidentLocationLongitudeTTIV.setTTIEnabled(true);
                    incidentLocationAddressTTIV.setTTIEnabled(true);
                    incidentCategoryTSV.setEnabledTSV(true);
                    incidentIsHurt.setEnabled(true);
                    incidentIsEyewitness.setEnabled(true);
                    incidentDescriptionTTIV.setTTIEnabled(true);

                    profileSurnameTTI.setTTIEnabled(true);
                    profileNameTTI.setTTIEnabled(true);
                    profileMidnameTTI.setTTIEnabled(true);
                    profileCarBrandTTI.setTTIEnabled(true);
                    profileCarNumberTTI.setTTIEnabled(true);
                    profileOsagoTTI.setTTIEnabled(true);
                    profileSnilsTTI.setTTIEnabled(true);
                    profileMedPolisTTI.setTTIEnabled(true);
                    profileBirthdayTTI.setTTIEnabled(true);

                    incidentDisabilityCategoryTSV.setEnabledTSV(true);

                    if (DevSettings.FILE_REMOVAL_ENABLED) {
                        for (AttachedImageView aiv: aivList) {
                            aiv.showDeleteButton(true);
                        }
                    }

                    incidentSendButton.setEnabled(true);

                    if (!wasRestored || modeWasChanged) {
                        searchOrganisations();
                    }

                    editBack.setEnabled(true);
//                    ((MenuActivity) requireActivity()).setBackOnNavigateUp(true);
                    menuSharedVM.setEnableBackOnNavigateUp(true);
                }

//                if (mode.equals("create")
////                        || mode.equals("edit")
//                        || mode.equals("view")
//                ) {
//                    viewModel.loadNeededInfo();
//                }
                if ((mode.equals("create")
//                        || mode.equals("edit")
                        || mode.equals("view"))
                        && !viewModel.isNeededInfoLoaded()
                ) {
                    viewModel.loadNeededInfo();
                    return;
                }
                modeObserverFinished = true;
                viewModel.rerunIncidentObserver();
            }
        });

        viewModel.getIncident().observe(getViewLifecycleOwner(), new Observer<Incident>() {
            @Override
            public void onChanged(Incident incident) {
                if (!modeObserverFinished) {
                    return;
                }
                if (incident != null && viewModel.getMode().getValue() != null) {
                    if (!viewModel.getMode().getValue().equals("create")) {
//                        if (!incident.isFullInfoLoaded()) {
//                            viewModel.loadFullIncidentInfo();
//                            return;
//                        }
//                        if (!viewModel.isNeededInfoLoaded()) {
//                            viewModel.loadNeededInfo();
//                            return;
//                        }
//                        if (viewModel.getMode().getValue().equals("create")) {
//                            refreshLocationWithPermissionsCheck();
//                        }
                        // TODO fill fields
                        incidentDocnumValue.setText("№ " + incident.getDocnum());
                        createdValue.setText(Static.formatLocalDateTime(incident.getCreatedDateTime()));
                        editedValue.setText(Static.formatLocalDateTime(incident.getModifiedDateTime()));
                        statusValue.setText(incident.getStatusText());
                        if (incident.getOperatorText() == null || incident.getOperatorText().isEmpty()) {
                            operatorLabel.setVisibility(View.GONE);
                            operatorValue.setVisibility(View.GONE);
                        } else {
                            operatorLabel.setVisibility(View.VISIBLE);
                            operatorValue.setVisibility(View.VISIBLE);
                            operatorValue.setText(incident.getOperatorText());
                        }

                        if (incident.getStatusText().equals("Завершена")
                                || incident.getStatusText().equals("Отменена оператором")
                                || incident.getStatusText().equals("Отменена обратившимся")
                                || incident.getStatusText().equals("Удалена")) {
                            incidentEditButton.setEnabled(false);
                            incidentVideoButton.setEnabled(false);
                        }

                        if (wasRestored) {
                            return;
                        }

                        if (incidentOrganisationCategoryTSV.getListItemsCount() == 0) {
                            incidentOrganisationCategoryTSV.setItems(Collections.singletonList(incident.getCategoryText()));
                        }
                        incidentOrganisationCategoryTSV.setSelectedItem(incident.getCategoryText());
                        if (incidentOrganisationTSV.getListItemsCount() == 0) {
                            incidentOrganisationTSV.setItems(Collections.singletonList(incident.getAbonentText()));
                        }
                        incidentOrganisationTSV.setSelectedItem(incident.getAbonentText());
                        incidentSearchRadiusTTIV.setInputText(String.valueOf(incident.getRadius()));
                        incidentLocationLatitudeTTIV.setInputText(String.valueOf(incident.getLatitude()));
                        incidentLocationLongitudeTTIV.setInputText(String.valueOf(incident.getLongitude()));
                        incidentLocationAddressTTIV.setInputText(incident.getAddress());
                        if (incidentCategoryTSV.getListItemsCount() == 0) {
                            incidentCategoryTSV.setItems(Collections.singletonList(incident.getIncidentTypeText()));
                        }
                        incidentCategoryTSV.setSelectedItem(incident.getIncidentTypeText());
                        incidentIsHurt.setChecked(incident.isHurt());
                        incidentIsEyewitness.setChecked(incident.isEyewitness());
                        incidentDescriptionTTIV.setInputText(incident.getDescription());

                        profileSurnameTTI.setInputText(incident.getClientSurname());
                        profileNameTTI.setInputText(incident.getClientName());
                        profileMidnameTTI.setInputText(incident.getClientPatronymic());
                        profilePhoneTTI.setInputText(incident.getPhone());
                        profileCarBrandTTI.setInputText(incident.getCarBrand());
                        profileCarNumberTTI.setInputText(incident.getCarNumber());
                        profileOsagoTTI.setInputText(incident.getOsago());
                        profileSnilsTTI.setInputText(incident.getSnils());
                        profileMedPolisTTI.setInputText(incident.getMedPolis());
                        if (incident.getBirthday() == null) {
                            profileBirthdayTTI.setInputText("");
                        } else {
                            profileBirthdayTTI.setInputText(incident.getBirthday().format(DateTimeFormatter.ofPattern("dd.MM.uuuu")));
                        }
                        // TODO choose one way of formatting date and time and use it everywhere

                        if (incidentDisabilityCategoryTSV.getListItemsCount() == 0) {
                            incidentDisabilityCategoryTSV.setItems(Collections.singletonList(incident.getDisabilityCategoryText()));
                        }
                        incidentDisabilityCategoryTSV.setSelectedItem(incident.getDisabilityCategoryText());

                        int size = aivList.size();
                        for (int i = 0; i < size; ++i) {
                            // The list item is removed in removeImage, so always remove the first item
                            removeImage(aivList.get(0));
                        }
//                        int
                        size = advList.size();
                        for (int i = 0; i < size; ++i) {
                            removeDocument(advList.get(0));
                        }
                        for (IncidentMediaFile imf: incident.getOrderMediaFile()) {
//                            View view;
//                            if (imf.getType().equals("image/jpeg") || imf.getType().equals("image/png")) {
//                                AttachedImageView view = addAiv();
//
//                                view.setFileName(imf.getName());
//                                view.showDownloadButton(true);
//                                if (viewModel.getMode().getValue().equals("view")) {
//                                    view.showDeleteButton(false);
//                                }
//
//                                view.setOnDeleteListener(onDeleteListener);
//                                view.setOnDownloadListener(onDownloadListener);
//
//                                photoList.add(imf);
//                                aivList.add(view);
//
//                                viewModel.getFileAndSetAttachedImageViewUri(imf, view, getContext());
//                            } else if (imf.getType().equals("application/pdf")) {
//                                AttachedDocumentView view = addAdv();
//
//                                view.setFileName(imf.getName());
//                                view.showDownloadButton(true);
//                                if (viewModel.getMode().getValue().equals("view")) {
//                                    view.showDeleteButton(false);
//                                }
//
//                                view.setOnDeleteListener(onDeleteListener);
//                                // TODO download listener
//                                view.setOnDownloadListener(onDownloadListener);
//
//                                documentList.add(imf);
//                                advList.add(view);
//                            } else {
//                                Snackbar.make(incidentTopCL, "Неподдерживаемый тип файла",
//                                        Snackbar.LENGTH_LONG).show();
//                                continue;
//                            }
                            addAttachedFile(imf);
//                            AttachedImageView view = new AttachedImageView(getContext());
//                            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
//                                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
//                                    ConstraintLayout.LayoutParams.WRAP_CONTENT
//                            );
//
//                            view.setId(View.generateViewId());
//
//                            lp.topToTop = incidentImagesListCL.getId();
//                            lp.bottomToBottom = incidentImagesListCL.getId();
//                            if (aivList.isEmpty()) {
//                                lp.startToStart = incidentImagesListCL.getId();
//                            } else {
//                                lp.startToEnd = aivList.get(aivList.size() - 1).getId();
//                            }
//
//                            // lp.setMargins?
//
//                            view.setLayoutParams(lp);
//
//                            incidentImagesListCL.addView(view);


//                            view.setUri(uri);
//                            viewModel.getFileAndSetAttachedImageViewUri(imf, view, getContext());
//                            view.setFileName(imf.getName());
//                            view.showDownloadButton(true);
//                            if (viewModel.getMode().getValue().equals("view")) {
//                                view.showDeleteButton(false);
//                            }
//                            view.setOnDeleteListener(onDeleteListener);
//                            view.setOnDownloadListener(onDownloadListener);
//
//                            photoList.add(imf);
//                            aivList.add(view);
//
//                            viewModel.getFileAndSetAttachedImageViewUri(imf, view, getContext());
                        }




                    }
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            private boolean ran;

            @Override
            public void onChanged(String error) {
                if (!ran) {
                    ran = true;
                    return;
                }
                if (error != null) {
                    Snackbar.make(incidentTopCL, error, Snackbar.LENGTH_LONG).show();
                    incidentSendButton.setEnabled(true);
                    incidentRequestOperatorButton.setEnabled(true);
                }
            }
        });

        viewModel.getStatusSprav().observe(getViewLifecycleOwner(), new Observer<IncidentStatusSprav>() {
            @Override
            public void onChanged(IncidentStatusSprav incidentStatusSprav) {
                // StatusSprav is not used in incident editor?
            }
        });

        viewModel.getTypeSprav().observe(getViewLifecycleOwner(), new Observer<IncidentTypeSprav>() {
            @Override
            public void onChanged(IncidentTypeSprav incidentTypeSprav) {
                if (incidentTypeSprav != null) {
                    incidentCategoryTSV.setItems(incidentTypeSprav.getNames());
                }
            }
        });

        viewModel.getDisabilityCategorySprav().observe(getViewLifecycleOwner(), new Observer<IncidentDisabilityCategorySprav>() {
            @Override
            public void onChanged(IncidentDisabilityCategorySprav incidentDisabilityCategorySprav) {
                if (incidentDisabilityCategorySprav != null) {
                    incidentDisabilityCategoryTSV.setItems(incidentDisabilityCategorySprav.getNames());
                }
            }
        });

        viewModel.getAbonentCategorySprav().observe(getViewLifecycleOwner(), new Observer<IncidentAbonentCategorySprav>() {
            @Override
            public void onChanged(IncidentAbonentCategorySprav incidentAbonentCategorySprav) {
                if (incidentAbonentCategorySprav != null) {
                    incidentOrganisationCategoryTSV.setItems(incidentAbonentCategorySprav.getNames());
                }
            }
        });

        viewModel.getAbonentsNearbyMap().observe(getViewLifecycleOwner(), new Observer<AbonentsNearbyMap>() {
            @Override
            public void onChanged(AbonentsNearbyMap abonentsNearbyMap) {
                if (abonentsNearbyMap != null) {
                    incidentOrganisationTSV.setItems(abonentsNearbyMap.getNames());
                    if (!viewModel.getMode().getValue().equals("view")) {
                        incidentOrganisationTSV.setEnabledTSV(true);
                    }
                }
            }
        });

        // TODO replace with latitude/longitude observers
//        viewModel.getLocation().observe(getViewLifecycleOwner(), new Observer<Location>() {
//            @Override
//            public void onChanged(Location location) {
//                if (location != null) {
//                    incidentLocationLatitudeTTIV.setInputText(String.valueOf(location.getLatitude()));
//                    incidentLocationLongitudeTTIV.setInputText(String.valueOf(location.getLongitude()));
//                } else if (enableGeolocationErrorSnackbar) {
//                    Snackbar.make(incidentTopCL, "Не удалось определить местоположение",
//                            Snackbar.LENGTH_LONG).show();
//                } else {
//                    enableGeolocationErrorSnackbar = true;
//                }
//            }
//        });

        viewModel.getAddress().observe(getViewLifecycleOwner(), new Observer<String>() {
            private boolean ran;

            @Override
            public void onChanged(String address) {
                if (!wasRestored || ran) {
                    if (address != null
//                        && (
//                        !wasRestored
//                        || ran
//                        )
                    ) {
                        incidentLocationAddressTTIV.setInputText(address);
                    } else if (incidentLocationAddressTTIV.getInputText().equals("Подождите...")) {
                        incidentLocationAddressTTIV.setInputText(addressBeforeRefresh);
                    }
                }
                if (!ran) {
                    ran = true;
                } else {
                    incidentLocationAddressTTIV.setTTIEnabled(true);
                }
            }
        });

        viewModel.getLatitude().observe(getViewLifecycleOwner(), new Observer<Float>() {
            private boolean ran;

            @Override
            public void onChanged(Float lat) {
                if (!wasRestored || ran) {
                    if (lat != null
//                        && (
//                                !wasRestored
//                                || ran
//                        )
                    ) {
                        incidentLocationLatitudeTTIV.setInputText(lat.toString());
                    } else if (incidentLocationLatitudeTTIV.getInputText().equals("Подождите...")) {
                        incidentLocationLatitudeTTIV.setInputText(latitudeBeforeRefresh);
                    }
                }
                if (!ran) {
                    ran = true;
                } else {
                    incidentLocationLatitudeTTIV.setTTIEnabled(true);
                }
            }
        });

        viewModel.getLongitude().observe(getViewLifecycleOwner(), new Observer<Float>() {
            private boolean ran;

            @Override
            public void onChanged(Float lon) {
                if (!wasRestored || ran) {
                    if (lon != null
//                        && (
//                                !wasRestored
//                                || ran
//                        )
                    ) {
                        incidentLocationLongitudeTTIV.setInputText(lon.toString());
                        // Latitude and longitude are updated at the same time, so no need to duplicate
                        // the following code on latitude observer, because longitude is updated the last
//                    if (geoUpdateType.equals("geolocation")) {
//                        onGeoCoordinatesChange();
//                    searchAddressByCoordinates();
//                    }
                        searchOrganisations();
//                    incidentLocationLatitudeTTIV.setEnabled(true);
//                    incidentLocationLongitudeTTIV.setEnabled(true);
//                    incidentLocationAddressTTIV.setEnabled(true);
                    } else if (incidentLocationLongitudeTTIV.getInputText().equals("Подождите...")) {
                        incidentLocationLongitudeTTIV.setInputText(longitudeBeforeRefresh);
                    }
                }
                if (!ran) {
                    ran = true;
                } else {
                    incidentLocationLongitudeTTIV.setTTIEnabled(true);
                }
            }
        });

        viewModel.getAddressRefreshNeeded().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            private boolean ran;

            @Override
            public void onChanged(Boolean needed) {
                if (wasRestored && !ran) {
                    ran = true;
                    return;
                }
                if (needed) {
                    searchAddressByCoordinates();
                }
            }
        });

        viewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (wasRestored) {
                    return;
                }
                if (!viewModel.getMode().getValue().equals("create")) {
                    return;
                }
                if (user == null) {
                    viewModel.loadUserFromDb();
                    return;
                }
                if (!userUpdatedFromServer) {
                    viewModel.updateUserFromServer();
                    userUpdatedFromServer = true;
                    return;
                }
                profileSurnameTTI.setInputText(user.getSurname());
                profileNameTTI.setInputText(user.getName());
                profileMidnameTTI.setInputText(user.getMidname());
                profilePhoneTTI.setInputText(user.getPhone());
//                if (DevSettings.HIDE_DUMMY_EMAIL && user.getEmail().endsWith("@localhost")) {
//                    profileEmailTTI.setInputText("");
//                } else {
//                    profileEmailTTI.setInputText(user.getEmail());
//                }
                profileCarBrandTTI.setInputText(user.getCarBrand());
                profileCarNumberTTI.setInputText(user.getCarNumber());
                profileOsagoTTI.setInputText(user.getOsago());
                profileSnilsTTI.setInputText(user.getSnils());
                profileMedPolisTTI.setInputText(user.getMedPolis());
                if (user.getBirthdayAsLocalDate() != null) {
                    profileBirthdayTTI.setInputText(user.getBirthdayAsLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd.MM.uuuu"))
                    );
                } else {
                    profileBirthdayTTI.setInputText("");
                }

                if (!incidentDisabilityCategoryTSV.getItems().contains(user.getDisabilityCategory())) {
                    incidentDisabilityCategoryTSV.addItem(user.getDisabilityCategory());
                }
                incidentDisabilityCategoryTSV.setSelectedItem(user.getDisabilityCategory());

            }
        });
    }

    private void searchAddressByCoordinates() {
        if (incidentLocationLatitudeTTIV.getInputText() != null
                && incidentLocationLongitudeTTIV.getInputText() != null
//            && incidentSearchRadiusTTIV.getInputText() != null
                && !incidentLocationLatitudeTTIV.getInputText().isEmpty()
                && !incidentLocationLongitudeTTIV.getInputText().isEmpty()
//            && !incidentSearchRadiusTTIV.getInputText().isEmpty()
        ) {
            float lat;
            float lon;


            try {
                lat = Float.parseFloat(incidentLocationLatitudeTTIV.getInputText());
                lon = Float.parseFloat(incidentLocationLongitudeTTIV.getInputText());
//                radius = Float.parseFloat(incidentSearchRadiusTTIV.getInputText());
            } catch (NumberFormatException e) {
                return;
            }

            viewModel.getAddressByCoordinates(lat, lon);
        }
    }

    private void searchOrganisations() {
        if (incidentLocationLatitudeTTIV.getInputText() != null
                && incidentLocationLongitudeTTIV.getInputText() != null
                && incidentSearchRadiusTTIV.getInputText() != null
                && incidentOrganisationCategoryTSV.getSelection() != null
                && !incidentLocationLatitudeTTIV.getInputText().isEmpty()
                && !incidentLocationLongitudeTTIV.getInputText().isEmpty()
                && !incidentSearchRadiusTTIV.getInputText().isEmpty()
                && !incidentOrganisationCategoryTSV.getSelection().isEmpty()
        ) {
            float lat;
            float lon;
            float radius;

            try {
                lat = Float.parseFloat(incidentLocationLatitudeTTIV.getInputText());
                lon = Float.parseFloat(incidentLocationLongitudeTTIV.getInputText());
                radius = Float.parseFloat(incidentSearchRadiusTTIV.getInputText());
            } catch (NumberFormatException e) {
                return;
            }

//            viewModel.getAddressByCoordinates(lat, lon);

//            if (incidentSearchRadiusTTIV.getInputText() != null
//                    && !incidentSearchRadiusTTIV.getInputText().isEmpty()) {
//
//                float radius;
//
//                try {
//                    radius = Float.parseFloat(incidentSearchRadiusTTIV.getInputText());
//                } catch (NumberFormatException e) {
//                    return;
//                }
            viewModel.searchAbonents(
                    incidentOrganisationCategoryTSV.getSelection(),
                    radius,
                    lat,
                    lon
            );
//            }

        }
    }

    private void onGeoCoordinatesChange() {
        // TODO what will happen if we type address, coordinates are searched and filled, and this
        // is called? Address will refresh again?
        searchAddressByCoordinates();
        searchOrganisations();
    }

    @Override
    protected void configureViewModel() {
        super.configureViewModel();
        viewModel.subscribeToLocationUpdates(getViewLifecycleOwner());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedVM = new ViewModelProvider(requireActivity()).get(IncidentSharedViewModel.class);
        menuSharedVM = new ViewModelProvider(requireActivity()).get(MenuSharedViewModel.class);
        setViewModel(IncidentViewModel.class);
        setViewBinding(FragmentIncidentBinding.class);
        configureSharedViewModelActions();
        getLocationPermissionCoarseLauncher = registerForActivityResult(PermissionActivity.getARC(),
                getLocationPermissionCoarseCallback);
        getLocationPermissionFineLauncher = registerForActivityResult(PermissionActivity.getARC(),
                getLocationPermissionFineCallback);
        pickPhotoLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                pickPhotoCallback);
        downloadLauncher = registerForActivityResult(PermissionActivity.getARC(), downloadCallback);
        pickDocumentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                pickDocumentCallback);
        videoLauncher = registerForActivityResult(VideoActivity.getARC(), videoCallback);
//        requireActivity().addMenuProvider(new MenuProvider() {
//            @Override
//            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
//                getActivity().
//            }
//
//            @Override
//            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
//                return false;
//            }
//        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("incidentOrganisationCategoryTSV", incidentOrganisationCategoryTSV.getSelection());
        outState.putString("incidentOrganisationTSV", incidentOrganisationTSV.getSelection());
        outState.putString("incidentSearchRadiusTTIV", incidentSearchRadiusTTIV.getInputText());
        outState.putString("incidentLocationLatitudeTTIV", incidentLocationLatitudeTTIV.getInputText());
        outState.putString("incidentLocationLongitudeTTIV", incidentLocationLongitudeTTIV.getInputText());
        outState.putString("incidentLocationAddressTTIV", incidentLocationAddressTTIV.getInputText());
        outState.putBoolean("incidentLocationLatitudeTTIV_enabled", incidentLocationLatitudeTTIV.isTTIEnabled());
        outState.putBoolean("incidentLocationLongitudeTTIV_enabled", incidentLocationLongitudeTTIV.isTTIEnabled());
        outState.putBoolean("incidentLocationAddressTTIV_enabled", incidentLocationAddressTTIV.isTTIEnabled());
        outState.putString("incidentCategoryTSV", incidentCategoryTSV.getSelection());
        outState.putBoolean("incidentIsHurt", incidentIsHurt.isChecked());
        outState.putBoolean("incidentIsEyewitness", incidentIsEyewitness.isChecked());
        outState.putString("incidentDescriptionTTIV", incidentDescriptionTTIV.getInputText());
        outState.putString("profileSurnameTTI", profileSurnameTTI.getInputText());
        outState.putString("profileNameTTI", profileNameTTI.getInputText());
        outState.putString("profileMidnameTTI", profileMidnameTTI.getInputText());
        outState.putString("profilePhoneTTI", profilePhoneTTI.getInputText());
        outState.putString("profileCarBrandTTI", profileCarBrandTTI.getInputText());
        outState.putString("profileCarNumberTTI", profileCarNumberTTI.getInputText());
        outState.putString("profileOsagoTTI", profileOsagoTTI.getInputText());
        outState.putString("profileSnilsTTI", profileSnilsTTI.getInputText());
        outState.putString("profileMedPolisTTI", profileMedPolisTTI.getInputText());
        outState.putString("profileBirthdayTTI", profileBirthdayTTI.getInputText());
        outState.putString("incidentDisabilityCategoryTSV", incidentDisabilityCategoryTSV.getSelection());
        outState.putString("latitudeBeforeRefresh", latitudeBeforeRefresh);
        outState.putString("longitudeBeforeRefresh", longitudeBeforeRefresh);
        outState.putString("addressBeforeRefresh", addressBeforeRefresh);
        outState.putInt("incidentNSVScrollY", incidentNSV.getScrollY());
        viewModel.setPreviousPhotoList(photoList);
        viewModel.setPreviousDocumentList(documentList);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.getString("incidentOrganisationCategoryTSV") != null) {
//            if (!incidentOrganisationCategoryTSV
//                    .getItems()
//                    .contains(savedInstanceState.getString("incidentOrganisationCategoryTSV"))) {
//                ArrayList<String> items = incidentOrganisationCategoryTSV.getItems();
//                items.add(savedInstanceState.getString("incidentOrganisationCategoryTSV"));
//                incidentOrganisationCategoryTSV.setItems(items);
//            }
            incidentOrganisationCategoryTSV.addItem(
                    savedInstanceState.getString("incidentOrganisationCategoryTSV")
            );
            incidentOrganisationCategoryTSV.setSelectedItem(
                    savedInstanceState.getString("incidentOrganisationCategoryTSV")
            );
        }
        if (savedInstanceState.getString("incidentOrganisationTSV") != null) {
//            if (!incidentOrganisationTSV
//                    .getItems()
//                    .contains(savedInstanceState.getString("incidentOrganisationTSV"))) {
//                ArrayList<String> items = incidentOrganisationTSV.getItems();
//                items.add(savedInstanceState.getString("incidentOrganisationTSV"));
//                incidentOrganisationTSV.setItems(items);
//            }
            incidentOrganisationTSV.addItem(
                    savedInstanceState.getString("incidentOrganisationTSV")
            );
            incidentOrganisationTSV.setSelectedItem(
                    savedInstanceState.getString("incidentOrganisationTSV")
            );
        }
        incidentSearchRadiusTTIV.setInputText(
                savedInstanceState.getString("incidentSearchRadiusTTIV")
        );
        incidentLocationLatitudeTTIV.setInputText(
                savedInstanceState.getString("incidentLocationLatitudeTTIV")
        );
        incidentLocationLongitudeTTIV.setInputText(
                savedInstanceState.getString("incidentLocationLongitudeTTIV")
        );
        incidentLocationAddressTTIV.setInputText(
                savedInstanceState.getString("incidentLocationAddressTTIV")
        );
        incidentLocationLatitudeTTIV.setTTIEnabled(
                savedInstanceState.getBoolean("incidentLocationLatitudeTTIV_enabled")
        );
        incidentLocationLongitudeTTIV.setTTIEnabled(
                savedInstanceState.getBoolean("incidentLocationLongitudeTTIV_enabled")
        );
        incidentLocationAddressTTIV.setTTIEnabled(
                savedInstanceState.getBoolean("incidentLocationAddressTTIV_enabled")
        );
        if (savedInstanceState.getString("incidentCategoryTSV") != null) {
            incidentCategoryTSV.addItem(
                    savedInstanceState.getString("incidentCategoryTSV")
            );
            incidentCategoryTSV.setSelectedItem(
                    savedInstanceState.getString("incidentCategoryTSV")
            );
        }
        incidentIsHurt.setChecked(savedInstanceState.getBoolean("incidentIsHurt"));
        incidentIsEyewitness.setChecked(savedInstanceState.getBoolean("incidentIsEyewitness"));
        incidentDescriptionTTIV.setInputText(
                savedInstanceState.getString("incidentDescriptionTTIV")
        );
        profileSurnameTTI.setInputText(
                savedInstanceState.getString("profileSurnameTTI")
        );
        profileNameTTI.setInputText(
                savedInstanceState.getString("profileNameTTI")
        );
        profileMidnameTTI.setInputText(
                savedInstanceState.getString("profileMidnameTTI")
        );
        profilePhoneTTI.setInputText(
                savedInstanceState.getString("profilePhoneTTI")
        );
        profileCarBrandTTI.setInputText(
                savedInstanceState.getString("profileCarBrandTTI")
        );
        profileCarNumberTTI.setInputText(
                savedInstanceState.getString("profileCarNumberTTI")
        );
        profileOsagoTTI.setInputText(
                savedInstanceState.getString("profileOsagoTTI")
        );
        profileSnilsTTI.setInputText(
                savedInstanceState.getString("profileSnilsTTI")
        );
        profileMedPolisTTI.setInputText(
                savedInstanceState.getString("profileMedPolisTTI")
        );
        profileBirthdayTTI.setInputText(
                savedInstanceState.getString("profileBirthdayTTI")
        );
        if (savedInstanceState.getString("incidentDisabilityCategoryTSV") != null) {
            incidentDisabilityCategoryTSV.addItem(
                    savedInstanceState.getString("incidentDisabilityCategoryTSV")
            );
            incidentDisabilityCategoryTSV.setSelectedItem(
                    savedInstanceState.getString("incidentDisabilityCategoryTSV")
            );
        }
        latitudeBeforeRefresh = savedInstanceState.getString("latitudeBeforeRefresh");
        longitudeBeforeRefresh = savedInstanceState.getString("longitudeBeforeRefresh");
        addressBeforeRefresh = savedInstanceState.getString("addressBeforeRefresh");
        for (IncidentMediaFile imf: viewModel.getPreviousPhotoList()) {
            addAttachedFile(imf);
        }
        for (IncidentMediaFile imf: viewModel.getPreviousDocumentList()) {
            addAttachedFile(imf);
        }
        scrollYRestored = savedInstanceState.getInt("incidentNSVScrollY");
//        incidentNSV.scrollTo(
//                0,
//                Integer.max(
//                        0,
//                        Integer.min(
//                                scrollYRestored,
//                                incidentNSV.getMeasuredHeight() - incidentNSV.getHeight()
//                        )
//                )
//        );
    }

    private boolean onPausePrevState;

    @Override
    public void onPause() {
//        ((MenuActivity) requireActivity()).setBackOnNavigateUp(false);
        onPausePrevState = menuSharedVM.getEnableBackOnNavigateUp().getValue();
        menuSharedVM.setEnableBackOnNavigateUp(false);
        super.onPause();
    }

    @Override
    public void onResume() {
//        ((MenuActivity) requireActivity()).setBackOnNavigateUp(false);
        menuSharedVM.setEnableBackOnNavigateUp(onPausePrevState);
        super.onResume();
    }
}
