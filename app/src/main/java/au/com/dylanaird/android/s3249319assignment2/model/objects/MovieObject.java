package au.com.dylanaird.android.s3249319assignment2.model.objects;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Dylan on 30/07/2015.
 */
public class MovieObject {

    //private class variables
    private String movieTitle;
    private String movieYear;
    private String movieFullPlot;
    private String movieShortPlot;
    private String moviePoster;
    private Bitmap moviePosterImage;
    private String movieId;
    private float movieRating;

    //Default construtor
    public MovieObject(String t, String y, String sP, String fP, String P, String id, float r) {
        this.movieTitle = t;
        this.movieYear = y;
        this.movieFullPlot = fP;
        this.movieShortPlot = sP;
        this.moviePoster = P;
        this.movieId = id;
        this.movieRating = r;

    }

    public MovieObject(String t, String id, String y) {
        this.movieTitle = t;
        this.movieId = id;
        this.movieYear = y;
    }

    //getters and setters
    public String getMovieYear() {
        return movieYear;
    }

    public String getMoviePoster() {
        return moviePoster;
    }

    public String getMovieShortPlot() {
        return movieShortPlot;
    }

    public String getMovieFullPlot() {
        return movieFullPlot;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public float getMovieRating() {
        return movieRating;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setRating(float rating) {
        this.movieRating = rating;
    }

    public void setPoster(String p) {
        this.moviePoster = p;
    }

    public String bitMapToString(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bmp.recycle();
        byte[] byteArray = stream.toByteArray();
        String imageFile = Base64.encodeToString(byteArray, Base64.NO_PADDING);
        return imageFile;
    }

    public Bitmap stringToBitmap(String b) {
        if (b.equals("Blank")) {
            return null;
        } else {
            String[] safe = b.split("=");
            byte[] imageAsBytes = Base64.decode(safe[0], Base64.NO_PADDING);
            Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
            return bmp;
        }

    }

    public Bitmap getMoviePosterImage() {
        return moviePosterImage;
    }

    public void setMoviePosterImage(Bitmap moviePosterImage) {
        this.moviePosterImage = moviePosterImage;
    }
}
