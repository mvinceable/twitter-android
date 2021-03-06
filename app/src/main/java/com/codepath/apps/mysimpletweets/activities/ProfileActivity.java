package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.fragments.UserTimelineFragment;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

public class ProfileActivity extends TimelineActivity implements UserTimelineFragment.UserTimelineCallback {
    private String screenName;
    UserTimelineFragment fragmentUserTimeline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSubClassOnCreated = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Store screenName to prevent viewing of same profile repeatedly
        Intent i = getIntent();
        this.screenName = i.getStringExtra("screen_name");

        if (savedInstanceState == null) {
            // retrieve extras
            String name = i.getStringExtra("name");
            int followersCount = i.getIntExtra("followers_count", 0);
            int friendsCount = i.getIntExtra("friends_count", 0);
            String profileImageUrl = i.getStringExtra("profile_image_url");
            String profileBannerUrl = i.getStringExtra("profile_banner_url");
            int statusesCount = i.getIntExtra("statuses_count", 0);
            String description = i.getStringExtra("description");

            // Create the user timeline fragment
            fragmentUserTimeline = UserTimelineFragment.newInstance(
                    this.screenName,
                    name,
                    followersCount,
                    friendsCount,
                    profileImageUrl,
                    profileBannerUrl,
                    statusesCount,
                    description);

            // Set callback for this fragment
            fragmentUserTimeline.setCallback(this);

            // Display user timeline fragment within this activity (dynamically)
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flContainer, fragmentUserTimeline);
            ft.commit(); // changes the fragments

            // Setup ActionBar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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

    @Override
    public void showProfile(User user) {
        // Show if not already showing for this user
        if (user.getScreenName().equals(this.screenName)) {
            // Shake the screen (credit goes here: http://droid-blog.net/2012/05/15/two-simple-ways-to-make-your-users-aware-of-incorrect-input/)
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            getWindow().getDecorView().findViewById(android.R.id.content).startAnimation(shake);
        } else {
            super.showProfile(user);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REPLY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // 1. Optimistically create a tweet object and add it to the listview
                String body = data.getStringExtra("body");
                final Tweet tweet = new Tweet(User.getCurrentUser(), body);
                // Optimistic update only for current user's profile
                User currentUser = User.getCurrentUser();
                if (currentUser != null && screenName.equals(currentUser.getScreenName())) {
                    fragmentUserTimeline.add(0, tweet);
                    fragmentUserTimeline.notifyDataSetChanged();
                }
                // 2. Post the tweet to twitter
                TwitterApplication.getRestClient().postReply(body, data.getLongExtra("replyToId", 0), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        tweet.setWithJSON(response);
                        // Save the tweet
                        tweet.save();
//                        Toast.makeText(ProfileActivity.this, "success replying from profile", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(ProfileActivity.this, "Failed posting reply", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (requestCode == DETAILS_REQUEST_CODE) {
            // Update the adapter in case states changed
            fragmentUserTimeline.notifyDataSetChanged();
        }
    }

    @Override
    public void showFollowing() {
        Intent i = new Intent(this, UsersListActivity.class);
        i.putExtra("type", "following");
        i.putExtra("screenName", screenName);
        startActivity(i);
    }

    @Override
    public void showFollowers() {
        Intent i = new Intent(this, UsersListActivity.class);
        i.putExtra("type", "followers");
        i.putExtra("screenName", screenName);
        startActivity(i);
    }
}
