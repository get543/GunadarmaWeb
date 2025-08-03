package id.ac.gunadarma.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public abstract class BaseWebViewModel extends ViewModel {
    protected final MutableLiveData<String> url = new MutableLiveData<>();

    public LiveData<String> getUrl() {
        return url;
    }
}