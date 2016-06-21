package au.com.dylanaird.android.s3249319assignment2.controller.activities;

import android.content.ContentResolver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.SyncService.FirebaseSyncAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.MovieAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.ContactSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.MovieSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.PartySQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.threads.MemoryModelUpdate;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.DatabaseSingleton;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;


public class MovieList extends AppCompatActivity {

    public static final String ACTION_UPDATE_MAP = "movieList.ACTION_UPDATE_MAP";
    private static IntentFilter syncIntentFilter = new IntentFilter(ACTION_UPDATE_MAP);
    //private varibles for this class.
    DatabaseSingleton mInstance;
    //Database Adapters
    MovieSQLAdapter movieSQLAdapter;
    ContactSQLAdapter contactSQLAdapter;
    PartySQLAdapter partySQLAdapter;
    private MovieAdapter adapter;
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HashMapSingleton hInstance;


    /*
    * @param m - This function syncs the memory model with rhe database.
    *
    *
    *
    * */
    /*This method will update the listview adapter by calling notifyDataSetChanged. onResume of
    * this activity*/
    @Override
    protected void onResume() {
        super.onResume();
        if (HashMapSingleton.getINSTANCE().getMovieMap().size() > 0) {
            MemoryModelUpdate m = new MemoryModelUpdate(this);
            Thread t = new Thread(m);
            t.start();
            adapter = new MovieAdapter(MovieList.this);


            //create the list view
            listView = (ListView) findViewById(R.id.lvMovies);
            listView.setAdapter(adapter);
        }
        if (adapter != null) {
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.movieSwipeToRefresh);
        listView = (ListView) findViewById(R.id.lvMovies);
        mInstance = DatabaseSingleton.getInstance(this);
        movieSQLAdapter = new MovieSQLAdapter(this);
        contactSQLAdapter = new ContactSQLAdapter(this);
        partySQLAdapter = new PartySQLAdapter(this);

        /*This if statement checks if the instance of the movie map exists if it doesnt it calls
        * the populateMovieMap function*/
        hInstance = HashMapSingleton.getINSTANCE();
        if (hInstance != null) {
            MemoryModelUpdate m = new MemoryModelUpdate(MovieList.this);
            m.run();
            adapter = new MovieAdapter(MovieList.this);
            listView.setAdapter(adapter);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (hInstance != null) {
                        partySQLAdapter.populatePartyHashMap();

                        for (PartyObject p : hInstance.getPartyMap().values()) {
                            movieSQLAdapter.populateMovieHashmap(p.getPartyMovieId());
                            if (contactSQLAdapter.populateContactLists(p.getPartyId()) != null) {
                                p.setAttendees(contactSQLAdapter.populateContactLists(p.getPartyId()));
                            }
                        }
                        adapter = new MovieAdapter(MovieList.this);
                        listView.setAdapter(adapter);
                        swipeRefreshLayout.setRefreshing(false);
                    }

                }
            });

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movielist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            if (!ContentResolver.isSyncActive(FirebaseSyncAdapter.getSyncAccount(this), this.getString(R.string.content_authority))) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                ContentResolver.requestSync(FirebaseSyncAdapter.getSyncAccount(this),
                        this.getString(R.string.content_authority), bundle);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
