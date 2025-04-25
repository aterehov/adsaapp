package ru.anoadsa.adsaapp.ui.activities.registration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.Contract;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;
import ru.anoadsa.adsaapp.ui.activities.smscode.SmsCodeActivity;

public class RegistrationActivity extends UiActivity<RegistrationViewModel> {

    private ConstraintLayout mainConstraintLayout;
    private ConstraintLayout phoneConstraintLayout;
    private EditText inputPhone;
    private TextView inputPhoneHint;
    private TextView label;
    private EditText inputPassword1;
    private EditText inputPassword2;
    private EditText inputSurname;
    private EditText inputName;
    private EditText inputPatronymic;
    private EditText inputEmail;
    private Button button;
    private Button loginButton;
    private Button enterCodeButton;

    private Snackbar errorSnackbar;

    private ActivityResultLauncher<Bundle> smsCodeActivityLauncher;

//    private ActivityResultContract<String, Boolean> smsCodeActivityContract = new ActivityResultContract<String, Boolean>() {
//        @NonNull
//        @Override
//        public Intent createIntent(@NonNull Context context, String action) {
//            Intent i = new Intent(context, SmsCodeActivity.class);
//            i.putExtra("action", action);
//            return i;
//        }
//
//        @Nullable
//        @Contract(pure = true)
//        @Override
//        public Boolean parseResult(int code, @Nullable Intent intent) {
//            return code == RESULT_OK;
//        }
//    };

    private ActivityResultCallback<Boolean> smsCodeActivityCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(@NonNull Boolean success) {
            // TODO
            System.out.println("TODO");
            if (success) {
                finish();
            } else {
                button.setEnabled(true);
            }
        }
    };

    private View.OnClickListener loginButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO registration actions
            smsCodeActivityLauncher.launch(null);
        }
    };

    protected void initUi() {
        setContentView(R.layout.activity_registration);
        mainConstraintLayout = findViewById(R.id.registrationMainConstraintLayout);
        phoneConstraintLayout = findViewById(R.id.registrationPhoneConstraintLayout);
        inputPhone = findViewById(R.id.registrationInputPhone);
        inputPhoneHint = findViewById(R.id.registrationInputPhoneHint);
        label = findViewById(R.id.registrationLabel);
        inputPassword1 = findViewById(R.id.registrationInputPassword1);
        inputPassword2 = findViewById(R.id.registrationInputPassword2);
        button = findViewById(R.id.registrationButton);
        loginButton = findViewById(R.id.registrationLoginButton);

        inputSurname = findViewById(R.id.registrationInputSurname);
        inputName = findViewById(R.id.registrationInputName);
        inputPatronymic = findViewById(R.id.registrationInputPatronymic);
        inputEmail = findViewById(R.id.registrationInputEmail);

        enterCodeButton = findViewById(R.id.registrationEnterCodeButton);
    }

    protected void configureUiState() {

    }

    protected void configureUiActions() {
//        loginButton.setOnClickListener(loginButtonOnClickListener);
//        button.setOnClickListener(buttonOnClickListener);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This should return to login activity launched before this activity
                finish();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inputPassword1.getText().toString().equals(inputPassword2.getText().toString())) {
                    viewModel.setErrorMessage("Пароли не совпадают");
                    return;
                }
                // TODO check if phone is correct
                button.setEnabled(false);
                viewModel.register(
                        inputPhone.getText().toString(),
                        inputPassword1.getText().toString(),
                        inputSurname.getText().toString(),
                        inputName.getText().toString(),
                        inputPatronymic.getText().toString(),
                        inputEmail.getText().toString()
                );
            }
        });

        enterCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputPhone.getText().toString() == null || inputPhone.getText().toString().isEmpty()) {
                    viewModel.setErrorMessage("Введите телефон");
                    return;
                }
                if (errorSnackbar != null) {
                    errorSnackbar.dismiss();
                }
                Bundle b = new Bundle();
                b.putString("action", "registration");
                b.putString("phone", inputPhone.getText().toString());
                smsCodeActivityLauncher.launch(b);
            }
        });
        // TODO configure phone and email format check
    }

    @Override
    protected void configureViewModelActions() {
        viewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null && !error.isEmpty()) {
                    if (errorSnackbar != null) {
                        errorSnackbar.dismiss();
                    }
                    errorSnackbar = Snackbar.make(
//                            getCurrentFocus(),
                            mainConstraintLayout,
                            error,
                            Snackbar.LENGTH_INDEFINITE);
                    errorSnackbar.show();
                }
            }
        });

        viewModel.getRegisteredSuccessful().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    if (errorSnackbar != null) {
                        errorSnackbar.dismiss();
                    }
                    Bundle b = new Bundle();
                    b.putString("action", "registration");
                    b.putString("phone", inputPhone.getText().toString());
                    smsCodeActivityLauncher.launch(b);
                } else {
                    button.setEnabled(true);
                }
            }
        });
    }

    private void configureActivityLaunchers() {
        smsCodeActivityLauncher = registerForActivityResult(SmsCodeActivity.getARC(), smsCodeActivityCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setViewModel(RegistrationViewModel.class);
        configureActivityLaunchers();
        super.onCreate(savedInstanceState);

    }
}
