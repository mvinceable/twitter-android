package com.codepath.apps.mysimpletweets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mvince on 2/8/15.
 */
// Taking the Tweet objects and turning them into Views displayed in the list
public class TweetsArrayAdapter extends ArrayAdapter<Tweet> {
    private TweetDetailsCallback callback;

    public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, android.R.layout.simple_list_item_1, tweets);
    }

    // Override and setup custom template
    // ViewHolder pattern

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Get the tweet
        final Tweet tweet = getItem(position);
        final Tweet tweetToDisplay;
        User currentUser = User.getCurrentUser();

        // If it's a retweeted tweet, display the original
        if (tweet.getRetweetedStatus() != null) {
            tweetToDisplay = tweet.getRetweetedStatus();
        } else {
            tweetToDisplay = tweet;
        }

        // 2. Find or inflate the template
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet, parent, false);
        }
        // 3. Find the subviews to fill with data in the template
        ImageView ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
        TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        TextView tvBody = (TextView) convertView.findViewById(R.id.tvBody);
        TextView tvReply = (TextView) convertView.findViewById(R.id.tvReply);
        final TextView tvRetweet = (TextView) convertView.findViewById(R.id.tvRetweet);
        final TextView tvFavorite = (TextView) convertView.findViewById(R.id.tvFavorite);
        TextView tvRetweeted = (TextView) convertView.findViewById(R.id.tvRetweeted);

        User u = tweetToDisplay.getUser();
        // 4. Populate data into the subviews
        tvName.setText(u.getName());
        tvUserName.setText("@" + u.getScreenName());
        tvTime.setText(tweetToDisplay.getRelativeCreatedTime());
        tvBody.setText(tweetToDisplay.getBody());
        ivProfileImage.setImageResource(android.R.color.transparent); // clear out the old image for a recycled view
        Picasso.with(getContext()).load(tweetToDisplay.getUser().getProfileImageUrl()).into(ivProfileImage);
        tvRetweet.setText(String.valueOf(tweet.getRetweetCount()));
        tvFavorite.setText(String.valueOf(tweetToDisplay.getFavoriteCount()));

        // Current user has retweeted
        setRetweetState(tweet.isRetweeted(), tvRetweet);

        // Someone has retweeted
        if (tweet.getRetweetedStatus() != null) {
            tvRetweeted.setText(tweet.getUser().getName() + " retweeted");
            tvRetweeted.setVisibility(View.VISIBLE);
        } else {
            tvRetweeted.setVisibility(View.GONE);
        }

        // Current user favorited
        if (tweetToDisplay.isFavorited()) {
            tvFavorite.setTextColor(getContext().getResources().getColor(R.color.favorited));
            tvFavorite.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.favorite_on, //left
                0, //top
                0, //right
                0//bottom
            );
        } else {
            tvFavorite.setTextColor(getContext().getResources().getColor(R.color.light_gray));
            tvFavorite.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.favorite, //left
                0, //top
                0, //right
                0//bottom
            );
        }

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.showProfile(tweetToDisplay.getUser());
                }
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.showDetails(tweet, tweetToDisplay);
                }
            }
        });
        tvReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onReply(tweetToDisplay);
                }
            }
        });

        // Tweet is current user's; this is flaky as currentUser may not be defined yet
        if (currentUser != null && currentUser.getScreenName().equals(tweetToDisplay.getUser().getScreenName())) {
            tvRetweet.setAlpha((float)0.25);
            tvRetweet.setText("");
        } else {
            tvRetweet.setAlpha((float)1);
            tvRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setRetweetState(!tweet.isRetweeted(), tvRetweet);
                    if (callback != null) {
                        // For destroying tweet, must supply original so retweetId is valid
                        callback.onRetweet(tweet);
                    }
                    updateRetweetCount(tvRetweet, tweet);
                }
            });
        }
        tvFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavoriteState(!tweetToDisplay.isFavorited(), tvFavorite);
                if (callback != null) {
                    callback.onFavorite(tweetToDisplay);
                }
                updateFavoriteCount(tvFavorite, tweetToDisplay);
            }
        });
        // 5. Return the view to be inserted into the list
        return convertView;
    }

    private void updateRetweetCount(TextView tvRetweet, Tweet tweet) {
        tvRetweet.setText(String.valueOf(tweet.getRetweetCount()));
    }

    private void updateFavoriteCount(TextView tvFavorite, Tweet tweet) {
        tvFavorite.setText(String.valueOf(tweet.getFavoriteCount()));
    }

    private void setRetweetState(boolean on, TextView tvRetweet) {
        if (on) {
            tvRetweet.setTextColor(getContext().getResources().getColor(R.color.retweeted));
            tvRetweet.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.retweet_on, //left
                    0, //top
                    0, //right
                    0//bottom
            );
        } else {
            tvRetweet.setTextColor(getContext().getResources().getColor(R.color.light_gray));
            tvRetweet.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.retweet, //left
                    0, //top
                    0, //right
                    0//bottom
            );
        }
    }

    private void setFavoriteState(boolean on, TextView tvFavorite) {
        if (on) {
            tvFavorite.setTextColor(getContext().getResources().getColor(R.color.favorited));
            tvFavorite.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.favorite_on, //left
                    0, //top
                    0, //right
                    0//bottom
            );
        } else {
            tvFavorite.setTextColor(getContext().getResources().getColor(R.color.light_gray));
            tvFavorite.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.favorite, //left
                    0, //top
                    0, //right
                    0//bottom
            );
        }
    }

    public void setCallback(TweetDetailsCallback callback){
        this.callback = callback;
    }

    public interface TweetDetailsCallback {
        public void showDetails(Tweet tweet, Tweet tweetToDisplay);
        public void showProfile(User user);
        public void onReply(Tweet tweetToDisplay);
        public void onRetweet(Tweet tweet);
        public void onFavorite(Tweet tweet);
    }
}
