package ru.anoadsa.adsaapp.ui.activities.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.Contract;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;

public class PermissionActivity extends UiActivity<EmptyViewModel> {
    private String permission;

    private TextView permissionDescription;
    private TextView permissionExplanation;
    private Button permissionAllowButton;
    private Button permissionDenyButton;

    private static ActivityResultContract<String, Boolean> arc = new ActivityResultContract<String, Boolean>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, String permission) {
            Intent i = new Intent(context, PermissionActivity.class);
            i.putExtra("permission", permission);
            return i;
        }

        @NonNull
        @Contract(pure = true)
        @Override
        public Boolean parseResult(int code, @Nullable Intent intent) {
            return code == Activity.RESULT_OK;
        }
    };

    public static ActivityResultContract<String, Boolean> getARC() {
        return arc;
    }

    @NonNull
    @Contract(pure = true)
    private String getPermissionName() {
        switch (permission) {
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "Доступ к примерному местоположению";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "Доступ к точному местоположению";
            case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                return "Доступ к местоположению в фоновом режиме";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Запись данных за пределами приложения";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Отправка уведомлений";
            case Manifest.permission.CAMERA:
                return "Доступ к камере";
            case Manifest.permission.RECORD_AUDIO:
                return "Запись аудио";
            case Manifest.permission.SEND_SMS:
                return "Отправка SMS";
            case Manifest.permission.READ_PHONE_STATE:
                return "Чтение состояния сотовой сети";
            default:
                return "Неизвестное разрешение";
        }
    }

    @NonNull
    @Contract(pure = true)
    private String getPermissionExplanation() {
        switch (permission) {
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "- Автоматическое заполнение координат при создании обращения\n" +
                        "- Отслеживание прохождения туристических маршрутов\n" +
                        "- Отправка местоположение при видеовызове\n" +
                        "- Отправка местоположения при экстренном вызове";
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "Автоматическое заполнение координат при создании обращения\n" +
                        "- Отслеживание прохождения туристических маршрутов\n" +
                        "- Отправка местоположения при видеовызове\n" +
                        "- Отправка местоположения при экстренном вызове";
            case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                return "- Отслеживание прохождения туристических маршрутов\n" +
                        "- Определение местоположения при отправке SMS при экстренном вызове";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Сохранение файлов, прикрепленных к обращениям";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Уведомления о новых сообщениях в чатах происшествий";
            case Manifest.permission.CAMERA:
                return "Видеовызовы";
            case Manifest.permission.RECORD_AUDIO:
                return "Видеовызовы";
            case Manifest.permission.SEND_SMS:
                return "Отправка SMS с местоположением и информацией о пользователе при экстренном вызове";
            case Manifest.permission.READ_PHONE_STATE:
                return "Определение сотовой подписки при отправке SMS при экстренном вызове";
            default:
                return "Неизвестные цели";
        }
    }

    private int getPermissionRequestCode() {
        switch (permission) {
            case Manifest.permission.ACCESS_COARSE_LOCATION: return 1;
            case Manifest.permission.ACCESS_FINE_LOCATION: return 2;
            case Manifest.permission.ACCESS_BACKGROUND_LOCATION: return 3;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE: return 4;
            case Manifest.permission.POST_NOTIFICATIONS: return 5;
            case Manifest.permission.CAMERA: return 6;
            case Manifest.permission.RECORD_AUDIO: return 7;
            case Manifest.permission.SEND_SMS: return 8;
            case Manifest.permission.READ_PHONE_STATE: return 9;
            default: return 0;
        }
    }

    @Override
    protected void initUi() {
        setContentView(R.layout.activity_permission);
        permissionDescription = findViewById(R.id.permissionDescription);
        permissionExplanation = findViewById(R.id.permissionExplanation);
        permissionAllowButton = findViewById(R.id.permissionAllowButton);
        permissionDenyButton = findViewById(R.id.permissionDenyButton);
    }

    @Override
    protected void configureUiState() {
        permissionDescription.setText(getPermissionName());
        permissionExplanation.setText(getPermissionExplanation());
    }

    @Override
    protected void configureUiActions() {
        permissionAllowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(
                        PermissionActivity.this,
                        new String[]{permission},
                        getPermissionRequestCode()
                );
            }
        });

        permissionDenyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endFailure();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permisiions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permisiions, grantResults);
        if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
            ManualDialogFragment d = new ManualDialogFragment();
            d.setOnCloseAction(new Runnable() {
                @Override
                public void run() {
                    endFailure();
                }
            });
            d.show(getSupportFragmentManager(), "MANUAL_DIALOG");
//            endFailure();
        } else {
            endSuccess();
        }
    }

    private void endSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    private void endFailure() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void configureViewModelActions() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        permission = getIntent().getStringExtra("permission");
        super.onCreate(savedInstanceState);
    }
}
