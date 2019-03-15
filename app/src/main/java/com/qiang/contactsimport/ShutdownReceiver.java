package com.qiang.contactsimport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author xiaoqiang
 * @date 19-3-15
 */
public class ShutdownReceiver extends BroadcastReceiver {

    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_SHUTDOWN.equals(action)) {
            Intent i = new Intent(context, ContactsImportService.class);
            i.putExtra("op", "export");
            context.startService(i);
        }
    }
}
