package ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount;

import android.Manifest;
import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.backgroundtasks.MessageData;

public class CheckMessageCountWatchdog extends Worker {
    public CheckMessageCountWatchdog(Context context, WorkerParameters params) {

        super(context, params);
        System.out.println("WATCHDOG CONSTRUCTOR WAS RUN");
    }

    private void sendDebugNotification() {
        System.out.println("WATCHDOG sending notification");
        if (!Static.checkPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)) {
            System.out.println("WATCHDOG no notification permission, failed to send notification");
            return;
        }

        System.out.println("WATCHDOG creating notification channel");
        Static.createChatNotificationChannel(getApplicationContext());

//        System.out.println("WATCHDOG creating intent and putting extras");
//        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
//        intent.putExtra("action", "chat");
//        intent.putExtra("incidentId", incident.getId());


//        System.out.println("WATCHDOG creating pending intent");
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                getApplicationContext(),
//                0,
//                intent,
//                PendingIntent.FLAG_IMMUTABLE
//        );


        System.out.println("WATCHDOG building notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                Static.CHAT_NOTIFICATION_CHANNEL_ID
        )
                .setSmallIcon(R.drawable.baseline_chat_24)
                .setContentTitle("DEBUG NOTIFICATION")
                .setContentText("CheckMessageCountWatchdog is running")
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
//        System.out.println("WATCHDOG getting notification id for incidentId = " + incident.getId());
//        int notificationId = ChatNotificationIdsManager.addNotificationId(incident.getId());
//        System.out.println("WATCHDOG notification id for incidentId = " + incident.getId() + " is " + notificationId);

        System.out.println("WATCHDOG finalizing notification send");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(100500, builder.build());
    }

    @NonNull
    @Override
    public Result doWork() {
//        sendDebugNotification();
        System.out.println("WATCHDOG Getting workmanager ");
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        System.out.println("WATCHDOG WorkManager " + workManager);
        System.out.println("WATCHDOG Getting LFinfo ");
        ListenableFuture<List<WorkInfo>> lfinfo =
                workManager.getWorkInfosForUniqueWork("checkMessageCountWorker");
        System.out.println("WATCHDOG LFinfo " + lfinfo);

        System.out.println("WATCHDOG Getting Linfo ");
        List<WorkInfo> linfo = null;

        try {
            linfo = lfinfo.get();
            System.out.println("WATCHDOG Linfo " + linfo);
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.out.println("WATCHDOG ExecutionException while getting linfo");
            return Result.retry();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("WATCHDOG InterruptedException while getting linfo");
            return Result.retry();
        }


        if (linfo.isEmpty()) {
            System.out.println("WATCHDOG Linfo is empty, rescheduling worker");
            CheckMessageCountWorker.scheduleNextWorkRequest(getApplicationContext());
            return Result.success(
//                    Data.fromByteArray("No scheduled work was found, scheduled a new one".getBytes())
                    MessageData.make("No scheduled work was found, scheduled a new one")
            );
        }
        System.out.println("WATCHDOG getting last workinfo");
        WorkInfo info = linfo.get(linfo.size() - 1);
        System.out.println("WATCHDOG last workinfo " + info);
        if (info.getState() == WorkInfo.State.CANCELLED) {
            System.out.println("WATCHDOG last work cancelled, rescheduling");
            CheckMessageCountWorker.scheduleNextWorkRequest(getApplicationContext());
            return Result.success(
//                    Data.fromByteArray("Found cancelled work, rescheduled it".getBytes())
                    MessageData.make("Found cancelled work, rescheduled it")
            );
        } else if (info.getState() == WorkInfo.State.RUNNING) {
            System.out.println("WATCHDOG last work is running, letting it continue and finishing");
            return Result.success(MessageData.make("Found already running worker"));
        }
        // Time difference is checked with 10% buffer to avoid rescheduling when worker is running but did not update the map yet
        if (CheckMessageCountWorker.getLastUpdatedIncidentMap() == null
            || Duration.between(
                    CheckMessageCountWorker.getLastUpdatedIncidentMap(),
                    LocalDateTime.now()
            ).toMinutes() >= 66) {
            System.out.println("WATCHDOG wrong lastUpdatedIncidentMap time, cancelling current work and rescheduling");
            // Cancel existing work
            workManager.cancelUniqueWork("checkMessageCountWorker");
            System.out.println("WATCHDOG cancelled current work");
            CheckMessageCountWorker.scheduleNextWorkRequest(getApplicationContext());
            System.out.println("WATCHDOG rescheduled work");
            return Result.success(
//                    Data.fromByteArray("IncidentMap has not been updated for more than an hour, cancelled previous worker and scheduled a new one".getBytes())
                    MessageData.make("IncidentMap has not been updated for more than an hour, cancelled previous worker and scheduled a new one")
            );
        }
        System.out.println("WATCHDOG everything is ok, no action done");
        return Result.success(
//                Data.fromByteArray("Everything seems ok with worker".getBytes())
                MessageData.make("Everything seems ok with worker")
        );
    }

    public static void schedule(@NonNull Context context) {
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

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        CheckMessageCountWatchdog.class,
                        15,
                        TimeUnit.MINUTES
                )
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS
                        )
                        .build();

        System.out.println("!!!!! SCHEDULING WATCHDOG !!!!!");

        WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(
                "checkMessageCountWatchdog",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                request
        );
    }
}
