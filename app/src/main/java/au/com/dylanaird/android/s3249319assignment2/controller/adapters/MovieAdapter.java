package au.com.dylanaird.android.s3249319assignment2.controller.adapters;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.activities.MovieView;
import au.com.dylanaird.android.s3249319assignment2.model.objects.MovieObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.VolleySingleton;

/**
 * Created by Dylan on 12/08/2015.
 */
public class MovieAdapter extends BaseAdapter {
    private HashMapSingleton hInstance;
    private ArrayList<String> mKeys = null;
    private Context context;
    private Activity activity;
    private boolean isConnected;
    private ImageLoader mImageLoader;

    public MovieAdapter(Activity activity) {
        this.context = activity;
        this.activity = activity;
        hInstance = HashMapSingleton.getINSTANCE();
        mKeys = new ArrayList<String>(hInstance.getMovieMap().keySet());
    }


    @Override
    public int getCount() {
        return hInstance.getMovieMap().size();
    }

    @Override
    public MovieObject getItem(int position) {
        return hInstance.getMovie(mKeys.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the movie object position
        final MovieObject m = getItem(position);
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        } else {
            result = convertView;
        }

        TextView movieTitle = (TextView) result.findViewById(R.id.tvTitle);
        TextView movieYear = (TextView) result.findViewById(R.id.tvYear);
        TextView movieShortPlot = (TextView) result.findViewById(R.id.tvShortPlot);
        final NetworkImageView movieImage = (NetworkImageView) result.findViewById(R.id.ivPoster);
        RatingBar mratingBar = (RatingBar) result.findViewById(R.id.mlvRatingBar);
        final ProgressBar mProgressbar = (ProgressBar)result.findViewById(R.id.imageProgressBar);
        mProgressbar.setVisibility(View.VISIBLE);

        Button movieView = (Button) result.findViewById(R.id.bViewParty);

        movieView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MovieView.class);
                i.putExtra("movieId", m.getMovieId());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });


        movieTitle.setText(m.getMovieTitle());
        movieYear.setText(m.getMovieYear());
        movieShortPlot.setText(m.getMovieShortPlot());

        if(!m.getMoviePoster().equals("blank")) {
            mImageLoader = VolleySingleton.getInstance(context).getImageLoader();
            movieImage.setImageUrl(m.getMoviePoster(), mImageLoader);
            mProgressbar.setVisibility(View.INVISIBLE);
        }

        mratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                m.setRating(rating);
                hInstance.addMovie(m.getMovieId(),m);

            }
        });

        mratingBar.setRating(m.getMovieRating());
        return result;
    }

    public class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            isNetworkConnected(context);
        }

        private Boolean isNetworkConnected(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                isConnected = false;
                return false;
            } else {
                isConnected = true;
                return true;
            }
        }
    }

}