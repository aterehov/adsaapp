package ru.anoadsa.adsaapp.ui.menufragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.time.format.DateTimeFormatter;

import ru.anoadsa.adsaapp.DevSettings;
import ru.anoadsa.adsaapp.MainActivity;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.databinding.FragmentProfileBinding;
import ru.anoadsa.adsaapp.models.data.IncidentDisabilityCategorySprav;
import ru.anoadsa.adsaapp.models.data.User;
import ru.anoadsa.adsaapp.ui.abstracts.UiMenuFragment;
import ru.anoadsa.adsaapp.ui.activities.menu.MenuSharedViewModel;
import ru.anoadsa.adsaapp.ui.views.TitleSpinnerView;
import ru.anoadsa.adsaapp.ui.views.TitleTextInputView;

public class ProfileFragment extends UiMenuFragment<ProfileViewModel, FragmentProfileBinding> {

//    private FragmentHomeBinding binding;
//    private ViewModel homeViewModel;
//    private View root;
//    private TextView textView;


    private NestedScrollView profileMainNestedScrollView;
    private ConstraintLayout profileMainConstraintLayout;
    private int scrollYRestored;

    private TitleTextInputView profileSurnameTTI;
    private TitleTextInputView profileNameTTI;
    private TitleTextInputView profileMidnameTTI;
    private TitleTextInputView profilePhoneTTI;
    private TitleTextInputView profileEmailTTI;
    private TitleTextInputView profileCarBrandTTI;
    private TitleTextInputView profileCarNumberTTI;
    private TitleTextInputView profileOsagoTTI;
    private TitleTextInputView profileSnilsTTI;
    private TitleTextInputView profileMedPolisTTI;
    private TitleTextInputView profileBirthdayTTI;
    // TODO use CalendarView to select birthday
    private CalendarView profileBirthdayCalendarView;
    private TitleTextInputView profilePassword1TTI;
    private TitleTextInputView profilePassword2TTI;
    private Button profilePasswordButton;

    private boolean userUpdatedFromServer = false;

    private MenuSharedViewModel menuSharedVM;
    private ConstraintLayout profileTopCL;

    private Button profileLogoutButton;

    private CheckBox profileOnlySMSCheckbox;
    private TitleSpinnerView profileDisabilityCategoryTSV;

    private boolean inputsSetFromUser;


    private Runnable saveUser = new Runnable() {
        @Override
        public void run() {
            if (profileSurnameTTI.getInputText() == null
                || profileSurnameTTI.getInputText().isEmpty()) {
                Snackbar.make(profileTopCL, "Введите фамилию", Snackbar.LENGTH_LONG).show();
//                return false;
            }
            if (profileEmailTTI.getInputText() != null
                && !profileEmailTTI.getInputText().isEmpty()
                && !profileEmailTTI.getInputText().contains("@")) {
                Snackbar.make(profileTopCL, "Неверный формат E-mail", Snackbar.LENGTH_LONG)
                        .show();
//                return false;
            }
            if (profileCarNumberTTI.getInputText() != null
                && !profileCarNumberTTI.getInputText().isEmpty()
                && !Static.checkCarNumber(profileCarNumberTTI.getInputText())) {
                Snackbar.make(profileTopCL, "Неверный формат номера транспортного средства",
                        Snackbar.LENGTH_LONG).show();
//                return false;
            }
            if (profileOsagoTTI.getInputText() != null
                && !profileOsagoTTI.getInputText().isEmpty()
                && !Static.checkOsago(profileOsagoTTI.getInputText())) {
                Snackbar.make(profileTopCL, "Неверный формат ОСАГО",
                        Snackbar.LENGTH_LONG).show();
//                return false;
            }
            if (profileSnilsTTI.getInputText() != null
                && !profileSnilsTTI.getInputText().isEmpty()
                && !Static.checkSnils(profileSnilsTTI.getInputText())) {
                Snackbar.make(profileTopCL, "Неверный формат СНИЛС",
                        Snackbar.LENGTH_LONG).show();
//                return false;
            }
            if (profileMedPolisTTI.getInputText() != null
                && !profileMedPolisTTI.getInputText().isEmpty()
                && !Static.checkMedPolis(profileMedPolisTTI.getInputText())) {
                Snackbar.make(profileTopCL, "Неверный формат медицинского полиса",
                        Snackbar.LENGTH_LONG).show();
//                return false;
            }
            if (profileBirthdayTTI.getInputText() != null
                && !profileBirthdayTTI.getInputText().isEmpty()
                && !Static.checkDateString(profileBirthdayTTI.getInputText())) {
                Snackbar.make(profileTopCL, "Формат даты рождения: ДД.ММ.ГГГГ",
                        Snackbar.LENGTH_LONG).show();
//                return false;
            }
            viewModel.sendUserToServer(
                    profileSurnameTTI.getInputText(),
                    profileNameTTI.getInputText(),
                    profileMidnameTTI.getInputText(),
                    profileEmailTTI.getInputText(),
                    profileCarBrandTTI.getInputText(),
                    profileCarNumberTTI.getInputText(),
                    profileOsagoTTI.getInputText(),
                    profileSnilsTTI.getInputText(),
                    profileMedPolisTTI.getInputText(),
                    Static.makeLocalDate(profileBirthdayTTI.getInputText()),
                    profileDisabilityCategoryTSV.getSelection(),
                    profileOnlySMSCheckbox.isChecked()
            );
//            return false;
        }
    };

    @Override
    protected void initUi(@NonNull LayoutInflater inflater, ViewGroup container) {
        super.initUi(inflater, container);
//        textView = binding.textHome;
        // setContentView?
        profileMainNestedScrollView = binding.profileMainNestedScrollView;
        profileMainConstraintLayout = binding.profileMainConstraintLayout;

        profileSurnameTTI = binding.profileSurnameTTI;
        profileNameTTI = binding.profileNameTTI;
        profileMidnameTTI = binding.profileMidnameTTI;
        profilePhoneTTI = binding.profilePhoneTTI;
        profileEmailTTI = binding.profileEmailTTI;
        profileCarBrandTTI = binding.profileArBrandTTI;
        profileCarNumberTTI = binding.profileCarNumberTTI;
        profileOsagoTTI = binding.profileOsagoTTI;
        profileSnilsTTI = binding.profileSnilsTTI;
        profileMedPolisTTI = binding.profileMedPolisTTI;
        profileBirthdayTTI = binding.profileBirthdayTTI;
//        profileBirthdayCalendarView = binding.profileBirthdayCalendarView;
        profilePassword1TTI = binding.profilePassword1TTI;
        profilePassword2TTI = binding.profilePassword2TTI;
        profilePasswordButton = binding.profilePasswordButton;

        profileTopCL = binding.profileTopCL;

        profileLogoutButton = binding.profileLogoutButton;

        profileOnlySMSCheckbox = binding.profileOnlySMSCheckbox;
        profileDisabilityCategoryTSV = binding.profileDisabilityCategoryTSV;
    }

    @Override
    protected void configureUiState() {
        profilePhoneTTI.setTTIEnabled(false);
        profileCarNumberTTI.setInputFilter(Static.carNumberInputFilter);
        profileOsagoTTI.setInputFilter(Static.osagoInputFilter);
        profileSnilsTTI.setInputFilter(Static.snilsInputFilter);
        profileMedPolisTTI.setInputFilter(Static.medPolisInputFilter);
        profileBirthdayTTI.setInputFilter(Static.birthdayInputFilter);

        // This fixes TTI not replacing password symbols with dots
        profilePassword1TTI.getEditText().setInputType(129);
        profilePassword2TTI.getEditText().setInputType(129);
    }

    @Override
    protected void configureViewModel() {
//        setViewModel(ProfileViewModel.class);
        super.configureViewModel();
//        viewModel.getText().observe(getViewLifecycleOwner(), uiState -> {
//            textView.setText(uiState);
//        });
    }

    @Override
    protected void configureUiActions() {
        profileSurnameTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);
        profileNameTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);
        profileMidnameTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);
        profileEmailTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);
        profileCarBrandTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);
        profileCarNumberTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);
        profileOsagoTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);
        profileSnilsTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);
        profileMedPolisTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);
        profileBirthdayTTI.setOnFocusChangeListenerTTI(TitleTextInputView.doOnTextChangedOnlyIfHasFocus);

        profileSurnameTTI.doOnTextChanged(null, 2500, saveUser);
        profileNameTTI.doOnTextChanged(null, 2500, saveUser);
        profileMidnameTTI.doOnTextChanged(null, 2500, saveUser);
        profileEmailTTI.doOnTextChanged(null, 2500, saveUser);
        profileCarBrandTTI.doOnTextChanged(null, 2500, saveUser);
        profileCarNumberTTI.doOnTextChanged(null, 2500, saveUser);
        profileOsagoTTI.doOnTextChanged(null, 2500, saveUser);
        profileSnilsTTI.doOnTextChanged(null, 2500, saveUser);
        profileMedPolisTTI.doOnTextChanged(null, 2500, saveUser);
        profileBirthdayTTI.doOnTextChanged(null, 2500, saveUser);

        profileSurnameTTI.setDoOnTextChangedEnabled(false);
        profileNameTTI.setDoOnTextChangedEnabled(false);
        profileMidnameTTI.setDoOnTextChangedEnabled(false);
        profileEmailTTI.setDoOnTextChangedEnabled(false);
        profileCarBrandTTI.setDoOnTextChangedEnabled(false);
        profileCarNumberTTI.setDoOnTextChangedEnabled(false);
        profileOsagoTTI.setDoOnTextChangedEnabled(false);
        profileSnilsTTI.setDoOnTextChangedEnabled(false);
        profileMedPolisTTI.setDoOnTextChangedEnabled(false);
        profileBirthdayTTI.setDoOnTextChangedEnabled(false);

        profileOnlySMSCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            private boolean ran;

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (!ran) {
//                    ran = true;
//                    return;
//                }
                if (inputsSetFromUser) {
                    saveUser.run();
                }
            }
        });

        profileDisabilityCategoryTSV.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean ran;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (inputsSetFromUser) {
                    if (!ran) {
                        ran = true;
                        return;
                    }
//                if (inputsSetFromUser) {
                    saveUser.run();
//                }
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        profilePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (profilePassword1TTI.getInputText() != null
                    && profilePassword2TTI.getInputText() != null) {
                    if (profilePassword1TTI.getInputText().length() < 10) {
                        Snackbar.make(
                                profileTopCL,
                                "Пароль должен содержать не менее 10 символов",
                                Snackbar.LENGTH_LONG
                        ).show();
                        return;
                    }
                    if (!profilePassword1TTI.getInputText().equals(profilePassword2TTI.getInputText())) {
                        Snackbar.make(
                                profileTopCL,
                                "Пароли не совпадают",
                                Snackbar.LENGTH_LONG
                        ).show();
                        return;
                    }
                    profilePasswordButton.setEnabled(false);
                    viewModel.changeUserPassword(profilePassword1TTI.getInputText());
                }
            }
        });

        profileLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileLogoutButton.setEnabled(false);
                viewModel.logout();
            }
        });

        profileMainNestedScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean ran;

            @Override
            public void onGlobalLayout() {
                if (wasRestored && !ran) {
                    ran = true;
                    profileMainNestedScrollView.scrollTo(
                            0,
                            Integer.max(
                                    0,
                                    Integer.min(
                                            scrollYRestored,
                                            profileMainConstraintLayout.getHeight()
                                                    - profileMainNestedScrollView.getHeight()
                                    )
                            )
                    );
                }
            }
        });
    }

    @Override
    protected void configureViewModelActions() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null) {
                    Snackbar.make(profileTopCL, error, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (inputsSetFromUser) {
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
                if (DevSettings.HIDE_DUMMY_EMAIL && user.getEmail().endsWith("@localhost")) {
                    profileEmailTTI.setInputText("");
                } else {
                    profileEmailTTI.setInputText(user.getEmail());
                }
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

                profileOnlySMSCheckbox.setChecked(user.getOnlySMS());
                if (!profileDisabilityCategoryTSV.getItems().contains(user.getDisabilityCategory())) {
                    profileDisabilityCategoryTSV.addItem(user.getDisabilityCategory());
                }
                profileDisabilityCategoryTSV.setSelectedItem(user.getDisabilityCategory());

                inputsSetFromUser = true;


            }
        });

        viewModel.getResetMenuValues().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean reset) {
                if (reset) {
                    menuSharedVM.setUserName(Static.makeFullName(
                            viewModel.getUser().getValue().getSurname(),
                            viewModel.getUser().getValue().getName(),
                            viewModel.getUser().getValue().getMidname()
                    ));
                    menuSharedVM.setUserPhone(viewModel.getUser().getValue().getPhone());
                }
            }
        });

        viewModel.getEnableChangePassword().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean enable) {
                if (enable) {
                    profilePasswordButton.setEnabled(true);
                }
            }
        });

        viewModel.getPasswordSuccessfullyChanged().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            private boolean ran;

            @Override
            public void onChanged(Boolean success) {
                if (wasRestored && !ran) {
                    ran = true;
                    return;
                }
                if (success) {
                    profilePassword1TTI.setInputText("");
                    profilePassword2TTI.setInputText("");
                }
            }
        });

        viewModel.getLogoutSuccessful().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                } else {
                    profileLogoutButton.setEnabled(true);
                }
            }
        });

        viewModel.getDisabilityCategorySprav().observe(getViewLifecycleOwner(), new Observer<IncidentDisabilityCategorySprav>() {
            @Override
            public void onChanged(IncidentDisabilityCategorySprav d) {
                if (d == null) {
                    viewModel.loadDisabilityCategorySprav();
                    return;
                }
                profileDisabilityCategoryTSV.setItems(d.getNames());
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setViewModel(ProfileViewModel.class);
        setViewBinding(FragmentProfileBinding.class);
        super.onCreateView(inflater, container, savedInstanceState);
        menuSharedVM = new ViewModelProvider(requireActivity()).get(MenuSharedViewModel.class);

//        final TextView tv = root.findViewById(R.id.text_home);
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("inputsSetFromUser", inputsSetFromUser);
        outState.putString("surname", profileSurnameTTI.getInputText());
        outState.putString("name", profileNameTTI.getInputText());
        outState.putString("midname", profileMidnameTTI.getInputText());
        outState.putString("phone", profilePhoneTTI.getInputText());
        outState.putString("email", profileEmailTTI.getInputText());
        outState.putString("carBrand", profileCarBrandTTI.getInputText());
        outState.putString("carNumber", profileCarNumberTTI.getInputText());
        outState.putString("osago", profileOsagoTTI.getInputText());
        outState.putString("snils", profileSnilsTTI.getInputText());
        outState.putString("medPolis", profileMedPolisTTI.getInputText());
        outState.putString("birthday", profileBirthdayTTI.getInputText());
        outState.putString("password1", profilePassword1TTI.getInputText());
        outState.putString("password2", profilePassword2TTI.getInputText());
        outState.putInt("scrollY", profileMainNestedScrollView.getScrollY());

        outState.putString("disabilityCategory", profileDisabilityCategoryTSV.getSelection());
        outState.putBoolean("onlySMS", profileOnlySMSCheckbox.isChecked());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        inputsSetFromUser = savedInstanceState.getBoolean("inputsSetFromUser");
        profileSurnameTTI.setInputText(savedInstanceState.getString("surname"));
        profileNameTTI.setInputText(savedInstanceState.getString("name"));
        profileMidnameTTI.setInputText(savedInstanceState.getString("midname"));
        profilePhoneTTI.setInputText(savedInstanceState.getString("phone"));
        profileEmailTTI.setInputText(savedInstanceState.getString("email"));
        profileCarBrandTTI.setInputText(savedInstanceState.getString("carBrand"));
        profileCarNumberTTI.setInputText(savedInstanceState.getString("carNumber"));
        profileOsagoTTI.setInputText(savedInstanceState.getString("osago"));
        profileSnilsTTI.setInputText(savedInstanceState.getString("snils"));
        profileMedPolisTTI.setInputText(savedInstanceState.getString("medPolis"));
        profileBirthdayTTI.setInputText(savedInstanceState.getString("birthday"));
        profilePassword1TTI.setInputText(savedInstanceState.getString("password1"));
        profilePassword2TTI.setInputText(savedInstanceState.getString("password2"));
        scrollYRestored = savedInstanceState.getInt("scrollY");

        if (!profileDisabilityCategoryTSV.getItems().contains(
                savedInstanceState.getString("disabilityCategory")
        )) {
            profileDisabilityCategoryTSV.addItem(
                    savedInstanceState.getString("disabilityCategory")
            );
        }
        profileDisabilityCategoryTSV.setSelectedItem(
                savedInstanceState.getString("disabilityCategory")
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.clearErrorMessage();
    }
}