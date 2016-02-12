package com.example.android.popularmoviesapp_p1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.RestAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {

    // strings for manipulating the JSON object received as extra

    final String TAG_ORIGINAL_TITLE = "original_title";
    final String TAG_RATINGS = "vote_average";
    final String TAG_RELEASE_DATE = "release_date";
    final String TAG_SYNOPSIS = "overview";
    final String TAG_POSTER_PATH = "poster_path";
    final String TAG_MOVIE_ID = "id";
    String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";    // base URL for the images, to be used by Picasso

    ListView review_listView;

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
                review_listView = (ListView) rootView.findViewById(R.id.review_listview);
                if (review_listView == null) {
                    Log.d(LOG_TAG, "ListView Adapter is null");
                }


                // load them up

                title_textView.setText(movieDetailsJSON.getString(TAG_ORIGINAL_TITLE));
                synopsis_textView.setText(movieDetailsJSON.getString(TAG_SYNOPSIS));
                year_textView.setText("Release Date: " + movieDetailsJSON.getString(TAG_RELEASE_DATE));
                ratings_textView.setText("Ratings: " + movieDetailsJSON.getString(TAG_RATINGS));

                // use Picasso to load up the Image View
                Picasso.with(getContext()).load(IMAGE_BASE_URL + movieDetailsJSON.getString(TAG_POSTER_PATH)).into(poster_imageView);

                FetchReviews newFetchReviews = new FetchReviews();
                newFetchReviews.execute(movieDetailsJSON.getString(TAG_MOVIE_ID));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return rootView;
    }

    private class FetchReviews extends AsyncTask<String, Void, JSONArray> {

        RestAdapter restAdapter;

        String API_URL = "http://api.themoviedb.org/3/movie/";
        String TAG_AUTHOR = "author";
        String TAG_CONTENT = "content";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            String movieID = params[0];
            API_URL = API_URL + movieID;
            restAdapter = new RestAdapter.Builder()
                    .setEndpoint(API_URL)
                    .build();
            ReviewsAPI reviewsAPI = restAdapter.create(ReviewsAPI.class);
            JSONObject reviewsJSONObject = reviewsAPI.getMovieReviews(BuildConfig.MOVIE_DB_API_KEY);
            Log.d(LOG_TAG, reviewsJSONObject.toString());
            JSONArray reviewsJSONArray = null;
            try {
                reviewsJSONArray = reviewsJSONObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return reviewsJSONArray;
        }

        @Override
        protected void onPostExecute(JSONArray reviewsJSONArray) {
            /*textView.setText(curators.title + "\n\n");
            for (Curator.Dataset dataset : curators.dataset) {
                textView.setText(textView.getText() + dataset.curator_title +
                        " - " + dataset.curator_tagline + "\n");
            }*/

            // set the text here

            ArrayList<String> reviewsArr  = new ArrayList<String>();

            for (int i = 0; i < reviewsJSONArray.length(); i++) {
                try {
                    JSONObject singleReview = reviewsJSONArray.getJSONObject(i);
                    String singleReviewStr = getFormattedReviewFromJSON(singleReview);
                    Log.d(LOG_TAG, singleReviewStr);
                    reviewsArr.add(singleReviewStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ArrayAdapter adapter = new ArrayAdapter<String> (getContext(), R.layout.list_item_reviews,
                    R.id.review_textview, new ArrayList<String>());
            review_listView.setAdapter(adapter);

        }

        private String getFormattedReviewFromJSON(JSONObject jsonObject) {
            String authorName = "";
            String content = "";
            try {
                authorName = jsonObject.getString(TAG_AUTHOR);
                content = jsonObject.getString(TAG_CONTENT);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "Name: " + authorName + "\n" + "Content: " + content + "\n";
        }
    }
}
