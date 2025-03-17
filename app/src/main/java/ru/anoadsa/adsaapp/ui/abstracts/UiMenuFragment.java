package ru.anoadsa.adsaapp.ui.abstracts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ru.anoadsa.adsaapp.databinding.FragmentHomeBinding;
import ru.anoadsa.adsaapp.ui.menufragments.home.HomeViewModel;

public abstract class UiMenuFragment<T extends ViewModel> extends Fragment {
    protected FragmentHomeBinding binding;
    protected View root;
    protected T viewModel;
    protected Class<T> vmclass;

    protected void getRoot(@NonNull LayoutInflater inflater, ViewGroup container) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();
    }

    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        getRoot(inflater, container);
    }

    protected void configureUiState() {

    }

    protected void setViewModel(Class<T> vmclass) {
        this.vmclass = vmclass;
    }

    protected void configureViewModel() {
        if(vmclass != null) {
            viewModel = new ViewModelProvider(this).get(vmclass);
        }
    }

    protected void configureUiActions() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        initUi(inflater, container);
        configureUiState();
        configureViewModel();
        configureUiActions();


//        final TextView tv = root.findViewById(R.id.text_home);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
