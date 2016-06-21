package au.com.dylanaird.android.s3249319assignment2.controller.asynctasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.JSONAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.JsonParser;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.SearchAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.MovieSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.model.objects.MovieObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

/**
 * Created by Dylan on 7/09/2015.
 */
public class JsonAsyncTask extends AsyncTask<String, String, JSONObject> {
    private Context context;
    private Activity activity;
    private HashMapSingleton hInstance;
    private ArrayList<String> mkeys = null;
    private ListView listView;
    private ArrayList<MovieObject> resultsList;
    private SearchAdapter adapter;
    private MovieSQLAdapter movieSQLAdapter;
    private MovieObject m;

    private String OMDBUrl = "http://www.omdbapi.com/?s=";

    public JsonAsyncTask(Activity activity) {
        this.activity = activity;
        this.context = activity;
        this.hInstance = HashMapSingleton.getINSTANCE();
        this.mkeys = new ArrayList<>(hInstance.getMovieMap().keySet());
        this.movieSQLAdapter = new MovieSQLAdapter(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listView = (ListView) activity.findViewById(R.id.lvResults);
        listView.clearChoices();
        resultsList = new ArrayList<MovieObject>();

    }

    @Override
    protected void onCancelled() {
        resultsList.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String input = "";
        if (params.length > 1) {
            for (int i = 0; i < params.length - 1; ++i) {
                input += params[i] + "+";
            }
            input += params[params.length - 1];
        } else {
            input = params[0];
        }

        JsonParser jsonParser = new JsonParser(context);
        JSONObject json = null;
        try {
            Log.i("URL String: ", OMDBUrl + input);
            json = jsonParser.getJSONFromUrl(OMDBUrl + input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    protected void onPostExecute(JSONObject json) {
            /*remove dialog and reset input string*/
        final ArrayList<String> mIdList = new ArrayList<>();

        JSONAdapter jsonAdapter = new JSONAdapter();
        try {
            JSONArray myJsonArray = (JSONArray) json.get("Search");
            if (myJsonArray != null) {
                for (int i = 0, size = myJsonArray.length(); i < size; i++) {
                    JSONObject myJObject = myJsonArray.getJSONObject(i);
                    jsonAdapter.ParseJsonSearchUrlObject(myJObject);

                    if (hInstance.getMovie(jsonAdapter.getjImdbID()) != null) {
                        m = hInstance.getMovie(jsonAdapter.getjImdbID());
                        resultsList.add(m);

                    } else if (movieSQLAdapter.searchForMovieById(jsonAdapter.getjImdbID()) > 0) {
                        m = movieSQLAdapter.getMovie(jsonAdapter.getjImdbID());
                        resultsList.add(m);

                    } else {
                        m = new MovieObject(jsonAdapter.getJtitle(), jsonAdapter.getjImdbID(), jsonAdapter.getjYear());
                        resultsList.add(m);
                    }
                    mIdList.add(jsonAdapter.getjImdbID());
                }

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject json = null;
                        JsonParser jsonParser = new JsonParser(context);
                        String Url = "http://www.omdbapi.com/?i=";

                        for (String s : mIdList) {
                            try {
                                //Log.d("URI", Url + s);
                                json = jsonParser.getJSONFromUrl(Url + s);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            JSONAdapter jsonAdapter = new JSONAdapter();

                            jsonAdapter.parseJsonMovieObject(json);


                            MovieObject m = new MovieObject(jsonAdapter.getJtitle(), jsonAdapter.getjYear(), jsonAdapter.getjShortPlot(), jsonAdapter.getjFullPlot(), jsonAdapter.getjImage(), jsonAdapter.getjImdbID(), Float.parseFloat(jsonAdapter.getRating()) / 2);

                            if (movieSQLAdapter.searchForMovieById(jsonAdapter.getjImdbID()) == 0) {
                                Log.d("Adding to Db", s);
                                movieSQLAdapter.addMovietoDb(m);
                            }
                        }

                    }
                });
                t.start();

                adapter = new SearchAdapter(resultsList, activity);
                listView.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
