package com.example.android.popularmoviesapp_p1;

import org.json.JSONArray;

/**
 * Created by DELL on 06-Feb-16.
 */
public class Reviews {
    int id;
    int page;
    JSONArray results;
    int total_pages;
    int total_results;

    public JSONArray getResults() {
        return results;
    }

    public int getTotalPages() {
        return total_pages;
    }

    public int getTotalResults() {
        return total_results;
    }
}
