package ru.anoadsa.adsaapp.backgroundtasks.sosbutton;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionPlan;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.telephony.SubscriptionManagerCompat;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import ru.anoadsa.adsaapp.DevSettings;
import ru.anoadsa.adsaapp.Geo;
import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.backgroundtasks.MessageData;
import ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount.ChatNotificationManager;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.models.data.IncidentMap;
import ru.anoadsa.adsaapp.models.data.User;
import ru.anoadsa.adsaapp.ui.activities.menu.MenuActivity;
import ru.anoadsa.adsaapp.ui.menufragments.sos.SosFragment;

public class SosButtonWorker extends Worker {


    public SosButtonWorker(Context context, WorkerParameters params) {
        super(context, params);
        System.out.println("SOS WORKER CONSTRUCTOR WAS RUN");
    }

    // This is the same for all Check Message Count workers
    private static IncidentMap incidentMap;
    private static IncidentMap incidentMapDb;
    private static LocalDateTime lastUpdatedIncidentMap;
    private static boolean incidentMapUpdateRequested = false;
//    private static HashMap<String, Integer> incidentIdToNotificationId;
//    private static int nextNotificationId = 0;
//    private static LocalDateTime lastRun;
    private String lastError = "";

    public static void requestIncidentMapUpdate() {
        incidentMapUpdateRequested = true;
    }

    private boolean updateIncidentMap() {
        System.out.println("WORKER updating incident map");
        incidentMap.updateFromServer().blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
            @Override
            public void accept(Pair<Boolean, String> pair) throws Throwable {
                System.out.println("WORKER incident map updated with first = " + pair.first + ", second = " + pair.second);
                if (pair.first) {
                    // No autosave to db because incidentMap only holds basic info here
//                        // Auto refresh info in db
//                        incidentMap.saveAllToDb().blockingSubscribe();
                    lastUpdatedIncidentMap = LocalDateTime.now();
                } else {
//                        incidentMap.loadAllFromDb().blockingSubscribe();
                    lastError = pair.second;
                }
            }
        });

        if (incidentMap.getType() != null && incidentMap.getType().equals("basicInfo")) {
            System.out.println("WORKER only basic info is loaded, loading full info");
            incidentMap.loadAllFullInfo().blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
                @Override
                public void accept(Pair<Boolean, String> pair) throws Throwable {
                    System.out.println("WORKER full info loaded, first = " + pair.first + ", second = " + pair.second);
                    if (pair.first) {

                    } else {
                        lastError = pair.second;
                    }
                }
            });
            // full info should be loaded now
        } else {
            System.out.println("WORKER incidentmap update failed (basic info not loaded)");
            return false;
        }

        if (incidentMap.getType() != null && incidentMap.getType().equals("fullInfo")) {
            System.out.println("WORKER incidentmap update succeeded (full info loaded)");
            return true;
        } else {
            System.out.println("WORKER incidentmap update failed (full info not loaded)");
            return false;
        }
    }

    private void sendNotification(@NonNull Incident incident, int messageCount) {
        System.out.println("WORKER sending notification, incident id = " + incident.getId() + ", incident docnum = " + incident.getDocnum() + ", messageCount = " + messageCount);
        if (!Static.checkPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)) {
            System.out.println("WORKER no notification permission, failed to send notification");
            return;
        }

        System.out.println("WORKER checking if notification has already been sent with this number of messages");
        if (ChatNotificationManager.getNotificationCount(incident.getId()) >= messageCount) {
            System.out.println("WORKER notification has already been sent with this number of messages, not sending a new one");
            return;
        }

        System.out.println("WORKER creating notification channel");
        Static.createChatNotificationChannel(getApplicationContext());

        // Using docnum instead
//        System.out.println("WORKER getting notification id for incidentId = " + incident.getId());
//        int notificationId = ChatNotificationManager.addNotificationId(incident.getId());
//        System.out.println("WORKER notification id for incidentId = " + incident.getId() + " is " + notificationId);


        System.out.println("WORKER creating intent and putting extras");
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        intent.putExtra("action", "chat");
        intent.putExtra("incidentId", incident.getId());


        System.out.println("WORKER creating pending intent");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
//                0,
                incident.getDocnum(),
                intent,
                PendingIntent.FLAG_IMMUTABLE,
                intent.getExtras()
        );


        System.out.println("WORKER building notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                Static.CHAT_NOTIFICATION_CHANNEL_ID
        )
                .setSmallIcon(R.drawable.baseline_chat_24)
                .setContentTitle("Обращение № " + incident.getDocnum())
                .setContentText(Static.getNewMessagesText(messageCount))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

//        TaskStackBuilder.create(getApplicationContext()).
//        Navigation
//                .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
//                .
//        ChatNotificationIdsManager.init();

//        if (incidentIdToNotificationId == null) {
//            incidentIdToNotificationId = new HashMap<String, Integer>();
//        }
//        if (!incidentIdToNotificationId.containsKey(incident.getId())) {
//            incidentIdToNotificationId.put(incident.getId(), nextNotificationId);
//            ++nextNotificationId;
//        }
//        System.out.println("WORKER getting notification id for incidentId = " + incident.getId());
//        int notificationId = ChatNotificationManager.addNotificationId(incident.getId());
//        System.out.println("WORKER notification id for incidentId = " + incident.getId() + " is " + notificationId);

        System.out.println("WORKER updating number of messages sent in previous notifications in ChatNotificationManager");
        ChatNotificationManager.setNotificationCount(incident.getId(), messageCount);

        System.out.println("WORKER finalizing notification send");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(incident.getDocnum(), builder.build());
    }

    private void sendDebugNotification() {
        System.out.println("WORKER sending notification");
        if (!Static.checkPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)) {
            System.out.println("WORKER no notification permission, failed to send notification");
            return;
        }

        System.out.println("WORKER creating notification channel");
        Static.createChatNotificationChannel(getApplicationContext());

//        System.out.println("WORKER creating intent and putting extras");
//        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
//        intent.putExtra("action", "chat");
//        intent.putExtra("incidentId", incident.getId());


//        System.out.println("WORKER creating pending intent");
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                getApplicationContext(),
//                0,
//                intent,
//                PendingIntent.FLAG_IMMUTABLE
//        );


        System.out.println("WORKER building notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                Static.CHAT_NOTIFICATION_CHANNEL_ID
        )
                .setSmallIcon(R.drawable.baseline_chat_24)
                .setContentTitle("DEBUG NOTIFICATION")
                .setContentText("CheckMessageCountWorker is running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true)
                ;

//        TaskStackBuilder.create(getApplicationContext()).
//        Navigation
//                .findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
//                .
//        ChatNotificationIdsManager.init();

//        if (incidentIdToNotificationId == null) {
//            incidentIdToNotificationId = new HashMap<String, Integer>();
//        }
//        if (!incidentIdToNotificationId.containsKey(incident.getId())) {
//            incidentIdToNotificationId.put(incident.getId(), nextNotificationId);
//            ++nextNotificationId;
//        }
//        System.out.println("WORKER getting notification id for incidentId = " + incident.getId());
//        int notificationId = ChatNotificationIdsManager.addNotificationId(incident.getId());
//        System.out.println("WORKER notification id for incidentId = " + incident.getId() + " is " + notificationId);

        System.out.println("WORKER finalizing notification send");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(100500, builder.build());
    }

    private static Location location;
    private static Float lat;
    private static Float lon;
    private static String addr;
    private static Context appContext;
    private static boolean run;

    public static void setAppContext(Context context) {
        appContext = context;
    }

    public static void run(Context context) {
        if (!Static.checkPermission(context, Manifest.permission.SEND_SMS)) {
            Toast.makeText(
                    context,
                    "Не удалось отправить SMS с данными пользователя, так как нет разрешения на отправку SMS",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }
        run = true;
        appContext = context;
        Geo.refreshLocation(appContext);
    }

    public static void run(Context appContext, Float lat, Float lon, String addr) {
        SosButtonWorker.lat = lat;
        SosButtonWorker.lon = lon;
        SosButtonWorker.addr = addr;
        scheduleNextWorkRequest(appContext);
    }

    public static void subscribeOnLocationUpdates() {
        Geo.getLocation().observeForever(new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (run) {
                    if (Geo.getUsedNetwork() && Geo.getUsedGps()) {
                        run = false;
                        SosButtonWorker.location = location;
                        scheduleNextWorkRequest(appContext);
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public Result doWork() {
        final String[] s = {"Экстренный вызов\n\n"};
        if (
//                location == null
                lat == null || lon == null
        ) {
            Toast.makeText(
                    getApplicationContext(),
                    "Не удалось определить местоположение при отправке SMS",
                    Toast.LENGTH_LONG
            ).show();
        } else {
            final String[] address = new String[1];
//            Geo.coordinatesToAddress(
////                    (float) location.getLatitude(),
//                    lat,
////                    (float) location.getLongitude()
//                    lon
//            ).blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
//                @Override
//                public void accept(Pair<Boolean, String> pair) throws Throwable {
//                    if (pair.first) {
//                        address[0] = pair.second;
//                    }
//                }
//            });
//            s[0] += "Местоположение:\n";
            s[0] +=
//                    "- Широта: "
//                    + location.getLatitude()
//                    +
                    lat
//                    + "\n";
                    + ", ";
            s[0] +=
//                    "- Долгота: "
//                    + location.getLongitude()
//                    +
                    lon
//                    + "\n";
//                    + ", ";
            ;
//            s[0] += "- Точность: ~" + location.getAccuracy() + " метров\n";
            if (
//                    address[0] != null
                    addr != null && !addr.isEmpty()
            ) {
                s[0] += "; ";
                s[0] +=
//                        "- Адрес: "
//                        + address[0]
//                        +
                        addr
//                        + "\n"
                        ;
            }
            s[0] += "\n";
            s[0] += "\n";
        }
        Static.db.userDao().getUser().concatMap(new Function<User, Single<Boolean>>() {
            @Override
            public Single<Boolean> apply(User user) throws Throwable {
                return user.updateFromServer().map(new Function<Pair<Boolean, String>, Boolean>() {
                    @Override
                    public Boolean apply(Pair<Boolean, String> pair) throws Throwable {
//                        s[0] += "Сведения о пользователе:\n";
//                        s[0] += "  Персональные данные:\n";
//                        s[0] += "  " + "- Фамилия: " + user.getSurname() + "\n";
//                        if (user.getName() != null && !user.getName().isEmpty()) {
//                            s[0] += "  - Имя: " + user.getName() + "\n";
//                        }
//                        if (user.getMidname() != null && !user.getMidname().isEmpty()) {
//                            s[0] += "  - Отчество: " + user.getMidname() + "\n";
//                        }
                        s[0] += "- ФИО: " + Static.makeFullName(
                                user.getSurname(),
                                user.getName(),
                                user.getMidname()
                        )
                        + "\n"
                        ;
//                        s[0] += "  Способы связи:\n";
                        s[0] +=
//                                "  "
//                                +
                                "- Телефон: " + user.getPhone();
                        if (user.getOnlySMS()) {
                            s[0] += " (только SMS)";
                        }
                        s[0] += "\n";
//                        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
//                            if (!DevSettings.HIDE_DUMMY_EMAIL || !user.getEmail().endsWith("@localhost")) {
//                                s[0] += "  - E-mail: " + user.getEmail() + "\n";
//                            }
//                        }
                        if (user.getCarBrand() != null && !user.getCarBrand().isEmpty()
                            || user.getCarNumber() != null && !user.getCarNumber().isEmpty()) {
//                            s[0] += "  Транспортное средство:\n";
                            s[0] += "- ТС: ";

                            if (user.getCarNumber() != null && !user.getCarNumber().isEmpty()) {
                                s[0] +=
//                                        "  - Номер транспортного средства: "
//                                        +
                                        user.getCarNumber()
//                                                + "\n"
                                ;
                            }
                            if (user.getCarBrand() != null && !user.getCarBrand().isEmpty()) {
                                if (user.getCarNumber() != null && !user.getCarNumber().isEmpty()) {
                                    s[0] += ", ";
                                }
                                s[0] +=
//                                        "  - Марка/тип транспортного средства: "
//                                                +
                                                user.getCarBrand()
//                                                + "\n"
                                                ;
                            }
                            s[0] += "\n";
                        }
//                        if (user.getOsago() != null && !user.getOsago().isEmpty()
//                            || user.getSnils() != null && !user.getSnils().isEmpty()
//                            || user.getMedPolis() != null && !user.getMedPolis().isEmpty()) {
//                            s[0] += "  Документы:\n";
//                            if (user.getOsago() != null && !user.getOsago().isEmpty()) {
//                                s[0] += "  - ОСАГО: " + user.getOsago() + "\n";
//                            }
//                            if (user.getSnils() != null && !user.getSnils().isEmpty()) {
//                                s[0] += "  - СНИЛС: " + user.getSnils() + "\n";
//                            }
//                            if (user.getMedPolis() != null && !user.getMedPolis().isEmpty()) {
//                                s[0] += "  - Медицинский полис: " + user.getMedPolis() + "\n";
//                            }
//                        }
                        if (
//                                user.getBirthday() != null && !user.getBirthday().isEmpty()
//                                ||
                                user.getDisabilityCategory() != null
                                && !user.getDisabilityCategory().isEmpty()
                                && !user.getDisabilityCategory().equals("-")
                        ) {
//                            s[0] += "  Прочая информация:\n";
//                            if (user.getBirthday() != null && !user.getBirthday().isEmpty()) {
//                                s[0] += "  - День рождения: "
//                                        + user.getBirthdayAsLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.uuuu"))
//                                        + "\n";
//                            }
//                            if (user.getDisabilityCategory() != null
//                                    && !user.getDisabilityCategory().isEmpty()
//                                    && !user.getDisabilityCategory().equals("-")) {
                            s[0] += "- Категория инвалидности: " + user.getDisabilityCategory() + "\n";
//                            }
                        }

                        return pair.first;
                    }
                });
            }
        }).blockingSubscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean updated) throws Throwable {
                if (updated) {
                    // ???
                } else {

                }
            }
        });
        s[0] += "\nОтправлено из мобильного приложения АНО АДСА";
        SmsManager smsManager;
        SubscriptionManager subscriptionManager = null;
        Integer smsSubscriptionId = null;
        PackageManager packageManager = getApplicationContext().getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            subscriptionManager = getApplicationContext().getSystemService(SubscriptionManager.class);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            subscriptionManager = SubscriptionManager.from(getApplicationContext());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            smsSubscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId();
        }
        if (
                Static.checkPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
                && (
                        smsSubscriptionId == null
                        || smsSubscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID
                )
        ) {
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptionInfoList == null || subscriptionInfoList.isEmpty()) {
//                Toast.makeText(
//                        getApplicationContext(),
//                        "Не найдено ни одной сотовой подписки для отправки SMS",
//                        Toast.LENGTH_LONG
//                ).show();
//                return Result.failure(MessageData.make("No subscriptions found to send SMS"));
                // Continue without subscription id, lower this would create smsmanager with getDefault
            } else {
                smsSubscriptionId = subscriptionInfoList.get(0).getSubscriptionId();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            smsManager = getApplicationContext().getSystemService(SmsManager.class);
            if (
                    smsSubscriptionId != null &&
                    smsSubscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID
//                && packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_MESSAGING)
            ) {
                smsManager = smsManager.createForSubscriptionId(smsSubscriptionId);
            }
        } else {
            if (smsSubscriptionId != null
                && smsSubscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID
                    // This condition is required, otherwise compiler complains
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
            ) {
                smsManager = SmsManager.getSmsManagerForSubscriptionId(smsSubscriptionId);
            } else {
                smsManager = SmsManager.getDefault();
            }
        }
        ArrayList<String> parts = smsManager.divideMessage(s[0]);
        smsManager.sendMultipartTextMessage(
                "112",
                null,
                parts,
                null,
                null
        );
        return Result.success(MessageData.make("SMS sending has been successfully started"));
    }

//    public static void scheduleNextWorkRequest(@NonNull Context context) {
//        scheduleNextWorkRequest(false, context);
//    }

    public static void scheduleNextWorkRequest(
//            boolean immediate,
            @NonNull Context context
    ) {
//        NetworkRequest.Builder nrBuilder = new NetworkRequest.Builder()
//                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            nrBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
//        }

//        NetworkRequest networkRequest = nrBuilder.build();

//        Constraints constraints = new Constraints.Builder()
////                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .setRequiredNetworkRequest(networkRequest, NetworkType.CONNECTED)
//                .build();

//        OneTimeWorkRequest nextWorkRequest = new OneTimeWorkRequest
//                .Builder(CheckMessageCountWorker.class)
//                .setInitialDelay(30, TimeUnit.SECONDS)
//                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
//                .setConstraints(constraints)
//                .build();

        OneTimeWorkRequest.Builder nwrBuilder = new OneTimeWorkRequest
                .Builder(SosButtonWorker.class)
//                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
//                .setConstraints(constraints)
                ;

//        if (!immediate) {
//            nwrBuilder = nwrBuilder.setInitialDelay(30, TimeUnit.SECONDS);
//        }

        OneTimeWorkRequest nextWorkRequest = nwrBuilder.build();

        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                "sosButtonWorker",
                ExistingWorkPolicy.REPLACE,
                nextWorkRequest
        );
    }

    public static LocalDateTime getLastUpdatedIncidentMap() {
        return lastUpdatedIncidentMap;
    }

    @Override
    public void onStopped() {

    }
}
