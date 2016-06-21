package au.com.dylanaird.android.s3249319assignment2.controller.adapters;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dylan on 3/09/2015.
 */
public class JsonParser {

    static InputStream is;
    static JSONObject jObj;
    static String json;
    private Context context;

    public JsonParser(Context c) {
        this.context = c;
    }


    public JSONObject getJSONFromUrl(String urlString) throws IOException {

        // Making HTTP request

        try {

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            // read the response
            Log.i("Response Code: ", Integer.toString(conn.getResponseCode()));
            is = new BufferedInputStream(conn.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
            is.close();
            json = sb.toString();
            //Log.i("Result: ",sb.toString());
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }
}
