package id.ac.gunadarma.ui.vclass;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import java.util.Objects;

import id.ac.gunadarma.databinding.FragmentVclassBinding;
import id.ac.gunadarma.viewmodel.BaseWebViewFragment;
import id.ac.gunadarma.viewmodel.BaseWebViewModel;

public class VclassFragment extends BaseWebViewFragment {

    private FragmentVclassBinding binding; // Using View Binding is best practice

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVclassBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    //* --- Implement the required abstract methods ---

    @Override
    protected WebView getWebView() {
        return binding.VclassWebView;
    }

    @Override
    protected Class<? extends BaseWebViewModel> getViewModelClass() {
        return VclassViewModel.class;
    }

    @Override
    protected FrameLayout getFullscreenContainer() {
        return binding.fullscreenContainer;
    }

    // --- Optional: Customize the WebViewClient ---
    // If THIS fragment needed a special client, you would override it here.
    @Override
    protected WebViewClient createWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Do something special for the home fragment...
            }
        };
    }

    @Override
    public void onDestroyView() {
        // It's very important to properly destroy the WebView to avoid memory leaks.
        WebView webView = getWebView();
        if (webView != null) {
            // Detach the WebView from its parent
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
        }

        binding = null;
        super.onDestroyView();
    }
}
