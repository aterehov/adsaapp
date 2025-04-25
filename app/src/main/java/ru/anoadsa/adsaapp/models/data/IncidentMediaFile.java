package ru.anoadsa.adsaapp.models.data;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.anoadsa.adsaapp.Net;
import ru.anoadsa.adsaapp.Static;

public class IncidentMediaFile {
    private String id;
    private String order;
    private String name;
    private String type;
    private int size;
    private Uri uri;
    private File file;

    private byte[] fileContent;
    private boolean fileContentRead;

    private boolean addedByUser;

    public boolean isAddedByUser() {
        return addedByUser;
    }

    public void setAddedByUser(boolean added) {
        addedByUser = added;
    }

    public IncidentMediaFile() {}

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getType() {
        return type;
    }

    public String detectTypeByUri(@NonNull Context context) {
        type = context.getContentResolver().getType(uri);
        return type;
    }

    public File createFileFromUri() {
        file = new File(Objects.requireNonNull(uri.getPath()));
        return file;
    }

    public String detectNameFromFile() {
        name = file.getName();
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
        if (!name.endsWith("." + ext)) {
            name += "." + ext;
        }
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean readFileContent(@NonNull Context context) {
        if (fileContentRead) {
            return false;
        }
        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        try {
            int len = 0;
            while (len != -1) {
                len = inputStream.read(buffer);
                if (len != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        fileContent = byteBuffer.toByteArray();
        size = fileContent.length;
        fileContentRead = true;
        return true;
    }

    public void setAllInfoByUri(Context context, Uri uri) {
        setUri(uri);
        detectTypeByUri(context);
        createFileFromUri();
        detectNameFromFile();
    }

    public String getName() {
        return name;
    }

    public JsonObject makeJsonObject() {
        JsonObject omf = new JsonObject();
        omf.addProperty("id", id);
        omf.addProperty("name", name);
        omf.addProperty("type", type);
        omf.addProperty("size", size);
        return omf;
    }

    public boolean isDownloadedToCache(@NonNull Context context) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        return new File(context.getCacheDir(), id).exists();
    }

    public boolean openFileFromCache(Context context) {
        if (!isDownloadedToCache(context)) {
            return false;
        }
        file = new File(context.getCacheDir(), id);
        uri = Uri.fromFile(file);
        return true;
    }

    public Uri getUri() {
        return uri;
    }

    public Single<Pair<Boolean, String>> getFileSingle(Context context) {
        if (isDownloadedToCache(context)) {
            return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
                @Override
                public Pair<Boolean, String> call() throws Exception {
                    return new Pair<Boolean, String>(openFileFromCache(context), null);
                }
            });
        } else {
            return downloadToCache(context);
        }
    }



    public Single<Pair<Boolean, String>> copyToGallery(Context context) {
//        context.getDir()
//        Environment.getExternalStorageState();
//        String path = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PICTURES + "/";
        // TODO remove code duplication in copyToGallery and copyToDocuments
        return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> call() throws Exception {
                switch (Environment.getExternalStorageState()) {
                    case Environment.MEDIA_BAD_REMOVAL:
                        return new Pair<Boolean, String>(false, "Ошибка: диск для записи был извлечен некорректно");
                    case Environment.MEDIA_CHECKING:
                        return new Pair<Boolean, String>(false, "Ошибка: диск не готов к работе");
                    case Environment.MEDIA_EJECTING:
                        return new Pair<Boolean, String>(false, "Ошибка: диск извлекается");
                    case Environment.MEDIA_MOUNTED_READ_ONLY:
                        return new Pair<Boolean, String>(false, "Ошибка: диск доступен только для чтения");
                    case Environment.MEDIA_NOFS:
                        return new Pair<Boolean, String>(false, "Ошибка: диск не отформатирован, либо файловая система не поддерживается");
                    case Environment.MEDIA_REMOVED:
                        return new Pair<Boolean, String>(false, "Ошибка: диск не найден");
                    case Environment.MEDIA_SHARED:
                        return new Pair<Boolean, String>(false, "Ошибка: доступ к диску блокируется USB-устройством");
                    case Environment.MEDIA_UNKNOWN:
                        return new Pair<Boolean, String>(false, "Ошибка: неизвестное состояние диска");
                    case Environment.MEDIA_UNMOUNTABLE:
                        return new Pair<Boolean, String>(false, "Ошибка: диск не может быть подключен. Вероятно, файловая система диска повреждена");
                    case Environment.MEDIA_UNMOUNTED:
                        return new Pair<Boolean, String>(false, "Ошибка: диск отключен");
                    case Environment.MEDIA_MOUNTED:
                        return new Pair<Boolean, String>(true, null);
                    default:
                        return new Pair<Boolean, String>(false, "Неверное состояние внешенго хранилища");
                }
            }
        }).concatMap(new Function<Pair<Boolean, String>, Single<Pair<Boolean, String>>>() {
            @Override
            public Single<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                if (pair.first) {
                    return getFileSingle(context);
                } else {
                    return Single.just(pair);
                }
            }
        }).map(new Function<Pair<Boolean, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Pair<Boolean, String> pair) throws Throwable {
                if (pair.first) {
//                    String path = dir + name;
//                    File outputFile = new File(path);
//                    outputFile.createNewFile();

//                    File outputFile = Static.createFileWithAutoRename(dir, name);
//                    FileInputStream fin = new FileInputStream(file);
//                    FileOutputStream fout = new FileOutputStream(outputFile);
//                    byte[] buffer = new byte[1024];
//                    int read = 0;
//                    while (read != -1) {
//                        read = fin.read(buffer);
//                        if (read != -1) {
//                            fout.write(buffer, 0, read);
//                        }
//                    }
//                    fin.close();
//                    fout.flush();
//                    fout.close();

                    boolean success = Static.saveFileToGallery(uri, name, type, context);
                    if (success) {
                        return new Pair<Boolean, String>(true, null);
                    } else {
                        return new Pair<Boolean, String>(false, "Ошибка при сохранении файла в галерею");
                    }
                }
                return pair;
            }
        });
    }

    public Single<Pair<Boolean, String>> copyToDocuments(Context context) {
//        context.getDir()
//        Environment.getExternalStorageState();
//        String path = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PICTURES + "/";
        // TODO remove code duplication in copyToGallery and copyToDocuments
        return Single.fromCallable(new Callable<Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> call() throws Exception {
                switch (Environment.getExternalStorageState()) {
                    case Environment.MEDIA_BAD_REMOVAL:
                        return new Pair<Boolean, String>(false, "Ошибка: диск для записи был извлечен некорректно");
                    case Environment.MEDIA_CHECKING:
                        return new Pair<Boolean, String>(false, "Ошибка: диск не готов к работе");
                    case Environment.MEDIA_EJECTING:
                        return new Pair<Boolean, String>(false, "Ошибка: диск извлекается");
                    case Environment.MEDIA_MOUNTED_READ_ONLY:
                        return new Pair<Boolean, String>(false, "Ошибка: диск доступен только для чтения");
                    case Environment.MEDIA_NOFS:
                        return new Pair<Boolean, String>(false, "Ошибка: диск не отформатирован, либо файловая система не поддерживается");
                    case Environment.MEDIA_REMOVED:
                        return new Pair<Boolean, String>(false, "Ошибка: диск не найден");
                    case Environment.MEDIA_SHARED:
                        return new Pair<Boolean, String>(false, "Ошибка: доступ к диску блокируется USB-устройством");
                    case Environment.MEDIA_UNKNOWN:
                        return new Pair<Boolean, String>(false, "Ошибка: неизвестное состояние диска");
                    case Environment.MEDIA_UNMOUNTABLE:
                        return new Pair<Boolean, String>(false, "Ошибка: диск не может быть подключен. Вероятно, файловая система диска повреждена");
                    case Environment.MEDIA_UNMOUNTED:
                        return new Pair<Boolean, String>(false, "Ошибка: диск отключен");
                    case Environment.MEDIA_MOUNTED:
                        return new Pair<Boolean, String>(true, null);
                    default:
                        return new Pair<Boolean, String>(false, "Неверное состояние внешенго хранилища");
                }
            }
        }).concatMap(new Function<Pair<Boolean, String>, Single<Pair<Boolean, String>>>() {
            @Override
            public Single<Pair<Boolean, String>> apply(Pair<Boolean, String> pair) throws Throwable {
                if (pair.first) {
                    return getFileSingle(context);
                } else {
                    return Single.just(pair);
                }
            }
        }).map(new Function<Pair<Boolean, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Pair<Boolean, String> pair) throws Throwable {
                if (pair.first) {
//                    String path = dir + name;
//                    File outputFile = new File(path);
//                    outputFile.createNewFile();

                    File outputFile = Static.createFileWithAutoRename(
                            Environment.getExternalStorageDirectory()
                                    + "/"
                                    + Environment.DIRECTORY_DOCUMENTS
                                    + "/"
                                    + Static.ADSA_DOCUMENTS_FOLDER
                                    + "/",
                            name
                    );
                    FileInputStream fin = new FileInputStream(file);
                    FileOutputStream fout = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while (read != -1) {
                        read = fin.read(buffer);
                        if (read != -1) {
                            fout.write(buffer, 0, read);
                        }
                    }
                    fin.close();
                    fout.flush();
                    fout.close();

//                    boolean success = Static.saveFileToGallery(uri, name, type, context);
//                    if (success) {
                    return new Pair<Boolean, String>(true, null);
//                    } else {
//                        return new Pair<Boolean, String>(false, "Ошибка при сохранении файла в галерею");
//                    }
                }
                return pair;
            }
        })
//                .onErrorReturn(new Function<Throwable, Pair<Boolean, String>>() {
//                    @Override
//                    public Pair<Boolean, String> apply(Throwable throwable) throws Throwable {
//                        return new Pair<Boolean, String>(false, throwable.getMessage());
//                    }
//                })
                ;
    }

    public Single<Pair<Boolean, String>> downloadToCache(Context context) {
        if (id == null || id.isEmpty()) {
            return Single.just(new Pair<Boolean, String>(false, "ID файла неизвестен"));
        }
        if (!isDownloadedToCache(context)) {
            File f;
//            try {
//                f = File.createTempFile(id, null, context.getCacheDir());
            f = new File(context.getCacheDir(), id);
//            } catch (IOException e) {
//                e.printStackTrace();
//                return Single.just(new Pair<Boolean, String>(false, "Ошибка при создании файла"));
//            }
            return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
                @Override
                public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                    Response response;
                    try {
                        response = Net.request(
                                Net.ADSA_SERVER + "/api/query/media/" + id,
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
//                        FileWriter writer = new FileWriter(f);
//                        writer.
                        // TODO replace with fileoutputstream
                        DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
                        dos.write(response.body().bytes());
                        dos.close();
                        file = f;
                        uri = Uri.fromFile(f);
                    }
                    return pair;
                }
            });
        } else {
            return Single.just(new Pair<Boolean, String>(false, "Файл уже загружен в кэш"));
        }
    };

    public void fromJsonObject(@NonNull JsonObject j) {
        id = j.get("id").getAsString();
//        order = j.get("order").getAsString();
        name = j.get("name").getAsString();
        type = j.get("type").getAsString();
        size = j.get("size").getAsInt();
    }

    public Single<Pair<Boolean, String>> uploadToServer() {
//        FormBody body = new FormBody.Builder()
//                .add()
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart(
                        "file",
                        name,
                        RequestBody.create(fileContent, MediaType.parse(type))
                )
                .build();

        return Static.getAuthorizedHeadersMap().map(new Function<Map<String, String>, Pair<Boolean, String>>() {
            @Override
            public Pair<Boolean, String> apply(Map<String, String> headers) throws Throwable {
                Response response;
                try {
                    response = Net.request(
                            Net.ADSA_SERVER + "/api/query/media/upload",
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
                    JsonObject rbody = new Gson().fromJson(response.body().string(), JsonObject.class);
                    id = rbody.get("id").getAsString();
                    name = rbody.get("name").getAsString();
                    type = rbody.get("type").getAsString();
                    size = rbody.get("size").getAsInt();
                }
                return pair;
            }
        });
    }
}
