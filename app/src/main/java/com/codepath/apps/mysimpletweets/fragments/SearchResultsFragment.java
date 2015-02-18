package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mvince on 2/17/15.
 */
public class SearchResultsFragment extends TweetsListFragment {
    private TwitterClient client;
    private String query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the client
        client = TwitterApplication.getRestClient(); // singleton client
        populateTimeline();
    }

    // Creates a new fragment given a query
    // SearchResultsFragment.newInstance("username");
    public static SearchResultsFragment newInstance(String query) {
        SearchResultsFragment fragmentSearchResults = new SearchResultsFragment();
        fragmentSearchResults.query = query;
        return fragmentSearchResults;
    }

    // Send an API request to get the timeline json
    // Fill the listview by creating the tweet objects from the json
    protected void populateTimeline() {
        client.getTweetsForQuery(query, new JsonHttpResponseHandler() {
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
                // DESERIALIZE JSON
                // CREATE MODELS AND ADD THEM TO THE ADAPTER
                // LOAD THE MODEL DATA INTO LISTVIEW
                clear();
                try {
                    addAll(Tweet.fromJSONArray(json.getJSONArray("statuses")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setRefreshing(false);
            }

            // FAILURE
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                setRefreshing(false);
//                Toast.makeText(TimelineActivity.this, "Failed populating timeline", Toast.LENGTH_SHORT).show();
                if (errorResponse != null) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            }
        });
    }

    // Get more tweets older than the last item in the tweets array
    protected void loadMoreTweets() {
        Tweet lastTweet = getLastTweet();
        // Null when we get here the very first time
        if (lastTweet == null) {
            return;
        }
        client.getTweetsForQueryBeforeTweet(query, lastTweet, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                try {
                    addAll(Tweet.fromJSONArray(json.getJSONArray("statuses")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                setRefreshing(false);
//                Toast.makeText(TimelineActivity.this, "Failed loading more tweets", Toast.LENGTH_SHORT).show();
                if (errorResponse != null) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            }
        });
    }

    // Updates the search results list with new query
    public void updateWithQuery(String query) {
        this.query = query;
        populateTimeline();
    }
}
