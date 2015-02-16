package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.codepath.apps.mysimpletweets.EndlessScrollListener;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.models.Tweet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mvince on 2/15/15.
 */
public class TweetsListFragment extends Fragment {

    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    protected ListView lvTweets;
    private SwipeRefreshLayout swipeContainer;

    // Inflation logic
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweets_list, container, false);

        // Find the listview
        lvTweets = (ListView) v.findViewById(R.id.lvTweets);

        // Connect adapter to listview
        lvTweets.setAdapter(aTweets);

        // Setup infinte scroll
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
            // Triggered only when new data needs to be appended to the list
            // Add whatever code is needed to append new items to your AdapterView
            loadMoreTweets();
            }
        });

        // Setup refresh
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            populateTimeline();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        return v;
    }

    // Creation lifecycle event
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create the arraylist (data source)
        tweets = new ArrayList<>();
        // Construct the adapter from data source
        aTweets = new TweetsArrayAdapter(getActivity(), tweets);
        // set callback
        aTweets.setCallback((TweetsArrayAdapter.TweetDetailsCallback) getActivity());
        // Prepopulate with stored data
//        aTweets.addAll(Tweet.getStoredTweets());
    }

    protected void addAll(List<Tweet> tweets) {
        aTweets.addAll(tweets);
        Log.d("DEBUG", aTweets.toString());
    }

    protected void clear() {
        aTweets.clear();
    }

    protected void setRefreshing(boolean on) {
        swipeContainer.setRefreshing(on);
    }

    protected Tweet getLastTweet() {
        return tweets.get(tweets.size() - 1);
    }

    protected void add(int i, Tweet tweet) {
        tweets.add(i, tweet);
    }

    protected void notifyDataSetChanged() {
        aTweets.notifyDataSetChanged();
    }

    protected void populateTimeline() {

    }

    protected void loadMoreTweets() {

    }
}
