package ru.anoadsa.adsaapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.Contract;

import ru.anoadsa.adsaapp.backgroundtasks.checkmessagecount.CheckMessageCountWatchdog;
import ru.anoadsa.adsaapp.backgroundtasks.sosbutton.SosButtonWorker;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;
import ru.anoadsa.adsaapp.ui.activities.login.LoginActivity;
import ru.anoadsa.adsaapp.ui.activities.menu.MenuActivity;
import ru.anoadsa.adsaapp.ui.activities.permission.PermissionActivity;

public class MainActivity extends UiActivity<MainViewModel> {

//    private AppBarConfiguration mAppBarConfiguration;
//    private ActivityMainBinding binding;

    private int permissionRequestCount;

    private ActivityResultLauncher<Object> loginActivityResultLauncher;

    private ActivityResultContract<Object, Object> loginActivityResultContract = new ActivityResultContract<Object, Object>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Object o) {
            return new Intent(context, LoginActivity.class);
        }

        @Nullable
        @Contract(pure = true)
        @Override
        public Object parseResult(int i, @Nullable Intent intent) {
            return null;
        }
    };

    private void requestPermissions() {
        switch (permissionRequestCount) {
            case 0:
                permissionActivityResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
                break;
            case 1:
                permissionActivityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                break;
            case 2:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    permissionActivityResultLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
//                    break;
//                } else {
//                    ++permissionRequestCount;
//                }
                // TODO: re-enable the above when tourist routes are implemented
                ++permissionRequestCount;
            case 3:
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    permissionActivityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    break;
                } else {
                    ++permissionRequestCount;
                }
            case 4:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionActivityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    break;
                } else {
                    ++permissionRequestCount;
                }
            case 5:
                permissionActivityResultLauncher.launch(Manifest.permission.CAMERA);
                break;
            case 6:
                permissionActivityResultLauncher.launch(Manifest.permission.RECORD_AUDIO);
                break;
            case 7:
                permissionActivityResultLauncher.launch(Manifest.permission.SEND_SMS);
                break;
            case 8:
                permissionActivityResultLauncher.launch(Manifest.permission.READ_PHONE_STATE);
                break;
            default:
                viewModel.setFirstRun(false);
                goToMenu();
        }
    }

    private void goToMenu() {
        startActivity(new Intent(MainActivity.this, MenuActivity.class));
        finish();
    }

    private ActivityResultCallback<Object> loginActivityResultCallback = new ActivityResultCallback<Object>() {
        @Override
        public void onActivityResult(Object o) {
            loggedInAction();
        }
    };

    private ActivityResultCallback<Boolean> permissionActivityResultCallback = new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean success) {
            ++permissionRequestCount;
            requestPermissions();
        }
    };

    private ActivityResultLauncher<String> permissionActivityResultLauncher;

    @Override
    protected void initUi() {
        setContentView(R.layout.activity_empty);
    }

    @Override
    protected void configureUiState() {

    }

    @Override
    protected void configureUiActions() {

    }

    @Override
    protected void configureViewModelActions() {
        viewModel.getLoggedIn().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedIn) {
                if (viewModel.getAppIsLaunching().getValue()) {
                    return;
                }
                if (loggedIn == null) {
                    viewModel.checkIfLoggedIn();
                } else if (!loggedIn) {
                    notLoggedInAction();
                } else {
                    loggedInAction();
                }
                if (loggedIn != null) {
//                    finish();
                }
            }
        });

        viewModel.getIsFirstRun().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isFirstRun) {
                if (viewModel.getAppIsLaunching().getValue()) {
                    return;
                }
                if (viewModel.getLoggedIn() == null) {
                    return;
                }
                if (
                        isFirstRun != null
//                        && viewModel.getLoggedIn().getValue()
                ) {
                    if (isFirstRun
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    ) {
                        // TODO launch first run activities

                        requestPermissions();


                    } else {
                        goToMenu();
                    }

                }
            }
        });

        viewModel.getAppIsLaunching().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLaunching) {
                if (isLaunching) {
//                    try {
//                        DevSettings.loadAppVersion(getApplicationContext());
//                    } catch (PackageManager.NameNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    Static.initDb(getApplicationContext());
//                    AppPreferences.initDataStore(getApplicationContext());
//                    Geo.initAutoselectBetterLocation();
//
//                    SosButtonWorker.subscribeOnLocationUpdates();
//                    CheckMessageCountWatchdog.schedule(getApplicationContext());

                    Static.runOnStart(getApplicationContext());

                    viewModel.finishAppLaunching();
                }
            }
        });
    }

    private void notLoggedInAction() {
//        startActivity(new Intent(this, LoginActivity.class));




        loginActivityResultLauncher.launch(null);
    }

    private void loggedInAction() {
        viewModel.checkFirstRun();
//        startActivity(new Intent(this, MenuActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Static.initDb(getApplicationContext());
//        AppPreferences.initDataStore(getApplicationContext());
//        Geo.initAutoselectBetterLocation();
//
//        CheckMessageCountWatchdog.schedule(getApplicationContext());

        setViewModel(MainViewModel.class);
        super.onCreate(savedInstanceState);

        loginActivityResultLauncher = registerForActivityResult(loginActivityResultContract, loginActivityResultCallback);
        permissionActivityResultLauncher = registerForActivityResult(PermissionActivity.getARC(), permissionActivityResultCallback);

        permissionRequestCount = 0;
//        if(true) { // if user is not logged in
//            startActivity(new Intent(this, LoginActivity.class));
//        } else {
//            startActivity(new Intent(this, MenuActivity.class));
//        }
//        finish();


//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        setSupportActionBar(binding.appBarMain.toolbar);
//        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null)
//                        .setAnchorView(R.id.fab).show();
//            }
//        });
//        DrawerLayout drawer = binding.drawerLayout;
//        NavigationView navigationView = binding.navView;
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        mAppBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
//                .setOpenableLayout(drawer)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
//
////        TextView tv = findViewById(R.id.text_home);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
}