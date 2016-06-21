package au.com.dylanaird.android.s3249319assignment2.controller.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.SearchAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.asynctasks.JsonAsyncTask;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.MovieSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.model.objects.MovieObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

public class SearchActivity extends AppCompatActivity {

    private EditText searchField;
    private ConnectivityReceiver receiver;
    private AsyncTask searchTask;
    private ConnectivityManager cm;
    private ListView listView;
    private HashMapSingleton hInstance;
    private MovieSQLAdapter movieSQLAdapter;
    private SearchAdapter adapter;
    private SharedPreferences preferences;
    private static Boolean isConnected;
    private ArrayList<MovieObject> results;
    private IntentFilter mIntentFilter;

    @Override
    protected void onPause() {
        super.onPause();
        if (searchField.length() > 0) {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("searchQuery", searchField.getText().toString());
            editor.apply();
        }


    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(this.receiver != null){
            this.unregisterReceiver(receiver);
        }

    }

    protected void onResume() {
        super.onResume();
        //maintain search term
        String s = preferences.getString("searchQuery", "");
        if (!s.equalsIgnoreCase("")) {
            searchField.setText(s);
        }
        registerReceiver(receiver,mIntentFilter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //set activity title
        setTitle(R.string.titleOMDBSearch);

        mIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        //register connectivity manager for inital network state
        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        //get shared preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //init all activity fields
        initFields();

    }

    private void initFields() {
        searchField = (EditText) findViewById(R.id.etSearchField);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 1) {
                    searchOMDB(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void searchOMDB(CharSequence s) {

        //this fucntion checks if network is connected and performs a search, otherwise iits searches local models.

        String[] input = s.toString().split("\\s+");
        results = new ArrayList<MovieObject>();

        if (searchTask != null) {
            searchTask.cancel(true);
        }
        if (isConnected) {
            searchTask = new JsonAsyncTask(SearchActivity.this).execute(input);
        } else {
            listView = (ListView) findViewById(R.id.lvResults);
            hInstance = HashMapSingleton.getINSTANCE();
            offlineChain(s.toString());

        }


    }

    public static class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
             isConnected = isNetworkConnected(context);
        }

        private Boolean isNetworkConnected(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                return false;
            } else {
                return true;
            }
        }
    }

    public void offlineChain(String searchQuery){
        movieSQLAdapter = new MovieSQLAdapter(SearchActivity.this);
        //Array list for storing objects
        //results.clear();

        //loop through the memory model
        for (MovieObject m : hInstance.getMovieMap().values()) {

            //check if the movie title contains the query
            if (m.getMovieTitle().toLowerCase().contains(searchQuery.toLowerCase())){
                results.add(m);
            }
        }

        if (movieSQLAdapter.searchForMoviebyString(searchQuery).size() > 0) {
            for (MovieObject m : movieSQLAdapter.searchForMoviebyString(searchQuery)) {

                for(int i = 0; i < results.size(); i++){
                    if(!results.get(i).getMovieTitle().toLowerCase().contains(m.getMovieTitle().toLowerCase())){
                        results.add(m);
                    }
                }
            }
        }
        if(results.size() > 0){
            adapter = new SearchAdapter(results, SearchActivity.this);
            listView.setAdapter(adapter);
        }else{
            Log.d("Results: ","NO MOVIES FOUND");
        }

    }

}
