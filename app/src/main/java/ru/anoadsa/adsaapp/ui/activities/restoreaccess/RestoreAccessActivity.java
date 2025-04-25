package ru.anoadsa.adsaapp.ui.activities.restoreaccess;

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

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.Contract;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;
import ru.anoadsa.adsaapp.ui.activities.smscode.SmsCodeActivity;

public class RestoreAccessActivity extends UiActivity<EmptyViewModel> {

    private ConstraintLayout mainConstraintLayout;
    private ConstraintLayout phoneConstraintLayout;
    private EditText inputPhone;
    private TextView inputPhoneHint;
    private TextView label;
    private EditText inputPassword1;
    private EditText inputPassword2;
    private Button button;
    private Button backButton;

    private Snackbar errorSnackbar;

    private ActivityResultLauncher<Bundle> smsCodeActivityLauncher;

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
        public void onActivityResult(@NonNull Boolean success) {
            System.out.println("TODO");
            if (success) {
                finish();
            } else {
                button.setEnabled(true);
            }
        }
    };

    private View.OnClickListener backButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO restore access actions
            smsCodeActivityLauncher.launch(null);
        }
    };

    private void showErrorMessage(String error) {
        if (errorSnackbar != null) {
            errorSnackbar.dismiss();
        }
        if (error != null && !error.isEmpty()) {
            errorSnackbar = Snackbar.make(
//                    getCurrentFocus(),
                    mainConstraintLayout,
                    error,
                    Snackbar.LENGTH_INDEFINITE
            );
            errorSnackbar.show();
        }
    }

    @Override
    protected void initUi() {
        setContentView(R.layout.activity_restore_access);
        mainConstraintLayout = findViewById(R.id.restoreAccessMainConstraintLayout);
        phoneConstraintLayout = findViewById(R.id.restoreAccessPhoneConstraintLayout);
        inputPhone = findViewById(R.id.restoreAccessInputPhone);
        inputPhoneHint = findViewById(R.id.restoreAccessInputPhoneHint);
        label = findViewById(R.id.restoreAccessLabel);
        inputPassword1 = findViewById(R.id.restoreAccessInputPassword1);
        inputPassword2 = findViewById(R.id.restoreAccessInputPassword2);
        button = findViewById(R.id.restoreAccessButton);
        backButton = findViewById(R.id.restoreAccessBackButton);
    }

    @Override
    protected void configureUiState() {

    }

    @Override
    protected void configureUiActions() {
        backButton.setOnClickListener(backButtonOnClickListener);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO check phone format
                if (inputPhone.getText().toString() == null || inputPhone.getText().toString().isEmpty()) {
                    showErrorMessage("Введите номер телефона");
                    return;
                }
                if (!Static.checkPhone(inputPhone.getText().toString())) {
                    showErrorMessage("Неверный формат номера телефона");
                    return;
                }
                if (inputPassword1.getText().toString() == null || inputPassword1.getText().toString().length() < 10) {
                    showErrorMessage("Пароль должен содержать не менее 10 символов");
                    return;
                }
                if (inputPassword2.getText().toString() == null
                    || !inputPassword2.getText().toString().equals(
                            inputPassword1.getText().toString()
                        )
                ) {
                    showErrorMessage("Пароли не совпадают");
                    return;
                }
                button.setEnabled(false);
                Bundle b = new Bundle();
                b.putString("action", "resetPassword");
                b.putString("phone", inputPhone.getText().toString());
                b.putString("password", inputPassword1.getText().toString());
                smsCodeActivityLauncher.launch(b);
            }
        });
    }

    @Override
    protected void configureViewModelActions() {

    }

    private void configureActivityLaunchers() {
        smsCodeActivityLauncher = registerForActivityResult(SmsCodeActivity.getARC(), smsCodeActivityCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        configureActivityLaunchers();
        super.onCreate(savedInstanceState);
    }
}
