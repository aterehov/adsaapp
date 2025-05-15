package ru.anoadsa.adsaapp;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Pair;
import android.util.TypedValue;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;
import org.reactivestreams.Subscriber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount.CheckMessageCountWatchdog;
import ru.anoadsa.adsaapp.backgroundtasks.sosbutton.SosButtonWorker;
import ru.anoadsa.adsaapp.models.data.User;
import ru.anoadsa.adsaapp.models.databases.AppDatabase;
import ru.anoadsa.adsaapp.ui.activities.permission.PermissionActivity;
import ru.anoadsa.adsaapp.ui.views.ChatMessageView;

public class Static {
//    @Keep
//    public interface ColorCompat {
//        public int getColor(int id);
//    }
//
//    private static class ColorCompatAdapter {
//        private
//
//        public ColorCompatAdapter(Context context) {
//
//        }
//    }

    public static String LETTERS_RUSSIAN_LOWER = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    public static String LETTERS_RUSSIAN_UPPER = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    public static DateFormat dateFormat = DateFormat.getDateInstance();
    public static AppDatabase db;

    public static String ADSA_PICTURES_FOLDER = "Фотографии АНО АДСА";
    public static String ADSA_DOCUMENTS_FOLDER = "Документы АНО АДСА";

    public static String CHAT_NOTIFICATION_CHANNEL_ID = "ChatNotifications";
    public static String wordsAllowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZабвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ0123456789-";

    public static Executor executor;
    public static HandlerThread handlerThread;

    private static boolean appStarted;

    public static void runOnStart(Context appContext) {
        if (appStarted) {
            return;
        }

        try {
            DevSettings.loadAppVersion(appContext);
            DevSettings.loadAppVersionCode(appContext);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        Static.initDb(appContext);
        AppPreferences.initDataStore(appContext);
        Geo.initAutoselectBetterLocation();

        SosButtonWorker.subscribeOnLocationUpdates();
        CheckMessageCountWatchdog.schedule(appContext);

        appStarted = true;
    }

    @NonNull
    public static ArrayList<String> stringToWords(@NonNull String text) {
        String splitChars = "";

        for (char c: text.toCharArray()) {
            if (wordsAllowedChars.indexOf(c) == -1 && splitChars.indexOf(c) == -1) {
                splitChars += c;
            }
        }
        ArrayList<String> words = new ArrayList<String>();
        words.add(text);
        for (char c: splitChars.toCharArray()) {
//            ArrayList<String> newWords = new ArrayList<String>(words);
            int len = words.size();
            for (int i = 0; i < len; ++i) {
                words.addAll(Arrays.asList(words.get(0).split(String.valueOf(c))));
                words.remove(0);
            }
        }
        for (int i = 0; i < words.size(); ++i) {
            if (words.get(i).isEmpty()) {
                words.remove(i);
                --i;
            }
        }
        return words;
    }

    public static int getColorCompat(Context context, int color) {
//        ColorCompat compat;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            compat = (ColorCompat) context;
            return context.getColor(color);
        } else {
//            compat = (ColorCompat) getResources();
//            compat = (ColorCompat) context.getResources();

            return context.getResources().getColor(color);
        }
//        return compat;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static void createChatNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHAT_NOTIFICATION_CHANNEL_ID,
                    "Уведомления чатов",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Уведомления о новых сообщениях в чатах происшествий");
            context
                    .getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }

    public static File createFileWithAutoRename(String dir, @NonNull String name) throws IOException {
        ArrayList<String> nameDotSeparated = new ArrayList<String>(Arrays.asList(name.split("\\.")));
        String ext = nameDotSeparated.get(nameDotSeparated.size() - 1);
        nameDotSeparated.remove(nameDotSeparated.size() - 1);
        String nameWithoutExt = String.join(".", nameDotSeparated);

        File file = new File(dir + nameWithoutExt + "." + ext);
        file.getParentFile().mkdirs();
        boolean success = file.createNewFile();
        int i = 1;
        while (!success) {
            file = new File(dir + nameWithoutExt + " (" + i + ")." + ext);
            success = file.createNewFile();
            ++i;
        }
        return file;
    }

    @NonNull
    public static String getNewFileName(String dir, @NonNull String name) throws IOException {
        ArrayList<String> nameDotSeparated = new ArrayList<String>(Arrays.asList(name.split("\\.")));
        String ext = nameDotSeparated.get(nameDotSeparated.size() - 1);
        nameDotSeparated.remove(nameDotSeparated.size() - 1);
        String nameWithoutExt = String.join(".", nameDotSeparated);

        File file = new File(dir + "/" + nameWithoutExt + "." + ext);
        file.getParentFile().mkdirs();
        boolean success = !file.exists();
        int i = 1;
        while (!success) {
            file = new File(dir + "/" + nameWithoutExt + " (" + i + ")." + ext);
            success = !file.exists();
            ++i;
        }
        return file.getName();
    }

//    public static void rescanGalleryFile(Context context, String path) {
//        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, path));
//    }

    public static boolean saveFileToGallery(Uri sourceUri, String fileName, String mimeType, @NonNull Context context) {
//        try {
//            File f = createFileWithAutoRename(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PICTURES + "/" + ADSA_PICTURES_FOLDER, fileName);
//            fileName = f.getName();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }

        try {
            fileName = getNewFileName(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PICTURES + "/" + ADSA_PICTURES_FOLDER, fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        ContentValues values = new ContentValues();
        Uri destinationUri;

        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/" + ADSA_PICTURES_FOLDER
            );
        } else {
            String fullPath = Environment.getExternalStorageDirectory()
                    + "/"
                    + Environment.DIRECTORY_PICTURES
                    + "/"
                    + ADSA_PICTURES_FOLDER
                    + "/"
                    + fileName;

            values.put(
                    MediaStore.MediaColumns.DATA,
//                    Environment.DIRECTORY_PICTURES + "/" + ADSA_PICTURES_FOLDER
                    fullPath
            );
        }

        destinationUri = context
                .getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);


        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(sourceUri);
            outputStream = context.getContentResolver().openOutputStream(destinationUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            while (len != -1) {
                len = inputStream.read(buffer);
                if (len != -1) {
                    outputStream.write(buffer, 0, len);
                }
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void initExecutor() {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
    }

    public static void initAndStartHandlerThread() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("adsaHandlerThread");
        }
        if (!handlerThread.isAlive()) {
            handlerThread.start();
        }
    }

    public static boolean checkPermission(Context context, String permission) {
        if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {

//            Intent intent = new Intent(context, PermissionActivity.class);
//            intent.putExtra("permission", permission);
//            context.startActivity(intent);

//            };
            return false;

        }
    };

    public static void makeFirstRun() {
        AppPreferences.setIsFirstRun(true).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .blockingSubscribe();
    }

    @NonNull
    public static String replaceCharacter(@NonNull String string, int pos, char c) {
        return string.substring(0, pos) + c + string.substring(pos + 1);
    }

    @NonNull
    public static String cutCharacter(@NonNull String string, int pos) {
        return string.substring(0, pos) + string.substring(pos + 1);
    }

    @NonNull
    public static String insertCharacter(@NonNull String string, int pos, char c) {
        return string.substring(0, pos) + c + string.substring(pos);
    }

    public static InputFilter carNumberInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(@NonNull CharSequence source, int start, int end,
                                   @NonNull Spanned dest, int dstart, int dend) {
            String slice = source.subSequence(start, end).toString();
            String result = dest.subSequence(0, dstart)
                    + slice
                    + dest.subSequence(dend, dest.length());
            for (int i = 0; i < slice.length(); ++i) {
                int rpos = dstart + i;
                // Handle format: а123бв45 or а123бв456
                if (rpos == 0 || rpos == 4 || rpos == 5) {
                    if (LETTERS_RUSSIAN_LOWER.indexOf(result.charAt(rpos)) == -1) {
                        if (LETTERS_RUSSIAN_UPPER.indexOf(result.charAt(rpos)) != -1) {
                            slice = replaceCharacter(slice, i,
                                    Character.toLowerCase(result.charAt(rpos)));
                            result = replaceCharacter(result, rpos,
                                    Character.toLowerCase(result.charAt(rpos)));
                        } else {
                            slice = cutCharacter(slice, i);
                            result = cutCharacter(result, rpos);
                            --i;
                        }
                    }
                } else if (rpos >= 1 && rpos <= 3 || rpos >= 6 && rpos <= 8) {
                    if (!Character.isDigit(result.charAt(rpos))) {
                        slice = cutCharacter(slice, i);
                        result = cutCharacter(result, rpos);
                        --i;
                    }
                } else if (rpos > 8) {
                    // Cut inserted slice to 9 symbols
                    slice = slice.substring(0, i);
                    break;
                }
            }
            if (result.length() > 9) {
                slice = slice.substring(0, Integer.max(Integer.min(9 - dest.length(), slice.length()), 0));
            }
            return slice;
        }
    };

    public static InputFilter osagoInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(@NonNull CharSequence source, int start, int end,
                                   @NonNull Spanned dest, int dstart, int dend) {
            String slice = source.subSequence(start, end).toString();
            String result = dest.subSequence(0, dstart)
                    + slice
                    + dest.subSequence(dend, dest.length());
            for (int i = 0; i < slice.length(); ++i) {
                int rpos = dstart + i;
                if (rpos >= 0 && rpos <= 2) {
                    if (LETTERS_RUSSIAN_UPPER.indexOf(result.charAt(rpos)) == -1) {
                        if (LETTERS_RUSSIAN_LOWER.indexOf(result.charAt(rpos)) != -1) {
                            slice = replaceCharacter(slice, i,
                                    Character.toUpperCase(result.charAt(rpos)));
                            result = replaceCharacter(result, i,
                                    Character.toUpperCase(result.charAt(rpos)));
                        } else {
                            slice = cutCharacter(slice, i);
                            result = cutCharacter(result, rpos);
                            --i;
                        }
                    }
                } else if (rpos == 3) {
                    if (result.charAt(rpos) != ' ') {
                        slice = insertCharacter(slice, i, ' ');
                        result = insertCharacter(result, rpos, ' ');
                    }
                } else if (rpos >= 4 && rpos <= 13) {
                    if (!Character.isDigit(result.charAt(rpos))) {
                        slice = cutCharacter(slice, i);
                        result = cutCharacter(result, rpos);
                        --i;
                    }
                } else if (rpos > 13) {
                    slice = slice.substring(0, i);
                    break;
                }
            }
            if (result.length() > 14) {
                slice = slice.substring(0, Integer.max(Integer.min(14 - dest.length(), slice.length()), 0));
            }
            return slice;
        }
    };

    public static InputFilter snilsInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(@NonNull CharSequence source, int start, int end,
                                   @NonNull Spanned dest, int dstart, int dend) {
            String slice = source.subSequence(start, end).toString();
            String result = dest.subSequence(0, dstart)
                    + slice
                    + dest.subSequence(dend, dest.length());
            for (int i = 0; i < slice.length(); ++i) {
                int rpos = dstart + i;
                if (rpos >= 0 && rpos <= 2 || rpos >= 4 && rpos <= 6 || rpos >= 8 && rpos <= 10 || rpos >= 12 && rpos <= 13) {
                    if (!Character.isDigit(result.charAt(rpos))) {
                        slice = cutCharacter(slice, i);
                        result = cutCharacter(result, rpos);
                        --i;
                    }
                } else if (rpos == 3 || rpos == 7) {
                    if (result.charAt(rpos) != '-') {
                        slice = insertCharacter(slice, i, '-');
                        result = insertCharacter(result, rpos, '-');
                    }
                } else if (rpos == 11) {
                    if (result.charAt(rpos) != ' ') {
                        if (result.charAt(rpos) == '-') {
                            slice = replaceCharacter(slice, i, ' ');
                            result = replaceCharacter(result, rpos, ' ');
                        } else {
                            slice = insertCharacter(slice, i, ' ');
                            result = insertCharacter(result, rpos, ' ');
                        }
                    }
                } else if (rpos > 13) {
                    slice = slice.substring(0, i);
                    break;
                }
            }
            if (result.length() > 14) {
                slice = slice.substring(0, Integer.max(Integer.min(14 - dest.length(), slice.length()), 0));
            }
            return slice;
        }
    };

    public static InputFilter medPolisInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(@NonNull CharSequence source, int start, int end,
                                   @NonNull Spanned dest, int dstart, int dend) {
            String slice = source.subSequence(start, end).toString();
            String result = dest.subSequence(0, dstart)
                    + slice
                    + dest.subSequence(dend, dest.length());
            for (int i = 0; i < slice.length(); ++i) {
                int rpos = dstart + i;
                if (rpos >= 0 && rpos <= 15) {
                    if (!Character.isDigit(result.charAt(rpos))) {
                        slice = cutCharacter(slice, i);
                        result = cutCharacter(result, rpos);
                        --i;
                    }
                } else if (rpos > 15) {
                    slice = slice.substring(0, i);
                    break;
                }
            }
            if (result.length() > 16) {
                slice = slice.substring(0, Integer.max(Integer.min(16 - dest.length(), slice.length()), 0));
            }
            return slice;
        }
    };

    public static InputFilter birthdayInputFilter = new InputFilter() {
        @Override
        public CharSequence filter(@NonNull CharSequence source, int start, int end,
                                   @NonNull Spanned dest, int dstart, int dend) {
            String slice = source.subSequence(start, end).toString();
            String result = dest.subSequence(0, dstart)
                    + slice
                    + dest.subSequence(dend, dest.length());
            for (int i = 0; i < slice.length(); ++i) {
                int rpos = dstart + i;
                if (rpos >= 0 && rpos <= 1 || rpos >= 3 && rpos <= 4 || rpos >= 6 && rpos <= 9) {
                    if (!Character.isDigit(result.charAt(rpos))) {
                        slice = cutCharacter(slice, i);
                        result = cutCharacter(result, rpos);
                        --i;
                    }
                } else if (rpos == 2 || rpos == 5) {
                    if (result.charAt(rpos) != '.') {
                        if (result.charAt(rpos) == '/' || result.charAt(rpos) == '-') {
                            slice = replaceCharacter(slice, i, '.');
                            result = replaceCharacter(result, rpos, '.');
                        } else {
                            slice = insertCharacter(slice, i, '.');
                            result = insertCharacter(result, rpos, '.');
                        }
                    }
                } else if (rpos > 9) {
                    slice = slice.substring(0, i);
                    break;
                }
            }
            if (result.length() > 10) {
                slice = slice.substring(0, Integer.max(Integer.min(10 - dest.length(), slice.length()), 0));
            }
            return slice;
        }
    };

    public static boolean checkOsago(@NonNull String osago) {
        return osago.matches("[А-Я]{3} [0-9]{10}");
    }

    public static boolean checkSnils(@NonNull String snils) {
        return snils.matches("[0-9]{3}-[0-9]{3}-[0-9]{3} [0-9]{2}");
    }

    public static boolean checkMedPolis(@NonNull String medPolis) {
        return medPolis.matches("[0-9]{16}");
    }

    public static boolean checkCarNumber(@NonNull String carNumber) {
        return carNumber.matches("[а-я][0-9]{3}[а-я]{2}[0-9]{2,3}");
    }

    public static boolean checkLatitude(float lat) {
        return lat >= -90 && lat <= 90;
    }

    public static boolean checkLongitude(float lon) {
        return lon >= -180 && lon <= 180;
    }

    @NonNull
    @Contract("_ -> new")
    public static Pair<Boolean, String> makeResponsePair(Response response) {
        Boolean ok = response != null && response.isSuccessful() && response.body() != null;
        String errorMessage = null;
        if (!ok) {
            errorMessage = getResponseErrorMessage(response);
        }
        return new Pair<>(ok, errorMessage);
    }

    public static int dpToPixels(int dp, @NonNull Context context) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    @NonNull
    public static String formatLocalDateTime(@NonNull LocalDateTime ldt) {
        String s = "";
        int day = ldt.getDayOfMonth();
        int month = ldt.getMonthValue();
        int year = ldt.getYear();
        int hour = ldt.getHour();
        int minute = ldt.getMinute();

        String sday = intToString(day);
        String smonth = intToString(month);
        String syear = intToString(year);
        String shour = intToString(hour);
        String sminute = intToString(minute);

        if (sday.length() < 2) {
            s += "0";
        }
        s += sday;
        s += ".";

        if (smonth.length() < 2) {
            s += "0";
        }
        s += smonth;
        s += ".";

        if (syear.length() < 4) {
            s += String.join("", Collections.nCopies(4 - syear.length(), "0"));
        }
        s += syear;
        s += " ";

        if (shour.length() < 2) {
            s += "0";
        }
        s += shour;
        s += ":";

        if (sminute.length() < 2) {
            s += "0";
        }
        s += sminute;

        return s;
    }

    @NonNull
    public static String intToString(int i) {
        return ((Integer) i).toString();
    }

    @NonNull
    public static String getNewMessagesText(int numberOfMessages) {
        String s = "";
        String snumber = String.valueOf(numberOfMessages);
        s += snumber;
        s += " ";
//        s += "нов";
        if (snumber.endsWith("0")
            || snumber.endsWith("5")
            || snumber.endsWith("6")
            || snumber.endsWith("7")
            || snumber.endsWith("8")
            || snumber.endsWith("9")
            || snumber.endsWith("11")
            || snumber.endsWith("12")
            || snumber.endsWith("13")
            || snumber.endsWith("14")) {
            s += "новых сообщений";
        } else if (snumber.endsWith("1")) {
            s += "новое сообщение";
        } else {
            s += "новых сообщения";
        }
//        s += " ";
        return s;
    }

    public static String getResponseErrorMessage(@NonNull Response response) {
        try {
//            return response.body() == null
//                    ? "Неизвестная ошибка"
//                    : new Gson()
//                    .fromJson(response.body().string(), JsonObject.class)
//                    .get("message")
//                    .getAsString();
            ResponseBody body = response.body();
            if (body == null) {
                return "Неизвестная ошибка";
            }
            JsonObject json = new Gson().fromJson(body.string(), JsonObject.class);
            if (json == null) {
                return "Неизвестная ошибка";
            }
            if (json.get("message").isJsonNull()) {
                return "Неизвестная ошибка";
            } else {
                return json.get("message").getAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Неизвестная ошибка";
        }
    }

    public static Single<Map<String, String>> getAuthorizedHeadersMap() {
//        return db.userDao().getUser().map(new Function<User, Map<String, String>>() {
//            @Override
//            public HashMap<String, String> apply(User user) throws Throwable {
//                HashMap<String, String> h = new HashMap<String, String>();
//                h.put("Authorization", user.getAuthToken());
//                return h;
//            }
//        });
        return db.userDao().getUser().concatMap(new Function<User, Single<Map<String, String>>>() {
            @Override
            public Single<Map<String, String>> apply(User user) throws Throwable {
                if (user == null || user.getPhone() == null || user.getPassword() == null) {
                    return Single.just(Collections.singletonMap("Authorization", ""));
                }
                HashMap<String, String> h = new HashMap<String, String>();
                h.put("Authorization", user.getAuthToken());
                Single<Map<String, String>> r = Single.just(h);
                if (System.currentTimeMillis() - user.getAuthTimestamp() > DevSettings.maxJWTValidity) {
                    r = r.concatMap(new Function<Map<String, String>, Single<Map<String, String>>>() {
                        @Override
                        public Single<Map<String, String>> apply(Map<String, String> headers) throws Throwable {
                            return user.login().map(new Function<Pair<Boolean, String>, Map<String, String>>() {
                                @Override
                                public Map<String, String> apply(Pair<Boolean, String> pair) throws Throwable {
                                    h.put("Authorization", user.getAuthToken());
                                    return h;
                                }
                            });
                        }
                    });
                }
                return r;
            }
        });
    }

    public static boolean checkPhone(String phone) {
        return Static.removePhoneSymbols(phone).matches("\\+[0-9]{11}");
    }

    public static void initDb(Context context) {
        if (db == null) {
            System.out.println("DB IS INITIALIZING");
            db = Room.databaseBuilder(context, AppDatabase.class, "adsa-db")
                    .addMigrations(
                            AppDatabase.MIGRATION_1_2,
                            AppDatabase.MIGRATION_2_3,
                            AppDatabase.MIGRATION_3_4
                    )
                    .enableMultiInstanceInvalidation()
                    .build();
        }
    }

    public static void wipeDb() {
        if (db != null) {
            Single.fromCallable(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    db.clearAllTables();
                    return true;
                }
            }).subscribeOn(Schedulers.io()).blockingSubscribe();
        }
    }

    public static void wipeDbUser() {
        if (db != null) {
            db.userDao().clearUser().subscribeOn(Schedulers.io()).blockingSubscribe();
        }
    }

    public static void pseudoAuth() {
        if (db != null) {
            db.userDao().newUser().concatMap(new Function<Boolean, SingleSource<Boolean>>() {
                @Override
                public SingleSource<Boolean> apply(Boolean success) throws Throwable {
                    if (success) {
                        User u = new User();
                        u.setAuthToken("qwerty");
                        return db.userDao().editUser(u);
                    } else {
                        return Single.just(false);
                    }
                }
            }).subscribeOn(Schedulers.io()).blockingSubscribe();
        }
    }

    @NonNull
    public static String removePhoneSymbols(@NonNull String phone) {
        return String.join("",
                String.join("",
                String.join("",
                String.join("",
                        phone.split(" ")
                            ).split("-")
                            ).split("\\(")
                            ).split("\\)")
        );
    }

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

    public static boolean checkDateString(String dateString) {
        try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.uuuu"));
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    public static LocalDate makeLocalDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.uuuu"));
    }

    @NonNull
    public static String makeFullName(String surname, String name, String midname) {
        String fullName = "";
        if (surname != null) {
            fullName += surname;
        }
        if (name != null) {
            if (!fullName.isEmpty()) {
                fullName += " ";
            }
            fullName += name;
        }
        if (midname != null) {
            if (!fullName.isEmpty()) {
                fullName += " ";
            }
            fullName += midname;
        }
        if (fullName.isEmpty()) {
            fullName = "Пользователь";
        }
        return fullName;
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
