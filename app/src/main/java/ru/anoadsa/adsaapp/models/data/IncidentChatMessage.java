package ru.anoadsa.adsaapp.models.data;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import java.time.LocalDateTime;
import java.util.Comparator;

public class IncidentChatMessage {
    private String id;
    private String order;
    private LocalDateTime createdDateTime;
    private String sender;
    private int senderIndex;
    private String messageType;
    private String content;

    public static Comparator<IncidentChatMessage> sortByDateOldFirst = new Comparator<IncidentChatMessage>() {
        @Override
        public int compare(@NonNull IncidentChatMessage icm1, @NonNull IncidentChatMessage icm2) {
            if (icm1.getCreatedDateTime().isBefore(icm2.getCreatedDateTime())) {
                return -1;
            } else if (icm1.getCreatedDateTime().isAfter(icm2.getCreatedDateTime())) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    public IncidentChatMessage(@NonNull JsonObject json) {
        id = json.get("id").getAsString();
        order = json.get("order").getAsString();
        createdDateTime = LocalDateTime.parse(json.get("createdDateTime").getAsString());
        if (!json.get("sender").isJsonNull()) {
            sender = json.get("sender").getAsString();
        }
        senderIndex = json.get("senderIndex").getAsInt();
        messageType = json.get("messageType").getAsString();
        content = json.get("content").getAsString();
    };

    public String getId() {
        return id;
    }

    public String getOrder() {
        return order;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public String getSender() {
        return sender;
    }

    public int getSenderIndex() {
        return senderIndex;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }
}
