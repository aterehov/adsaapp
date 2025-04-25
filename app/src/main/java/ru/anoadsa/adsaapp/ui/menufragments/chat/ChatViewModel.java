package ru.anoadsa.adsaapp.ui.menufragments.chat;

import android.content.Context;
import android.util.Pair;

import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount.ChatNotificationManager;
import ru.anoadsa.adsaapp.models.data.Incident;

public class ChatViewModel extends ViewModel {
    private MutableLiveData<Incident> incident;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> messageSentSuccess;

    private boolean restoreState;

    private Disposable newMessagesChecker;

    public ChatViewModel() {
        incident = new MutableLiveData<Incident>(null);
        errorMessage = new MutableLiveData<String>(null);
        messageSentSuccess = new MutableLiveData<Boolean>(false);
    }

    public boolean getRestoreState() {
        return restoreState;
    }

    public void setRestoreState(boolean r) {
        restoreState = r;
    }

    public LiveData<Boolean> getMessageSentSuccess() {
        return messageSentSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setIncident(Incident incident) {
        this.incident.setValue(incident);
    }

    public LiveData<Incident> getIncident() {
        return incident;
    }

    public void hideNotification(Context context) {
        // Using docnum instead
//        Integer notificationId = ChatNotificationManager
//                .getNotificationId(
//                        incident
//                                .getValue()
//                                .getId()
//                );
//        if (incident.getValue().getDocnum() != null) {
        NotificationManagerCompat
                .from(context)
                .cancel(
//                        ChatNotificationIdsManager
//                                .getNotificationId(
//                                        incident
//                                                .getValue()
//                                                .getId()
//                                )
//                        notificationId
                        incident.getValue().getDocnum()
                );
//        }

        ChatNotificationManager.setNotificationCount(incident.getValue().getId(), 0);
    }

    public void loadFullIncidentInfo() {
        incident.getValue().loadFullInfo().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            incident.setValue(incident.getValue());
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void setViewedMessageCountInDb(int count) {
        incident.getValue().setViewedMessageCount(count);
        incident.getValue().saveToDb().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Throwable {
                        if (!success) {
                            errorMessage.setValue("Не удалось обновить число прочитанных сообщений");
                        }
                    }
                });
    }

    public void sendMessage(String message) {
        incident.getValue().sendMessage(message).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            messageSentSuccess.setValue(true);
                            getNewMessages();
                        } else {
                            messageSentSuccess.setValue(false);
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void getNewMessages() {
        incident.getValue().getNewMessagesFromServer().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
                    @Override
                    public void accept(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            // Rerun incident observer
                            incident.setValue(incident.getValue());
                        } else {
                            errorMessage.setValue(pair.second);
                        }
                    }
                });
    }

    public void startCheckingNewMessages() {
        newMessagesChecker = Observable.interval(30, TimeUnit.SECONDS).concatMapSingle(new Function<Long, Single<Pair<Boolean, String>>>() {
            @Override
            public Single<Pair<Boolean, String>> apply(Long l) throws Throwable {
                return incident.getValue().getNewMessagesFromServer();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Pair<Boolean, String>>() {
            @Override
            public void accept(Pair<Boolean, String> pair) throws Throwable {
                if (pair.first) {
                    // Rerun incident observer
                    incident.setValue(incident.getValue());
                } else {
                    errorMessage.setValue(pair.second);
                }
            }
        });
    }

    public void stopCheckingNewMessages() {
        if (newMessagesChecker != null) {
            newMessagesChecker.dispose();
        }
    }
}
