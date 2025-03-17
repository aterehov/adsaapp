package ru.anoadsa.adsaapp.ui.activities.smscode;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;

public class SmsCodeActivity extends UiActivity<EmptyViewModel> {

    private ConstraintLayout mainConstraintLayout;
    private TextView label;
    private EditText inputCode;
    private Button button;
    private Button backButton;



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
    }

    @Override
    protected void configureUiState() {

    }

    @Override
    protected void configureUiActions() {
        backButton.setOnClickListener(backButtonOnClickListener);
    }
}
