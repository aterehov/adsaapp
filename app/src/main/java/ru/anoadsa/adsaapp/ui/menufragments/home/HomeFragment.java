package ru.anoadsa.adsaapp.ui.menufragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.anoadsa.adsaapp.databinding.FragmentHomeBinding;
import ru.anoadsa.adsaapp.ui.abstracts.UiMenuFragment;

public class HomeFragment extends UiMenuFragment<HomeViewModel> {

//    private FragmentHomeBinding binding;
//    private ViewModel homeViewModel;
//    private View root;
    private TextView textView;


    @Override
    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.initUi(inflater, container);
        textView = binding.textHome;
    }

    @Override
    protected void configureViewModel() {
        setViewModel(HomeViewModel.class);
        super.configureViewModel();
        viewModel.getText().observe(getViewLifecycleOwner(), uiState -> {
            textView.setText(uiState);
        });
    }

//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//
//        super.onCreateView(inflater, container, savedInstanceState);
//
//
////        final TextView tv = root.findViewById(R.id.text_home);
//        return root;
//    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//    }
}