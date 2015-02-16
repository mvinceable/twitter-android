package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.fragments.HomeTimelineFragment;
import com.codepath.apps.mysimpletweets.fragments.MentionsTimelineFragment;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

public class TimelineActivity extends ActionBarActivity implements TweetsArrayAdapter.TweetDetailsCallback {

    private TwitterClient client;
    public final static int COMPOSE_REQUEST_CODE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // Get the client
        client = TwitterApplication.getRestClient(); // singleton client

        // Setup the custom action bar
        setupActionBar();

        // Get the viewpager
        ViewPager vpPager = (ViewPager) findViewById(R.id.viewpager);
        // Set the viewpager adapter for the pager
        vpPager.setAdapter(new TweetsPagerAdapter(getSupportFragmentManager()));
        // Find the sliding tabstrip
        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL), Typeface.NORMAL);
        tabStrip.setTextColor(getResources().getColor(R.color.twitter_primary));
        tabStrip.setTextSize(24);
        // Attach the tabsstrip to the viewpager
        tabStrip.setViewPager(vpPager);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.drawable.ic_logo_twitter);
        actionBar.setDisplayUseLogoEnabled(true);
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
                Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
                startActivityForResult(i, COMPOSE_REQUEST_CODE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == COMPOSE_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                // 1. Optimistically create a tweet object and add it to the listview
//                String body = data.getStringExtra("body");
//                final Tweet tweet = new Tweet(User.getCurrentUser(), body);
//                fragmentTweetsList.add(0, tweet);
//                fragmentTweetsList.notifyDataSetChanged();
//                // 2. Post the tweet to twitter
//                client.postStatus(body, new JsonHttpResponseHandler() {
//                    // 3. On success, update the tweet object with the proper attributes
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                        tweet.setWithJSON(response);
//                        // Save the tweet
//                        tweet.save();
//                        Toast.makeText(TimelineActivity.this, "success tweeting", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                        Toast.makeText(TimelineActivity.this, "Failed posting tweet", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        }
//    }

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

    @Override
    public void showProfile(User user) {
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra("screen_name", user.getScreenName());
        i.putExtra("description", user.getDescription());
        i.putExtra("followers_count", user.getFollowersCount());
        i.putExtra("friends_count", user.getFriendsCount());
        i.putExtra("profile_image_url", user.getProfileImageUrl());
        i.putExtra("name", user.getName());
        i.putExtra("profile_banner_url", user.getProfileBannerUrl());
        i.putExtra("statuses_count", user.getStatusesCount());
        startActivity(i);
    }

    public void onProfileView(MenuItem item) {
        // Launch the profile view for current user
        User currentUser = User.getCurrentUser();
        if (currentUser == null) {
            client.getCurrentUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    User user = User.fromJSON(response);
                    User.setCurrentUser(user);
                    showProfile(user);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(TimelineActivity.this, "Failed getting current user", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            showProfile(currentUser);
        }
    }

    // Log the current user out
    public void onLogout(MenuItem item) {
        client.clearAccessToken();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    // Return the order of the fragments in the view pager
    public class TweetsPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = { "Home", "Mentions" };

        public TweetsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0 ) {
                return new HomeTimelineFragment();
            } else if (position == 1) {
                return new MentionsTimelineFragment();
            } else {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }
    }
}
