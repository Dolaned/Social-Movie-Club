package au.com.dylanaird.android.s3249319assignment2.controller.threads;

import android.content.Context;
import android.util.Log;

import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.ContactSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.MovieSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.PartySQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.DatabaseSingleton;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

/**
 * Created by Dylan on 13/10/2015.
 */


public class MemoryModelUpdate implements Runnable {
    Context context;
    //Database Access.
    DatabaseSingleton mInstance;
    HashMapSingleton hInstance;
    //Database Adapters
    MovieSQLAdapter movieSQLAdapter;
    ContactSQLAdapter contactSQLAdapter;
    PartySQLAdapter partySQLAdapter;

    public MemoryModelUpdate(Context context) {
        this.context = context;

    }

    @Override
    public void run() {
        mInstance = DatabaseSingleton.getInstance(context);
        movieSQLAdapter = new MovieSQLAdapter(context);
        contactSQLAdapter = new ContactSQLAdapter(context);
        partySQLAdapter = new PartySQLAdapter(context);
        if (hInstance != null) {
            partySQLAdapter.populatePartyHashMap();

            for (PartyObject p : hInstance.getPartyMap().values()) {
                movieSQLAdapter.populateMovieHashmap(p.getPartyMovieId());
                if (contactSQLAdapter.populateContactLists(p.getPartyId()) != null) {
                    p.setAttendees(contactSQLAdapter.populateContactLists(p.getPartyId()));
                }
            }
        }
        Log.d("UPDATING", "Model");
    }
}

