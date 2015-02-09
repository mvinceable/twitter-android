package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
    long uid;
    String userName;

    public final static int COMPOSE_REQUEST_CODE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);
        setupViews();
        // Store these as we don't want to get them from a "back" intent
        uid = getIntent().getLongExtra("uid", 0);
        userName = getIntent().getStringExtra("userName");
    }

    private void setupViews() {
        tvName = (TextView) findViewById(R.id.tvName);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        tvBody = (TextView) findViewById(R.id.tvBody);
        tvTime = (TextView) findViewById(R.id.tvTime);

        Intent i = getIntent();
        tvName.setText(i.getStringExtra("name"));
        tvUserName.setText("@" + i.getStringExtra("userName"));
        ivProfileImage.setImageResource(android.R.color.transparent); // clear out the old image for a recycled view
        Picasso.with(this).load(i.getStringExtra("profileImageUrl")).into(ivProfileImage);
        tvBody.setText(i.getStringExtra("body"));
        tvTime.setText(i.getStringExtra("time"));

        // Add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        return super.onOptionsItemSelected(item);
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
}
