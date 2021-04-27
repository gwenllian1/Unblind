package com.example.unblind;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class BackgroundViewModel extends AndroidViewModel {

    private WorkManager mWorkManager;

    public BackgroundViewModel(@NonNull Application application) {
        super(application);
        mWorkManager = WorkManager.getInstance(application);
    }

    void accessDatabase() {
        mWorkManager.enqueue(OneTimeWorkRequest.from(BackgroundWorker.class));
    }
}
