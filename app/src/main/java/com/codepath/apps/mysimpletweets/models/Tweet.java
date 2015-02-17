package com.codepath.apps.mysimpletweets.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mvince on 2/8/15.
 */

// Parse the JSON + Store the data, encapsulate state logic or display logic
@Table(name = "tweets")
public class Tweet extends Model {
    // List out the attributes
    @Column(name = "body")
    private String body;
    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;
    @Column(name = "user", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    private User user;
    @Column(name = "created_at")
    private String createdAt;
    @Column(name = "retweet_count")
    private int retweetCount;
    @Column(name = "favorite_count")
    private int favoriteCount;
    @Column(name = "favorited")
    private boolean favorited;
    @Column(name = "retweeted")
    private boolean retweeted;
    @Column(name = "retweeted_status", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    private Tweet retweetedStatus;
    @Column(name = "current_user_retweet_id")
    private long retweetId;

    // This stores the tweet we are currently showing details for
    private static Tweet tweetShowingDetails;

    //"created_at":"Wed Aug 27 13:08:45 +0000 2008"
    private final String TWITTER_DATE_FORMAT = "EEE MMM d HH:mm:ss Z y";
    // 9:25 PM - 08 Feb 15
    private final String TWEET_DETAILS_DATE_FORMAT = "h:mm a - dd MMM yy";
    // "2/15/15"
    private final String TWEET_RELATIVE_LONG_FORMAT = "dd MMM yy";

    public Tweet() {
        super();
    }

    public Tweet(User user, String body) {
        super();
        this.user = user;
        this.body = body;
        SimpleDateFormat sdf = new SimpleDateFormat(TWITTER_DATE_FORMAT);
        this.createdAt = sdf.format(new Date());
    }

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public User getUser() {
        return user;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public Tweet getRetweetedStatus() {
        return retweetedStatus;
    }

    public void setRetweetId(long retweetId) {
        this.retweetId = retweetId;
    }

    public long getRetweetId() {
        return retweetId;
    }

    public static void setTweetShowingDetails(Tweet tweetShowingDetails) {
        Tweet.tweetShowingDetails = tweetShowingDetails;
    }

    public static void retweetTweetShowingDetails() {
        if (Tweet.tweetShowingDetails != null) {
            Tweet.tweetShowingDetails.retweet();
        }
    }

    public static void favoriteTweetShowingDetails() {
        Tweet tweetToFavorite;
        if (Tweet.tweetShowingDetails != null) {
            // Favorite the original tweet if it's a retweet
            if (Tweet.tweetShowingDetails.getRetweetedStatus() != null) {
                tweetToFavorite = Tweet.tweetShowingDetails.getRetweetedStatus();
            } else {
                tweetToFavorite = Tweet.tweetShowingDetails;
            }
            tweetToFavorite.favorite();
        }
    }

    // Deserialize the JSON and build Tweet objects
    // Tweet.fromJSON("{ ... }") => <Tweet>
    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        // Extract the values from the json, store them
        try {
            tweet.body = jsonObject.getString("text");
            tweet.uid = jsonObject.getLong("id");
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
            tweet.retweetCount = jsonObject.getInt("retweet_count");
            // favorite_count is nullable
            if (jsonObject.has("favorite_count")) {
                tweet.favoriteCount = jsonObject.getInt("favorite_count");
            } else {
                tweet.favoriteCount = 0;
            }
            tweet.favorited = jsonObject.getBoolean("favorited");
            tweet.retweeted = jsonObject.getBoolean("retweeted");
            if (jsonObject.has("retweeted_status")) {
                tweet.retweetedStatus = Tweet.fromJSON(jsonObject.getJSONObject("retweeted_status"));
            }
            if (jsonObject.has("current_user_retweet")) {
                tweet.retweetId = jsonObject.getJSONObject("current_user_retweet").getLong("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Save the tweet
        tweet.save();
        // Return the tweet object
        return tweet;
    }


    // Tweet.fromJSONArray([ { ... }, { ... } ] => List<Tweet>
    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        // Iterate the json array and create tweets
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tweetJson;
            try {
                tweetJson = jsonArray.getJSONObject(i);
                Tweet tweet = Tweet.fromJSON(tweetJson);
                if (tweet != null) {
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        // Return the finished list
        return tweets;
    }

    // Returns relative string from created_time
    public String getRelativeCreatedTime() {
        return getRelativeTime(this.createdAt);
    }

    // Returns relative time from time {
    public String getRelativeTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat(TWITTER_DATE_FORMAT);
        Long secondsSince;
        Date d;

        try {
            d = sdf.parse(time);
            secondsSince = (new Date().getTime() - d.getTime())/1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";  // returns empty string; but should never get here
        }

        if (secondsSince >= 86400) {
            // show month, day, and year
            return new SimpleDateFormat(TWEET_RELATIVE_LONG_FORMAT).format(d);
        } else if (secondsSince >= 3600) {
            // show hours
            return String.valueOf(secondsSince/3600) + "h";
        } else if (secondsSince >= 60){
            // show minutes
            return String.valueOf(secondsSince/60) + "m";
        } else {
            // show seconds
            return String.valueOf(secondsSince) + "s";
        }
    }

    // Updates tweet with the values from a json
    public void setWithJSON(JSONObject json) {
        try {
            this.body = json.getString("text");
            this.uid = json.getLong("id");
            this.createdAt = json.getString("created_at");
            this.user = User.fromJSON(json.getJSONObject("user"));
            this.retweetCount = json.getInt("retweet_count");
            // favorite_count is nullable
            if (json.has("favorite_count")) {
                this.favoriteCount = json.getInt("favorite_count");
            } else {
                this.favoriteCount = 0;
            }
            this.favorited = json.getBoolean("favorited");
            this.retweeted = json.getBoolean("retweeted");
            if (json.has("retweeted_status")) {
                this.retweetedStatus = Tweet.fromJSON(json.getJSONObject("retweeted_status"));
            }
            if (json.has("current_user_retweet")) {
                this.retweetId = json.getJSONObject("current_user_retweet").getLong("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<Tweet> getStoredTweets() {
        return new Select().from(Tweet.class).orderBy("uid DESC").execute();
    }

    // Returns time for detailed view
    public String getTimeForDetails() {
        try {
            Date date = new SimpleDateFormat(TWITTER_DATE_FORMAT).parse(this.createdAt);
            SimpleDateFormat toDate = new SimpleDateFormat(TWEET_DETAILS_DATE_FORMAT);
            return toDate.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean retweet() {
        if (retweeted) {
            retweetCount--;
            final Tweet thisTweet = this;

            TwitterApplication.getRestClient().postUnretweet(this, new JsonHttpResponseHandler() {
                // 3. On success, update the tweet object with the proper attributes
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("DEBUG", "Success unretweeting tweet (destroying) with id " + String.valueOf(thisTweet.getRetweetId()));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", "Failed unretweeting (destroying) tweet with id " + String.valueOf(thisTweet.getRetweetId()));
                }
            });
        } else {
            retweetCount++;
            final Tweet thisTweet = this;
            TwitterApplication.getRestClient().postRetweet(this, new JsonHttpResponseHandler() {
                // 3. On success, update the tweet object with the proper attributes
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("DEBUG", "Success retweeting tweet with id " + String.valueOf(thisTweet.getUid()));
                    // Store the retweetId so it can be unretweeted
                    try {
                        thisTweet.setRetweetId(response.getLong("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("DEBUG", "Did not get new id for retweet");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", "Failed retweeting tweet with id " + String.valueOf(thisTweet.getUid()));
                }
            });
        }
        retweeted = !retweeted;

        return retweeted;
    }

    public boolean favorite() {
        if (favorited) {
            favoriteCount--;
            final Tweet thisTweet = this;

            TwitterApplication.getRestClient().postUnfavorite(this, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("DEBUG", "Success unfavoriting tweet with id " + String.valueOf(thisTweet.getUid()));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", "Failed unfavoriting tweet with id " + String.valueOf(thisTweet.getUid()));
                }
            });
        } else {
            favoriteCount++;
            final Tweet thisTweet = this;
            TwitterApplication.getRestClient().postFavorite(this, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("DEBUG", "Success favoriting tweet with id " + String.valueOf(thisTweet.getUid()));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", "Failed favoriting tweet with id " + String.valueOf(thisTweet.getUid()));
                }
            });
        }
        favorited = !favorited;

        return favorited;
    }
}


