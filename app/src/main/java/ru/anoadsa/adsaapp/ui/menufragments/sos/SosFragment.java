package ru.anoadsa.adsaapp.ui.menufragments.sos;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.backgroundtasks.sosbutton.SosButtonWorker;
import ru.anoadsa.adsaapp.databinding.FragmentSosBinding;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiMenuFragment;
import ru.anoadsa.adsaapp.ui.activities.video.VideoActivity;
import ru.anoadsa.adsaapp.ui.activities.video.VideoSharedViewModel;
import ru.anoadsa.adsaapp.ui.menufragments.incident.IncidentSharedViewModel;
import ru.anoadsa.adsaapp.ui.views.FixedDonutProgress;

public class SosFragment extends UiMenuFragment<SosViewModel, FragmentSosBinding> {
    private Button sosButton;
    private Button sosVideocallButton;
    private Button sosNewIncidentButton;

    private FixedDonutProgress sosProgress;
    private FixedDonutProgress sosVideoProgress;

    private Handler sosHandler = new Handler();
    private Handler sosVideoHandler = new Handler();

    private Disposable sosAnimationDisposable;
    private Disposable sosVideoAnimationDisposable;

    private Runnable sosRunnable = new Runnable() {
        @Override
        public void run() {
            if (Static.checkPermission(getContext(), Manifest.permission.SEND_SMS)) {
                viewModel.resetAll();
                new SelectGeoDialog().show(getActivity().getSupportFragmentManager(), "GEO_DIALOG");
            } else {
                Intent dial = new Intent(Intent.ACTION_DIAL);
                dial.setData(Uri.parse("tel:112"));
                startActivity(dial);
            }
        }
    };

    private Runnable sosVideoRunnable = new Runnable() {
        @Override
        public void run() {
            VideoSharedViewModel videoSharedVM = new ViewModelProvider(getActivity())
                    .get(VideoSharedViewModel.class);
            videoSharedVM.setHasIncident(false);
            videoSharedVM.setIncident(null);
//                startActivity(new Intent(getContext(), VideoActivity.class));
            videoLauncher.launch(null);
        }
    };

    private ActivityResultLauncher<String> videoLauncher;
    private ActivityResultCallback<Boolean> videoCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean success) {
            // Do nothing
        }
    };

    @Override
    protected void setViewBinding(Class<FragmentSosBinding> vbclass) {
        super.setViewBinding(vbclass);
    }

    @Override
    protected void getRoot(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.getRoot(inflater, container);
    }

    @Override
    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.initUi(inflater, container);
        sosButton = binding.sosButton;
        sosVideocallButton = binding.sosVideocallButton;
        sosNewIncidentButton = binding.sosNewIncidentButton;

        sosProgress = binding.sosProgress;
        sosVideoProgress = binding.sosVideoProgress;
    }

    @Override
    protected void configureUiState() {

    }

    @Override
    protected void setViewModel(Class<SosViewModel> vmclass) {
        super.setViewModel(vmclass);
    }

    @Override
    protected void configureViewModel() {
        if(vmclass != null) {
            viewModel = new ViewModelProvider(getActivity()).get(vmclass);
        }
    }

//    @Override
//    protected void configureViewModel() {
//        super.configureViewModel();
//    }

    @Override
    protected void configureUiActions() {
        sosNewIncidentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IncidentSharedViewModel incidentSharedVM = new ViewModelProvider(getActivity())
                        .get(IncidentSharedViewModel.class);
                incidentSharedVM.setMode("create");
                incidentSharedVM.setIncident(null);
                Navigation
                        .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_sos_to_incident);
            }
        });
        
//        sosVideocallButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                VideoSharedViewModel videoSharedVM = new ViewModelProvider(getActivity())
//                        .get(VideoSharedViewModel.class);
//                videoSharedVM.setHasIncident(false);
//                videoSharedVM.setIncident(null);
////                startActivity(new Intent(getContext(), VideoActivity.class));
//                videoLauncher.launch(null);
//            }
//        });

//        sosButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                SosButtonWorker.run(getContext().getApplicationContext());
//
////                Intent dial = new Intent(Intent.ACTION_DIAL);
////                dial.setData(Uri.parse("tel:112"));
////                startActivity(dial);
//                if (Static.checkPermission(getContext(), Manifest.permission.SEND_SMS)) {
//                    viewModel.resetAll();
//                    new SelectGeoDialog().show(getActivity().getSupportFragmentManager(), "GEO_DIALOG");
//                } else {
//                    Intent dial = new Intent(Intent.ACTION_DIAL);
//                    dial.setData(Uri.parse("tel:112"));
//                    startActivity(dial);
//                }
//            }
//        });

        sosButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sosAnimationDisposable = Observable.interval(50, TimeUnit.MILLISECONDS)
                            .doOnNext(new Consumer<Long>() {
                                @Override
                                public void accept(Long i) throws Throwable {
                                    sosProgress.setProgress(sosProgress.getProgress() + 1);
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
//                    sosHandler = new Handler();
                    sosHandler.postDelayed(sosRunnable, 5000);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sosHandler.removeCallbacks(sosRunnable);
                    sosAnimationDisposable.dispose();
                    sosProgress.setProgress(0);
                }
                return true;
            }
        });

        sosVideocallButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    sosVideoAnimationDisposable = Observable.interval(50, TimeUnit.MILLISECONDS)
                            .doOnNext(new Consumer<Long>() {
                                @Override
                                public void accept(Long i) throws Throwable {
                                    sosVideoProgress.setProgress(sosVideoProgress.getProgress() + 1);
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
//                    sosHandler = new Handler();
                    sosVideoHandler.postDelayed(sosVideoRunnable, 5000);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    sosVideoHandler.removeCallbacks(sosVideoRunnable);
                    sosVideoAnimationDisposable.dispose();
                    sosVideoProgress.setProgress(0);
                }
                return true;
            }
        });
    }

    @Override
    protected void configureViewModelActions() {
        viewModel.getNext().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean next) {
                if (!next) {
                    return;
                }

                SosButtonWorker.run(
                        getActivity().getApplicationContext(),
                        viewModel.getLatitude().getValue(),
                        viewModel.getLongitude().getValue(),
                        viewModel.getAddress().getValue()
                );

                Intent dial = new Intent(Intent.ACTION_DIAL);
                dial.setData(Uri.parse("tel:112"));
                startActivity(dial);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setViewBinding(FragmentSosBinding.class);
        setViewModel(SosViewModel.class);
        videoLauncher = registerForActivityResult(VideoActivity.getARC(), videoCallback);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }
}
