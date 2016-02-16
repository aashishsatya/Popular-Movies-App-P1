package com.example.android.popularmoviesapp_p1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback{

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private static final String MOVIE_DETAILS_KEY = "movie_details_key";
    private boolean mTwoPane;
    private String sortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sortOrder = sharedPref.getString(getString(R.string.sort_order_key),
                getString(R.string.sort_order_default));
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;// show detail view in this activity by adding or replacing the detail fragment using a fragment transaction
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }
        else {
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // launch the Settings intent
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update movie selection in our second pane using the fragment manager
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String newSortOrder = sharedPref.getString(getString(R.string.sort_order_key),
                getString(R.string.sort_order_default));
        if (newSortOrder != null && !newSortOrder.equals(sortOrder)) {
            MainActivityFragment mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            if (mainActivityFragment != null) {
                mainActivityFragment.onPreferenceChanged();
            }
            MovieDetailFragment movieDetailFragment = (MovieDetailFragment) getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            /*if (movieDetailFragment != null) {
                movieDetailFragment.onPreferenceChanged();
            }*/
            sortOrder = newSortOrder;
        }
    }

    @Override
    public void onItemSelected(String movieDetails) {
        if (mTwoPane) {
            Toast.makeText(MainActivity.this, "In two pane UI", Toast.LENGTH_SHORT).show();
            // add or replace detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString(MOVIE_DETAILS_KEY, movieDetails);

            MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
            movieDetailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, movieDetailFragment, DETAIL_FRAGMENT_TAG).commit();
        }
        else {
            // launch activity as earlier
            Intent movieDetailsIntent = new Intent(this, MovieDetail.class).putExtra(MOVIE_DETAILS_KEY, movieDetails);
            startActivity(movieDetailsIntent);
        }
    }
}
