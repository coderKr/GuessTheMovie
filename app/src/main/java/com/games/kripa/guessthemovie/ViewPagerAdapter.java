package com.games.kripa.guessthemovie;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by Kripa on 10/9/2016.
 */

public class ViewPagerAdapter extends PagerAdapter {
    // Declare Variables
    Context context;
    int[] moviePosters;
    LayoutInflater inflater;

    @Override
    public int getCount() {
        return moviePosters.length;
    }

    public ViewPagerAdapter(Context context, int[] moviePosters) {
        this.context = context;
        this.moviePosters = moviePosters;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        // Declare Variables
        ImageView poster;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpager_item, container,
                false);

        // Locate the ImageView in viewpager_item.xml
        poster = (ImageView) itemView.findViewById(R.id.poster);
        // Capture position and set to the ImageView
        poster.setImageResource(moviePosters[position]);

        // Add viewpager_item.xml to ViewPager
        ((ViewPager) container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}

