package ru.anoadsa.adsaapp.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;

public class AttachedImageView extends ConstraintLayout {
    private String text;

    private TextView textView;
    private Spinner spinner;

    private int adapterView;
    private int dropDownView;

    private boolean enabled;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> items;

    // -----------
    private ImageView imageView;
    private TextView fileName;
    private Button downloadButton;
    private Button removeButton;

    public AttachedImageView(Context context) {
        this(context, null);
    }

    public AttachedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AttachedImageView,
                0,
                0
        );

        try {
//            text = a.getString(R.styleable.TitleSpinnerView_textTSV);
//            enabled = a.getBoolean(R.styleable.TitleSpinnerView_enabledTSV, true);
        } finally {
            a.recycle();
        }

        LayoutInflater.from(context).inflate(R.layout.view_attached_image, this);

//        textView = findViewById(R.id.textView);
//        spinner = findViewById(R.id.spinner);
        imageView = findViewById(R.id.imageView);
        fileName = findViewById(R.id.fileName);
        downloadButton = findViewById(R.id.downloadButton);
        removeButton = findViewById(R.id.removeButton);

//        textView.setText(text);

//        adapterView = android.R.layout.simple_spinner_item;
//        dropDownView = android.R.layout.simple_spinner_dropdown_item;
//        items = new ArrayList<String>();


//        adapter = new ArrayAdapter<String>(getContext(), adapterView, items);
//        adapter.setDropDownViewResource(dropDownView);
//        spinner.setAdapter(adapter);

//        spinner.setEnabled(enabled);
    }

    public void setSelectedItem(String item) {
        spinner.setSelection(adapter.getPosition(item));
        invalidate();
        requestLayout();
    }

    public boolean isEnabledTSV() {
        return enabled;
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        spinner.setOnItemSelectedListener(listener);
        invalidate();
        requestLayout();
    }

    public void setEnabledTSV(boolean enabled) {
        this.enabled = enabled;
        spinner.setEnabled(enabled);
        invalidate();
        requestLayout();
    }

    public String getSelection() {
        if (spinner.getSelectedItem() == null) {
            return null;
        }
        return spinner.getSelectedItem().toString();
    }

    public void setAdapterView(int adapterView) {
        this.adapterView = adapterView;
        adapter = new ArrayAdapter<String>(getContext(), adapterView, items);
        adapter.setDropDownViewResource(dropDownView);
        spinner.setAdapter(adapter);
        invalidate();
        requestLayout();
    }

    public void setDropDownView(int dropDownView) {
        this.dropDownView = dropDownView;
        adapter.setDropDownViewResource(dropDownView);
        invalidate();
        requestLayout();
    }

    public void setItems(List<String> items) {
        String selectedItem = getSelection();
        this.items = new ArrayList<>(items);
        adapter.clear();
        adapter.addAll(items);
        if (selectedItem != null && adapter.getPosition(selectedItem) != -1) {
            spinner.setSelection(adapter.getPosition(selectedItem));
        } else {
            spinner.setSelection(0);
        }
        invalidate();
        requestLayout();

    }

    public int getListItemsCount() {
        return adapter.getCount();
    }

    public int getAdapterView() {
        return adapterView;
    }

    public int getDropDownView() {
        return dropDownView;
    }

    public ArrayAdapter<String> getAdapter() {
        return adapter;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public String getText() {
        return text;
    }

    public void setUri(Uri uri) {
        imageView.setImageURI(uri);
//        imageView.setBackgroundColor(getResources().getColor(R.color.white));
        imageView.setBackgroundColor(Static.getColorCompat(getContext(), R.color.transparent));
        invalidate();
        requestLayout();
    }

    public void showDeleteButton(boolean show) {
        removeButton.setVisibility(show ? VISIBLE : GONE);
        invalidate();
        requestLayout();
    }

    public void setFileName(String name) {
        fileName.setText(name);
        invalidate();
        requestLayout();
    }

    public void setOnDeleteListener(OnClickListener l) {
        removeButton.setOnClickListener(l);
    }

    public void setOnDownloadListener(OnClickListener l) {
        downloadButton.setOnClickListener(l);
    }

    public void showDownloadButton(boolean show) {
        downloadButton.setVisibility(show ? VISIBLE : GONE);
        invalidate();
        requestLayout();
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

    public Spinner getSpinner() {
        return spinner;
    }

//    public void setSpinner(Spinner spinner) {
//        this.spinner = spinner;
//    }

    //    public void setTextView(TextView textView) {
//        this.textView = textView;
//    }
}
