package ru.anoadsa.adsaapp.ui.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.resources.TextAppearance;

import java.time.LocalDateTime;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;

public class ChatMessageView extends ConstraintLayout {
    private String text;

    private TextView textView;

    private CardView cardView;
    private TextView sender;
    private TextView senderType;
    private TextView messageText;
    private TextView sentDateTime;

    private ColorStateList defaultCardBackgroundColor;

    private ConstraintLayout innerCL;

    public ChatMessageView(Context context) {
        this(context, null);
    }

    public ChatMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ChatMessageView,
                0,
                0
        );

        try {
//            text = a.getString(R.styleable.DividerTitleView_textDTV);
        } finally {
            a.recycle();
        }

        LayoutInflater.from(context).inflate(R.layout.view_chat_message, this);

//        textView = findViewById(R.id.textView);
//        textView.setText(text);
        cardView = findViewById(R.id.cardView);
        sender = findViewById(R.id.sender);
        senderType = findViewById(R.id.senderType);
        messageText = findViewById(R.id.messageText);
        sentDateTime = findViewById(R.id.sentDateTime);

        innerCL = findViewById(R.id.innerCL);

        setWidthOfTwoThirdsOfScreen();

        defaultCardBackgroundColor = this.cardView.getCardBackgroundColor();
    }

    public void setWidthOfTwoThirdsOfScreen() {
        LayoutParams lp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.matchConstraintMaxWidth = Static.getScreenWidth() * 2 / 3;
        lp.constrainedWidth = true;
        cardView.setLayoutParams(lp);
    }

    public void setSentDateTime(LocalDateTime ldt) {
        sentDateTime.setText(Static.formatLocalDateTime(ldt));
        invalidate();
        requestLayout();
    }

    public void setSender(String sender) {
        this.sender.setText(sender);
        invalidate();
        requestLayout();
    }

    public void setMessageText(String text) {
        messageText.setText(text);
        invalidate();
        requestLayout();
    }

    public void setSenderType(int senderType) {
//        if (senderType == 0) {
//            this.senderType.setText("Оператор");
//            this.senderType.setVisibility(VISIBLE);
//            this.sender.setVisibility(VISIBLE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                this.messageText.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            } else {
//                this.messageText.setTextAppearance(getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            }
//        } else if (senderType == 1) {
//            this.senderType.setText("Организация");
//            this.senderType.setVisibility(VISIBLE);
//            this.sender.setVisibility(VISIBLE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                this.messageText.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            } else {
//                this.messageText.setTextAppearance(getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            }
//        } else if (senderType == 2) {
//            this.senderType.setVisibility(GONE);
//            this.sender.setVisibility(VISIBLE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                this.messageText.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            } else {
//                this.messageText.setTextAppearance(getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            }
//        } else if (senderType == 99) {
//            this.senderType.setText("Анонимный пользователь");
//            this.senderType.setVisibility(VISIBLE);
//            this.sender.setVisibility(GONE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                this.messageText.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            } else {
//                this.messageText.setTextAppearance(getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            }
//        } else if (senderType == -1) {
//            this.senderType.setVisibility(GONE);
//            this.sender.setVisibility(GONE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                this.messageText.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Small);
//            } else {
//                this.messageText.setTextAppearance(getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Small);
//            }
//            this.cardView.setCardBackgroundColor(getResources().getColor(R.color.gray_400));
//        }
        senderTypeTextBySenderType(senderType);
        senderTypeVisibilityBySenderType(senderType);
        senderVisibilityBySenderType(senderType);
        messageTextAppearanceBySenderType(senderType);
        cardViewBackgroundColorBySenderType(senderType);
        sentDateTimeAlignmentBySenderType(senderType);
        messageTextStyleBySenderType(senderType);

        if (senderType == 99) {
            this.sender.setText("");
        }

        invalidate();
        requestLayout();
    }

    private void messageTextStyleBySenderType(int senderType) {
        switch (senderType) {
            case 0:
            case 1:
            case 2:
            case 99:
                this.messageText.setTypeface(Typeface.DEFAULT);
                return;
            case -1:
                this.messageText.setTypeface(this.messageText.getTypeface(), Typeface.ITALIC);
                return;
            default:
                return;
        }
    }

    private void sentDateTimeAlignmentBySenderType(int senderType) {
        ConstraintLayout.LayoutParams lp =
//                new LayoutParams(
//                LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT
//        );
                (LayoutParams) sentDateTime.getLayoutParams();
        switch (senderType) {
            case 0:
            case 1:
            case 99:
                lp.startToStart = LayoutParams.UNSET;
                lp.endToEnd = innerCL.getId();
                break;
            case 2:
                lp.startToStart = innerCL.getId();
                lp.endToEnd = LayoutParams.UNSET;
                break;
            case -1:
                lp.startToStart = innerCL.getId();
                lp.endToEnd = innerCL.getId();
                break;
            default:
                break;
        }

        sentDateTime.setLayoutParams(lp);
    }

    private void cardViewBackgroundColorBySenderType(int senderType) {

        switch (senderType) {
            case 0:
            case 1:
//            case 2:
            case 99:
                this.cardView.setCardBackgroundColor(
//                        getResources().getColor(R.color.white)
                        defaultCardBackgroundColor
                );
                return;
            case -1:
                this.cardView.setCardBackgroundColor(getResources().getColor(R.color.gray_400));
                return;
            case 2:
                this.cardView.setCardBackgroundColor(getResources().getColor(
//                        R.color.teal_200
                        R.color.light_blue_400
                ));
            default:
                return;
        }
    }

    private void messageTextAppearanceBySenderType(int senderType) {
        switch (senderType) {
            case 0:
            case 1:
            case 2:
            case 99:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.messageText.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
                } else {
                    this.messageText.setTextAppearance(getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
                }
                return;
            case -1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.messageText.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Small);
                } else {
                    this.messageText.setTextAppearance(getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Small);
                }
                return;
            default:
                return;
        }

//        if (senderType == 0) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                this.messageText.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            } else {
//                this.messageText.setTextAppearance(getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            }
//        } else if (senderType == 1) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                this.messageText.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            } else {
//                this.messageText.setTextAppearance(getContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
//            }
//        }
    }

    private void senderVisibilityBySenderType(int senderType) {
        switch (senderType) {
            case 0:
            case 1:
            case 2:
                this.sender.setVisibility(VISIBLE);
                return;
            case 99:
//                this.sender.setVisibility(INVISIBLE);
//                return;
            case -1:
                this.sender.setVisibility(GONE);
                return;
            default:
                return;
        }

//        if (senderType == 0) {
//            this.sender.setVisibility(VISIBLE);
//        } else if (senderType == 1) {
//            this.sender.setVisibility(VISIBLE);
//        } else if (senderType == 2) {
//            this.sender.setVisibility(VISIBLE);
//        } else if (senderType == 99) {
//            this.sender.setVisibility(GONE);
//        } else if (senderType == -1) {
//            this.sender.setVisibility(GONE);
//        }
    }

    private void senderTypeVisibilityBySenderType(int senderType) {
        switch (senderType) {
            case 0:
            case 1:
            case 99:
                this.senderType.setVisibility(VISIBLE);
                return;
            case 2:
            case -1:
                this.senderType.setVisibility(GONE);
                return;
            default:
                return;
        }

//        if (senderType == 0) {
//            this.senderType.setVisibility(VISIBLE);
//        } else if (senderType == 1) {
//            this.senderType.setVisibility(VISIBLE);
//        } else if (senderType == 2) {
//            this.senderType.setVisibility(GONE);
//        } else if (senderType == 99) {
//            this.senderType.setVisibility(VISIBLE);
//        } else if (senderType == -1) {
//            this.senderType.setVisibility(GONE);
//        }
    }

    private void senderTypeTextBySenderType(int senderType) {
        switch (senderType) {
//            if (senderType == 0) {
            case 0:
                this.senderType.setText("Оператор");
                return;
//            } else if (senderType == 1) {
            case 1:
                this.senderType.setText("Организация");
                return;
//            } else if (senderType == 2) {
            case 2:
                this.senderType.setText("Вы");
                return;
//            } else if (senderType == 99) {
            case 99:
                this.senderType.setText("Анонимный пользователь");
                return;
//            } else if (senderType == -1) {
            case -1:
                this.senderType.setText("Система");
                return;
//            }
            default:
                return;
        }
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
