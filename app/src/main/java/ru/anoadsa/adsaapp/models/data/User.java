package ru.anoadsa.adsaapp.models.data;

import android.util.Pair;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.anoadsa.adsaapp.Net;
import ru.anoadsa.adsaapp.Static;

@Entity
public class User {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "auth_token")
    private String authToken;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "auth_timestamp")
    private long authTimestamp = 0;

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "surname")
    private String surname;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "midname")
    private String midname;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "carBrand")
    private String carBrand;

    @ColumnInfo(name = "carNumber")
    private String carNumber;

    @ColumnInfo(name = "osago")
    private String osago;

    @ColumnInfo(name = "snils")
    private String snils;

    @ColumnInfo(name = "medPolis")
    private String medPolis;

    @ColumnInfo(name = "birthday")
    private String birthday;

    @ColumnInfo(name = "disability_category")
    private String disabilityCategory = "-";

    @ColumnInfo(name = "only_SMS")
    private boolean onlySMS = false;

    public String getDisabilityCategory() {
        return disabilityCategory;
    }

    public boolean getOnlySMS() {
        return onlySMS;
    }

    public void setDisabilityCategory(String category) {
        disabilityCategory = category;
    }

    public void setOnlySMS(boolean onlySMS) {
        this.onlySMS = onlySMS;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMidname() {
        return midname;
    }

    public void setMidname(String midname) {
        this.midname = midname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getOsago() {
        return osago;
    }

    public void setOsago(String osago) {
        this.osago = osago;
    }

    public String getSnils() {
        return snils;
    }

    public void setSnils(String snils) {
        this.snils = snils;
    }

    public String getMedPolis() {
        return medPolis;
    }

    public void setMedPolis(String medPolis) {
        this.medPolis = medPolis;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public LocalDate getBirthdayAsLocalDate() {
//        return birthday;
        if (birthday == null) {
            return null;
        }
        return LocalDate.parse(birthday);
    }

    public void setBirthdayFromLocalDate(LocalDate birthday) {
        if (birthday == null) {
            this.birthday = null;
        } else {
            this.birthday = birthday.toString();
        }
    }

    public User() {
        this.id = 1;
    }

    public String getAuthToken() {
        return authToken;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public long getAuthTimestamp() {
        return authTimestamp;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthTimestamp(long timestamp) {
        authTimestamp = timestamp;
    }

    public Single<Boolean> saveChanges() {
        return Static.db.userDao().editUser(this);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
//        return Static.db.userDao().editUser(this);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
//        return Static.db.userDao().editUser(this);
    }

    public Single<Pair<Boolean, String>> updateFromServer() {
        return Static.getAuthorizedHeadersMap().concatMap(new Function<Map<String, String>, Single<Pair<Boolean, String>>>() {
            @Override
            public Single<Pair<Boolean, String>> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/profile/get",
                            "GET",
                            headers,
                            null,
                            null
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return Single.just(new Pair<Boolean, String>(false, "Ошибка сети"));
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {
                    JsonObject profile = new Gson().fromJson(response.body().string(), JsonObject.class);
                    setSurname(profile.get("surname").getAsString());
                    if (!profile.get("name").isJsonNull()) {
                        setName(profile.get("name").getAsString());
                    } else {
                        setName("");
                    }
                    if (!profile.get("patronymic").isJsonNull()) {
                        setMidname(profile.get("patronymic").getAsString());
                    } else {
                        setMidname("");
                    }
//                    setPhone(profile.get("phone").getAsString());
                    if (!profile.get("email").isJsonNull()) {
                        setEmail(profile.get("email").getAsString());
                    } else {
                        setEmail("");
                    }
                    if (!profile.get("carBrand").isJsonNull()) {
                        setCarBrand(profile.get("carBrand").getAsString());
                    } else {
                        setCarBrand("");
                    }
                    if (!profile.get("carNumber").isJsonNull()) {
                        setCarNumber(profile.get("carNumber").getAsString());
                    } else {
                        setCarNumber("");
                    }
                    if (!profile.get("osago").isJsonNull()) {
                        setOsago(profile.get("osago").getAsString());
                    } else {
                        setOsago("");
                    }
                    if (!profile.get("snils").isJsonNull()) {
                        setSnils(profile.get("snils").getAsString());
                    } else {
                        setSnils("");
                    }
                    if (!profile.get("medPolis").isJsonNull()) {
                        setMedPolis(profile.get("medPolis").getAsString());
                    } else {
                        setMedPolis("");
                    }
                    if (!profile.get("birthday").isJsonNull()) {
                        birthday = profile.get("birthday").getAsString();
                    } else {
                        birthday = null;
                    }
                    return saveChanges().map(new Function<Boolean, Pair<Boolean, String>>() {
                        @Override
                        public Pair<Boolean, String> apply(Boolean success) throws Throwable {
                            if (success) {
                                return new Pair<Boolean, String>(true, null);
                            } else {
                                return new Pair<Boolean, String>(false, "Не удалось сохранить профиль пользователя в базу данных");
                            }
                        }
                    });
                } else {
                    return Single.just(pair);
                }
            }
        });
    }

    public Single<Pair<Boolean, String>> saveToServer() {
        FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add("surname", surname)
                .add("name", name)
                .add("patronymic", midname)
                .add("email", email)
                .add("carBrand", carBrand)
                .add("carNumber", carNumber)
                .add("osago", osago)
                .add("snils", snils)
                .add("medPolis", medPolis)
//                .add("birthday", birthday)
                .add("password", "");
//                .build();

        if (birthday != null) {
            bodyBuilder.add("birthday", birthday);
        }

        FormBody body = bodyBuilder.build();

        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/profile/save",
                            "PUT",
                            headers,
                            null,
                            body
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
//                if (pair.first) {
//
//                } else {
//
//                }
                return pair;
            }
        });
    }

    public Single<Pair<Boolean, String>> changePassword(String newPassword) {
        FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add("surname", surname)
                .add("name", name)
                .add("patronymic", midname)
                .add("email", email)
                .add("carBrand", carBrand)
                .add("carNumber", carNumber)
                .add("osago", osago)
                .add("snils", snils)
                .add("medPolis", medPolis)
//                .add("birthday", "")
                .add("password", newPassword);
//                .build();

        if (birthday != null) {
            bodyBuilder.add("birthday", birthday);
        }

        FormBody body = bodyBuilder.build();

        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/profile/save",
                            "PUT",
                            headers,
                            null,
                            body
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
//                if (pair.first) {
//
//                } else {
//
//                }
                return pair;
            }
        });
    }

    // TODO this can be static?
    public Single<Boolean> create() {
        return Static.db.userDao().newUser();
    }

    public static Single<Pair<Boolean, String>> register(String phone, String password, String surname,
                                                  String name, String patronymic, String email) {
        FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add("phone", Static.removePhoneSymbols(phone))
                .add("password", password)
                .add("surname", surname);

        if (name != null && !name.isEmpty()) {
            bodyBuilder.add("name", name);
        }
        if (patronymic != null && !patronymic.isEmpty()) {
            bodyBuilder.add("patronymic", patronymic);
        }
        if (email != null && !email.isEmpty()) {
            bodyBuilder.add("email", email);
        }

        RequestBody body = bodyBuilder.build();

        return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> call() throws Exception {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/register",
                            "POST",
                            null,
                            null,
                            body
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {

                } else {

                }
                return pair;
            }
        });
    }

    public static Single<Pair<Boolean, String>> confirmRegistration(String phone, String code) {
        RequestBody body = new FormBody.Builder()
                .add("phone", phone)
                .add("code", code)
                .build();
        return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> call() throws Exception {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/register/confirm",
                            "POST",
                            null,
                            null,
                            body
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {

                } else {

                }
                return pair;
            }
        });
    }

    public static Single<Pair<Boolean, String>> resendRegistrationSms(String phone) {
        RequestBody body = new FormBody.Builder()
                .add("phone", Static.removePhoneSymbols(phone))
                .build();
        return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> call() throws Exception {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/register/resend",
                            "POST",
                            null,
                            null,
                            body
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {

                } else {

                }
                return pair;
            }
        });
    }

    public static Single<Pair<Boolean, String>> requestResetPasswordCode(String phone) {
        RequestBody body = new FormBody.Builder()
                .add("phone", Static.removePhoneSymbols(phone))
                .build();

        return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> call() throws Exception {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/password/sendcode",
                            "POST",
                            null,
                            null,
                            body
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {

                } else {

                }
                return pair;
            }
        });
    }

    public static Single<Pair<Boolean, String>> resetPassword(String phone, String password,
                                                              String code) {
        RequestBody body = new FormBody.Builder()
                .add("phone", Static.removePhoneSymbols(phone))
                .add("password", password)
                .add("password_confirm", password)
                .add("code", code)
                .build();

        return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> call() throws Exception {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/password/reset",
                            "POST",
                            null,
                            null,
                            body
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {

                } else {

                }
                return pair;
            }
        });
    }

    public Single<Pair<Boolean, String>> login() {
        return login(phone, password);
    }

    public Single<Pair<Boolean, String>> login(String phone, String password) {
//        JsonObject body = new JsonObject();
//        body.addProperty("phone", Static.removePhoneSymbols(phone));
//        body.addProperty("password", password);
        RequestBody body = new FormBody.Builder()
                .add("phone", Static.removePhoneSymbols(phone))
                .add("password", password)
                .build();
//        Net.request(
//                Net.ADSA_SERVER + "/api/auth",
//                "POST",
//                null,
//                null,
//                body.toString()
//        );
//        Single.fromCallable(new Callable<Response>() {
//            @Override
//            public Response call() throws Exception {
//                try {
//                    return Net.request(
//                            Net.ADSA_SERVER + "/api/auth",
//                            "POST",
//                            null,
//                            null,
//                            body.toString()
//                    );
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        }).doOnSuccess()
//        }).concatMap(new Function<Response, SingleSource<Pair>>() {
//            @Override
//            public SingleSource<?> apply(Response response) throws Throwable {
//                if (response != null && response.isSuccessful() && response.body() != null) {
//                    return setAuthToken(response.body().string())
//                }
//            }
//        })
//        ArrayList<Single<?>> arr = new ArrayList<>();
//        arr.add(
        return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> call() throws Exception {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/auth",
                            "POST",
                            null,
                            null,
                            body
//                                    .toString()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Pair<Boolean, String>(false, "Ошибка сети");
                }
                Pair<Boolean, String> pair = Static.makeResponsePair(response);
                if (pair.first) {
                    setAuthToken(response.body().string());
                    setPhone(phone);

                    User.this.password = password;
                    User.this.authTimestamp = System.currentTimeMillis();
                }
                return pair;
            }
        }).concatMap(new Function<Pair<Boolean, String>, SingleSource<Pair<Boolean, String>>>() {
            @Override
            public SingleSource<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                if (pair.first) {
//                    setAuthToken(response.body().string());
//                    setPhone(phone);
                    return saveChanges().map(new Function<Boolean, Pair<Boolean, String>>() {
                        @Override
                        public Pair<Boolean, String> apply(Boolean success) throws Throwable {
//                            return response;
                            if (success) {
                                return new Pair<Boolean, String>(true, null);
                            } else {
                                return new Pair<Boolean, String>(false, "Ошибка сохранения параметров авторизации");
                            }
                        }
                    });
//                    return setAuthToken(response.body().toString()).concatMap(new Function<Boolean, SingleSource<Boolean>>() {
//                        @Override
//                        public SingleSource<Boolean> apply(Boolean success) throws Throwable {
//                            return null;
//                        }
//                    });
                } else {
                    return Single.just(pair);
                }
            }
        });
//        arr.add()
//        Single.concat(arr);
    }
}
