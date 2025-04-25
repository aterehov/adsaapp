package ru.anoadsa.adsaapp.ui.abstracts;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public abstract class UiActivity<T extends ViewModel> extends AppCompatActivity {
    protected T viewModel;
    protected Class<T> vmclass;

    protected abstract void initUi();

    protected abstract void configureUiState();

    protected void setViewModel(Class<T> vmclass) {
        this.vmclass = vmclass;
    }

    protected T getViewModel() {
        return viewModel;
    }

    protected void configureViewModel() {
        if(vmclass != null) {
            viewModel = new ViewModelProvider(this).get(vmclass);
        }
    }

    protected abstract void configureUiActions();

    protected abstract void configureViewModelActions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        configureUiState();
        configureViewModel();
        configureUiActions();
        configureViewModelActions();
    }
}
