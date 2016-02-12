package com.example.android.popularmoviesapp_p1;

import org.json.JSONObject;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by DELL on 06-Feb-16.
 */
public interface ReviewsAPI {

    /*Retrofit get annotation with our URL
       And our method that will return us the list ob Book
    */
    @GET("/reviews")
    JSONObject getMovieReviews(
            @Query("api_key") String key
    );
}
