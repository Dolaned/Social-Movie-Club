package au.com.dylanaird.android.s3249319assignment2.controller.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.activities.MovieView;
import au.com.dylanaird.android.s3249319assignment2.model.objects.MovieObject;

/**
 * Created by Dylan on 3/09/2015.
 */
public class SearchAdapter extends BaseAdapter {
    ArrayList<MovieObject> resultsList;
    Context context;
    Activity activity;

    public SearchAdapter(ArrayList<MovieObject> list, Activity activity) {
        this.resultsList = list;
        this.activity = activity;
        this.context = activity;
    }

    @Override
    public int getCount() {
        return resultsList.size();
    }

    @Override
    public MovieObject getItem(int position) {
        return resultsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MovieObject m = getItem(position);
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        } else {
            result = convertView;
        }

        TextView title = (TextView) result.findViewById(R.id.tvJsonTitle);
        TextView year = (TextView) result.findViewById(R.id.tvJsonYear);
        TextView onDevice = (TextView) result.findViewById(R.id.tvOnDevice);
        Button viewMovie = (Button) result.findViewById(R.id.bViewMovie);
        onDevice.setVisibility(View.GONE);

        viewMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MovieView.class);
                i.putExtra("search", m.getMovieId());
                i.putExtra("movieId", m.getMovieId());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                activity.finish();
            }
        });
        if (m != null) {
            title.setText(m.getMovieTitle());
            year.setText(m.getMovieYear());
            if (m.getMovieShortPlot() != null) {
                onDevice.setVisibility(View.VISIBLE);
            }
        }


        return result;
    }
}
