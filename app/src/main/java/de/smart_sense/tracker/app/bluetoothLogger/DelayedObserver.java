package de.smart_sense.tracker.app.bluetoothLogger;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by phil on 11/19/14.
 */
public class DelayedObserver implements ObservableLinkedList.Observer {
    protected static Handler mHandler;
    protected final Runnable mAction;
    private long mDelay;

    public final static long DEFAULT_DELAY = 500;

    public DelayedObserver(long delay, Runnable r) {
        if (mHandler==null)
            mHandler = new Handler(Looper.getMainLooper());
        mAction = r;
        mDelay = delay;
    }

    @Override
    public void listChanged() {
        mHandler.removeCallbacks(mAction, null);
        mHandler.postDelayed(mAction, mDelay);
    }
}