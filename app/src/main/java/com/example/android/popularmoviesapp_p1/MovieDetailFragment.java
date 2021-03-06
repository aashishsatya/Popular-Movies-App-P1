package com.example.android.popularmoviesapp_p1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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
    final String FAV_MOVIE_KEY = "favorite_movies";
    String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";    // base URL for the images, to be used by Picasso

    ListView review_listView;
    ListView trailers_listView;

    JSONObject movieDetailsJSON;
    int currentMovieIndexInArr = -1;
    SharedPreferences sharedPref;

    // variables for showing the 'Starred' status
    boolean isCurrentMovieFav = false;
    ImageButton favoritesButton;
    String currentFavs;
    JSONArray currentFavsJSONArr;


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
                movieDetailsJSON = new JSONObject(movieDetails);

                // get all the required views

                TextView title_textView = (TextView) rootView.findViewById(R.id.title_textView);
                TextView synopsis_textView = (TextView) rootView.findViewById(R.id.synopsis_textView);
                ImageView poster_imageView = (ImageView) rootView.findViewById(R.id.poster_imageView);
                TextView year_textView = (TextView) rootView.findViewById(R.id.year_textView);
                TextView ratings_textView = (TextView) rootView.findViewById(R.id.ratings_textView);
                review_listView = (ListView) rootView.findViewById(R.id.review_listview);
                trailers_listView = (ListView) rootView.findViewById(R.id.trailers_listView);
                favoritesButton = (ImageButton) rootView.findViewById(R.id.fav_imageButton);

                // load them up

                title_textView.setText(movieDetailsJSON.getString(TAG_ORIGINAL_TITLE));
                synopsis_textView.setText(movieDetailsJSON.getString(TAG_SYNOPSIS));
                year_textView.setText("Release Date: " + movieDetailsJSON.getString(TAG_RELEASE_DATE));
                ratings_textView.setText("Ratings: " + movieDetailsJSON.getString(TAG_RATINGS));

                // load the correct icon for 'Starred'

                // check if the movie has been starred by the user

                isCurrentMovieFav = false;

                // get the list of movies starred first

                sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                currentFavs = sharedPref.getString(FAV_MOVIE_KEY, "");   // all the current favourites stored
                if (currentFavs.equals("")) {
                    currentFavsJSONArr = new JSONArray();
                    Log.d(LOG_TAG, "Got empty stored pref");
                }
                else {
                    currentFavsJSONArr = new JSONArray(currentFavs);
                    Log.d(LOG_TAG + "retr:", currentFavsJSONArr.toString());
                }
                for (int i = 0; i < currentFavsJSONArr.length(); i++) {
                    JSONObject favMovie = currentFavsJSONArr.getJSONObject(i);
                    if (favMovie.getString(TAG_MOVIE_ID).equals(movieDetailsJSON.getString(TAG_MOVIE_ID))) {
                        // current movie has been favorited
                        isCurrentMovieFav = true;
                        currentMovieIndexInArr = i;
                        break;
                    }
                }

                // if the movie is a favorite, use the golden star
                if (isCurrentMovieFav) {
                    favoritesButton.setImageResource(R.drawable.star_yes);
                }
                // if no, use the grey star
                else {
                    favoritesButton.setImageResource(R.drawable.star_no);
                }
                // set the ClickListenerItem for the image button
                favoritesButton.setOnClickListener(listener);

                // use Picasso to load up the Image View
                Picasso.with(getContext()).load(IMAGE_BASE_URL + movieDetailsJSON.getString(TAG_POSTER_PATH)).into(poster_imageView);

                // query and load trailers
                FetchTrailers newFetchTrailers = new FetchTrailers();
                newFetchTrailers.execute(movieDetailsJSON.getString(TAG_MOVIE_ID));

                // query and load movie reviews
                FetchReviews newFetchReviews = new FetchReviews();
                newFetchReviews.execute(movieDetailsJSON.getString(TAG_MOVIE_ID));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return rootView;
    }

    // define the OnClickListener item for the ImageButton

    ImageButton.OnClickListener listener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            // function to change the favorite status
            // if this function gets called this means the star icon has been clicked

            // check if the movie is already a favourite

            if (isCurrentMovieFav) {

                // means now the movie needs to be de-starred

                JSONArray newFavsJSONArr = new JSONArray();

                // remove it from the list of movies
                // current movie's index (currentMovieIndexArr) has already been calculated; need not do so again
                for (int i = 0; i < currentFavsJSONArr.length(); i++) {
                    if (i != currentMovieIndexInArr) {
                        // add it to the new list
                        try {
                            newFavsJSONArr.put(currentFavsJSONArr.getJSONObject(i));
                            // unfortunately a simple .remove() cannot be used because our MinSDK is 15
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // write the result back to the shared preference
                sharedPref.edit().putString(FAV_MOVIE_KEY, newFavsJSONArr.toString()).apply();

                // de-star
                favoritesButton.setImageResource(R.drawable.star_no);
                // set isCurrentMovieFav to false
                isCurrentMovieFav = false;
            }
            else {

                // means we have a new movie to star

                currentFavsJSONArr.put(movieDetailsJSON);
                // write to file
                sharedPref.edit().putString(FAV_MOVIE_KEY, currentFavsJSONArr.toString()).apply();
                // change icon to starred
                favoritesButton.setImageResource(R.drawable.star_yes);
                isCurrentMovieFav = true;
            }
        }
    };

    private class FetchReviews extends AsyncTask<String, Void, JSONArray> {

        String API_URL = "http://api.themoviedb.org/3/movie/";
        String API_PARAM = "api_key";
        String TAG_AUTHOR = "author";
        String TAG_CONTENT = "content";
        String TAG_RESULTS = "results";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String reviewsJSONStr = null;

            try {

                // code referenced from Udacity's Sunshine app

                // Construct the URL for the MovieDB query
                // Possible parameters are avaiable at MovieDB's forecast API page, at
                // http://docs.themoviedb.apiary.io/#reference/discover

                // build the new URL

                Uri builtUri = Uri.parse(API_URL + params[0] + "/reviews")
                        .buildUpon()
                        .appendQueryParameter(API_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                reviewsJSONStr = buffer.toString();
                // obtain and return the data
                try {
                    JSONObject reviewsJSONObject = new JSONObject(reviewsJSONStr);
                    Log.d(LOG_TAG, reviewsJSONObject.toString());
                    JSONArray reviewJSONArray = reviewsJSONObject.getJSONArray(TAG_RESULTS);
                    // successfully obtained movie data from JSON string
                    // now return this value
                    return reviewJSONArray;
                } catch (JSONException e) {
                    // can't get movie data from obtained string
                    // nothing to do
                    return null;
                }
            } catch (IOException e) {
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Toast.makeText(getActivity(), "Error closing data stream", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(JSONArray reviewsJSONArray) {

            // set the text here

            ArrayList<String> reviewsArr  = new ArrayList<String>();

            for (int i = 0; i < reviewsJSONArray.length(); i++) {
                try {
                    JSONObject singleReview = reviewsJSONArray.getJSONObject(i);
                    String singleReviewStr = getFormattedReviewFromJSON(singleReview);
                    //Log.d(LOG_TAG, singleReviewStr);
                    reviewsArr.add(singleReviewStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ArrayAdapter adapter = new ArrayAdapter<String> (getContext(), R.layout.list_item_reviews,
                    R.id.review_textview, reviewsArr);
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

            return "Name: " + authorName + "\n" + "Review: " + content + "\n";
        }
    }

    private class FetchTrailers extends AsyncTask<String, Void, JSONArray> {

        String API_URL = "http://api.themoviedb.org/3/movie/";
        String API_PARAM = "api_key";
        String TAG_RESULTS = "results";
        String TAG_NAME = "name";
        String TAG_KEY = "key";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String videosJSONStr = null;

            try {

                // code referenced from Udacity's Sunshine app

                // Construct the URL for the MovieDB query
                // Possible parameters are avaiable at MovieDB's forecast API page, at
                // http://docs.themoviedb.apiary.io/#reference/discover

                // build the new URL

                Uri builtUri = Uri.parse(API_URL + params[0] + "/videos")
                        .buildUpon()
                        .appendQueryParameter(API_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                videosJSONStr = buffer.toString();
                // obtain and return the data
                try {
                    JSONObject reviewsJSONObject = new JSONObject(videosJSONStr);
                    Log.d(LOG_TAG, reviewsJSONObject.toString());
                    JSONArray reviewJSONArray = reviewsJSONObject.getJSONArray(TAG_RESULTS);
                    // successfully obtained movie data from JSON string
                    // now return this value
                    return reviewJSONArray;
                } catch (JSONException e) {
                    // can't get movie data from obtained string
                    // nothing to do
                    return null;
                }
            } catch (IOException e) {
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Toast.makeText(getActivity(), "Error closing data stream", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(final JSONArray reviewsJSONArray) {

            // set the text to display here

            final ArrayList<String> videosArr  = new ArrayList<String>();

            for (int i = 0; i < reviewsJSONArray.length(); i++) {
                try {
                    JSONObject singleVideo = reviewsJSONArray.getJSONObject(i);
                    String singleVideoString = singleVideo.getString(TAG_NAME);
                    //Log.d(LOG_TAG, singleVideoString);
                    videosArr.add(singleVideoString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // reusing the layout and TextViews here
            ArrayAdapter adapter = new ArrayAdapter<String> (getContext(), R.layout.list_item_reviews,
                    R.id.review_textview, videosArr);
            trailers_listView.setAdapter(adapter);
            trailers_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // get the URL of the youtube video and pass as Intent
                    if (videosArr.size() != 0) {
                        // launch the YouTube video
                        try {
                            JSONObject requiredVideoJSONObject = reviewsJSONArray.getJSONObject(position);
                            String trailerKey = requiredVideoJSONObject.getString(TAG_KEY);
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailerKey)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }
    }
}
