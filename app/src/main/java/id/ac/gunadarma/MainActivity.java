package id.ac.gunadarma;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment; // Important: Use this specific class

import id.ac.gunadarma.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //! edge-to-edge design some say it's "magic" I say it's complicated
        // WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        // 1. Inflate the layout using View Binding
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Find the NavHostFragment from your layout
        //    This is the safest and most recommended way to get the NavController.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        // 3. Get the NavController from the NavHostFragment
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        // 4. Set up the AppBar (the top bar) to show titles
        //    Make sure the NavController is passed to it correctly.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home,
//                R.id.navigation_dashboard,
//                R.id.navigation_notifications
//        ).build();
//
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 5. Set up the BottomNavigationView
        //    Connect your BottomNavigationView (binding.navView) to the same NavController.
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

}