package com.codepath.apps.mysimpletweets.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.EndlessScrollListener;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;

public class TimelineActivity extends ActionBarActivity implements TweetsArrayAdapter.TweetDetailsCallback {

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    private ListView lvTweets;
    private SwipeRefreshLayout swipeContainer;

    public final static int COMPOSE_REQUEST_CODE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        // Find the listview
        lvTweets = (ListView) findViewById(R.id.lvTweets);
        // Create the arraylist (data source)
        tweets = new ArrayList<>();
        // Construct the adapter from data source
        aTweets = new TweetsArrayAdapter(this, tweets);
        // set callback
        aTweets.setCallback(this);
        // Connect adapter to listview
        lvTweets.setAdapter(aTweets);
        // Get the client
        client = TwitterApplication.getRestClient(); // singleton client

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
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

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

        // Prepopulate with stored data
        aTweets.addAll(Tweet.getStoredTweets());
        populateTimeline();
    }

    // Get more tweets older than the last item in the tweets array
    private void loadMoreTweets() {
        Tweet lastTweet = tweets.get(tweets.size() - 1);
        client.getHomeTimelineBeforeTweet(lastTweet, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                aTweets.addAll(Tweet.fromJSONArray(response));
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                swipeContainer.setRefreshing(false);
                Toast.makeText(TimelineActivity.this, "Failed loading more tweets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Send an API request to get the timeline json
    // Fill the listview by creating the tweet objects from the json
    private void populateTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                Log.d("DEBUG", json.toString());
                // DESERIALIZE JSON
                // CREATE MODELS AND ADD THEM TO THE ADAPTER
                // LOAD THE MODEL DATA INTO LISTVIEW
                aTweets.clear();
                aTweets.addAll(Tweet.fromJSONArray(json));
                swipeContainer.setRefreshing(false);
                Log.d("DEBUG", aTweets.toString());

            }

            // FAILURE
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                swipeContainer.setRefreshing(false);
                Toast.makeText(TimelineActivity.this, "Failed populating timeline", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_compose) {
            // Launch compose for current user
            User currentUser = User.getCurrentUser();
            if (currentUser == null) {
                client.getCurrentUser(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        User.setCurrentUser(User.fromJSON(response));
                        Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
                        startActivityForResult(i, COMPOSE_REQUEST_CODE);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(TimelineActivity.this, "Failed getting current user", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Intent i = new Intent(this, ComposeActivity.class);
                startActivityForResult(i, COMPOSE_REQUEST_CODE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COMPOSE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // 1. Optimistically create a tweet object and add it to the listview
                String body = data.getStringExtra("body");
                final Tweet tweet = new Tweet(User.getCurrentUser(), body);
                tweets.add(0, tweet);
                aTweets.notifyDataSetChanged();
                // 2. Post the tweet to twitter
                client.postStatus(body, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        tweet.setWithJSON(response);
                        // Save the tweet
                        tweet.save();
                        Toast.makeText(TimelineActivity.this, "success tweeting", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(TimelineActivity.this, "Failed posting tweet", Toast.LENGTH_SHORT).show();
                    }
                });
                // 3. On success, update the tweet object with the proper attributes


            }
        }
    }

    @Override
    public void showDetails(Tweet tweet) {
        Intent i = new Intent(this, TweetDetailsActivity.class);
        User u = tweet.getUser();
        i.putExtra("name", u.getName());
        i.putExtra("userName", u.getScreenName());
        i.putExtra("profileImageUrl", u.getProfileImageUrl());
        i.putExtra("body", tweet.getBody());
        i.putExtra("time", tweet.getTimeForDetails());
        i.putExtra("uid", tweet.getUid());
        startActivity(i);
    }
}
