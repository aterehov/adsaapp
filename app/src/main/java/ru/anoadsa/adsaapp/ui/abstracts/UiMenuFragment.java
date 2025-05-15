package ru.anoadsa.adsaapp.ui.abstracts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import ru.anoadsa.adsaapp.databinding.FragmentHomeBinding;
import ru.anoadsa.adsaapp.ui.menufragments.home.HomeViewModel;

//@Keep
public abstract class UiMenuFragment<T extends ViewModel, B extends ViewBinding> extends Fragment {
    protected B binding;
    protected View root;
    protected T viewModel;
    protected Class<T> vmclass;
    protected Class<B> vbclass;

    protected boolean wasRestored;

    protected void setViewBinding(Class<B> vbclass) {
        this.vbclass = vbclass;
    }

//    @Keep
    protected void getRoot(@NonNull LayoutInflater inflater, ViewGroup container) {
//        binding = FragmentHomeBinding.inflate(inflater, container, false);
        try {
            Class<?>[] argtypes = {LayoutInflater.class, ViewGroup.class, boolean.class};
            binding = vbclass.cast(vbclass.getMethod("inflate", argtypes)
                    .invoke(null, inflater, container, false));
//        if (b instanceof B) {
//            binding = (B) b;
//        }
//        binding = bindingClass
            root = Objects.requireNonNull(binding).getRoot();
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        getRoot(inflater, container);
    }

    protected abstract void configureUiState();

    protected void setViewModel(Class<T> vmclass) {
        this.vmclass = vmclass;
    }

    protected void configureViewModel() {
        if(vmclass != null) {
            viewModel = new ViewModelProvider(this).get(vmclass);
        }
    }

    protected abstract void configureUiActions();

    protected abstract void configureViewModelActions();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        wasRestored = savedInstanceState != null;
        initUi(inflater, container);
        configureUiState();
        configureViewModel();
        configureUiActions();
        configureViewModelActions();


//        final TextView tv = root.findViewById(R.id.text_home);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
