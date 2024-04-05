package com.example.semsemgallery.domain;

import android.os.Handler;
import android.os.Looper;

public abstract class TaskBase<Param, Process, Result> {
    public Handler mHandler = new Handler(Looper.getMainLooper());

    public abstract void preExecute(Param... params); // Before create and run a new thread

    public abstract Result doInBackground(Param... params); // Main task

    public abstract void onProcessUpdate(Process... processes); // Call during the main task runs

    public abstract void postExecute(Result res);

    @SafeVarargs
    public final void execute(final Param... params) {
        preExecute(params);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Result res = doInBackground(params);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        postExecute(res);
                    }
                });
            }
        }).start();
    }
}
