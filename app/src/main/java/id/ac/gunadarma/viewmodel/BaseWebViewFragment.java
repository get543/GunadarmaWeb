/*
    !View Binding is a feature that automatically generates a Java class for each of your XML layout files. This generation follows a very strict rule:
    - The XML file's name is converted from snake_case to PascalCase.
    - The word "Binding" is added to the end.

    *Examples:
    - fragment_home.xml -> generates -> FragmentHomeBinding.java
    - activity_main.xml -> generates -> ActivityMainBinding.java
    - my_cool_layout.xml -> generates -> MyCoolLayoutBinding.java
*/

package id.ac.gunadarma.viewmodel;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import id.ac.gunadarma.MainActivity;
import id.ac.gunadarma.databinding.FragmentVclassBinding;

public abstract class BaseWebViewFragment extends Fragment {

    //* --- Abstract methods that the child fragment MUST implement ---

    /**
     * The child fragment must provide its specific WebView instance.
     */
    protected abstract WebView getWebView();

    /**
     * The child fragment must provide its specific ViewModel class.
     */
    protected abstract Class<? extends BaseWebViewModel> getViewModelClass();

    //* --- Common logic that all child fragments will inherit ---
    private ActivityResultLauncher<Intent> mUploadMessageLauncher;
    private ValueCallback<Uri[]> mFilePathCallback;
    private Bundle webViewBundle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // Retrieve the bundle here.
            webViewBundle = savedInstanceState.getBundle("webview_state");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the specific WebView from the child fragment
        WebView webView = getWebView();

        // If there's no webview
        if (webView == null) return;

        // Initialize the launcher here, in onCreate.
        setupFileUploadHandler();

        // Back button handler
        setupOnBackPressed();

        // Configure all the common settings
        configureWebViewSettings(webView);

        // This logic block ensures either the state is restored OR the initial URL is loaded, but never both.
        if (webViewBundle != null) {
            // !THIS BLOCK HANDLES ALL CONFIGURATION CHANGES (ROTATION, DARK MODE, ETC.)
            // *If a bundle exists, it means the fragment is being recreated.
            // *The restoreState() command is king. It reloads the WebView's history
            // *and navigates to the last page the user was on. We do NOT need to
            // *load the initial URL from the ViewModel.

            webView.restoreState(webViewBundle);
        } else {
            // !THIS BLOCK ONLY RUNS ON THE VERY FIRST LAUNCH of the fragment,
            // *when there is no saved state to restore.
            // *We load the default URL from the ViewModel.

            // Get the specific ViewModel for the fragment
            BaseWebViewModel viewModel = new ViewModelProvider(this).get(getViewModelClass());

            // Observe the LiveData to load the URL
            viewModel.getUrl().observe(getViewLifecycleOwner(), url -> {
                if (url != null && !url.isEmpty()) {
                    webView.loadUrl(url);
                }
            });
        }
    }

    // * Setup Method
    /**
     * This method contains all the shared settings.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebViewSettings(WebView webView) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        // ... add any other common settings here ...

        // This tells the WebView to not save form data and, critically, not to
        // engage with the autofill framework, which prevents the crash.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.getSettings().setSaveFormData(false);
        }

        // Set the clients using methods that can be overridden
        webView.setWebViewClient(createWebViewClient());
        webView.setWebChromeClient(createWebChromeClient());
        webView.setDownloadListener(createDownloadListener());

        //! --- START OF DARK MODE (FORCED) LOGIC ---
        // Use the modern method on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            webView.getSettings().setAlgorithmicDarkeningAllowed(true);
        }
        // Use the legacy method on Android 10-12
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                webView.getSettings().setForceDark(WebSettings.FORCE_DARK_ON);
            }
        }
        //! --- END OF DARK MODE (FORCED) LOGIC ---
    }

    /**
     * Save the WebView's state into a new bundle and put it in the outState.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        WebView webView = getWebView();
        if (webView != null) {
            Bundle bundle = new Bundle();
            webView.saveState(bundle);
            outState.putBundle("webview_state", bundle);
        }
    }

    /**
     * Initializes the ActivityResultLauncher for handling WebView file uploads.
     * MUST be called in onCreate().
     */
    private void setupFileUploadHandler() {
        mUploadMessageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // This is the callback that handles the file picker result.
                if (mFilePathCallback == null) {
                    return;
                }

                Uri[] results = null;
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null && result.getData().getDataString() != null) {
                        results = new Uri[]{Uri.parse(result.getData().getDataString())};
                    }
                }

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            }
        );
    }

    /**
     * Sets up the custom back button behavior for the WebView.
     */
    private void setupOnBackPressed() {
        if (getActivity() == null) return;

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                WebView webView = getWebView();
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false); // Disable this callback
                    // Trigger the default back behavior
                    if (getActivity() != null) {
                        getActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            }
        };
        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    //* --- Overridable methods for customization ---

    /**
     * Child fragments can override this method to provide a custom WebViewClient.
     */
    protected WebViewClient createWebViewClient() {
        // Default implementation
        return new WebViewClient();
    }

    //! --- START OF DOWNLOAD LISTENER LOGIC ---
    protected DownloadListener createDownloadListener() {
        return new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // Safety check: A fragment's context can be null if it's detached.
                final Context context = getContext();
                if (context == null) return;

                // Create a request for the DownloadManager
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                // Get cookies for the URL
                String cookies = CookieManager.getInstance().getCookie(url);

                // Add the authentication cookies to the request header
                request.addRequestHeader("Cookie", cookies);

                // Set the MimeType and User-Agent
                request.setMimeType(mimetype);
                request.addRequestHeader("User-Agent", userAgent);

                // Set the notification to be visible and to show a notification when complete
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                // Guess the filename from the URL and Content-Disposition
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                request.setTitle(fileName);

                // Set the destination for the downloaded file
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                // Get the DownloadManager service and enqueue the request
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                if (downloadManager != null) {
                    downloadManager.enqueue(request);
                }

                // Show a toast message to the user
                Toast.makeText(context, "Downloading File...", Toast.LENGTH_SHORT).show();
            }
        };
    }
    //! --- END OF DOWNLOAD LISTENER LOGIC ---

    private WebChromeClient createWebChromeClient() {
        return new WebChromeClient() {


            //! --- START OF FILE UPLOAD LOGIC REQUEST ---
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // If another file chooser is already open, cancel it
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }
                mFilePathCallback = filePathCallback;

                // Create an intent to open the file chooser
                Intent intent = fileChooserParams.createIntent();
                try {
                    // Launch the file chooser using our modern launcher
                    mUploadMessageLauncher.launch(intent);
                } catch (Exception e) {
                    // Handle the exception (e.g., no file picker available)
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                    return false;
                }

                return true; // We've handled the file chooser
            }
        };
        //! --- END OF FILE UPLOAD LOGIC REQUEST ---
    }

}
