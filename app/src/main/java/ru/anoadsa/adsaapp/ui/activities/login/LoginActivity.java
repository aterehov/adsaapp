package ru.anoadsa.adsaapp.ui.activities.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.jetbrains.annotations.Contract;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;
import ru.anoadsa.adsaapp.ui.activities.registration.RegistrationActivity;
import ru.anoadsa.adsaapp.ui.activities.restoreaccess.RestoreAccessActivity;

public class LoginActivity extends UiActivity<EmptyViewModel> {
    private ConstraintLayout mainConstraintLayout;
    private ConstraintLayout phoneConstraintLayout;
    private EditText inputPhone;
    private TextView inputPhoneHint;
    private TextView label;
    private EditText inputPassword;
    private Button button;
    private Button restoreAccessButton;
    private Button registerButton;

    private ActivityResultLauncher<Object> registrationActivityLauncher;

    private ActivityResultContract<Object, Object> registrationActivityContract = new ActivityResultContract<Object, Object>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Object o) {
            return new Intent(context, RegistrationActivity.class);
        }

        @Nullable
        @Contract(pure = true)
        @Override
        public Object parseResult(int i, @Nullable Intent intent) {
            return null;
        }
    };

    private ActivityResultCallback<Object> registrationActivityCallback = new ActivityResultCallback<Object>() {
        @Override
        public void onActivityResult(Object o) {
            System.out.println("TODO");
        }
    };

    private View.OnClickListener registerButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
//            registrationActivityLauncher.launch(null);
        }
    };

    private View.OnClickListener restoreAccessButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(LoginActivity.this, RestoreAccessActivity.class));
        }
    };

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO login actions
        }
    };

    private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            moveTaskToBack(true);
        }
    };

    @Override
    protected void initUi() {
        setContentView(R.layout.activity_login);
        mainConstraintLayout = findViewById(R.id.loginMainConstraintLayout);
        phoneConstraintLayout = findViewById(R.id.loginPhoneConstraintLayout);
        inputPhone = findViewById(R.id.loginInputPhone);
        inputPhoneHint = findViewById(R.id.loginInputPhoneHint);
        label = findViewById(R.id.loginLabel);
        inputPassword = findViewById(R.id.loginInputPassword);
        button = findViewById(R.id.loginButton);
        restoreAccessButton = findViewById(R.id.loginRestoreAccessButton);
        registerButton = findViewById(R.id.loginRegisterButton);
    }

    @Override
    protected void configureUiState() {

    }

    @Override
    protected void configureUiActions() {
        registerButton.setOnClickListener(registerButtonOnClickListener);
        restoreAccessButton.setOnClickListener(restoreAccessButtonOnClickListener);
        button.setOnClickListener(buttonOnClickListener);
    }

    private void configureBackButtonAction() {
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void configureActivityLaunchers() {
        registrationActivityLauncher = registerForActivityResult(registrationActivityContract, registrationActivityCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        configureActivityLaunchers();
        super.onCreate(savedInstanceState);
        configureBackButtonAction();
    }
}
