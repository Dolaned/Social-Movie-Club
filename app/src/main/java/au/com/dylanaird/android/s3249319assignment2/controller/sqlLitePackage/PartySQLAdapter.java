package au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import au.com.dylanaird.android.s3249319assignment2.controller.adapters.JSONAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.JsonParser;
import au.com.dylanaird.android.s3249319assignment2.model.objects.MovieObject;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.DatabaseSingleton;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

/**
 * Created by Dylan on 13/08/2015.
 */
public class PartySQLAdapter {

    DatabaseSingleton mInstance;
    HashMapSingleton hInstance;
    MovieSQLAdapter movieSQLAdapter;
    Context context;
    Firebase partyRef;
    String partyId;

    public PartySQLAdapter(Context context) {
        this.context = context;
        mInstance = DatabaseSingleton.getInstance(this.context);
        hInstance = HashMapSingleton.getINSTANCE();
        movieSQLAdapter = new MovieSQLAdapter(context);
    }

    public Boolean syncWithFirebase() {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();
        String query = "SELECT * FROM " + DatabaseSingleton.TABLE_PARTIES + " ;";
        Cursor c = db.rawQuery(query, null);
        partyRef = new Firebase("https://social-movie-club.firebaseio.com/parties");

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while ((!c.isAfterLast()) && c.getPosition() != c.getCount()) {

                /*
                * Check for each movie child in the cloud that a sqlite entry exists.
                * if not pull it down. then check if any new objects are in the sqlite database that
                * need to go to the cloud.
                * */
                partyId = c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_ID));
                Log.d("PartyID", partyId);
                partyRef.child(partyId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Log.d("Pushing to Firebase", partyId);
                            PartyObject p = getParty(partyId);
                            partyRef.child(partyId).setValue(p);
                            partyRef.child(partyId).child("lastUpdated").setValue(System.currentTimeMillis());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                c.moveToNext();
            }

            c.close();
        }
        partyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (searchForPartyById(childSnapshot.getKey()) == 0) {

                        Toast.makeText(context, "Adding From Firebase: " + childSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                        Log.d("Adding From Firebase", childSnapshot.getKey());
                        //fix add from here

                        PartyObject p = new PartyObject(childSnapshot.child(DatabaseSingleton.COLUMN_PARTY_MOVIE_ID).getValue().toString(),
                                childSnapshot.child(DatabaseSingleton.COLUMN_PARTYVENUE).getValue().toString(),
                                childSnapshot.child(DatabaseSingleton.COLUMN_PARTYLOCATION).getValue().toString(),
                                childSnapshot.child(DatabaseSingleton.COLUMN_PARTY_DATE).getValue().toString(),
                                childSnapshot.child(DatabaseSingleton.COLUMN_PARTY_TIME).getValue().toString());
                        p.setPartyId(UUID.fromString(childSnapshot.child(DatabaseSingleton.COLUMN_PARTY_ID).getValue().toString()));
                        final String movieId = childSnapshot.child("partyMovieId").getValue().toString();

                        if (movieSQLAdapter.searchForMovieById(movieId) == 0) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject json = null;
                                    JsonParser jsonParser = new JsonParser(context);
                                    String Url = "http://www.omdbapi.com/?i=";

                                    try {
                                        Log.d("URI", Url + movieId);
                                        json = jsonParser.getJSONFromUrl(Url + movieId);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    JSONAdapter jsonAdapter = new JSONAdapter();
                                    jsonAdapter.parseJsonMovieObject(json);


                                    MovieObject m = new MovieObject(jsonAdapter.getJtitle(), jsonAdapter.getjYear(), jsonAdapter.getjShortPlot(), jsonAdapter.getjFullPlot(), jsonAdapter.getjImage(), jsonAdapter.getjImdbID(), Float.parseFloat(jsonAdapter.getRating()) / 2);
                                    movieSQLAdapter.addMovietoDb(m);
                                }
                            });
                            t.start();

                        }

                        addPartyToDb(p);
                    } else {
                        if (childSnapshot.child(DatabaseSingleton.COLUMN_CONTACT_LAST_UPDATED).getValue() != null) {
                            Long lastUpdatedFirebase =
                                    Long.parseLong(childSnapshot.child(DatabaseSingleton.COLUMN_PARTY_LAST_UPDATED)
                                            .getValue().toString());
                            Long lastUpdatedSqlite = getLastUpdated(childSnapshot.getKey());

                            if (lastUpdatedSqlite > lastUpdatedFirebase) {
                                PartyObject p = getParty(childSnapshot.getKey());
                                Log.d("Updating to Firebase", childSnapshot.getKey());
                                Map<String, Object> updates = new HashMap<String, Object>();
                                updates.put(DatabaseSingleton.COLUMN_PARTY_MOVIE_ID, p.getPartyMovieId());
                                updates.put(DatabaseSingleton.COLUMN_PARTYVENUE, p.getPartyVenue());
                                updates.put(DatabaseSingleton.COLUMN_PARTYLOCATION, p.getPartyLocation());
                                updates.put(DatabaseSingleton.COLUMN_PARTY_DATE, p.getPartyDate());
                                updates.put(DatabaseSingleton.COLUMN_PARTY_TIME, p.getPartyTime());
                                updates.put(DatabaseSingleton.COLUMN_PARTY_LAST_UPDATED, System.currentTimeMillis());
                                partyRef.child(childSnapshot.getKey()).updateChildren(updates);


                            } else if (lastUpdatedFirebase > lastUpdatedSqlite) {

                                Log.d("Updating From Firebase", childSnapshot.getKey());

                                PartyObject p = new PartyObject(childSnapshot.child(DatabaseSingleton.COLUMN_PARTY_MOVIE_ID).getValue().toString(),
                                        childSnapshot.child(DatabaseSingleton.COLUMN_PARTYVENUE).getValue().toString(),
                                        childSnapshot.child(DatabaseSingleton.COLUMN_PARTYLOCATION).getValue().toString(),
                                        childSnapshot.child(DatabaseSingleton.COLUMN_PARTY_DATE).getValue().toString(),
                                        childSnapshot.child(DatabaseSingleton.COLUMN_PARTY_TIME).getValue().toString());
                                p.setPartyId(UUID.fromString(childSnapshot.child(DatabaseSingleton.COLUMN_PARTY_ID).getValue().toString()));
                                updateParty(p);
                            }
                        } else {
                            Log.d("Pushing to Firebase", partyId);
                            PartyObject p = getParty(partyId);
                            partyRef.child(partyId).setValue(p);
                            partyRef.child(partyId).child("lastUpdated").setValue(System.currentTimeMillis());
                        }

                    }

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return true;
    }

    public long getLastUpdated(String pId) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_MOVIE_LAST_UPDATED};
        String selection = DatabaseSingleton.COLUMN_PARTY_ID + " =?";
        String[] selectionArgs = {pId}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_PARTIES, columnsToReturn, selection, selectionArgs, null, null, null);

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            Long lastUpdatedSqlite = Long.parseLong(c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_LAST_UPDATED)));

            c.close();
            return lastUpdatedSqlite;
        } else {

            return 0;
        }
    }


    public int searchForPartyById(String pId) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_PARTY_ID};
        String selection = DatabaseSingleton.COLUMN_PARTY_ID + " =?";
        String[] selectionArgs = {pId}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_PARTIES, columnsToReturn, selection, selectionArgs, null, null, null);

        if (c != null && c.getCount() > 0) {

            c.close();
            return 1;
        } else {

            return 0;
        }
    }


    public void addPartyToDb(PartyObject p) {
        //get instance of Database.
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        //Values to insert.
        ContentValues cv = new ContentValues();
        cv.put(DatabaseSingleton.COLUMN_PARTY_ID, p.getPartyId().toString());
        cv.put(DatabaseSingleton.COLUMN_PARTY_MOVIE_ID, p.getPartyMovieId());
        cv.put(DatabaseSingleton.COLUMN_PARTYLOCATION, p.getPartyLocation());
        cv.put(DatabaseSingleton.COLUMN_PARTYVENUE, p.getPartyVenue());
        cv.put(DatabaseSingleton.COLUMN_PARTY_DATE, p.getPartyDate());
        cv.put(DatabaseSingleton.COLUMN_PARTY_TIME, p.getPartyTime());
        cv.put(DatabaseSingleton.COLUMN_PARTY_LAST_UPDATED, System.currentTimeMillis());
        Log.i("Content Values: ", cv.toString());
        db.insert(DatabaseSingleton.TABLE_PARTIES, null, cv);

    }

    public Boolean deletePartyFromDB(UUID pId) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();
        return db.delete(DatabaseSingleton.TABLE_PARTIES, DatabaseSingleton.COLUMN_PARTY_ID + " = '" + pId.toString() + "' ;", null) > 0;
    }

    public void populatePartyHashMap() {

        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseSingleton.TABLE_PARTIES + " ;", null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                PartyObject p = new PartyObject(
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_MOVIE_ID)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTYVENUE)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTYLOCATION)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_DATE)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_TIME)));

                String id = c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_ID));
                Log.d("Populating UUID", id.toString());
                p.setPartyId(UUID.fromString(id));
                hInstance.addParty(p.getPartyId(), p);
            }
            c.close();
        }

    }

    public int updateParty(PartyObject p) {
        // get writeaable database
        SQLiteDatabase db = mInstance.getMyWritableDatabase();
        int i;

        //set update content values
        ContentValues cv = new ContentValues();
        cv.put(DatabaseSingleton.COLUMN_PARTY_ID, p.getPartyId().toString());
        cv.put(DatabaseSingleton.COLUMN_PARTY_MOVIE_ID, p.getPartyMovieId());
        cv.put(DatabaseSingleton.COLUMN_PARTYLOCATION, p.getPartyLocation());
        cv.put(DatabaseSingleton.COLUMN_PARTYVENUE, p.getPartyVenue());
        cv.put(DatabaseSingleton.COLUMN_PARTY_DATE, p.getPartyDate());
        cv.put(DatabaseSingleton.COLUMN_PARTY_TIME, p.getPartyTime());
        cv.put(DatabaseSingleton.COLUMN_CONTACT_LAST_UPDATED, System.currentTimeMillis());

        i = db.update(DatabaseSingleton.TABLE_PARTIES, cv, DatabaseSingleton.COLUMN_PARTY_ID + " = '" + p.getPartyId().toString() + "';", null);
        if (i > 0) {
            Log.d("Party", p.getPartyId() + " Updated");
            return i;
        }
        Log.d("Party", p.getPartyId() + " Not Updated");
        return i;
    }

    public PartyObject getParty(String pId) {
        PartyObject p = null;
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_PARTY_ID,
                DatabaseSingleton.COLUMN_PARTYLOCATION,
                DatabaseSingleton.COLUMN_PARTYVENUE,
                DatabaseSingleton.COLUMN_PARTY_DATE,
                DatabaseSingleton.COLUMN_PARTY_TIME,
                DatabaseSingleton.COLUMN_PARTY_MOVIE_ID};
        String selection = DatabaseSingleton.COLUMN_PARTY_ID + " =?";
        String[] selectionArgs = {pId}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_PARTIES, columnsToReturn, selection, selectionArgs, null, null, null);
        if (c.moveToFirst()) {

            p = new PartyObject(c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_MOVIE_ID)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTYVENUE)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTYLOCATION)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_DATE)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_TIME)));
            p.setPartyId(UUID.fromString(c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_PARTY_ID))));
        }

        c.close();
        return p;
    }
}
