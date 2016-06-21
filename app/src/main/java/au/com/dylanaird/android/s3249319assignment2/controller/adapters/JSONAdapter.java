package au.com.dylanaird.android.s3249319assignment2.controller.adapters;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Dylan on 13/08/2015.
 * <p/>
 * This is a custon adapter i created rather than static coding the movie objects into the hashmap,
 * as we are using json parsing for the second assignment i wanted to give myself a head start.
 */
public class JSONAdapter {

    private static final String TITLE = "Title";
    private static final String YEAR = "Year";
    private static final String FULLPLOT = "Plot";
    private static final String RATING = "imdbRating";
    private static final String SHORTPLOT = "Plot";
    private static final String POSTER = "Poster";
    private static final String omdbID = "imdbID";
    static String json;
    private String jtitle;
    private String jYear;
    private String jFullPlot;
    private String jShortPlot;
    private int jPosterInt;
    private String poster;
    private String jRating;
    private String jImageUrl;
    private String jImdbID;
    private JSONObject jObj;

    public JSONAdapter() {
    }

    public void ParseJsonSearchUrlObject(JSONObject jObj) {
        try {

            this.jObj = jObj;
            this.jtitle = jObj.getString(TITLE);
            this.jYear = jObj.getString(YEAR);
            this.jImdbID = this.jObj.getString(omdbID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parseJsonMovieObject(JSONObject jObj) {
        try {
            this.jObj = jObj;
            this.jtitle = jObj.getString(TITLE);
            this.jYear = jObj.getString(YEAR);
            this.jFullPlot = jObj.getString(FULLPLOT);
            this.jShortPlot = jObj.getString(SHORTPLOT);
            this.jRating = jObj.getString(RATING);
            this.jImageUrl = jObj.getString(POSTER);

            if (this.jRating.equals("N/A")) {
                this.jRating = "0";
            }
            if (jObj.getString(POSTER).equals("N/A")) {
                this.poster = "blank";
            } else {
                this.poster = jObj.getString(POSTER);
            }


            this.jImdbID = this.jObj.getString(omdbID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean ParseJsonTitleUrlUbject() {
        return true;
    }

    public String getJtitle() {
        return jtitle;
    }

    public int getjPosterInt() {
        return this.jPosterInt;
    }

    public String getjYear() {
        return jYear;
    }

    public String getjFullPlot() {
        return jFullPlot;
    }

    public String getjShortPlot() {
        return jShortPlot;
    }

    public String getjImage() {
        return poster;
    }

    public String getjImdbID() {
        return jImdbID;
    }

    public String getRating() {
        return jRating;
    }

    public JSONObject getJSONFromFile(InputStream i) {
        try {
            BufferedReader jsonBufferedReader = new BufferedReader(new InputStreamReader(
                    i), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = jsonBufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            i.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        return jObj;
    }
}
