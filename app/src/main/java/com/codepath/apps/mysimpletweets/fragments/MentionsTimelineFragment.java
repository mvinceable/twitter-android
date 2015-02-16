package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by mvince on 2/15/15.
 */
public class MentionsTimelineFragment extends TweetsListFragment {
    private TwitterClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the client
        client = TwitterApplication.getRestClient(); // singleton client
        populateTimeline();
    }

    // Send an API request to get the timeline json
    // Fill the listview by creating the tweet objects from the json
    protected void populateTimeline() {
        client.getMentionsTimeline(new JsonHttpResponseHandler() {
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                Log.d("DEBUG", json.toString());
                // DESERIALIZE JSON
                // CREATE MODELS AND ADD THEM TO THE ADAPTER
                // LOAD THE MODEL DATA INTO LISTVIEW
                clear();
                addAll(Tweet.fromJSONArray(json));
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
        client.getHomeTimelineBeforeTweet(lastTweet, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                addAll(Tweet.fromJSONArray(response));
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
}
