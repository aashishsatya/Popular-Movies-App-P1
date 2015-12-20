package com.example.android.popularmoviesapp_p1;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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
public class MainActivityFragment extends Fragment {

    ArrayList<String> posterPaths = new ArrayList<String>();
    JSONArray movieArray = null;
    GridView gridView;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView) rootView.findViewById(R.id.gridview);
        new FetchMovieTask().execute();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (movieArray != null) {
                    // get the movie details
                    try {
                        String movieDetails = movieArray.getJSONObject(position).toString();
                        Intent movieDetailsIntent = new Intent(getActivity(), MovieDetail.class);
                        movieDetailsIntent.putExtra(Intent.EXTRA_TEXT, movieDetails);
                        startActivity(movieDetailsIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return rootView;
    }

    public class FetchMovieTask extends AsyncTask<Void, Void, JSONArray> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
        final String SORT_PARAM = "sort_by";
        final String API_PARAM = "api_key";
        final String TAG_POSTER_PATH = "poster_path";

        @Override
        protected JSONArray doInBackground(Void... nothing) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJSONStr = null;
            int numMovies = 20;

            try {
                // Construct the URL for the MovieDB query
                // Possible parameters are avaiable at MovieDB's forecast API page, at
                // http://docs.themoviedb.apiary.io/#reference/discover

                // build the new URL

                Uri builtUri = Uri.parse(MOVIE_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(SORT_PARAM, "popularity.desc")
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
                movieJSONStr = buffer.toString();
                // Log.d(LOG_TAG, movieJSONStr);
                // obtain and return the data
                try {
                    JSONArray movieData = getMovieDataFromJSON(movieJSONStr, numMovies);
                    // successfully obtained movie data from JSON string
                    // now return this value
                    return movieData;
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
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
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private JSONArray getMovieDataFromJSON(String movieJSONStr, int numMovies)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIE_LIST = "results";

            JSONObject movieJSON = new JSONObject(movieJSONStr);
            movieArray = movieJSON.getJSONArray(MOVIE_LIST);

            /*for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movieDetails = movieArray.getJSONObject(i);
                Log.v(LOG_TAG, "Movie Detail: " + movieDetails.toString());
            }*/

            return movieArray;

        }

        @Override
        protected void onPostExecute(JSONArray movieArray) {
            super.onPostExecute(movieArray);

            // now we need to send the poster paths to the ImageAdapter
            // for it to load images on to the GridView

            for (int i = 0; i < movieArray.length(); i++) {
                try {
                    posterPaths.add(movieArray.getJSONObject(i).getString(TAG_POSTER_PATH));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // now we have all the poster paths ready
            // just send it to ImageAdapter as a parameter

            if (posterPaths != null) {
                gridView.setAdapter(new ImageAdapter(getActivity(), posterPaths));
            }
        }
    }
}
