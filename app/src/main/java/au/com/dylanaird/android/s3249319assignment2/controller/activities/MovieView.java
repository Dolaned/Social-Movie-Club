package au.com.dylanaird.android.s3249319assignment2.controller.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.SyncService.FirebaseSyncAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.PartyAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.MovieSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.threads.BackgroundThread;
import au.com.dylanaird.android.s3249319assignment2.model.objects.MovieObject;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.VolleySingleton;


public class MovieView extends AppCompatActivity {

    //Private Class Varibles to be initialised.
    public PartyAdapter adapter;
    private ListView listView;
    private String movieId;
    private RatingBar rating;
    private Button addParty;
    private ImageView movieImageView;
    private TextView movieShortPlot;
    private TextView movieYear;
    private MovieObject m;
    private HashMapSingleton hInstance;
    private MovieSQLAdapter movieSQLAdapter;
    private BackgroundThread backgroundThread;
    private ImageLoader mImageLoader;

    protected void onResume() {
        super.onResume();
        adapter = new PartyAdapter(movieId, MovieView.this);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_view);
        movieSQLAdapter = new MovieSQLAdapter(this);

        //pull parcelable objects from intent Extra
        Bundle bdl = getIntent().getExtras();

        //get the party array and the position of the item clicked.
        movieId = bdl.getString("movieId");
        initFields();

        if (hInstance.getMovie(movieId) == null) {
            hInstance.addMovie(movieId, movieSQLAdapter.getMovie(movieId));
        }


        m = HashMapSingleton.getINSTANCE().getMovie(movieId);
        populateFields();

        if(!m.getMoviePoster().equals("blank")) {

            NetworkImageView movieImage = (NetworkImageView) findViewById(R.id.ivPoster);
            ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.imageProgressBar);
            mImageLoader = VolleySingleton.getInstance(MovieView.this).getImageLoader();
            movieImage.setImageUrl(m.getMoviePoster(), mImageLoader);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        addParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PartyManager.class);
                i.putExtra("movieId", movieId);
                i.putExtra("movieTitle", m.getMovieTitle());
                i.putExtra("CRUDMODE", "new");
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        movieSQLAdapter.updateMovie(m);
                    }
                };
                backgroundThread = new BackgroundThread(r);
                backgroundThread.run();
                startActivity(i);

            }
        });


        //create the new party adapter.
        adapter = new PartyAdapter(movieId, MovieView.this);
        listView.setAdapter(adapter);
    }

    private void populateFields() {
        //set page title.
        setTitle(m.getMovieTitle());

        //set all fields with the objects
        movieImageView.setImageBitmap(m.getMoviePosterImage());
        movieShortPlot.setText(m.getMovieShortPlot());
        rating.setClickable(true);
        rating.setRating(m.getMovieRating());


        movieYear.setText(m.getMovieYear());
        movieYear.setGravity(Gravity.CENTER);

        //change rating bar here,
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float r, boolean fromUser) {
                m.setRating(r);
                Toast.makeText(getApplicationContext(), "Rating Updated!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (m != null) {
            if (movieSQLAdapter.searchForMovie(m.getMovieTitle()) > 0) {
                movieSQLAdapter.updateMovie(m);
            }
            int i = 0;
            for (PartyObject p : hInstance.getPartyMap().values()) {
                if (p.getPartyMovieId().equals(m.getMovieId())) {
                    i++;
                }
            }
            if (i == 0) {
                hInstance.deleteMovie(m.getMovieId());
            }
        }
        if (!ContentResolver.isSyncActive(FirebaseSyncAdapter.getSyncAccount(this), this.getString(R.string.content_authority))) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            ContentResolver.requestSync(FirebaseSyncAdapter.getSyncAccount(this),
                    this.getString(R.string.content_authority), bundle);
        }

        this.finish();
    }

    public void initFields() {

        //Declare Views and fields
        movieImageView = (ImageView) findViewById(R.id.ivPoster);
        movieShortPlot = (TextView) findViewById(R.id.mvLongPlot);
        movieYear = (TextView) findViewById(R.id.mvYear);
        rating = (RatingBar) findViewById(R.id.mvRatingBar);

        addParty = (Button) findViewById(R.id.bAddParty);
        listView = (ListView) findViewById(R.id.lvParties);
        hInstance = HashMapSingleton.getINSTANCE();
    }
}