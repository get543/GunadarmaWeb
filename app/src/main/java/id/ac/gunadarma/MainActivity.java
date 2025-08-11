package id.ac.gunadarma;

import android.app.PictureInPictureParams;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Rational;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.Fade;

import id.ac.gunadarma.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //! edge-to-edge design some say it's "magic" I say it's complicated
         WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // 1. Inflate the layout using View Binding
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //! FIX NAVBAR MENU PADDING ON NEWER PHONES WITH GESTURES
        // Apply a listener to the root container of your activity
        ViewCompat.setOnApplyWindowInsetsListener(binding.container, (v, insets) -> {
            // Get the height of the status bar (top) and the gesture navigation bar (bottom)
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply the top inset as top padding to your Fragment Container
            binding.navHostFragmentActivityMain.setPadding(0, topInset, 0, 0);

            // --- THE CORRECTED JAVA CODE ---
            // Apply the bottom inset as a bottom margin to your BottomNavigationView.
            ViewGroup.LayoutParams layoutParams = binding.navView.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                marginLayoutParams.bottomMargin = bottomInset;
                binding.navView.setLayoutParams(marginLayoutParams);
            }
            // --- END OF CORRECTED CODE ---

            // We've handled the insets, so return the default ones
            return WindowInsetsCompat.CONSUMED;
        });

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

        //! HIDE OR SHOW NAVBAR ON FULLSCREEN
        // Get an instance of the shared ViewModel.
        // The key is using "this" (the Activity) as the ViewModelStoreOwner.
        ActivityViewModel activityViewModel = new ViewModelProvider(this).get(ActivityViewModel.class);

        // Observe the fullscreen LiveData.
        activityViewModel.isFullscreen().observe(this, fullscreen -> {
            // Create a fade transition. You can also use Slide, Explode, etc.
            Fade fade = new Fade();
            // Set the duration of the animation in milliseconds (e.g., 300ms)
            fade.setDuration(300);
            // Optional: Set an interpolator for a more natural feel
            fade.setInterpolator(new FastOutSlowInInterpolator());

            // Tell the TransitionManager to animate any changes that happen inside our root layout.
            TransitionManager.beginDelayedTransition(binding.getRoot());

            if (fullscreen) {
                // If we are in fullscreen, HIDE the bottom navigation bar.
                binding.navView.setVisibility(View.GONE);
            } else {
                // If we are NOT in fullscreen, SHOW it again.
                binding.navView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * This method is called when the user presses the Home button or a similar action
     * that indicates they are about to leave the app. This is our cue to enter PiP mode.
     */
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();

        ActivityViewModel activityViewModel = new ViewModelProvider(this).get(ActivityViewModel.class);

        // We check our shared ViewModel to see if a video is currently fullscreen.
        if (activityViewModel.isFullscreen().getValue() != null && Boolean.TRUE.equals(activityViewModel.isFullscreen().getValue())) {

            // This check is required.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Build the parameters for the PiP window.
                // We'll give it a 16:9 aspect ratio.
                PictureInPictureParams params = new PictureInPictureParams.Builder()
                        .setAspectRatio(new Rational(16, 9))
                        .build();

                // Enter Picture-in-Picture mode.
                enterPictureInPictureMode(params);
            }
        }
    }

}