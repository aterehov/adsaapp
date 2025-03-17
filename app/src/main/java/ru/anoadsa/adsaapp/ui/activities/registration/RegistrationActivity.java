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

import org.jetbrains.annotations.Contract;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;
import ru.anoadsa.adsaapp.ui.activities.smscode.SmsCodeActivity;

public class RegistrationActivity extends UiActivity<EmptyViewModel> {

    private ConstraintLayout mainConstraintLayout;
    private ConstraintLayout phoneConstraintLayout;
    private EditText inputPhone;
    private TextView inputPhoneHint;
    private TextView label;
    private EditText inputPassword1;
    private EditText inputPassword2;
    private Button button;
    private Button loginButton;

    private ActivityResultLauncher<Object> smsCodeActivityLauncher;

    private ActivityResultContract<Object, Boolean> smsCodeActivityContract = new ActivityResultContract<Object, Boolean>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Object o) {
            return new Intent(context, SmsCodeActivity.class);
        }

        @Nullable
        @Contract(pure = true)
        @Override
        public Boolean parseResult(int i, @Nullable Intent intent) {
            return null;
        }
    };

    private ActivityResultCallback<Boolean> smsCodeActivityCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            System.out.println("TODO");
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
    }

    protected void configureUiState() {

    }

    protected void configureUiActions() {
        loginButton.setOnClickListener(loginButtonOnClickListener);
        button.setOnClickListener(buttonOnClickListener);
    }

    private void configureActivityLaunchers() {
        smsCodeActivityLauncher = registerForActivityResult(smsCodeActivityContract, smsCodeActivityCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        configureActivityLaunchers();
        super.onCreate(savedInstanceState);

    }
}
