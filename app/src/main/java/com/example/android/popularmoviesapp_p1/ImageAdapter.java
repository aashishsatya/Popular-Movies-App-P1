package com.example.android.popularmoviesapp_p1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Aashish Satyajith on 15-12-2015.
 * Code referenced from http://developer.android.com/guide/topics/ui/layout/gridview.html
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> posterPaths;
    private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    public ImageAdapter(Context c, ArrayList<String> imagePaths) {
        mContext = c;
        posterPaths = imagePaths;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return posterPaths.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            // each "w185" image is 185 x 277
            // but this size is too small on a screen
            // so double it
            imageView.setLayoutParams(new GridView.LayoutParams(400, 600));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        }
        else {
            imageView = (ImageView) convertView;
        }
        // ImageView has been set up
        // Now use Picasso to load up the ImageView
        Picasso.with(mContext).load(IMAGE_BASE_URL + posterPaths.get(position)).into(imageView);
        return imageView;
    }
}
