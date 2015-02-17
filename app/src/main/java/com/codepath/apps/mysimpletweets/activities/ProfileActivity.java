package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.fragments.UserTimelineFragment;
import com.codepath.apps.mysimpletweets.models.User;

public class ProfileActivity extends TimelineActivity {
    private String screenName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            UserTimelineFragment fragmentUserTimeline = UserTimelineFragment.newInstance(
                    this.screenName,
                    name,
                    followersCount,
                    friendsCount,
                    profileImageUrl,
                    profileBannerUrl,
                    statusesCount,
                    description);
            // Display user timeline fragment within this activity (dynamically)
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flContainer, fragmentUserTimeline);
            ft.commit(); // changes the fragments

            // Setup ActionBar
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.ic_logo_twitter);
            actionBar.setDisplayUseLogoEnabled(true);
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
}
