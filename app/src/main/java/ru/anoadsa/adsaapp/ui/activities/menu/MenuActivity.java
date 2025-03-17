package ru.anoadsa.adsaapp.ui.activities.menu;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import ru.anoadsa.adsaapp.R;
import ru.anoadsa.adsaapp.databinding.ActivityMainBinding;
import ru.anoadsa.adsaapp.ui.EmptyViewModel;
import ru.anoadsa.adsaapp.ui.abstracts.UiActivity;

public class MenuActivity extends UiActivity<EmptyViewModel> {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private View root;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private NavController navController;
    private FloatingActionButton fab;

    private final View.OnClickListener fabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .setAnchorView(R.id.fab).show();
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
        fab = binding.appBarMain.fab;

    }

    @Override
    protected void configureUiState() {
        setSupportActionBar(toolbar);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
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
        fab.setOnClickListener(fabOnClickListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}