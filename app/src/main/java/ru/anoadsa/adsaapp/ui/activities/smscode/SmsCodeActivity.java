package ru.anoadsa.adsaapp.ui.activities.smscode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.Contract;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;

public class SmsCodeActivity extends UiActivity<SmsCodeViewModel> {
    private String action;
    private String phone;
    private String password;

    private ConstraintLayout mainConstraintLayout;
    private TextView label;
    private EditText inputCode;
    private Button button;
    private Button backButton;
    private Button resendButton;

    private Snackbar errorSnackbar;

    private boolean firstResume = true;

    private static ActivityResultContract<Bundle, Boolean> activityResultContract = new ActivityResultContract<Bundle, Boolean>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Bundle bundle) {
            Intent i = new Intent(context, SmsCodeActivity.class);
//            i.putExtra("action", action);
            i.putExtras(bundle);
            return i;
        }

        @Nullable
        @Contract(pure = true)
        @Override
        public Boolean parseResult(int code, @Nullable Intent intent) {
            return code == RESULT_OK;
        }
    };

    public static ActivityResultContract<Bundle, Boolean> getARC() {
        return activityResultContract;
    }

    private View.OnClickListener backButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO registration actions
        }
    };

    @Override
    protected void initUi() {
        setContentView(R.layout.activity_sms_code);
        mainConstraintLayout = findViewById(R.id.smsCodeMainConstraintLayout);
        label = findViewById(R.id.smsCodeLabel);
        inputCode = findViewById(R.id.smsCodeInputCode);
        button = findViewById(R.id.smsCodeButton);
        backButton = findViewById(R.id.smsCodeBackButton);
        resendButton = findViewById(R.id.smsCodeResendButton);
    }

    @Override
    protected void configureUiState() {

    }

    @Override
    protected void configureUiActions() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendButton.setEnabled(false);
                if (action.equals("registration")) {
                    viewModel.resendRegistrationCode(phone);
                } else if (action.equals("resetPassword")) {
                    viewModel.sendPasswordResetCode(phone);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setEnabled(false);
                if (action.equals("registration")) {
                    viewModel.confirmRegistration(phone, inputCode.getText().toString());
                } else if (action.equals("resetPassword")) {
                    viewModel.resetPassword(phone, password, inputCode.getText().toString());
                }
            }
        });
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
                            Snackbar.LENGTH_INDEFINITE
                    );
                    errorSnackbar.show();
                }
            }
        });

        viewModel.getResendSuccessful().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    // ?
                }
                resendButton.setEnabled(true);
            }
        });

        viewModel.getActionSuccessful().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    setResult(RESULT_OK);
                    finish();
                }
                button.setEnabled(true);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        action = getIntent().getStringExtra("action");
        phone = getIntent().getStringExtra("phone");
        password = getIntent().getStringExtra("password");

        setViewModel(SmsCodeViewModel.class);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!firstResume) {
            return;
        }
        firstResume = false;
        if (action.equals("registration")) {

        } else if (action.equals("resetPassword")) {
            viewModel.sendPasswordResetCode(phone);
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
