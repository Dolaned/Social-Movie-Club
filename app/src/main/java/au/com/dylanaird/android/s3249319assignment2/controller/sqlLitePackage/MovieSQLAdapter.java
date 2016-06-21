package au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.firebase.client.Firebase;

import java.util.ArrayList;

import au.com.dylanaird.android.s3249319assignment2.model.objects.MovieObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.DatabaseSingleton;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

/**
 * Created by Dylan on 13/08/2015.
 */
public class MovieSQLAdapter {
    DatabaseSingleton mInstance;
    HashMapSingleton hInstance;
    Context context;
    Firebase movieRef;
    String movieId;

    public MovieSQLAdapter(Context context) {
        this.context = context;
        mInstance = DatabaseSingleton.getInstance(this.context);
        hInstance = HashMapSingleton.getINSTANCE();
        Firebase.setAndroidContext(context);
    }


    public void populateMovieHashmap(String mId) {

        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_TITLE,
                DatabaseSingleton.COLUMN_MOVIE_ID,
                DatabaseSingleton.COLUMN_YEAR,
                DatabaseSingleton.COLUMN_SHORTPLOT,
                DatabaseSingleton.COLUMN_FULLPLOT,
                DatabaseSingleton.COLUMN_POSTER,
                DatabaseSingleton.COLUMN_RATING};
        String selection = DatabaseSingleton.COLUMN_MOVIE_ID + " =?";
        String[] selectionArgs = {mId}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_MOVIES, columnsToReturn, selection, selectionArgs, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                MovieObject m = new MovieObject(
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_TITLE)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_YEAR)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_SHORTPLOT)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_FULLPLOT)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_POSTER)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_MOVIE_ID)),
                        c.getFloat(c.getColumnIndex(DatabaseSingleton.COLUMN_RATING)));
                hInstance.addMovie(m.getMovieId(), m);
            }

            c.close();
        }
    }

    public void addMovietoDb(MovieObject m) {
        //get instance of Database.
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        //Values to insert.
        ContentValues cv = new ContentValues();
        cv.put(DatabaseSingleton.COLUMN_MOVIE_ID, m.getMovieId());
        cv.put(DatabaseSingleton.COLUMN_TITLE, m.getMovieTitle());
        cv.put(DatabaseSingleton.COLUMN_YEAR, m.getMovieYear());
        cv.put(DatabaseSingleton.COLUMN_SHORTPLOT, m.getMovieShortPlot());
        cv.put(DatabaseSingleton.COLUMN_FULLPLOT, m.getMovieFullPlot());
        cv.put(DatabaseSingleton.COLUMN_POSTER, m.getMoviePoster());
        cv.put(DatabaseSingleton.COLUMN_RATING, m.getMovieRating());
        cv.put(DatabaseSingleton.COLUMN_MOVIE_LAST_UPDATED, System.currentTimeMillis());
        db.insert(DatabaseSingleton.TABLE_MOVIES, null, cv);

    }

    public int searchForMovie(String mName) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_TITLE, DatabaseSingleton.COLUMN_MOVIE_ID};
        String selection = DatabaseSingleton.COLUMN_TITLE + " =?";
        String[] selectionArgs = {mName}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_MOVIES, columnsToReturn, selection, selectionArgs, null, null, null);

        if (c != null && c.getCount() > 0) {

            c.close();
            return 1;
        } else {

            return 0;
        }
    }

    public int searchForMovieById(String mName) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_TITLE, DatabaseSingleton.COLUMN_MOVIE_ID};
        String selection = DatabaseSingleton.COLUMN_MOVIE_ID + " =?";
        String[] selectionArgs = {mName}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_MOVIES, columnsToReturn, selection, selectionArgs, null, null, null);

        if (c != null && c.getCount() > 0) {
            c.close();
            return 1;
        } else {

            return 0;
        }
    }

    public MovieObject getMovie(String mId) {
        MovieObject m = null;
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_TITLE,
                DatabaseSingleton.COLUMN_MOVIE_ID,
                DatabaseSingleton.COLUMN_YEAR,
                DatabaseSingleton.COLUMN_SHORTPLOT,
                DatabaseSingleton.COLUMN_FULLPLOT,
                DatabaseSingleton.COLUMN_POSTER,
                DatabaseSingleton.COLUMN_RATING};
        String selection = DatabaseSingleton.COLUMN_MOVIE_ID + " =?";
        String[] selectionArgs = {mId}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_MOVIES, columnsToReturn, selection, selectionArgs, null, null, null);
        if (c.moveToFirst()) {

            m = new MovieObject(
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_TITLE)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_YEAR)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_SHORTPLOT)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_FULLPLOT)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_POSTER)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_MOVIE_ID)),
                    c.getFloat(c.getColumnIndex(DatabaseSingleton.COLUMN_RATING)));
        }

        c.close();
        return m;
    }

    public int updateMovie(MovieObject m) {
        // get writeaable database
        SQLiteDatabase db = mInstance.getMyWritableDatabase();
        int i;

        //set update content values
        ContentValues cv = new ContentValues();
        cv.put(DatabaseSingleton.COLUMN_MOVIE_ID, m.getMovieId());
        cv.put(DatabaseSingleton.COLUMN_TITLE, m.getMovieTitle());
        cv.put(DatabaseSingleton.COLUMN_YEAR, m.getMovieYear());
        cv.put(DatabaseSingleton.COLUMN_SHORTPLOT, m.getMovieShortPlot());
        cv.put(DatabaseSingleton.COLUMN_FULLPLOT, m.getMovieFullPlot());
        cv.put(DatabaseSingleton.COLUMN_POSTER, m.getMoviePoster());
        cv.put(DatabaseSingleton.COLUMN_RATING, m.getMovieRating());
        cv.put(DatabaseSingleton.COLUMN_MOVIE_LAST_UPDATED, System.currentTimeMillis());

        i = db.update(DatabaseSingleton.TABLE_MOVIES, cv, DatabaseSingleton.COLUMN_MOVIE_ID + " = '" + m.getMovieId() + "';", null);
        if (i > 0) {
            return i;
        }
        return i;
    }

    public void deleteMovieFromDb(String mId) {

        SQLiteDatabase db = mInstance.getMyWritableDatabase();
        db.delete(DatabaseSingleton.TABLE_MOVIES, DatabaseSingleton.COLUMN_MOVIE_ID + " = ?", new String[]{mId});

    }

    public ArrayList<MovieObject> searchForMoviebyString(String input) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();
        MovieObject m = null;
        ArrayList<MovieObject> mList = new ArrayList<MovieObject>();
        String[] columnsToReturn = {DatabaseSingleton.COLUMN_TITLE,
                DatabaseSingleton.COLUMN_MOVIE_ID,
                DatabaseSingleton.COLUMN_YEAR,
                DatabaseSingleton.COLUMN_SHORTPLOT,
                DatabaseSingleton.COLUMN_FULLPLOT,
                DatabaseSingleton.COLUMN_POSTER,
                DatabaseSingleton.COLUMN_RATING};
        Cursor c = db.query(true, DatabaseSingleton.TABLE_MOVIES, columnsToReturn,
                DatabaseSingleton.COLUMN_TITLE + " LIKE '%" + input + "%'", null, null, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (!c.moveToLast()) {
                m = new MovieObject(
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_TITLE)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_YEAR)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_SHORTPLOT)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_FULLPLOT)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_POSTER)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_MOVIE_ID)),
                        c.getFloat(c.getColumnIndex(DatabaseSingleton.COLUMN_RATING)));
                mList.add(m);
            }
        }

        return mList;
    }
}
