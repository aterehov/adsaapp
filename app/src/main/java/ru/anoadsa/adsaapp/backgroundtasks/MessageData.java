package ru.anoadsa.adsaapp.backgroundtasks;

import androidx.annotation.NonNull;
import androidx.work.Data;

import org.jetbrains.annotations.Contract;

import java.util.Collections;

public class MessageData {
    @NonNull
    @Contract("_ -> new")
    public static Data make(String message) {
        return new Data(Collections.singletonMap("message", message));
    }
}
