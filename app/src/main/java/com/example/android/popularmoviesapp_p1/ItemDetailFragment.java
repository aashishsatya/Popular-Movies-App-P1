package com.example.android.popularmoviesapp_p1;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.popularmoviesapp_p1.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    // strings for manipulating the JSON object received as extra

    final String TAG_ORIGINAL_TITLE = "original_title";
    final String TAG_RATINGS = "vote_average";
    final String TAG_RELEASE_DATE = "release_date";
    final String TAG_SYNOPSIS = "overview";
    final String TAG_POSTER_PATH = "poster_path";
    final String TAG_MOVIE_ID = "id";
    final String FAV_MOVIE_KEY = "favorite_movies";
    String LOG_TAG = ItemDetailFragment.class.getSimpleName();

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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.content);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.item_detail)).setText(mItem.details);
        }

        return rootView;
    }
}
