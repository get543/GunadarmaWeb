package id.ac.gunadarma;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ActivityViewModel extends ViewModel {
    // This LiveData will hold the fullscreen state.
    // true = we are in fullscreen, false = we are not.
    private final MutableLiveData<Boolean> isFullscreen = new MutableLiveData<>(false);

    public LiveData<Boolean> isFullscreen() {
        return isFullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        isFullscreen.setValue(fullscreen);
    }
}