package ua.kpi.comsys.IB8118.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> title;

    public HomeViewModel() {
        title = new MutableLiveData<>();
        title.setValue("Лиса Еліза\nГрупа IВ-81\nЗК IВ-8118");
    }

    public LiveData<String> getText() {
        return title;
    }
}