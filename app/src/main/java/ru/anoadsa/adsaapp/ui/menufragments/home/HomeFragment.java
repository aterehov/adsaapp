package ru.anoadsa.adsaapp.ui.menufragments.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.InvocationTargetException;

import ru.anoadsa.adsaapp.databinding.FragmentHomeBinding;
import ru.anoadsa.adsaapp.ui.abstracts.UiMenuFragment;

public class HomeFragment extends UiMenuFragment<HomeViewModel, FragmentHomeBinding> {

//    private FragmentHomeBinding binding;
//    private ViewModel homeViewModel;
//    private View root;
    private TextView textView;

    private Button testButton;


    @Override
    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.initUi(inflater, container);
        textView = binding.textHome;
        testButton = binding.testButton;
    }

    @Override
    protected void configureUiState() {

    }

    @Override
    protected void configureViewModel() {
        setViewModel(HomeViewModel.class);
        super.configureViewModel();
        viewModel.getText().observe(getViewLifecycleOwner(), uiState -> {
            textView.setText(uiState);
        });
    }

    @Override
    protected void configureUiActions() {
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(Intent.ACTION_CALL);
                Intent i = new Intent("com.android.phone.EmergencyDialer.DIAL");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                i.setData(Uri.parse("tel:112"));
                startActivity(i);
            }
        });
    }

    @Override
    protected void configureViewModelActions() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setViewBinding(FragmentHomeBinding.class);
        return super.onCreateView(inflater, container, savedInstanceState);
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