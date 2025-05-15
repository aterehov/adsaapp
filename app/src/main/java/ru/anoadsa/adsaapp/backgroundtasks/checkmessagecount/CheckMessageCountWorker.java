package ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.functions.Consumer;
import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.backgroundtasks.MessageData;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.models.data.IncidentMap;
import ru.anoadsa.adsaapp.ui.activities.menu.MenuActivity;

public class CheckMessageCountWorker extends Worker {


    public CheckMessageCountWorker(Context context, WorkerParameters params) {
        super(context, params);
        System.out.println("WORKER CONSTRUCTOR WAS RUN");
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

    @NonNull
    @Override
    public Result doWork() {
        // TODO do not do anything if user is not logged in
        // TODO improve performance: request data on incidents in parallel, not one incident after another
//        sendDebugNotification();
        System.out.println("WORKER checking notification permission");
        if (!Static.checkPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)) {
            System.out.println("WORKER notification permission not found, rescheduling and failing");
            scheduleNextWorkRequest(getApplicationContext());
            return Result.failure(
//                    Data.fromByteArray("Notification permission is not granted".getBytes())
                    MessageData.make("Notification permission is not granted")
            );
//            return Result.retry();
        }
        // Init
        System.out.println("WORKER initializing db and maps");
        Static.initDb(getApplicationContext());
        if (incidentMap == null) {
            incidentMap = new IncidentMap();
        }
        if (incidentMapDb == null) {
            incidentMapDb = new IncidentMap();
        }
//        String lastError = "";
        // if incident map is not loaded or it's time for update, get from server
        System.out.println("WORKER checking if update needed");
        if (incidentMap.getType() == null
            || !incidentMap.getType().equals("fullInfo")
            || lastUpdatedIncidentMap == null
            || Duration.between(lastUpdatedIncidentMap, LocalDateTime.now()).toHours() >= 1
            || incidentMapUpdateRequested) {
//            incidentMap.updateFromServer().blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
//                @Override
//                public void accept(Pair<Boolean, String> pair) throws Throwable {
//                    if (pair.first) {
//                        // No autosave to db because incidentMap only holds basic info here
////                        // Auto refresh info in db
////                        incidentMap.saveAllToDb().blockingSubscribe();
//                        lastUpdatedIncidentMap = LocalDateTime.now();
//                    } else {
////                        incidentMap.loadAllFromDb().blockingSubscribe();
//                        lastError = pair.second;
//                    }
//                }
//            });
            System.out.println("WORKER update needed, updating incidentmap");
//            do {
//                System.out.println("WORKER detected incidentMap update requested, updating");
//                incidentMapUpdateRequested = false;
            incidentMapUpdateRequested = false;
            updateIncidentMap();
//            } while (incidentMapUpdateRequested);
//            updateIncidentMap();

            // basic info should be loaded now
        }
        // if after loading from server incidentMap is still not loaded, fail
//        if (incidentMap.getType() == null) {
//            return Result.failure(
////            Data.fromByteArray(("Failed to make first load of incident list from server: " + lastError).getBytes())
//                MessageData.make("Failed to make first load of incident list from server: " + lastError)
//            );
//        }
//        // if only basic info is loaded, load full info
//        if (incidentMap.getType().equals("basicInfo")) {
//            incidentMap.loadAllFullInfo().blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
//                @Override
//                public void accept(Pair<Boolean, String> pair) throws Throwable {
//                    if (pair.first) {
//
//                    } else {
//                        lastError = pair.second;
//                    }
//                }
//            });
//            // full info should be loaded now
//        }
//        // if still only basic info is loaded, it's a failure
//        if (incidentMap.getType().equals("basicInfo")) {
//            return Result.failure(
////            Data.fromByteArray(("Failed to load full incident info from server: " + lastError).getBytes())
//                MessageData.make("Failed to load full incident info from server: " + lastError)
//            );
//        }

        if (incidentMap.getType() == null || !incidentMap.getType().equals("fullInfo")) {
            System.out.println("WORKER incidentmap fullinfo not loaded, rescheduling and failing");
            scheduleNextWorkRequest(getApplicationContext());
            return Result.failure(
//                    Data.fromByteArray(lastError.getBytes())
                    MessageData.make(lastError)
            );
        }

        System.out.println("WORKER loading incidentmapdb");
        incidentMapDb.loadAllFromDb().blockingSubscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) throws Throwable {
                System.out.println("WORKER incidentmapdb loaded, success = " + success);
                if (success) {

                } else {

                }
            }
        });

        if (incidentMapDb.getType() == null || !incidentMapDb.getType().equals("chatCountOnly")) {
            System.out.println("WORKER wrong incidentmapdb type, rescheduling and failing");
            scheduleNextWorkRequest(getApplicationContext());
            return Result.failure(
//                    Data.fromByteArray("Failed to load incident info from DB".getBytes())
                    MessageData.make("Failed to load incident info from DB")
            );
        }

//        System.out.println("WORKER loading incidentmap message count");
//        incidentMap.loadAllMessageCount().blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
//            @Override
//            public void accept(Pair<Boolean, String> pair) throws Throwable {
//                System.out.println("WORKER loaded incidentmap message count, first = " + pair.first + ", second = " + pair.second);
//                if (pair.first) {
//
//                } else {
//
//                }
//            }
//        });

        System.out.println("WORKER making set of all ids");
        HashSet<String> allIds = new HashSet<String>(incidentMap.getAllIds());
        allIds.addAll(incidentMapDb.getAllIds());
        System.out.println("WORKER set of all ids is " + allIds);

        System.out.println("WORKER handling ids from set");
        for (String id: allIds) {
            System.out.println("WORKER current id is " + id);
            // if in incidentmap but not in db, save to db
            if (incidentMap.containsId(id) && !incidentMapDb.containsId(id)) {
                System.out.println("WORKER id in incidentmap but not in db, saving to db");
                Incident i = incidentMap.getMap().get(id);

                System.out.println("WORKER loading message count for id " + id);
                i.loadChatMessagesCount().blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {

                        } else {
                            lastError = pair.second;
                        }
                    }
                });

                if (i.getMessageCount() == -1) {
                    System.out.println("WORKER failed to load message count for id " + id);
                    System.out.println("WORKER error was " + lastError);
                    continue;
                }

                System.out.println("WORKER loaded message count for id " + id + ", saving to db");

                i.setViewedMessageCount(i.getMessageCount());
                i.saveToDb().blockingSubscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Throwable {
                        System.out.println("WORKER saved incident to db, result = " + success);
                        if (success) {

                        } else {

                        }
                    }
                });

                System.out.println("WORKER saved new id " + id + " to db");
            } else if (!incidentMap.containsId(id) && incidentMapDb.containsId(id)) {
                // if in db but not in incidentmap, refresh incidentmap
                System.out.println("WORKER incident in db but not in incidentmap, this is a new one, updating incidentmap");
                updateIncidentMap();
            } else {
                System.out.println("WORKER incident both in map and db");
                Incident i = incidentMap.getMap().get(id);
                Incident idb = incidentMapDb.getMap().get(id);


                // TODO simplify if-else clauses below
                if (!idb.isCanWriteChat() && !i.isCanWriteChat()) {
                    System.out.println("WORKER incident is locked everywhere, ignoring it");
                    continue;
                } else if (idb.isCanWriteChat() && !i.isCanWriteChat()) {
                    System.out.println("WORKER incident locked on server but not in db, sending notification if needed and updating in db");
                    System.out.println("WORKER loading number of messages for incident " + id);
                    i.loadChatMessagesCount().blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
                        @Override
                        public void accept(Pair<Boolean, String> pair) throws Throwable {
                            if (pair.first) {

                            } else {

                            }
                        }
                    });

                    System.out.println("WORKER loaded message count, checking if notification needed");
                    if (i.getMessageCount() > idb.getViewedMessageCount()) {
                        // TODO Send notification
                        System.out.println("WORKER sending notification");
                        sendNotification(i, i.getMessageCount() - idb.getViewedMessageCount());
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder()
                    }
//                    idb.setViewedMessageCount(i.getMessageCount());
                    System.out.println("WORKER updating and locking in db");
                    idb.setCanWriteChat(false);
                    idb
//                    i
                            .saveToDb().blockingSubscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean success) throws Throwable {
                            System.out.println("WORKER locked in db, success = " + success);
                            if (success) {

                            } else {
                                // Failed to update data in db, notification will be sent again next time
                            }
                        }
                    });
                } else {
                    // TODO send notification
                    System.out.println("WORKER incident is unlocked");
                    System.out.println("WORKER loading number of messages for incident " + id);
                    i.loadChatMessagesCount().blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
                        @Override
                        public void accept(Pair<Boolean, String> pair) throws Throwable {
                            if (pair.first) {

                            } else {

                            }
                        }
                    });

                    System.out.println("WORKER loaded message count, checking if notification needed");

                    if (i.getMessageCount() > idb.getViewedMessageCount()) {
                        System.out.println("WORKER notification sending needed, sending");
                        sendNotification(i, i.getMessageCount() - idb.getViewedMessageCount());
//                        NotificationCompat.Builder builder = new NotificationCompat.Builder()
                    }

                    if (!idb.isCanWriteChat()) {
                        System.out.println("WORKER incident locked in db but unlocked on server, unlocking in db");
                        idb.setCanWriteChat(true);
                        idb.saveToDb().blockingSubscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean success) throws Throwable {
                                if (success) {

                                } else {

                                }
                            }
                        });
                    }
                }
            }

        }

//        for (Incident i: incidentMap.getIncidents()) {
//            final boolean[] checkSuccess = {false};
//            i.loadChatMessagesCount().blockingSubscribe(new Consumer<Pair<Boolean, String>>() {
//                @Override
//                public void accept(Pair<Boolean, String> pair) throws Throwable {
//                    if (pair.first) {
//                        checkSuccess[0] = true;
//                    } else {
//                        // failed to check new messages, go to next incident
//
//                    }
//                }
//            });
//            if (!checkSuccess[0]) {
//                continue;
//            }
//            if (!incidentMapDb.containsId(i.getId())) {
//                // New incident, save as is
//
//            }
//        }
        //
//        OneTimeWorkRequest nextWorkRequest = new OneTimeWorkRequest.Builder(this.getClass())
//                .setInitialDelay(30, TimeUnit.SECONDS)
//                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
//                .build();
//        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
//                "checkMessageCountWorker",
//                ExistingWorkPolicy.APPEND_OR_REPLACE,
//                nextWorkRequest
//        );
        System.out.println("WORKER id handling finished, rescheduling");
        scheduleNextWorkRequest(getApplicationContext());

        System.out.println("WORKER rescheduled worker, finishing");

        // TODO fix return type
        return Result.success(
//                Data.fromByteArray("Sent notifications and scheduled next run successfully".getBytes())
                MessageData.make("Sent notifications and scheduled next run successfully")
        );
    }

    public static void scheduleNextWorkRequest(@NonNull Context context) {
        scheduleNextWorkRequest(false, context);
    }

    public static void scheduleNextWorkRequest(boolean immediate, @NonNull Context context) {
        NetworkRequest.Builder nrBuilder = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nrBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }

        NetworkRequest networkRequest = nrBuilder.build();

        Constraints constraints = new Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiredNetworkRequest(networkRequest, NetworkType.CONNECTED)
                .build();

//        OneTimeWorkRequest nextWorkRequest = new OneTimeWorkRequest
//                .Builder(CheckMessageCountWorker.class)
//                .setInitialDelay(30, TimeUnit.SECONDS)
//                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
//                .setConstraints(constraints)
//                .build();

        OneTimeWorkRequest.Builder nwrBuilder = new OneTimeWorkRequest
                .Builder(CheckMessageCountWorker.class)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .setConstraints(constraints);

        if (!immediate) {
            nwrBuilder = nwrBuilder.setInitialDelay(30, TimeUnit.SECONDS);
        }

        OneTimeWorkRequest nextWorkRequest = nwrBuilder.build();

        System.out.println("!!!!! SCHEDULING WORKER !!!!!");
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                "checkMessageCountWorker",
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
