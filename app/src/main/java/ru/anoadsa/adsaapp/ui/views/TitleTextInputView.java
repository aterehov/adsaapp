package ru.anoadsa.adsaapp.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.autofill.AutofillValue;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;

public class TitleTextInputView extends ConstraintLayout {
    private String titleText;
    private String inputText;
    private String autofillHints;
    private String inputHint;
    private String inputType;
    private boolean singleLine;
    private boolean enabled;
    private int minLines;
    private String digits;
    private String listenerType;

    private DigitsKeyListener listener;

    private InputFilter inputFilter;

    private TextView textView;
    private EditText editText;

    private OnFocusChangeListener onFocusChangeListener;

    private TextWatcher textWatcher;
    private boolean doOnTextChangedEnabled;

    public TitleTextInputView(Context context) {
        this(context, null);
    }

    public TitleTextInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TitleTextInputView,
                0,
                0
        );

        try {
            titleText = a.getString(R.styleable.TitleTextInputView_titleText);
            inputText = a.getString(R.styleable.TitleTextInputView_inputText);
            autofillHints = a.getString(R.styleable.TitleTextInputView_autofillHints);
            inputHint = a.getString(R.styleable.TitleTextInputView_inputHint);
            inputType = a.getString(R.styleable.TitleTextInputView_inputType);
            singleLine = a.getBoolean(R.styleable.TitleTextInputView_singleLine, true);
            enabled = a.getBoolean(R.styleable.TitleTextInputView_enabled, true);
            minLines = a.getInteger(R.styleable.TitleTextInputView_minLines, 1);
            digits = a.getString(R.styleable.TitleTextInputView_digitsTTIV);
        } finally {
            a.recycle();
        }

        LayoutInflater.from(context).inflate(R.layout.view_title_text_input, this);

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        textView.setId(View.generateViewId());
        editText.setId(View.generateViewId());

        LayoutParams lp = (LayoutParams) editText.getLayoutParams();
        lp.leftToLeft = textView.getId();
        lp.topToBottom = textView.getId();
        editText.setLayoutParams(lp);

        textView.setText(titleText);
        editText.setText(inputText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editText.setAutofillHints(autofillHints);
        }
        editText.setHint(inputHint);
        editText.setInputType(Static.inputTypeStringConverter(inputType));
        editText.setSingleLine(singleLine);
        editText.setEnabled(enabled);
        if (!singleLine) {
            editText.setMinLines(minLines);
        } else {
            editText.setMaxLines(1);
        }

        if (digits != null) {
            editText.setKeyListener(DigitsKeyListener.getInstance(digits));
        }
//        if (digits != null) {
//            switch (listenerType) {
//                case "text":
//                    listener = TextKeyListener.getInstance();
//            }
//        }

    }

    public static View.OnFocusChangeListener doOnTextChangedOnlyIfHasFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(@NonNull View view, boolean hasFocus) {
            //                    incidentOrganisationTSV.setEnabledTSV(false);
            //                    searchOrganisations();
            TitleTextInputView tti = (TitleTextInputView) view.getParent().getParent();
            if (tti.isTTIEnabled()) {
                tti.setDoOnTextChangedEnabled(hasFocus);
            }
        }
    };

    public void setDoOnTextChangedEnabled(boolean enabled) {
        this.doOnTextChangedEnabled = enabled;
    }

    public void doOnTextChanged(Runnable taskOnChange, long delay, Runnable taskAfterDelay) {
        doOnTextChangedEnabled = true;
        setOnTextChangedListener(new TextWatcher() {
//            private boolean isTyping = false;
            private Timer timer = new Timer();
//            private long delay = inputDelay;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                if (!isTyping) {
//                    isTyping = true;
//                }
                if (doOnTextChangedEnabled) {
                    if (taskOnChange != null) {
                        taskOnChange.run();
                    }
                    if (taskAfterDelay != null) {
                        timer.cancel();
                        timer = new Timer();
    //                timer.schedule(task, inputDelay);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                taskAfterDelay.run();
//                                return false;
                            }
                        }, delay);
                    }
                }
            }
        });
    }

    public void setOnTextChangedListener(TextWatcher textWatcher) {
        editText.removeTextChangedListener(this.textWatcher);
        this.textWatcher = textWatcher;
        editText.addTextChangedListener(textWatcher);
        invalidate();
        requestLayout();
    }

    public void setOnFocusChangeListenerTTI(OnFocusChangeListener onFocusChangeListener) {
        this.onFocusChangeListener = onFocusChangeListener;
        editText.setOnFocusChangeListener(onFocusChangeListener);
        invalidate();
        requestLayout();
    }

    public void setInputFilter(InputFilter inputFilter) {
        this.inputFilter = inputFilter;
        editText.setFilters(new InputFilter[]{inputFilter});
        invalidate();
        requestLayout();
    }

    public int getMinLines() {
        return minLines;
    }

    public void setMinLines(int minLines) {
        this.minLines = minLines;
        editText.setMinLines(minLines);
        invalidate();
        requestLayout();
    }

    //https://stackoverflow.com/questions/47863889/autofill-for-credit-card-expiry-date-how-to-use-4-digit-year
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int getAutofillType() {
        if (inputType != null && inputType.contains("date")) {
            return AUTOFILL_TYPE_DATE;
        }
        return editText.getAutofillType();
    }

    //https://stackoverflow.com/questions/47863889/autofill-for-credit-card-expiry-date-how-to-use-4-digit-year
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public AutofillValue getAutofillValue() {
        if (inputType != null && inputType.contains("date")) {
            AutofillValue autofillValue = super.getAutofillValue();
            if (autofillValue == null) {
                return null;
            }
            Date date = Static.tryParseDate(autofillValue.getTextValue().toString());
            return date != null ? AutofillValue.forDate(date.getTime()) : autofillValue;
        }
        return editText.getAutofillValue();
    }

    //https://stackoverflow.com/questions/47863889/autofill-for-credit-card-expiry-date-how-to-use-4-digit-year
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void autofill(@NonNull AutofillValue value) {
        if (inputType != null && inputType.contains("date")) {
            if (!value.isDate()) {
//            Timber.w(value + " could not be autofilled into " + this);
                return;
            }

            long autofilledValue = value.getDateValue();

            // First autofill it...
//            setInputText(Static.formatDate(new Date(autofilledValue)));
            setInputText(Static.dateFormat.format(new Date(autofilledValue)));
            // ...then move cursor to the end.
            final CharSequence text = getInputText();
            if ((text != null)) {
                editText.setSelection(text.length());
            }
        }
        editText.autofill(value);
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
        textView.setText(titleText);
        invalidate();
        requestLayout();
    }

    public String getInputText() {
        inputText = editText.getText().toString();
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
        editText.setText(inputText);
        invalidate();
        requestLayout();
    }

    public String getTTIAutofillHints() {
        return autofillHints;
    }

    public void setAutofillHints(String autofillHints) {
        this.autofillHints = autofillHints;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editText.setAutofillHints(autofillHints);
        }
        invalidate();
        requestLayout();
    }

    public String getInputHint() {
        return inputHint;
    }

    public void setInputHint(String inputHint) {
        this.inputHint = inputHint;
        editText.setHint(inputHint);
        invalidate();
        requestLayout();
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
        editText.setInputType(Static.inputTypeStringConverter(inputType));
        invalidate();
        requestLayout();
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
        editText.setSingleLine(singleLine);
        invalidate();
        requestLayout();
    }


    public boolean isTTIEnabled() {
        return enabled;
    }

    public void setTTIEnabled(boolean enabled) {
        this.enabled = enabled;
        editText.setEnabled(enabled);
        invalidate();
        requestLayout();
    }

    public TextView getTextView() {
        return textView;
    }

//    public void setTextView(TextView textView) {
//        this.textView = textView;
//    }

    public EditText getEditText() {
        return editText;
    }

//    public void setEditText(EditText editText) {
//        this.editText = editText;
//    }
}
