package com.example.android.popularmoviesapp_p1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MovieDetail extends AppCompatActivity {

    private static final String MOVIE_DETAILS_KEY = "movie_details_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

            // create the detail fragment and add it to the activity using a fragment transaction

            Bundle arguments = new Bundle();
            arguments.putString(MOVIE_DETAILS_KEY, getIntent().getStringExtra(MOVIE_DETAILS_KEY));

            MovieDetailFragment detailFragment = new MovieDetailFragment();
            detailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, detailFragment)
                    .commit();
        }
    }

}
