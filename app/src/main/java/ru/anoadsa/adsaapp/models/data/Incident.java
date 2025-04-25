package ru.anoadsa.adsaapp.models.data;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.anoadsa.adsaapp.Net;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount.CheckMessageCountWorker;

@Entity
public class Incident {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id = "";

//    @ColumnInfo(name = "messageCount")
    @Ignore
    private int messageCount;

    @ColumnInfo(name = "viewedMessageCount")
    private int viewedMessageCount;

    @ColumnInfo(name = "canWriteChat")
    private boolean canWriteChat = true;

    public void setViewedMessageCount(int count) {
        viewedMessageCount = count;
    }

    public void setCanWriteChat(boolean can) {
        canWriteChat = can;
    }

    @Ignore
    private int docnum;

    @Ignore
    private String operator;

    @Ignore
    private String operatorText;

    @Ignore
    private String abonentText;

    @Ignore
    private String clientSurname;

    @Ignore
    private float latitude;

    @Ignore
    private float longitude;

    @Ignore
    private String address;

    @Ignore
    private String statusText;

    @Ignore
    private LocalDateTime createdDateTime;

    @Ignore
    private LocalDateTime modifiedDateTime;

    //-----------
    @Ignore
    private String status;

    @Ignore
    private String incidentType;

    @Ignore
    private String incidentTypeText;

    @Ignore
    private boolean isHurt;

    @Ignore
    private String abonent;

    @Ignore
    private String category;

    @Ignore
    private String categoryText;

    @Ignore
    private float radius;

    @Ignore
    private boolean isEyewitness;

    @Ignore
    private String clientName;

    @Ignore
    private String clientPatronymic;

    @Ignore
    private String carBrand;

    @Ignore
    private String carNumber;

    @Ignore
    private String osago;

    @Ignore
    private String medPolis;

    @Ignore
    private String snils;

    @Ignore
    private LocalDate birthday;

    @Ignore
    private String disabilityCategory;

    @Ignore
    private String disabilityCategoryText;

    @Ignore
    private String description;

    @Ignore
    private ArrayList<IncidentMediaFile> orderMediaFile;

    @Ignore
    private ArrayList<IncidentChatMessage> icmPreprocessList = new ArrayList<IncidentChatMessage>();

    @Ignore
    private String phone;

    @Ignore
    private boolean fullInfoLoaded;

    @Ignore
    private boolean imagesDownloaded;

    @Ignore
    private ArrayList<IncidentChatMessage> chatMessages;

    public int getViewedMessageCount() {
        return viewedMessageCount;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public Single<Pair<Boolean, String>> loadChatMessagesCount() {
        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/query/chat/" + id + "/message/count",
                            "GET",
                            headers,
                            null,
                            null
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    messageCount = -1;
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {
                    messageCount = new Gson().fromJson(response.body().string(), JsonObject.class)
                            .get("count").getAsInt();
                } else {
                    messageCount = -1;
                }
                return pair;
            }
        });
    }

    public Single<Boolean> loadViewedMessageCountFromDb() {
        return Static.db.incidentDao().getIncident(id).map(new Function<Pair<Boolean, Incident>, Boolean>() {
            @Override
            public Boolean apply(Pair<Boolean, Incident> pair) throws Throwable {
                if (pair.first) {
                    viewedMessageCount = pair.second.getViewedMessageCount();
                }
                return pair.first;
            }
        });
    }

    public Single<Boolean> saveToDb() {
//        if (!fullInfoLoaded) {
//            return Single.just(false);
//        }
        return Static.db.incidentDao().saveIncident(this);
    }

    public Single<Pair<Boolean, String>> sendMessage(String message) {
        FormBody body = new FormBody.Builder()
                .add("message", message)
                .build();

        return Static.getAuthorizedHeadersMap()
                .concatMap(new Function<Map<String, String>, Single<Pair<Boolean, String>>>() {
                    @Override
                    public Single<Pair<Boolean, String>> apply(Map<String, String> headers) throws Throwable {
                        Response response;
                        try {
                            response = Net.request(
                                    Net.ADSA_SERVER + "/api/query/chat/" + id + "/message/send",
                                    "POST",
                                    headers,
                                    null,
                                    body
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                            return Single.just(new Pair<Boolean, String>(false, "Ошибка сети"));
                        }
                        Pair<Boolean, String> pair = Static.makeResponsePair(response);
//                        if (pair.first) {
//                            return Incident.this.getNewMessagesFromServer();
//                        } else {
//                            return Single.just(pair);
//                        }
                        return Single.just(pair);
                    }
                });
//                .map(new Function<Map<String, String>, Pair<Boolean, String>>() {
//            @Override
//            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
//
//            }
//        })
    }

    public Single<Pair<Boolean, String>> getNewMessagesFromServer() {
        // WARNING: old messages are NOT refreshed with this method
        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/query/chat/" + id + "/message/all",
                            "GET",
                            headers,
                            null,
                            null
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {
                    if (chatMessages == null) {
                        chatMessages = new ArrayList<IncidentChatMessage>();
                    }
                    JsonArray jsonArray = new Gson().fromJson(response.body().string(), JsonArray.class);
                    icmPreprocessList.clear();
                    for (int i = chatMessages.size(); i < jsonArray.size(); ++i) {
                        JsonObject jsonMsg = jsonArray.get(i).getAsJsonObject();
                        icmPreprocessList.add(new IncidentChatMessage(jsonMsg));
                    }
                    icmPreprocessList.sort(IncidentChatMessage.sortByDateOldFirst);
                    chatMessages = icmPreprocessList;
                }
                return pair;
            }
        });
    }

    public ArrayList<IncidentChatMessage> getChatMessages() {
        return chatMessages;
    }

    public boolean isFullInfoLoaded() {
        return fullInfoLoaded;
    }

    public boolean areImagesDownloaded() {
        return imagesDownloaded;
    }

    public Single<Pair<Boolean, String>> loadFullInfo() {
        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/query/" + id,
                            "GET",
                            headers,
                            null,
                            null
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {
                    JsonObject info = new Gson().fromJson(response.body().string(), JsonObject.class);

                    id = info.get("id").getAsString();
                    docnum = info.get("docnum").getAsInt();
                    if (info.get("operator").isJsonNull()) {
                        operator = "";
                    } else {
                        operator = info.get("operator").getAsString();
                    }
                    if (info.get("operator__Text").isJsonNull()) {
                        operatorText = "";
                    } else {
                        operatorText = info.get("operator__Text").getAsString();
                    }
                    latitude = info.get("latitude").getAsFloat();
                    longitude = info.get("longitude").getAsFloat();
                    category = info.get("category").getAsString();
                    categoryText = info.get("category__Text").getAsString();
                    radius = info.get("radius").getAsFloat();
                    clientSurname = info.get("client_surname").getAsString();
                    if (info.get("client_name").isJsonNull()) {
                        clientName = "";
                    } else {
                        clientName = info.get("client_name").getAsString();
                    }
                    if (info.get("client_patronymic").isJsonNull()) {
                        clientPatronymic = "";
                    } else {
                        clientPatronymic = info.get("client_patronymic").getAsString();
                    }
                    isHurt = info.get("is_hurt").getAsBoolean();
                    isEyewitness = info.get("is_eyewitness").getAsBoolean();
                    phone = info.get("phone").getAsString();
                    address = info.get("address").getAsString();
                    if (info.get("description").isJsonNull()) {
                        description = "";
                    } else {
                        description = info.get("description").getAsString();
                    }
                    if (info.get("carBrand").isJsonNull()) {
                        carBrand = "";
                    } else {
                        carBrand = info.get("carBrand").getAsString();
                    }
                    if (info.get("carNumber").isJsonNull()) {
                        carNumber = "";
                    } else {
                        carNumber = info.get("carNumber").getAsString();
                    }
                    if (info.get("osago").isJsonNull()) {
                        osago = "";
                    } else {
                        osago = info.get("osago").getAsString();
                    }
                    if (info.get("medPolis").isJsonNull()) {
                        medPolis = "";
                    } else {
                        medPolis = info.get("medPolis").getAsString();
                    }
                    if (info.get("snils").isJsonNull()) {
                        snils = "";
                    } else {
                        snils = info.get("snils").getAsString();
                    }
                    if (info.get("birthday").isJsonNull()) {
                        birthday = null;
                    } else {
                        birthday = LocalDate.parse(info.get("birthday").getAsString());
                    }
                    abonent = info.get("abonent").getAsString();
                    abonentText = info.get("abonent__Text").getAsString();
                    if (info.get("disabilityCategory").isJsonNull()) {
                        disabilityCategory = "";
                    } else {
                        disabilityCategory = info.get("disabilityCategory").getAsString();
                    }
                    if (info.get("disabilityCategory__Text").isJsonNull()) {
                        disabilityCategoryText = "-";
                    } else {
                        disabilityCategoryText = info.get("disabilityCategory__Text").getAsString();
                    }
                    incidentType = info.get("incidentType").getAsString();
                    incidentTypeText = info.get("incidentType__Text").getAsString();
                    status = info.get("status").getAsString();
                    statusText = info.get("status__Text").getAsString();
                    canWriteChat = info.get("canWriteChat").getAsBoolean();
                    // TODO media files
                    if (orderMediaFile != null) {
                        orderMediaFile.clear();
                    } else {
                        orderMediaFile = new ArrayList<IncidentMediaFile>();
                    }
                    JsonArray mediaFiles = info.get("mediaFiles").getAsJsonArray();
                    for (JsonElement j: mediaFiles) {
                        JsonObject jj = j.getAsJsonObject();
                        IncidentMediaFile imf = new IncidentMediaFile();
                        imf.fromJsonObject(jj);
                        orderMediaFile.add(imf);
                    }

                    createdDateTime = LocalDateTime.parse(info.get("createdDateTime").getAsString());
                    modifiedDateTime = LocalDateTime.parse(info.get("modifiedDateTime").getAsString());

                    fullInfoLoaded = true;
//                } else {
//                    return pair;
                }
                return pair;
            }
        });
    }

    public Incident() {

    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // TODO use setters instead
    public void editAll(String id, String status, String incidentType, boolean isHurt, String abonent,
                        String category, float radius, float latitude, float longitude, String address,
                        boolean isEyewitness, String clientSurname, String clientName,
                        String clientPatronymic, String carBrand, String carNumber, String osago,
                        String medPolis, String snils, LocalDate birthday, String disabilityCategory,
                        String description, ArrayList<IncidentMediaFile> orderMediaFile) {
        this.id = id;
        this.status = status;
        this.incidentType = incidentType;
        this.isHurt = isHurt;
        this.abonent = abonent;
        this.category = category;
        this.radius = radius;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.isEyewitness = isEyewitness;
        this.clientSurname = clientSurname;
        this.clientName = clientName;
        this.clientPatronymic = clientPatronymic;
        this.carBrand = carBrand;
        this.carNumber = carNumber;
        this.osago = osago;
        this.medPolis = medPolis;
        this.snils = snils;
        this.birthday = birthday;
        this.disabilityCategory = disabilityCategory;
        this.description = description;
        this.orderMediaFile = orderMediaFile;
    }



    public Single<Pair<Boolean, String>> sendToServer(Context context) {
        if (status == null || status.isEmpty()) {
            return Single.just(new Pair<Boolean, String>(false, "Введите статус обращения"));
        } else if (abonent == null || abonent.isEmpty()) {
            return Single.just(new Pair<Boolean, String>(false, "Введите получателя обращения"));
        } else if (category == null || category.isEmpty()) {
            return Single.just(new Pair<Boolean, String>(false, "Введите категорию организации"));
        } else if (address == null || address.isEmpty()) {
            return Single.just(new Pair<Boolean, String>(false, "Введите адрес происшествия"));
        } else if (clientSurname == null || clientSurname.isEmpty()) {
            return Single.just(new Pair<Boolean, String>(false, "Введите фамилию обращающегося"));
        } else if (phone == null || phone.isEmpty()) {
            return Single.just(new Pair<Boolean, String>(false, "Не указан номер телефона"));
        }

        FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add("status", status)
                .add("abonent", abonent)
                .add("category", category)
                .add("radius", String.valueOf(radius))
                .add("latitude", String.valueOf(latitude))
                .add("longitude", String.valueOf(longitude))
                .add("address", address)
                .add("client_surname", clientSurname)
                .add("phone", phone);

        if (id != null && !id.isEmpty()) {
            bodyBuilder.add("id", id);
        }
        if (incidentType != null && !incidentType.isEmpty()) {
            bodyBuilder.add("incidentType", incidentType);
        }
        bodyBuilder.add("is_hurt", String.valueOf(isHurt));
        bodyBuilder.add("is_eyewitness", String.valueOf(isEyewitness));
//        if (clientSurname != null && !clientSurname.isEmpty()) {
//            bodyBuilder.add("client_surname", clientSurname);
//        }
        if (clientName != null
//                && !clientName.isEmpty()
        ) {
            bodyBuilder.add("client_name", clientName);
        }
        if (clientPatronymic != null
//                && !clientPatronymic.isEmpty()
        ) {
            bodyBuilder.add("client_patronymic", clientPatronymic);
        }
        if (carBrand != null
//                && !carBrand.isEmpty()
        ) {
            bodyBuilder.add("carBrand", carBrand);
        }
        if (carNumber != null
//                && !carNumber.isEmpty()
        ) {
            bodyBuilder.add("carNumber", carNumber);
        }
        if (osago != null
//                && !osago.isEmpty()
        ) {
            bodyBuilder.add("osago", osago);
        }
        if (medPolis != null
//                && !medPolis.isEmpty()
        ) {
            bodyBuilder.add("medPolis", medPolis);
        }
        if (snils != null
//                && !snils.isEmpty()
        ) {
            bodyBuilder.add("snils", snils);
        }
        if (birthday != null) {
            bodyBuilder.add("birthday", birthday.toString());
        } else {
            bodyBuilder.add("birthday", "");
        }
        // TODO: add empty disability category
        if (disabilityCategory != null
//                && !disabilityCategory.isEmpty()
        ) {
            bodyBuilder.add("disabilityCategory", disabilityCategory);
        } else {
            bodyBuilder.add("disabilityCategory", "");
        }
        if (description != null
//                && !description.isEmpty()
        ) {
            bodyBuilder.add("description", description);
        }
        // TODO: implement file attachment
//        RequestBody body = bodyBuilder.build();

//        JsonArray omfArray = new JsonArray();
        // TODO use existing orderMediaFile array instead
//        ArrayList<String> omfArray = new ArrayList<String>();

        Single<Pair<Boolean, String>> task = Single.just(new Pair<Boolean, String>(true, null));

        for (IncidentMediaFile i: orderMediaFile) {
            if (i.getId() == null || i.getId().isEmpty()) {
                task = task.concatMap(new Function<Pair<Boolean, String>, SingleSource<Pair<Boolean, String>>>() {
                    @Override
                    public SingleSource<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                        if (pair.first) {
                            i.readFileContent(context);
                            return i.uploadToServer();
                        } else {
                            return Single.just(pair);
                        }
                    }
                });
            }

//            task = task.map(new Function<Pair<Boolean, String>, Pair<Boolean, String>>() {
//                @Override
//                public Pair<Boolean, String> apply(Pair<Boolean, String> pair) throws Throwable {
//                    if (pair.first) {
//                        omfArray.add(
////                                i.formJsonObject()
//                                i.getId()
//                        );
//                    }
//                    return pair;
//                }
//            });
        }

        task = task.concatMap(new Function<Pair<Boolean, String>, SingleSource<Pair<Boolean, String>>>() {
            @Override
            public SingleSource<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                if (!pair.first) {
                    return Single.just(pair);
                } else {
//                    bodyBuilder.add("orderMediaFile", omfArray.asList().toString());
//                    bodyBuilder.add("orderMediaFile[]", omfArray.toString());
//                    for (String i: omfArray) {
//                        bodyBuilder.add("orderMediaFile", i);
//                    }
                    for (IncidentMediaFile i: orderMediaFile) {
                        bodyBuilder.add("orderMediaFile", i.getId());
                    }
//                    bodyBuilder.add("qwerty", RequestBody.create(omfArray.asList().toString()))
                    //TODO Convert from jsonarray to arraylist
//                    for (int i = 0; i < omfArray.size(); ++i) {
//                        bodyBuilder.add("orderMediaFile[" + i + "]", omfArray.get(i).getAsString());
//                    }
                    RequestBody body = bodyBuilder.build();
                    return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
                        @Override
                        public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                            Response response;
                            try {
                                response = Net.request(
                                        Net.ADSA_SERVER + "/api/query/save",
                                        "POST",
                                        headers,
                                        null,
                                        body
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                                return new Pair<Boolean, String>(false, "Ошибка сети");
                            }
                            Pair<Boolean, String> pair = Static.makeResponsePair(response);
                            if (pair.first) {
                                // this should be in viewmodel
//                                if (id == null || id.isEmpty()) {
                                    // if this is a new incident, request incidentMap update in notification service so that notifications for it are received
//                                    CheckMessageCountWorker.requestIncidentMapUpdate();
//                                }
                            }
                            return pair;
                        }
                    });
                }
            }
        });

        return task;

//        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
//            @Override
//            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
//                Response response;
//                try {
//                    response = Net.request(
//                            Net.ADSA_SERVER + "/api/query/save",
//                            "POST",
//                            headers,
//                            null,
//                            body
//                    );
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return new Pair<Boolean, String>(false, "Ошибка сети");
//                }
//                Pair<Boolean, String> pair = Static.makeResponsePair(response);
////                if (pair.first) {
////
////                }
//                return pair;
//            }
//        });
    }

    public Single<Pair<Boolean, String>> requestOperator() {
        FormBody body = new FormBody.Builder()
                .add("id", id)
                .build();

        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/query/request-operator",
                            "POST",
                            headers,
                            null,
                            body
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {
                    // ???
                } else {

                }
                return pair;
            }
        });
    }

    public Incident(@NonNull JsonObject basic) {
        id = basic.get("id").getAsString();
        docnum = basic.get("docnum").getAsInt();
        operatorText = basic.get("operator").getAsString();
        abonentText = basic.get("abonent").getAsString();
        clientSurname = basic.get("client_surname").getAsString();
        latitude = basic.get("latitude").getAsFloat();
        longitude = basic.get("longitude").getAsFloat();
        address = basic.get("address").getAsString();
        statusText = basic.get("status").getAsString();
        createdDateTime = LocalDateTime.parse(basic.get("createdDateTime").getAsString());
        modifiedDateTime = LocalDateTime.parse(basic.get("modifiedDateTime").getAsString());
    }

    public Incident(String id, int docnum, String operatorText, String abonentText,
                    String clientSurname, float latitude, float longitude, String address,
                    String statusText, String createdDateTime,
                    String modifiedDateTime) {
        this.id = id;
        this.docnum = docnum;
        this.operatorText = operatorText;
        this.abonentText = abonentText;
        this.clientSurname = clientSurname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.statusText = statusText;
        this.createdDateTime = LocalDateTime.parse(createdDateTime);
        this.modifiedDateTime = LocalDateTime.parse(modifiedDateTime);
    }

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDocnum() {
        return docnum;
    }

    public void setIncidentTypeText(String incidentTypeText) {
        this.incidentTypeText = incidentTypeText;
    }

    public void setDocnum(int docnum) {
        this.docnum = docnum;
    }

    public void setDisabilityCategoryText(String disabilityCategoryText) {
        this.disabilityCategoryText = disabilityCategoryText;
    }

    public String getOperatorText() {
        return operatorText;
    }

    public void setOperatorText(String operatorText) {
        this.operatorText = operatorText;
    }

    public String getAbonentText() {
        return abonentText;
    }

    public void setAbonentText(String abonentText) {
        this.abonentText = abonentText;
    }

    public String getClientSurname() {
        return clientSurname;
    }

    public void setClientSurname(String clientSurname) {
        this.clientSurname = clientSurname;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public LocalDateTime getModifiedDateTime() {
        return modifiedDateTime;
    }

    public void setModifiedDateTime(LocalDateTime modifiedDateTime) {
        this.modifiedDateTime = modifiedDateTime;
    }

    public String getOperator() {
        return operator;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public String getIncidentTypeText() {
        return incidentTypeText;
    }

    public boolean isHurt() {
        return isHurt;
    }

    public String getAbonent() {
        return abonent;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryText() {
        return categoryText;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isEyewitness() {
        return isEyewitness;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientPatronymic() {
        return clientPatronymic;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public String getOsago() {
        return osago;
    }

    public String getMedPolis() {
        return medPolis;
    }

    public String getSnils() {
        return snils;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getDisabilityCategory() {
        return disabilityCategory;
    }

    public String getDisabilityCategoryText() {
        return disabilityCategoryText;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<IncidentMediaFile> getOrderMediaFile() {
        return orderMediaFile;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isCanWriteChat() {
        return canWriteChat;
    }

    public void setCategoryText(String categoryText) {
        this.categoryText = categoryText;
    }



    public static int compareRecentlyCreatedFirst(@NonNull Incident a, @NonNull Incident b) {
        if (a.getCreatedDateTime().isAfter(b.getCreatedDateTime())) {
            return -1;
        } else if (a.getCreatedDateTime().isBefore(b.getCreatedDateTime())) {
            return 1;
        }
        return 0;
    }

}
