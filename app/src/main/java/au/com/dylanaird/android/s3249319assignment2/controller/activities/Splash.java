package au.com.dylanaird.android.s3249319assignment2.controller.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.SyncService.FirebaseSyncAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.ContactSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.MovieSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.PartySQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;
import au.com.dylanaird.android.s3249319assignment2.view.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Splash extends Activity {
    /**
     * Duration of wait
     **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    MovieSQLAdapter movieSQLAdapter;
    ContactSQLAdapter contactSQLAdapter;
    PartySQLAdapter partySQLAdapter;
    HashMapSingleton hInstance = HashMapSingleton.getINSTANCE();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        movieSQLAdapter = new MovieSQLAdapter(Splash.this);
        partySQLAdapter = new PartySQLAdapter(Splash.this);
        contactSQLAdapter = new ContactSQLAdapter(Splash.this);

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        setContentView(R.layout.activity_splash);
        /*populate movielist*/
        if (isConnected) {
            FirebaseSyncAdapter.syncImmediately(Splash.this);
        } else {
            if (hInstance != null) {
                partySQLAdapter.populatePartyHashMap();

                for (PartyObject p : hInstance.getPartyMap().values()) {
                    movieSQLAdapter.populateMovieHashmap(p.getPartyMovieId());
                    if (contactSQLAdapter.populateContactLists(p.getPartyId()) != null) {
                        p.setAttendees(contactSQLAdapter.populateContactLists(p.getPartyId()));
                    }
                }
            }
        }
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(Splash.this, WelcomePage.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);


    }


}



