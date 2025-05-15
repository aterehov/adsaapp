package ru.anoadsa.adsaapp.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;

import ru.anoadsa.adsaapp.R;

public class RouteCardView extends ConstraintLayout {
    private String docnum;
    private String datetime;
    private String abonent;
    private String client;
    private String address;
    private String status;
    private String operator;

    private TextView docnumValue;
    private TextView datetimeTextView;
    private TextView abonentValue;
    private TextView clientValue;
    private TextView addressValue;
    private TextView statusValue;
    private TextView operatorValue;
    private TextView operatorLabel;

    private MaterialButton chatButton;
    private int defaultIconPadding;

    // ----------
    private TextView numberValue;
    private TextView fromValue;
    private TextView toValue;
    private TextView startValue;
    private TextView endValue;
    private TextView peopleValue;

    public void setAll(String docnum, String datetime, String abonent, String client,
                       String address, String status, String operator) {
        this.docnum = docnum;
        this.datetime = datetime;
        this.abonent = abonent;
        this.client = client;
        this.address = address;
        this.status = status;
        this.operator = operator;

        docnumValue.setText(docnum);
        datetimeTextView.setText(datetime);
        abonentValue.setText(abonent);
        clientValue.setText(client);
        addressValue.setText(address);
        statusValue.setText(status);
        operatorValue.setText(operator);

        if (operator == null || operator.isEmpty()) {
            operatorValue.setVisibility(GONE);
            operatorLabel.setVisibility(GONE);
        } else {
            operatorValue.setVisibility(VISIBLE);
            operatorLabel.setVisibility(VISIBLE);
        }

        invalidate();
        requestLayout();
    }

    public RouteCardView(Context context) {
        this(context, null);
    }

    public RouteCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RouteCardView,
                0,
                0
        );

        try {
//            docnum = a.getString(R.styleable.IncidentCardView_docnum);
//            datetime = a.getString(R.styleable.IncidentCardView_datetime);
//            abonent = a.getString(R.styleable.IncidentCardView_abonent);
//            client = a.getString(R.styleable.IncidentCardView_client);
//            address = a.getString(R.styleable.IncidentCardView_address);
//            status = a.getString(R.styleable.IncidentCardView_status);
//            operator = a.getString(R.styleable.IncidentCardView_operator);
        } finally {
            a.recycle();
        }

        LayoutInflater.from(context).inflate(R.layout.view_route_card, this);

//        docnumValue = findViewById(R.id.docnumValue);
//        datetimeTextView = findViewById(R.id.datetime);
//        abonentValue = findViewById(R.id.abonentValue);
//        clientValue = findViewById(R.id.clientValue);
//        addressValue = findViewById(R.id.addressValue);
//        statusValue = findViewById(R.id.statusValue);
//        operatorValue = findViewById(R.id.operatorValue);
//
//        operatorLabel = findViewById(R.id.operatorLabel);
//
//        chatButton = findViewById(R.id.chatButton);

        numberValue = findViewById(R.id.numberValue);
        fromValue = findViewById(R.id.fromValue);
        toValue = findViewById(R.id.toValue);
        startValue = findViewById(R.id.startValue);
        endValue = findViewById(R.id.endValue);
        peopleValue = findViewById(R.id.peopleValue);

//        docnumValue.setText(docnum);
//        datetimeTextView.setText(datetime);
//        abonentValue.setText(abonent);
//        clientValue.setText(client);
//        addressValue.setText(address);
//        statusValue.setText(status);
//        operatorValue.setText(operator);

//        if (operator == null || operator.isEmpty()) {
//            operatorValue.setVisibility(GONE);
//            operatorLabel.setVisibility(GONE);
//        }

//        defaultIconPadding = chatButton.getIconPadding();
//        setNewMessagesCount(0);
    }

    public void setNumber(int number) {
        numberValue.setText(String.valueOf(number));
        invalidate();
        requestLayout();
    }

    public void setFrom(String from) {
        fromValue.setText(from);
        invalidate();
        requestLayout();
    }

    public void setTo(String to) {
        toValue.setText(to);
        invalidate();
        requestLayout();
    }

    public void setStart(String start) {
        startValue.setText(start);
        invalidate();
        requestLayout();
    }

    public void setEnd(String end) {
        endValue.setText(end);
        invalidate();
        requestLayout();
    }

    public void setNewMessagesCount(int count) {
        if (count == 0) {
            chatButton.setText("");
            chatButton.setIconPadding(0);
        } else {
            chatButton.setText(String.valueOf(count));
            chatButton.setIconPadding(defaultIconPadding);
        }
        invalidate();
        requestLayout();
    }

    public void setChatButtonOnClickListener(OnClickListener l) {
        chatButton.setOnClickListener(l);
    }

    public void setPeople(int people) {
        peopleValue.setText(String.valueOf(people));
        invalidate();
        requestLayout();
    }

    public String getDocnum() {
        return docnum;
    }

    public void setDocnum(String docnum) {
        this.docnum = docnum;
        docnumValue.setText(docnum);
        invalidate();
        requestLayout();
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
        datetimeTextView.setText(datetime);
        invalidate();
        requestLayout();
    }

    public String getAbonent() {
        return abonent;
    }

    public void setAbonent(String abonent) {
        this.abonent = abonent;
        abonentValue.setText(abonent);
        invalidate();
        requestLayout();
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
        clientValue.setText(client);
        invalidate();
        requestLayout();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        addressValue.setText(address);
        invalidate();
        requestLayout();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        statusValue.setText(status);
        invalidate();
        requestLayout();
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(@Nullable String operator) {
        this.operator = operator;
        operatorValue.setText(operator);
        if (operator == null || operator.isEmpty()) {
            operatorValue.setVisibility(GONE);
            operatorLabel.setVisibility(GONE);
        } else {
            operatorLabel.setVisibility(VISIBLE);
            operatorValue.setVisibility(VISIBLE);
        }
    }

    public TextView getDocnumValue() {
        return docnumValue;
    }

//    public void setDocnumValue(TextView docnumValue) {
//        this.docnumValue = docnumValue;
//    }

    public TextView getDatetimeTextView() {
        return datetimeTextView;
    }

//    public void setDatetimeTextView(TextView datetimeTextView) {
//        this.datetimeTextView = datetimeTextView;
//    }

    public TextView getAbonentValue() {
        return abonentValue;
    }

//    public void setAbonentValue(TextView abonentValue) {
//        this.abonentValue = abonentValue;
//    }

    public TextView getClientValue() {
        return clientValue;
    }

//    public void setClientValue(TextView clientValue) {
//        this.clientValue = clientValue;
//    }

    public TextView getAddressValue() {
        return addressValue;
    }

//    public void setAddressValue(TextView addressValue) {
//        this.addressValue = addressValue;
//    }

    public TextView getStatusValue() {
        return statusValue;
    }

//    public void setStatusValue(TextView statusValue) {
//        this.statusValue = statusValue;
//    }

    public TextView getOperatorValue() {
        return operatorValue;
    }

//    public void setOperatorValue(TextView operatorValue) {
//        this.operatorValue = operatorValue;
//    }

    public TextView getOperatorLabel() {
        return operatorLabel;
    }

//    public void setOperatorLabel(TextView operatorLabel) {
//        this.operatorLabel = operatorLabel;
//    }
}
