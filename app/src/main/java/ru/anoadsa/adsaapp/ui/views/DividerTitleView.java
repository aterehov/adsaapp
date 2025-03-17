package ru.anoadsa.adsaapp.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import ru.anoadsa.adsaapp.R;

public class DividerTitleView extends ConstraintLayout {
    private String text;

    private TextView textView;

    public DividerTitleView(Context context) {
        this(context, null);
    }

    public DividerTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DividerTitleView,
                0,
                0
        );

        try {
            text = a.getString(R.styleable.DividerTitleView_text);
        } finally {
            a.recycle();
        }

        LayoutInflater.from(context).inflate(R.layout.view_divider_title, this);

        textView = findViewById(R.id.textView);
        textView.setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        textView.setText(text);
        invalidate();
        requestLayout();
    }

    public TextView getTextView() {
        return textView;
    }

//    public void setTextView(TextView textView) {
//        this.textView = textView;
//    }
}
