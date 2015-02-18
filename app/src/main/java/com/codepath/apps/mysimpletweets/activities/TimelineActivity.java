package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TweetsArrayAdapter;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.fragments.HomeTimelineFragment;
import com.codepath.apps.mysimpletweets.fragments.MentionsTimelineFragment;
import com.codepath.apps.mysimpletweets.fragments.TweetsListFragment;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

public class TimelineActivity extends ActionBarActivity implements TweetsArrayAdapter.TweetDetailsCallback, SearchView.OnQueryTextListener {

    protected TwitterClient client;
    public final static int COMPOSE_REQUEST_CODE = 50;
    public final static int DETAILS_REQUEST_CODE = 60;
    public final static int REPLY_REQUEST_CODE = 70;
    public final static int SEARCH_RESULT_CODE = 80;
    ViewPager vpPager;
    TweetsPagerAdapter pagerAdapterTweets;
    HomeTimelineFragment fragmentHome;
    MentionsTimelineFragment fragmentMentions;
    protected SearchView mSearchView;

    // subclass sets the to true so that onCreate initialization isn't performed unnecesssarily
    protected boolean mSubClassOnCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the client
        client = TwitterApplication.getRestClient(); // singleton client

        // if a child class already initialized, then this class doesn't have to
        if (!mSubClassOnCreated) {
            setContentView(R.layout.activity_timeline);

            // Setup the custom action bar
            setupActionBar();

            // Get the viewpager
            vpPager = (ViewPager) findViewById(R.id.viewpager);
            // Set the viewpager adapter for the pager
            pagerAdapterTweets = new TweetsPagerAdapter(getSupportFragmentManager());
            vpPager.setAdapter(pagerAdapterTweets);
            // Find the sliding tabstrip
            PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            tabStrip.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL), Typeface.NORMAL);
            tabStrip.setTextColor(getResources().getColor(R.color.twitter_primary));
            int pxTabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics());
            tabStrip.setTextSize(pxTabTextSize);
            // Attach the tabsstrip to the viewpager
            tabStrip.setViewPager(vpPager);
        }
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

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        mSearchView.setQueryHint(getResources().getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(this);

        styleSearchView();

        return true;
    }

    // Customize the SearchView
    protected void styleSearchView() {
        View searchPlate = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        searchPlate.setBackgroundColor(getResources().getColor(R.color.twitter_primary));

        ImageView searchMagIcon = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchMagIcon.setImageResource(R.drawable.ic_action_search);
        // doesn't work
//        ImageView searchHintIcon = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
//        searchHintIcon.setImageResource(R.drawable.ic_action_search);
        ImageView searchCloseIcon = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchCloseIcon.setImageResource(R.drawable.ic_action_close);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COMPOSE_REQUEST_CODE || requestCode == REPLY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // 1. Optimistically create a tweet object and add it to the listview
                String body = data.getStringExtra("body");
                final Tweet tweet = new Tweet(User.getCurrentUser(), body);
                int fragmentPosition = vpPager.getCurrentItem();
                // Optimistic update only for home timeline
                if (fragmentPosition == 0) {
                    TweetsListFragment fragment = (TweetsListFragment) pagerAdapterTweets.getItem(fragmentPosition);
                    fragment.add(0, tweet);
                    fragment.notifyDataSetChanged();
                }
                if (requestCode == COMPOSE_REQUEST_CODE) {
                    // 2. Post the tweet to twitter
                    client.postStatus(body, new JsonHttpResponseHandler() {
                        // 3. On success, update the tweet object with the proper attributes
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
                } else if (requestCode == REPLY_REQUEST_CODE) {
                    // 2. Post the tweet to twitter
                    TwitterApplication.getRestClient().postReply(body, data.getLongExtra("replyToId", 0), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            tweet.setWithJSON(response);
                            // Save the tweet
                            tweet.save();
                            Toast.makeText(TimelineActivity.this, "success replying from timeline", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Toast.makeText(TimelineActivity.this, "Failed posting reply", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        } else if (requestCode == DETAILS_REQUEST_CODE) {
            if (vpPager != null) {
                // Update the adapter in case states changed
                int fragmentPosition = vpPager.getCurrentItem();
                TweetsListFragment fragment = (TweetsListFragment) pagerAdapterTweets.getItem(fragmentPosition);
                fragment.notifyDataSetChanged();
            }
        } else if (requestCode == SEARCH_RESULT_CODE) {
            // Refresh options menu so that search bar is collapsed
            invalidateOptionsMenu();
        }
    }

    @Override
    public void showDetails(Tweet tweet, Tweet tweetToDisplay) {
        Intent i = new Intent(this, TweetDetailsActivity.class);
        User u = tweetToDisplay.getUser();
        i.putExtra("name", u.getName());
        i.putExtra("userName", u.getScreenName());
        i.putExtra("profileImageUrl", u.getProfileImageUrl());
        i.putExtra("body", tweetToDisplay.getBody());
        i.putExtra("time", tweetToDisplay.getTimeForDetails());
        i.putExtra("uid", tweetToDisplay.getUid());
        i.putExtra("description", u.getDescription());
        i.putExtra("followersCount", u.getFollowersCount());
        i.putExtra("friendsCount", u.getFriendsCount());
        i.putExtra("profileBannerUrl", u.getProfileBannerUrl());
        i.putExtra("statusesCount", u.getStatusesCount());
        i.putExtra("retweetCount", tweet.getRetweetCount());
        i.putExtra("favoritesCount", tweetToDisplay.getFavoriteCount());
        i.putExtra("retweeted", tweet.isRetweeted());
        i.putExtra("favorited", tweetToDisplay.isFavorited());
        i.putExtra("mediaUrl", tweetToDisplay.getMediaUrl());
        Tweet.setTweetShowingDetails(tweet);
        startActivityForResult(i, DETAILS_REQUEST_CODE);
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

    @Override
    public void onReply(final Tweet tweet) {
        // Launch reply for current user
        User currentUser = User.getCurrentUser();
        if (currentUser == null) {
            client.getCurrentUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    User.setCurrentUser(User.fromJSON(response));
                    Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
                    i.putExtra("replyToId", tweet.getUid());
                    i.putExtra("replyToUserName", tweet.getUser().getScreenName());
                    startActivityForResult(i, REPLY_REQUEST_CODE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(TimelineActivity.this, "Failed getting current user", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
            i.putExtra("replyToId", tweet.getUid());
            i.putExtra("replyToUserName", tweet.getUser().getScreenName());
            startActivityForResult(i, REPLY_REQUEST_CODE);
        }
    }

    @Override
    public void onRetweet(Tweet tweet) {
        tweet.retweet();
    }

    @Override
    public void onFavorite(Tweet tweet) {
        tweet.favorite();
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
        User.setCurrentUser(null);
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Intent i = new Intent(this, SearchActivity.class);
        i.putExtra("query", s);
        startActivityForResult(i, SEARCH_RESULT_CODE);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
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
                if (fragmentHome == null) {
                    fragmentHome = new HomeTimelineFragment();
                }
                return fragmentHome;
            } else if (position == 1) {
                if (fragmentMentions == null) {
                    fragmentMentions = new MentionsTimelineFragment();
                }
                return fragmentMentions;
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
