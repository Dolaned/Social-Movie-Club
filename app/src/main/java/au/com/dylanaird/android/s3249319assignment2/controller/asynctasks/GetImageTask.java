package au.com.dylanaird.android.s3249319assignment2.controller.asynctasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.model.objects.MovieObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

import static android.graphics.BitmapFactory.decodeStream;

/**
 * Created by Dylan on 7/09/2015.
 */
public class GetImageTask extends AsyncTask<String, String, MovieObject> {
    private ProgressDialog pDialog;
    private Context context;
    private Activity activity;
    private MovieObject m;
    private HashMapSingleton hInstance;
    private ProgressBar mProgressBar;


    public GetImageTask(Activity activity) {
        this.activity = activity;
        this.context = activity;
        this.hInstance = HashMapSingleton.getINSTANCE();

    }

    @Override
    protected void onPreExecute() {
        mProgressBar = (ProgressBar) activity.findViewById(R.id.imageProgressBar);
    }

    @Override
    protected MovieObject doInBackground(String... params) {
        m = this.hInstance.getMovie(params[0]);
        Log.d("URL", m.getMoviePoster());
        InputStream is = null;
        try {
            is = (InputStream) new URL(m.getMoviePoster()).getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bmp = decodeStream(is);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        m.setMoviePosterImage(bmp);
        return m;
    }

    @Override
    protected void onPostExecute(MovieObject movie) {
        /*remove dialog and reset input string*/
        m = movie;
        ImageView movieImageView = (ImageView) activity.findViewById(R.id.ivPoster);
        //set all fields with the objects
        movieImageView.setImageBitmap(m.getMoviePosterImage());
        mProgressBar.setVisibility(View.INVISIBLE);

    }
}
