package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.codepath.apps.mysimpletweets.EndlessScrollListener;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.adapters.UsersArrayAdapter;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UsersListActivity extends ActionBarActivity {
    private TwitterClient client;

    private ArrayList<User> users;
    private UsersArrayAdapter aUsers;
    protected ListView lvUsers;

    private String screenName;
    private String type;
    private String nextCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        // Setup ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        type = i.getStringExtra("type");
        screenName = i.getStringExtra("screenName");
        client = TwitterApplication.getRestClient();

        // Create the arraylist (data source)
        users = new ArrayList<>();
        // Construct the adapter from data source
        aUsers = new UsersArrayAdapter(this, users);

        // Find the listview
        lvUsers = (ListView) findViewById(R.id.lvUsers);

        // Connect adapter to listview
        lvUsers.setAdapter(aUsers);

        // Setup infinte scroll
        lvUsers.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadMoreUsers();
            }
        });

        if (type.equals("following")) {
            actionBar.setTitle(R.string.title_following);
            getFollowing();
        } else {
            actionBar.setTitle(R.string.title_followers);
            getFollowers();
        }
    }

    private void loadMoreUsers() {
        if (type.equals("followers")) {
            getFollowers();
        } else {
            getFollowing();
        }
    }

    private void getFollowing() {
        client.getFollowing(screenName, nextCursor, new JsonHttpResponseHandler() {
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
                // DESERIALIZE JSON
                // CREATE MODELS AND ADD THEM TO THE ADAPTER
                // LOAD THE MODEL DATA INTO LISTVIEW
                // nextCursor is null if we we're loading the first page
                if (nextCursor == null) {
                    aUsers.clear();
                }
                try {
                    nextCursor = json.getString("next_cursor_str");
                    aUsers.addAll(User.fromJSONArray(json.getJSONArray("users")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // FAILURE
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                Toast.makeText(TimelineActivity.this, "Failed populating timeline", Toast.LENGTH_SHORT).show();
                if (errorResponse != null) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            }
        });
    }

    private void getFollowers() {
        client.getFollowers(screenName, nextCursor, new JsonHttpResponseHandler() {
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d("DEBUG", json.toString());
                // DESERIALIZE JSON
                // CREATE MODELS AND ADD THEM TO THE ADAPTER
                // LOAD THE MODEL DATA INTO LISTVIEW
                // nextCursor is null if we we're loading the first page
                if (nextCursor == null) {
                    aUsers.clear();
                }
                try {
                    nextCursor = json.getString("next_cursor_str");
                    aUsers.addAll(User.fromJSONArray(json.getJSONArray("users")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // FAILURE
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                Toast.makeText(TimelineActivity.this, "Failed populating timeline", Toast.LENGTH_SHORT).show();
                if (errorResponse != null) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_users_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
