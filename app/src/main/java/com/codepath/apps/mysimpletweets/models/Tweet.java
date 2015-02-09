package com.codepath.apps.mysimpletweets.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

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

    //"created_at":"Wed Aug 27 13:08:45 +0000 2008"
    private final String TWITTER_DATE_FORMAT = "EEE MMM d HH:mm:ss Z y";
    // 9:25 PM - 08 Feb 15
    private final String TWEET_DETAILS_DATE_FORMAT = "h:mm a - dd MMM yy";

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
        return getRelativeTime(createdAt);
    }

    // Returns relative time from time {
    public String getRelativeTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat(TWITTER_DATE_FORMAT);
        Long secondsSince;
        try {
            Date d = sdf.parse(time);
            secondsSince = (new Date().getTime() - d.getTime())/1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";  // returns empty string; but should never get here
        }

        if (secondsSince >= 86400) {
            // show month, day, and year
            return new SimpleDateFormat("M/d/yy").format(time);
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
}


