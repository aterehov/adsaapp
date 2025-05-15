package ru.anoadsa.adsaapp.ui.activities.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.Static;
import ru.anoadsa.adsaapp.databinding.ActivityMainBinding;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;
import ru.anoadsa.adsaapp.ui.menufragments.chat.ChatFragment;
import ru.anoadsa.adsaapp.ui.menufragments.chat.ChatSharedViewModel;
import ru.anoadsa.adsaapp.ui.menufragments.home.HomeFragment;
import ru.anoadsa.adsaapp.ui.menufragments.incidents.IncidentsFragment;

public class MenuActivity extends UiActivity<MenuViewModel> {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private View root;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private NavController navController;
    private FloatingActionButton fab;

    private MenuSharedViewModel sharedVM;

    private TextView menuUserName;
    private TextView menuUserPhone;
    private ImageView imageView;

    private DrawerLayout drawerLayout;

    private String action;

    private View.OnClickListener openProfileFragment = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (navController.getCurrentDestination().getId() != R.id.nav_profile) {
                navController.popBackStack(R.id.mobile_navigation, false);
//                new NavOptions.Builder().setPopUpTo(R.id.mobile_navigation, true);
                navController.navigate(R.id.nav_profile, null);
//                navController.navigate()
                drawerLayout.close();
            }
        }
    };

    private final View.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null)
//                    .setAnchorView(R.id.fab).show();
        }
    };

    private void getRoot() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        root = binding.getRoot();
    }

    @Override
    protected void initUi() {
        getRoot();
        setContentView(root);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        toolbar = binding.appBarMain.toolbar;
//        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController = ((NavHostFragment) Objects.requireNonNull(
                getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main)))
                .getNavController();
//        navController.navigate(R.id.nav_home);
//        fab = binding.appBarMain.fab;
//        menuUserName = findViewById(R.id.menuUserName);
//        menuUserPhone = findViewById(R.id.menuUserPhone);
//        imageView = findViewById(R.id.imageView);
        menuUserName = navigationView.getHeaderView(0).findViewById(R.id.menuUserName);
        menuUserPhone = navigationView.getHeaderView(0).findViewById(R.id.menuUserPhone);
        imageView = navigationView.getHeaderView(0).findViewById(R.id.imageView);
        drawerLayout = binding.drawerLayout;
    }

    @Override
    protected void configureUiState() {
        setSupportActionBar(toolbar);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_sos,
//                R.id.nav_home,
//                R.id.nav_gallery,
//                R.id.nav_slideshow,
                R.id.nav_incidents,
                R.id.nav_profile,
                R.id.nav_map,
                R.id.nav_about
        )
                .setOpenableLayout(drawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


    }

//    @Override
//    protected void configureViewModel() {
//
//    }

    @Override
    protected void configureUiActions() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                if (navController.getPreviousBackStackEntry() != null) {
//                    navController.popBackStack();
//                } else {
                moveTaskToBack(true);
//                }
            }
        });

//        fab.setOnClickListener(fabOnClickListener);

        imageView.setOnClickListener(openProfileFragment);
        menuUserName.setOnClickListener(openProfileFragment);
        menuUserPhone.setOnClickListener(openProfileFragment);
    }

    @Override
    protected void configureViewModelActions() {
        viewModel.getFirstRun().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean firstRun) {
                if (firstRun
                        && (action == null || action.isEmpty())
                ) {
//                    navController.navigate(R.id.nav_sos);
                    viewModel.setFirstRun(false);
                }
            }
        });
    }

    protected void configureSharedViewModelActions() {
        sharedVM.getUserName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String userName) {
                menuUserName.setText(userName);
            }
        });

        sharedVM.getUserPhone().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String userPhone) {
                menuUserPhone.setText(userPhone);
            }
        });

        sharedVM.loadUserDataFromDb();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Static.runOnStart(getApplicationContext());

        setViewModel(MenuViewModel.class);
        super.onCreate(savedInstanceState);

//        Static.initDb(getApplicationContext());

        sharedVM = new ViewModelProvider(this).get(MenuSharedViewModel.class);
        configureSharedViewModelActions();

        action = getIntent().getStringExtra("action");
        if (action == null || action.isEmpty()) {
            return;
        }
        if (action.equals("chat")) {
            String incidentId = getIntent().getStringExtra("incidentId");
            if (incidentId == null || incidentId.isEmpty()) {
                return;
            }
            ChatSharedViewModel chatSharedVM = new ViewModelProvider(this).get(ChatSharedViewModel.class);
            Incident incident = new Incident();
            incident.setId(incidentId);
            chatSharedVM.setIncident(incident);

//            navController.navigate(R.id.nav_sos);
//            navController.navigate(R.id.nav_incidents);
//            navController.navigate(R.id.action_incidents_to_chat);

            navController.navigate(R.id.nav_chat);

//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(
//                            R.id.nav_host_fragment_content_main,
//                            new HomeFragment()
//                    )
//                    .addToBackStack(null)
//                    .setReorderingAllowed(true)
//                    .commit();
//
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(
//                            R.id.nav_host_fragment_content_main,
//                            new IncidentsFragment()
//                    )
//                    .addToBackStack(null)
//                    .setReorderingAllowed(true)
//                    .commit();
//
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(
//                            R.id.nav_host_fragment_content_main,
//                            new ChatFragment()
//                    )
//                    .addToBackStack(null)
//                    .setReorderingAllowed(true)
//                    .commit();
        }
//        initUi();
//        configureUiState();
//        configureViewModel();
//        configureUiActions();
//        setContentView(binding.getRoot());
//        TextView tv = findViewById(R.id.text_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

//    private boolean backOnNavigateUp;
//
//    public void setBackOnNavigateUp(boolean backOnNavigateUp) {
//        this.backOnNavigateUp = backOnNavigateUp;
//    }

    @Override
    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        if (sharedVM.getEnableBackOnNavigateUp().getValue() && getOnBackPressedDispatcher().hasEnabledCallbacks()) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}