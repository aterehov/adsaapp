package ru.anoadsa.adsaapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.InputType;
import android.util.Pair;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class Static {
    public static DateFormat dateFormat = DateFormat.getDateInstance();

    @Contract(pure = true)
    public static int inputTypeStringToInt(@NonNull String inputType) {
        switch (inputType) {
            case "date": return InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE;
            case "datetime": return  InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_NORMAL;
            case "none": return 0;
            case "number": return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL;
            case "numberDecimal": return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
            case "numberPassword": return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD;
            case "numberSigned": return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
            case "phone": return InputType.TYPE_CLASS_PHONE;
//            case "text": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
            case "textAutoComplete": return InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
            case "textAutoCorrect": return InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;
            case "textCapCharacters": return InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
            case "textCapSentences": return InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
            case "textCapWords": return InputType.TYPE_TEXT_FLAG_CAP_WORDS;
            case "textEmailAddress": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            case "textEmailSubject": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT;
            case "textEnableTextConversionSuggestions":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return InputType.TYPE_TEXT_FLAG_ENABLE_TEXT_CONVERSION_SUGGESTIONS;
                }
            case "textFilter": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_FILTER;
            case "textImeMultiLine": return InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE;
            case "textLongMessage": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE;
            case "textMultiLine": return InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            case "textNoSuggestions": return InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
            case "textPassword": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            case "textPersonName": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME;
            case "textPhonetic": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PHONETIC;
            case "textPostalAddress": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS;
            case "textShortMessage": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE;
            case "textUri": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;
            case "textVisiblePassword": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
            case "textWebEditText": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT;
            case "textWebEmailAddress": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
            case "textWebPassword": return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
            case "time": return InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME;
            default: return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
        }
    };

    public static int inputTypeStringConverter(String inputType) {
        if (inputType == null) {
            inputType = "text";
        }
        String[] types = String.join("", inputType.split(" ")).split("\\|");
        int res = 0;
        boolean notEmpty = false;
        for (String type: types) {
            if (!type.isEmpty()) {
                notEmpty = true;
                res |= inputTypeStringToInt(type);
            }
        }
        if(!notEmpty) {
            res |= inputTypeStringToInt("text");
        }
        return res;
    }

    @Nullable
    public static Date tryParseDate(String dateString) {
        try
        {
            return dateFormat.parse(dateString);
        }
        catch (ParseException e) {
            return null;
        }
    }

    @NonNull
    public static String formatDate(Date date) {
//        LocalDate.parse(date.toString(), DateTimeFormatter.ofPattern())
//        LocalDate.from(date.toInstant().atZone(ZoneId.))
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        String sday = ((Integer) day).toString();
        String smonth = ((Integer) month).toString();
        String syear = ((Integer) year).toString();
        if (sday.length() < 2) {
            sday = "0" + sday;
        }
        if (smonth.length() < 2) {
            smonth = "0" + smonth;
        }
        if (syear.length() < 4) {
            syear = String.join("", Collections.nCopies(4 - syear.length(), "0")) + syear;
        }
        return sday + "." + smonth + "." + syear;
    }
//    public static <I, O> void callActivity(Class<? extends Activity> activityClass) {
//        ActivityResultContract<I, O> arc = new ActivityResultContract<I, O>() {
//            @NonNull
//            @Override
//            public Intent createIntent(@NonNull Context context, I i) {
//                return new Intent(context, activityClass);
//            }
//
//            @Override
//            public O parseResult(int i, @Nullable Intent intent) {
//                return new Pair<Integer, Intent>((Integer) i, intent);
//            }
//        }
//    }
}
