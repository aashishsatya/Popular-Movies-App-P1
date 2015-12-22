package com.example.android.popularmoviesapp_p1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {

    final String TAG_ORIGINAL_TITLE = "original_title";
    final String TAG_RATINGS = "vote_average";
    final String TAG_RELEASE_DATE = "release_date";
    final String TAG_SYNOPSIS = "overview";
    final String TAG_POSTER_PATH = "poster_path";
    final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Intent oldIntent = getActivity().getIntent();
        if (oldIntent != null && oldIntent.hasExtra(Intent.EXTRA_TEXT)) {
            String movieDetails = oldIntent.getStringExtra(Intent.EXTRA_TEXT);
            try {

                // convert the String extra back into JSON
                JSONObject movieDetailsJSON = new JSONObject(movieDetails);

                // get all the required views

                TextView title_textView = (TextView) rootView.findViewById(R.id.title_textView);
                TextView synopsis_textView = (TextView) rootView.findViewById(R.id.synopsis_textView);
                ImageView poster_imageView = (ImageView) rootView.findViewById(R.id.poster_imageView);
                TextView year_textView = (TextView) rootView.findViewById(R.id.year_textView);
                TextView ratings_textView = (TextView) rootView.findViewById(R.id.ratings_textView);

                // load them up

                title_textView.setText(movieDetailsJSON.getString(TAG_ORIGINAL_TITLE));
                synopsis_textView.setText(movieDetailsJSON.getString(TAG_SYNOPSIS));
                year_textView.setText(movieDetailsJSON.getString(TAG_RELEASE_DATE));
                ratings_textView.setText(movieDetailsJSON.getString(TAG_RATINGS));

                // use Picasso again to load up the Image View
                Picasso.with(getContext()).load(IMAGE_BASE_URL + movieDetailsJSON.getString(TAG_POSTER_PATH)).into(poster_imageView);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return rootView;
    }
}
