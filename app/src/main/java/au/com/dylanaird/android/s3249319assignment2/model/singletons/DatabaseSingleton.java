package au.com.dylanaird.android.s3249319assignment2.model.singletons;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Dylan on 12/08/2015.
 * <p/>
 * <p/>
 * this class is currently just a template i have put together and is out of scope
 * for assignment 1
 */
public class DatabaseSingleton extends SQLiteOpenHelper {

    public static final String TABLE_PARTIES = "parties";
    public static final String TABLE_MOVIES = "movies";
    public static final String TABLE_CONTACTS = "contacts";
    //Constant Data Columns Parties
    public static final String COLUMN_PARTY_ID = "partyId";
    public static final String COLUMN_PARTYLOCATION = "partyLocation";
    public static final String COLUMN_PARTYVENUE = "partyVenue";
    public static final String COLUMN_PARTY_DATE = "partyDate";
    public static final String COLUMN_PARTY_TIME = "partyTime";
    public static final String COLUMN_PARTY_MOVIE_ID = "partyMovieId";
    public static final String COLUMN_PARTY_LAST_UPDATED = "lastUpdated";
    //Constant Data Columns Movies
    public static final String COLUMN_MOVIE_ID = "movieId";
    public static final String COLUMN_TITLE = "movieTitle";
    public static final String COLUMN_YEAR = "movieYear";
    public static final String COLUMN_FULLPLOT = "movieFullPlot";
    public static final String COLUMN_SHORTPLOT = "movieShortPlot";
    public static final String COLUMN_POSTER = "moviePoster";
    public static final String COLUMN_RATING = "movieRating";
    public static final String COLUMN_MOVIE_LAST_UPDATED = "lastUpdated";
    //Constant Data Columns Contacts
    public static final String COLUMN_CONTACT_ID = "contactId";
    public static final String COLUMN_CONTACT_PARTY_ID = "contactPartyId";
    public static final String COLUMN_EMAIL = "contactEmailAddress";
    public static final String COLUMN_DISPLAYNAME = "contactDisplayName";
    public static final String COLUMN_NUMBER = "contactPhoneNumber";
    public static final String COLUMN_CONTACT_LAST_UPDATED = "lastUpdated";

    //Database Table constants
    protected static final String DATABASE_NAME = "socialClubDba2";
    protected static final int DATABASE_VERSION = 1;
    private static final String TAG = "DBBaseAdapter";

    /*Table for the Movies*/
    private static final String TABLE_CREATE_MOVIE = "CREATE TABLE IF NOT EXISTS " + TABLE_MOVIES + "("
            + COLUMN_MOVIE_LAST_UPDATED + " TEXT NOT NULL,"
            + COLUMN_MOVIE_ID + " CHAR(50) PRIMARY KEY NOT NULL,"
            + COLUMN_TITLE + " TEXT NOT NULL,"
            + COLUMN_YEAR + " TEXT NOT NULL,"
            + COLUMN_SHORTPLOT + " TEXT NOT NULL,"
            + COLUMN_FULLPLOT + " TEXT NOT NULL,"
            + COLUMN_POSTER + " TEXT NOT NULL,"
            + COLUMN_RATING + " REAL NOT NULL);";

    /*Table for parties*/
    private static final String TABLE_CREATE_PARTY = "CREATE TABLE IF NOT EXISTS " + TABLE_PARTIES + "("
            + COLUMN_PARTY_LAST_UPDATED + " TEXT NOT NULL,"
            + COLUMN_PARTY_ID + " TEXT PRIMARY KEY NOT NULL,"
            + COLUMN_PARTYLOCATION + " TEXT NOT NULL,"
            + COLUMN_PARTYVENUE + " TEXT NOT NULL,"
            + COLUMN_PARTY_DATE + " TEXT NOT NULL,"
            + COLUMN_PARTY_TIME + " TEXT NOT NULL,"
            + COLUMN_PARTY_MOVIE_ID + " TEXT NOT NULL);";

    /*Table for contacts*/
    private static final String TABLE_CREATE_CONTACT = "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTS + "("
            + COLUMN_CONTACT_LAST_UPDATED + " TEXT NOT NULL,"
            + COLUMN_CONTACT_ID + " TEXT PRIMARY KEY NOT NULL,"
            + COLUMN_CONTACT_PARTY_ID + " TEXT NOT NULL,"
            + COLUMN_DISPLAYNAME + " TEXT NOT NULL,"
            + COLUMN_NUMBER + " TEXT NOT NULL,"
            + COLUMN_EMAIL + " TEXT);";
    protected static DatabaseSingleton mInstance;
    private static SQLiteDatabase myWritableDb;
    protected Context mContext;

    private DatabaseSingleton(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.mContext = context;
    }

    public static synchronized DatabaseSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseSingleton(context);
        }
        return mInstance;
    }

    public synchronized SQLiteDatabase getMyWritableDatabase() {
        if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
            myWritableDb = this.getWritableDatabase();
        }
        return myWritableDb;
    }

    @Override
    public synchronized void close() {
        super.close();
        if (myWritableDb != null) {
            myWritableDb.close();
            myWritableDb = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_PARTY);
        db.execSQL(TABLE_CREATE_MOVIE);
        db.execSQL(TABLE_CREATE_CONTACT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " +
                newVersion + ", which will destroy all old data");
        onCreate(db);
    }
}
