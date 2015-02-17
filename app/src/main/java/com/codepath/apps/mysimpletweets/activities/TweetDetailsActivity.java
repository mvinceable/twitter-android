package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

public class TweetDetailsActivity extends ActionBarActivity {
    TextView tvName;
    TextView tvUserName;
    ImageView ivProfileImage;
    TextView tvBody;
    TextView tvTime;
    TextView tvRetweets;
    TextView tvFavorites;
    ImageButton ibRetweet;
    ImageButton ibFavorite;
    long uid;
    String userName;
    String description;
    int followersCount;
    int friendsCount;
    String profileImageUrl;
    String name;
    String profileBannerUrl;
    int statusesCount;
    int retweetCount;
    int favoritesCount;
    boolean retweeted;
    boolean favorited;

    public final static int COMPOSE_REQUEST_CODE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);

        // Store these as we don't want to get them from a "back" intent
        Intent i = getIntent();
        uid = i.getLongExtra("uid", 0);
        userName = i.getStringExtra("userName");
        description = i.getStringExtra("description");
        followersCount = i.getIntExtra("followersCount", 0);
        friendsCount = i.getIntExtra("friendsCount", 0);
        profileImageUrl = i.getStringExtra("profileImageUrl");
        name = i.getStringExtra("name");
        profileBannerUrl = i.getStringExtra("profileBannerUrl");
        statusesCount = i.getIntExtra("statusesCount", 0);
        retweetCount = i.getIntExtra("retweetCount", 0);
        favoritesCount = i.getIntExtra("favoritesCount", 0);
        retweeted = i.getBooleanExtra("retweeted", false);
        favorited = i.getBooleanExtra("favorited", false);

        setupViews();
    }

    private void setupViews() {
        tvName = (TextView) findViewById(R.id.tvName);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        tvBody = (TextView) findViewById(R.id.tvBody);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvRetweets = (TextView) findViewById(R.id.tvRetweets);
        tvFavorites = (TextView) findViewById(R.id.tvFavorites);
        ibRetweet = (ImageButton) findViewById(R.id.ibRetweet);
        ibFavorite = (ImageButton) findViewById(R.id.ibFavorite);

        Intent i = getIntent();
        tvName.setText(name);
        tvUserName.setText("@" + userName);
        ivProfileImage.setImageResource(android.R.color.transparent); // clear out the old image for a recycled view
        Picasso.with(this).load(profileImageUrl).into(ivProfileImage);
        tvBody.setText(i.getStringExtra("body"));
        tvTime.setText(i.getStringExtra("time"));

        updateButtonStates();

        User currentUser = User.getCurrentUser();
        // Disable retweet if necessary
        if (currentUser != null && currentUser.getScreenName().equals(userName)) {
            ibRetweet.setEnabled(false);
            ibRetweet.setAlpha((float) .5);
        } else {
            ibRetweet.setEnabled(true);
            ibRetweet.setAlpha((float)1);
        }

        // Setup ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.drawable.ic_logo_twitter);
        actionBar.setDisplayUseLogoEnabled(true);
    }

    // Updates button colors and counts
    private void updateButtonStates() {
        tvRetweets.setText(String.valueOf(retweetCount));
        tvFavorites.setText(String.valueOf(favoritesCount));
        if (retweeted) {
            ibRetweet.setBackground(getResources().getDrawable(R.drawable.retweet_on));
        } else {
            ibRetweet.setBackground(getResources().getDrawable(R.drawable.retweet));
        }

        if (favorited) {
            ibFavorite.setBackground(getResources().getDrawable(R.drawable.favorite_on));
        } else {
            ibFavorite.setBackground(getResources().getDrawable(R.drawable.favorite));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_reply) {
            // Launch reply for current user
            openReply();
        } else if (id == android.R.id.home) {
            setResult(RESULT_OK, new Intent());
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openReply() {
        User currentUser = User.getCurrentUser();
        TwitterClient client = TwitterApplication.getRestClient(); // singleton client
        if (currentUser == null) {
            client.getCurrentUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    User.setCurrentUser(User.fromJSON(response));
                    Intent i = new Intent(TweetDetailsActivity.this, ComposeActivity.class);
                    i.putExtra("replyToId", getIntent().getLongExtra("uid", 0));
                    i.putExtra("replyToUserName", getIntent().getStringExtra("userName"));
                    startActivityForResult(i, COMPOSE_REQUEST_CODE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(TweetDetailsActivity.this, "Failed getting current user", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Intent i = new Intent(TweetDetailsActivity.this, ComposeActivity.class);
            i.putExtra("replyToId", uid);
            i.putExtra("replyToUserName", userName);
            startActivityForResult(i, COMPOSE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COMPOSE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String body = data.getStringExtra("body");
                final Tweet tweet = new Tweet(User.getCurrentUser(), body);
                // 2. Post the tweet to twitter
                TwitterApplication.getRestClient().postReply(body, uid, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        tweet.setWithJSON(response);
                        // Save the tweet
                        tweet.save();
                        Toast.makeText(TweetDetailsActivity.this, "success tweeting", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(TweetDetailsActivity.this, "Failed posting reply", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public void showProfile(View view) {
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra("screen_name", userName);
        i.putExtra("description", description);
        i.putExtra("followers_count", followersCount);
        i.putExtra("friends_count", friendsCount);
        i.putExtra("profile_image_url", profileImageUrl);
        i.putExtra("name", name);
        i.putExtra("profile_banner_url", profileBannerUrl);
        i.putExtra("statuses_count", statusesCount);
        startActivity(i);
    }

    public void onReply(View view) {
        openReply();
    }

    public void onRetweet(View view) {
        if (retweeted) {
            retweetCount--;
        } else {
            retweetCount++;
        }
        retweeted = !retweeted;
        updateButtonStates();
        Tweet.retweetTweetShowingDetails();
    }

    public void onFavorite(View view) {
        if (favorited) {
            favoritesCount--;
        } else {
            favoritesCount++;
        }
        favorited = !favorited;
        updateButtonStates();
        Tweet.favoriteTweetShowingDetails();
    }
}
