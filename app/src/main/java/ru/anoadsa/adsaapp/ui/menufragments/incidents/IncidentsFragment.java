package ru.anoadsa.adsaapp.ui.menufragments.incidents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount.ChatNotificationManager;
import ru.anoadsa.adsaapp.databinding.FragmentIncidentsBinding;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.ui.abstracts.UiMenuFragment;
import ru.anoadsa.adsaapp.ui.menufragments.chat.ChatSharedViewModel;
import ru.anoadsa.adsaapp.ui.menufragments.incident.IncidentFragment;
import ru.anoadsa.adsaapp.ui.menufragments.incident.IncidentSharedViewModel;
import ru.anoadsa.adsaapp.ui.views.IncidentCardView;

public class IncidentsFragment extends UiMenuFragment<IncidentsViewModel, FragmentIncidentsBinding> {
    private Button incidentsNewButton;
    private Button incidentsUpdateButton;
    private ConstraintLayout incidentsMainCL;
    private ConstraintLayout incidentsTopCL;

    private ArrayList<IncidentCardView> icvList = new ArrayList<>();

    private IncidentSharedViewModel incidentSharedVM;


    @Override
    protected void getRoot(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.getRoot(inflater, container);
    }

    @Override
    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.initUi(inflater, container);
        incidentsNewButton = binding.incidentsNewButton;
//        incidentsUpdateButton = binding.incidentsUpdateButton;
        incidentsMainCL = binding.incidentsMainCL;
        incidentsTopCL = binding.incidentsTopCL;
    }

    @Override
    protected void configureUiState() {
    }

    @Override
    protected void setViewModel(Class<IncidentsViewModel> vmclass) {
        super.setViewModel(vmclass);
    }

    @Override
    protected void configureViewModel() {
        super.configureViewModel();
    }

    @Override
    protected void configureUiActions() {
//        incidentsUpdateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                viewModel.refresh();
//            }
//        });

        incidentsNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sharedVM.setMode("create");
                // TODO: call incident fragment
//                IncidentFragment incidentFragment = new IncidentFragment();
                incidentSharedVM = new ViewModelProvider(requireActivity()).get(IncidentSharedViewModel.class);
                incidentSharedVM.setMode("create");
                incidentSharedVM.setIncident(null);
//                requireActivity().getSupportFragmentManager().beginTransaction()
//                        .replace(
//                                ((ViewGroup) requireView().getParent()).getId(),
//                                incidentFragment,
//                                "incidentFragment"
//                        )
//                        .addToBackStack(null)
//                        .commit();
                Navigation
                        .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                        .navigate(R.id.action_incidents_to_incident);
//                getActivity().getActionBar()
            }
        });


    }

    private View.OnClickListener icvOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO check if it is necessary to initialize incidentFragment twice
//            IncidentFragment incidentFragment = new IncidentFragment();
            incidentIndexToOpen = icvList.indexOf((IncidentCardView) view);
            actionToDo = "view";
            viewModel.loadFullIncidentInfo(incidentIndexToOpen);

//            sharedVM = new ViewModelProvider(requireActivity()).get(IncidentSharedViewModel.class);
//            sharedVM.setMode("view");
//            sharedVM.setIncident(
//                    viewModel.getSortedIncidentsList().getValue().get(
//                            icvList.indexOf((IncidentCardView) view)
//                    )
//            );
//            Navigation
//                    .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
//                    .navigate(R.id.action_incidents_to_incident);
        }
    };

    private int incidentIndexToOpen;
    private String actionToDo;

    private View.OnClickListener chatOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(@NonNull View view) {
            // view is button, converting to incidentcardview
            IncidentCardView v = (IncidentCardView) view.getParent().getParent().getParent().getParent();
            ChatSharedViewModel chatSharedVM = new ViewModelProvider(getActivity()).get(ChatSharedViewModel.class);
            chatSharedVM.setIncident(viewModel.getSortedIncidentsList().getValue().get(icvList.indexOf(v)));
            // ChatFragment checks if incident is not fully loaded and loads it info automatically

            Navigation
                    .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                    .navigate(R.id.action_incidents_to_chat);
        }
    };

    @Override
    protected void configureViewModelActions() {
        viewModel.getSortedIncidentsList().observe(getViewLifecycleOwner(), new Observer<List<Incident>>() {
            @Override
            public void onChanged(List<Incident> incidents) {
                if (incidents == null) {
                    // Refresh is done automaticallu in onResume
//                    viewModel.refresh();
                } else {

                    // TODO improve performance. Maybe we could update existing incidents instead of deleting them and creating again
                    for (IncidentCardView icv: icvList) {
                        incidentsMainCL.removeView(icv);
                    }
                    icvList.clear();

                    for (int i = 0; i < incidents.size(); i++) {
                        IncidentCardView icv =
                                new IncidentCardView(IncidentsFragment.this.getContext());
//                        ConstraintSet constraints = new ConstraintSet();
                        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.MATCH_PARENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                        );

                        icv.setId(View.generateViewId());
//                        incidentsMainCL.addView(icv);
//                        constraints.clone(incidentsMainCL);

                        // ???????????
//                        constraints.constrainHeight(icv.getId(), ConstraintSet.WRAP_CONTENT);
//                        constraints.constrainWidth(icv.getId(), ConstraintSet.WRAP_CONTENT);

//                        constraints.connect(icv.getId(), ConstraintSet.START,
//                                incidentsMainCL.getId(), ConstraintSet.START);
//                        constraints.connect(icv.getId(), ConstraintSet.END,
//                                incidentsMainCL.getId(), ConstraintSet.END);
                        lp.startToStart = incidentsMainCL.getId();
                        lp.endToEnd = incidentsMainCL.getId();
                        if (i == 0) {
//                            constraints.connect(icv.getId(), ConstraintSet.TOP,
//                                    incidentsMainCL.getId(), ConstraintSet.TOP);
                            lp.topToTop = incidentsMainCL.getId();
                        } else {
//                            constraints.connect(icv.getId(), ConstraintSet.TOP,
//                                    icvList.get(i - 1).getId(), ConstraintSet.BOTTOM);
                            lp.topToBottom = icvList.get(i - 1).getId();
                        }
//                        constraints.applyTo(incidentsMainCL);

                        lp.setMargins(
                                0,
                                Static.dpToPixels(i == 0 ? 16 : 24, requireContext()),
                                0,
                                0
                        );
                        icv.setLayoutParams(lp);

                        Incident ii = incidents.get(i);

                        icv.setAll(
                                ((Integer) ii.getDocnum()).toString(),
                                Static.formatLocalDateTime(ii.getCreatedDateTime()),
                                ii.getAbonentText(),
                                ii.getClientSurname(),
                                ii.getAddress(),
                                ii.getStatusText(),
                                ii.getOperatorText()
                        );

                        icv.setNewMessagesCount(ChatNotificationManager.getNotificationCount(ii.getId()));
                        icv.setChatButtonOnClickListener(chatOnClickListener);

                        incidentsMainCL.addView(icv);

                        icv.setOnClickListener(icvOnClickListener);

                        icvList.add(icv);
                    }
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null) {
                    Snackbar.make(incidentsTopCL, error, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getIncidentFullInfoLoaded().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loaded) {
                if (loaded) {
                    if (actionToDo == null) {
                        return;
                    }
                    if (actionToDo.equals("view")) {
                        incidentSharedVM = new ViewModelProvider(requireActivity()).get(IncidentSharedViewModel.class);
                        incidentSharedVM.setMode("view");
                        incidentSharedVM.setIncident(
                                viewModel.getSortedIncidentsList().getValue().get(
//                                    icvList.indexOf((IncidentCardView) view)
                                        incidentIndexToOpen
                                )
                        );
                        Navigation
                                .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                                .navigate(R.id.action_incidents_to_incident);
                    }
                    actionToDo = null;
                }
            }
        });
    }

    public void configureChatNotificationManagerActions() {
        ChatNotificationManager.getAllNotificationCountLiveData().observe(getViewLifecycleOwner(), new Observer<HashMap<String, Integer>>() {
            @Override
            public void onChanged(HashMap<String, Integer> idToMsgCount) {
                // TODO check performance? Number of new messages set both on incident list refresh and on notification count refresh
                // Also on notification count refresh it is set for all values, even those that are not refreshed
                if (idToMsgCount == null) {
                    return;
                }
                if (icvList.isEmpty()) {
                    return;
                }
//                for (String id: idToMsgCount.keySet()) {
//
//                    Incident i = viewModel.getIncidentHashMap().get(id);
//                    if (i == null) {
//                        continue;
//                    }
//                    int docnum = i.getDocnum();
//                    IncidentCardView v = icvList.get(icvList.size() - i.getDocnum());
//                    v.setNewMessagesCount(idToMsgCount.get(id));
//                }
                List<Incident> l = viewModel.getSortedIncidentsList().getValue();
                if (l == null) {
                    return;
                }
                for (int i = 0; i < l.size(); ++i) {
                    if (idToMsgCount.containsKey(l.get(i).getId())) {
                        icvList.get(i).setNewMessagesCount(idToMsgCount.get(l.get(i).getId()));
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setViewBinding(FragmentIncidentsBinding.class);
        setViewModel(IncidentsViewModel.class);
//        sharedVM = new ViewModelProvider(this).get(IncidentSharedViewModel.class);
        configureChatNotificationManagerActions();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
        viewModel.startAutoRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.stopAutoRefresh();
    }
}
