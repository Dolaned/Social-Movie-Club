package au.com.dylanaird.android.s3249319assignment2.controller.SyncService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Dylan on 11/10/2015.
 */
public class AuthenticatorService extends Service {

    //instiate field for authenticator
    private Authenticator mAuthenticator;

    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
