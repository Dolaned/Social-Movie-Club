package au.com.dylanaird.android.s3249319assignment2.controller.SyncService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Dylan on 11/10/2015.
 */
public class FirebaseSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static FirebaseSyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            Log.d("SyncService", "onCreate - SyncService");
            if (syncAdapter == null) {
                syncAdapter = new FirebaseSyncAdapter(getApplicationContext(), true);
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
