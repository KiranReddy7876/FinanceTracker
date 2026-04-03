package com.financetracker.ui;

import android.os.Bundle;
import android.util.Log;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.financetracker.R;
import com.financetracker.utils.PermissionManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ── Edge-to-edge: removes DrawerLayout from the insets dispatch chain.
        // Without this, DrawerLayout re-layouts on every keyboard animation frame,
        // causing the 3-4 black flashes seen on the emulator. ──────────────────
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ── Status bar: push AppBarLayout content below the status bar ────────
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), statusBar.top,
                         v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // ── Nav bar: push BottomNavigationView above the system nav bar ───────
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                         v.getPaddingRight(), navBar.bottom);
            return insets;
        });

        // ── Status-bar icon colour (white icons for our dark primary toolbar) ─
        WindowInsetsControllerCompat wic =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        wic.setAppearanceLightStatusBars(false);   // false = white/light icons

        // 1. Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) return;
        navController = navHostFragment.getNavController();

        // 3. DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // 4. AppBarConfiguration — ALL bottom-nav tabs are top-level destinations
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboardFragment,
                R.id.smsImportFragment,
                R.id.reportsFragment,
                R.id.accountsFragment)
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 5. Side Drawer clicks
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(item -> {
            boolean navigated = NavigationUI.onNavDestinationSelected(item, navController);
            if (!navigated) {
                Log.e(TAG, "Drawer navigation failed for: " + item.getTitle());
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // 6. Bottom Navigation clicks
        bottomNav.setOnItemSelectedListener(item -> {
            boolean navigated = NavigationUI.onNavDestinationSelected(item, navController);
            if (!navigated) {
                Log.e(TAG, "Bottom nav navigation failed for: " + item.getTitle());
            }
            return navigated;
        });

        // Sync bottom nav highlight whenever the NavController destination changes
        // (covers back-press, drawer navigation, and any programmatic navigation)
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.dashboardFragment
                    || id == R.id.smsImportFragment
                    || id == R.id.reportsFragment
                    || id == R.id.accountsFragment) {
                // Temporarily remove listener to avoid re-entrant navigation
                bottomNav.setOnItemSelectedListener(null);
                bottomNav.setSelectedItemId(id);
                bottomNav.setOnItemSelectedListener(item -> {
                    boolean navigated = NavigationUI.onNavDestinationSelected(item, navController);
                    if (!navigated) {
                        Log.e(TAG, "Bottom nav navigation failed for: " + item.getTitle());
                    }
                    return navigated;
                });
            }
        });

        // 7. Back press — close the drawer if open, otherwise normal back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // 8. Request SMS permissions (required for reading SMS)
        Log.d(TAG, "Requesting SMS permissions");
        PermissionManager.requestSmsPermissions(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionManager.handleSmsPermissionResult(requestCode, permissions, grantResults)) {
            Log.d(TAG, "✓ SMS permissions granted - SMS reading is now enabled");
        } else {
            Log.w(TAG, "✗ SMS permissions denied - SMS reading will not work");
        }
    }
}
