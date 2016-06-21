package au.com.dylanaird.android.s3249319assignment2.controller.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import au.com.dylanaird.android.s3249319assignment2.controller.SyncService.FirebaseBackgroundService;

/**
 * Created by Dylan on 11/10/2015.
 */
public class FirebaseOnChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, FirebaseBackgroundService.class));
    }
}
