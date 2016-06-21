package au.com.dylanaird.android.s3249319assignment2.controller.SyncService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.ContactSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.MovieSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.PartySQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.DatabaseSingleton;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

/**
 *
 * This class is a sync adapter used to sync firebase and the local database
 * Created by Dylan on 9/10/2015.
 */
public class FirebaseSyncAdapter extends AbstractThreadedSyncAdapter {

    //Database Access.
    DatabaseSingleton mInstance;
    HashMapSingleton hInstance;
    //Database Adapters
    MovieSQLAdapter movieSQLAdapter;
    ContactSQLAdapter contactSQLAdapter;
    PartySQLAdapter partySQLAdapter;


    public FirebaseSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        hInstance = HashMapSingleton.getINSTANCE();
        mInstance = DatabaseSingleton.getInstance(context);
        movieSQLAdapter = new MovieSQLAdapter(context);
        contactSQLAdapter = new ContactSQLAdapter(context);
        partySQLAdapter = new PartySQLAdapter(context);
    }

    public FirebaseSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {


        partySQLAdapter.syncWithFirebase();
        contactSQLAdapter.syncWithFirebase();
        Log.e("Sync Status", "Called");
        updateAll();

    }

    public void updateAll() {
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
}
