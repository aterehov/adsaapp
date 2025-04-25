//package ru.anoadsa.adsaapp.ui.menufragments.video;
//
//import android.Manifest;
//import android.location.Location;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.webkit.PermissionRequest;
//import android.webkit.WebChromeClient;
//import android.webkit.WebResourceRequest;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//
//import androidx.activity.result.ActivityResultCallback;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.annotation.NonNull;
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.navigation.Navigation;
//
//import com.google.android.material.snackbar.Snackbar;
//
//import ru.anoadsa.adsaapp.Geo;
//import ru.anoadsa.adsaapp.R;
//import ru.anoadsa.adsaapp.Static;
//import ru.anoadsa.adsaapp.databinding.FragmentVideoBinding;
//import ru.anoadsa.adsaapp.models.data.Incident;
//import ru.anoadsa.adsaapp.models.data.Video;
//import ru.anoadsa.adsaapp.ui.EmptyViewModel;
//import ru.anoadsa.adsaapp.ui.abstracts.UiMenuFragment;
//import ru.anoadsa.adsaapp.ui.activities.permission.PermissionActivity;
//
//public class VideoFragment extends UiMenuFragment<VideoViewModel, FragmentVideoBinding> {
//    private WebView webView;
//
//    private VideoSharedViewModel sharedVM;
//
//    private class VideoWebViewClient extends WebViewClient {
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, @NonNull WebResourceRequest request) {
////            if (request.getUrl().getPath().equals("https://event33.ru/static/close2.html")) {
////                Navigation
////                        .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
//////                        .navigate(R.id.action_incident_to_chat);
////                        .popBackStack();
////                return true;
////            }
////            return false;
//            return quitIfNeeded(view, request.getUrl().getPath());
//        }
//
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            return quitIfNeeded(view, url);
//        }
//
//
//
//        public boolean quitIfNeeded(WebView view, @NonNull String url) {
//            if (url.equals("https://event33.ru/static/close2.html")) {
//                Navigation
//                        .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
////                        .navigate(R.id.action_incident_to_chat);
//                        .popBackStack();
//                return true;
//            }
//            return false;
//        }
//    }
//
//    private ActivityResultLauncher<String> locationLauncher1;
//    private ActivityResultLauncher<String> locationLauncher2;
//
//    private ActivityResultCallback<Boolean> locationCallback1 = new ActivityResultCallback<Boolean>() {
//        @Override
//        public void onActivityResult(@NonNull Boolean success) {
//            if (success) {
//                refreshLocation();
//            } else {
//                // Try coarse location
//                if (!Static.checkPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                    locationLauncher2.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
//                } else {
//                    refreshLocation();
//                }
//            }
//        }
//    };
//
//    private ActivityResultCallback<Boolean> locationCallback2 = new ActivityResultCallback<Boolean>() {
//        @Override
//        public void onActivityResult(@NonNull Boolean success) {
//            if (success) {
//                refreshLocation();
//            } else {
//                // This would be run after location getting in viewmodel actions
//                viewModel.getVideoFromServer();
//            }
//        }
//    };
//
//    private ActivityResultLauncher<String> cameraLauncher;
//    private ActivityResultLauncher<String> microphoneLauncher;
//
//    private ActivityResultCallback<Boolean> cameraCallback = new ActivityResultCallback<Boolean>() {
//        @Override
//        public void onActivityResult(Boolean success) {
//            if (!Static.checkPermission(getContext(), Manifest.permission.RECORD_AUDIO)) {
//                microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO);
//            } else {
//                startCall();
//            }
//        }
//    };
//
//    private ActivityResultCallback<Boolean> microphoneCallback = new ActivityResultCallback<Boolean>() {
//        @Override
//        public void onActivityResult(Boolean success) {
//            startCall();
//        }
//    };
//
//    private void startCall() {
//        if (!Static.checkPermission(getContext(), Manifest.permission.CAMERA)
//            && !Static.checkPermission(getContext(), Manifest.permission.RECORD_AUDIO)) {
//            viewModel.setErrorMessage("Нет доступа к камере и микрофону");
//            return;
//        }
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setDomStorageEnabled(true);
////        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
//        webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, @NonNull WebResourceRequest request) {
////            if (request.getUrl().getPath().equals("https://event33.ru/static/close2.html")) {
////                Navigation
////                        .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
//////                        .navigate(R.id.action_incident_to_chat);
////                        .popBackStack();
////                return true;
////            }
////            return false;
//                return quitIfNeeded(view, request.getUrl().getPath());
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return quitIfNeeded(view, url);
//            }
//
//            public boolean quitIfNeeded(WebView view, @NonNull String url) {
//                if (url.equals("/static/close2.html")) {
//                    Navigation
//                            .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
////                        .navigate(R.id.action_incident_to_chat);
//                            .popBackStack();
//                    return true;
//                }
//                return false;
//            }
//        });
//        webView.setWebChromeClient(new WebChromeClient(){
//            @Override
//            public void onPermissionRequest(PermissionRequest request) {
//                request.grant(request.getResources());
//            }
//        });
//        webView.loadUrl(viewModel.getVideo().getValue().getConfomeetLink());
//    }
//
//    private void refreshLocation() {
//        viewModel.prepareForLocationUpdate();
//        Geo.refreshLocation(getContext());
//    }
//
//
//    @Override
//    protected void setViewBinding(Class<FragmentVideoBinding> vbclass) {
//        super.setViewBinding(vbclass);
//    }
//
//    @Override
//    protected void getRoot(@NonNull LayoutInflater inflater, ViewGroup container) {
//        super.getRoot(inflater, container);
//    }
//
//    @Override
//    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
//        super.initUi(inflater, container);
//        webView = binding.videoWebView;
//    }
//
//    @Override
//    protected void configureUiState() {
//
//    }
//
//    @Override
//    protected void setViewModel(Class<VideoViewModel> vmclass) {
//        super.setViewModel(vmclass);
//    }
//
//    @Override
//    protected void configureViewModel() {
//        super.configureViewModel();
//    }
//
//    @Override
//    protected void configureUiActions() {
//
//    }
//
//    @Override
//    protected void configureViewModelActions() {
//        viewModel.subscribeOnGeoUpdates(getViewLifecycleOwner());
//
//        viewModel.getHasIncident().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean has) {
//                // TODO Delete this??
//                if (has == null) {
//                    return;
//                }
//                if (has) {
//
//                }
//            }
//        });
//
//        viewModel.getIncident().observe(getViewLifecycleOwner(), new Observer<Incident>() {
//            @Override
//            public void onChanged(Incident incident) {
//                // ???
//            }
//        });
//
//        viewModel.getLocation().observe(getViewLifecycleOwner(), new Observer<Location>() {
//            @Override
//            public void onChanged(Location location) {
//                // ???
//            }
//        });
//
//        viewModel.getLocationUpdated().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean updated) {
//                if (updated) {
//                    viewModel.getVideoFromServer();
//                } else {
//                    if (!Static.checkPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        locationLauncher1.launch(Manifest.permission.ACCESS_FINE_LOCATION);
//                    } else {
//                        refreshLocation();
//                    }
//                }
//            }
//        });
//
//        viewModel.getVideo().observe(getViewLifecycleOwner(), new Observer<Video>() {
//            @Override
//            public void onChanged(Video video) {
//                if (video == null) {
//                    return;
//                }
//                // Show video in webview
////                webView.getSettings().setJavaScriptEnabled(true);
//////                webView.getSettings().setDomStorageEnabled(true);
////                webView.loadUrl(video.getConfomeetLink());
//                if (!Static.checkPermission(getContext(), Manifest.permission.CAMERA)) {
//                    cameraLauncher.launch(Manifest.permission.CAMERA);
//                } else if (!Static.checkPermission(getContext(), Manifest.permission.RECORD_AUDIO)) {
//                    microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO);
//                } else {
//                    startCall();
//                }
//            }
//        });
//
//        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(String error) {
//                if (error != null && !error.isEmpty()) {
//                    Snackbar.make(webView, error, Snackbar.LENGTH_LONG).show();
//                }
//            }
//        });
//    }
//
//    protected void configureSharedViewModelActions() {
//        sharedVM.getHasIncident().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean has) {
//                viewModel.setHasIncident(has);
//            }
//        });
//
//        sharedVM.getIncident().observe(getViewLifecycleOwner(), new Observer<Incident>() {
//            @Override
//            public void onChanged(Incident incident) {
//                viewModel.setIncident(incident);
//            }
//        });
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        setViewModel(VideoViewModel.class);
//        setViewBinding(FragmentVideoBinding.class);
//        sharedVM = new ViewModelProvider(getActivity()).get(VideoSharedViewModel.class);
//        configureSharedViewModelActions();
//        locationLauncher1 = registerForActivityResult(PermissionActivity.getARC(), locationCallback1);
//        locationLauncher2 = registerForActivityResult(PermissionActivity.getARC(), locationCallback2);
//        cameraLauncher = registerForActivityResult(PermissionActivity.getARC(), cameraCallback);
//        microphoneLauncher = registerForActivityResult(PermissionActivity.getARC(), microphoneCallback);
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//    }
//}
