package com.qiang.contactsimport;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * @author xiaoqiang
 * @date 19-3-15
 */
class WorkHandler {
    private static Handler sWorkHandler;
    private static HandlerThread sWorkThread;

    private WorkHandler() {
    }

    synchronized static Handler getWorkHandler() {
        if (sWorkThread == null) {
            sWorkThread = new HandlerThread("contactsimport");
            sWorkThread.start();
            sWorkHandler = new Handler(sWorkThread.getLooper());
        }
        return sWorkHandler;
    }
}
