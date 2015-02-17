package com.codepath.apps.mysimpletweets;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.util.Log;

import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "BhATr89VMStKeBbfMKlgsr16c";       // Change this
	public static final String REST_CONSUMER_SECRET = "mA0Z1R4HN68nn3wn41WWjPpcYf39ZdLpTLtJHMXaMNSucXtsrk"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cpsimpletweets"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

    // METHOD === ENDPOINT

    // HomeTimeline - Gets us the home timeline
    // GET statuses/home_timeline.json
    //    count=25
    //    since_id=1
    public void getHomeTimeline(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("count", 25);
        // This parameter enables current_user_retweet to be returned for destroying retweets
        params.put("include_my_retweet", 1);
        // Execute the request
        getClient().get(apiUrl, params, handler);
    }

    // Get tweets older than max_id
    public void getHomeTimelineBeforeTweet(Tweet tweet, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("count", 25);
        // This parameter enables current_user_retweet to be returned for destroyoing retweets
        params.put("include_my_retweet", 1);
        // Subtract 1 from uid so it's not included in the response
        params.put("max_id", tweet.getUid() - 1);
        // Execute the request
        getClient().get(apiUrl, params, handler);
    }

    // Return current logged in user
    // https://api.twitter.com/1.1/account/verify_credentials.json
    public void getCurrentUser(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        getClient().get(apiUrl, null, handler);
    }

    // Post a status/tweet
    // https://api.twitter.com/1.1/statuses/update.json
    public void postStatus(String body, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", body);
        getClient().post(apiUrl, params, handler);
    }

    // Post a reply
    // https://api.twitter.com/1.1/statuses/update.json
    //   in_reply_to_status_id
    public void postReply(String body, long replyToId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", body);
        params.put("in_reply_to_status_id", String.valueOf(replyToId));
        getClient().post(apiUrl, params, handler);
    }

    // Get mentions
    public void getMentionsTimeline(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("count", 25);
        // Execute the request
        getClient().get(apiUrl, params, handler);
    }

    // get mentions older than max_id
    public void getMentionsTimelineBeforeTweet(Tweet tweet, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("count", 25);
        // Subtract 1 from uid so it's not included in the response
        params.put("max_id", tweet.getUid() - 1);
        // Execute the request
        getClient().get(apiUrl, params, handler);
    }

    // Get user timeline
    public void getUserTimeline(String screenName, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("screen_name", screenName);
        // This parameter enables current_user_retweet to be returned for destroying retweets
        params.put("include_my_retweet", 1);
        params.put("include_rts", 1);
        // Execute the request
        getClient().get(apiUrl, params, handler);
    }

    // Get tweets older than max_id
    public void getUserTimelineBeforeTweet(String screenName, Tweet tweet, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        // Specify the params
        RequestParams params = new RequestParams();
        params.put("count", 25);
        params.put("screen_name", screenName);
        // This parameter enables current_user_retweet to be returned for destroying retweets
        params.put("include_my_retweet", 1);
        params.put("include_rts", 1);
        // Subtract 1 from uid so it's not included in the response
        params.put("max_id", tweet.getUid() - 1);
        // Execute the request
        getClient().get(apiUrl, params, handler);
    }

    // Retweet a tweet
    // https://api.twitter.com/1.1/statuses/retweet/:id.json
    public void postRetweet(Tweet tweet, AsyncHttpResponseHandler handler) {
        Log.d("DEBUG", "Retweeting tweet with id " + String.valueOf(tweet.getUid()));
        String apiUrl = getApiUrl("statuses/retweet/" + String.valueOf(tweet.getUid()) + ".json");
        getClient().post(apiUrl, null, handler);
    }

    // Unretweet
    // https://api.twitter.com/1.1/statuses/destroy/:id
    public void postUnretweet(Tweet tweet, AsyncHttpResponseHandler handler) {
        // if current user is same as tweet's user, then the retweet id is the id itself
        User currentUser = User.getCurrentUser();
        long tweetIdToDestroy;
        if (currentUser != null && currentUser.getScreenName().equals(tweet.getUser().getScreenName())) {
            tweetIdToDestroy = tweet.getUid();
        } else {
            tweetIdToDestroy = tweet.getRetweetId();
        }
        Log.d("DEBUG", "Destroying tweet with id " + String.valueOf(tweetIdToDestroy));
        String apiUrl = getApiUrl("statuses/destroy/" + String.valueOf(tweetIdToDestroy) + ".json");
        getClient().post(apiUrl, null, handler);
    }

    // Favorite a tweet
    // https://api.twitter.com/1.1/favorites/create.json?id=<id>
    public void postFavorite(Tweet tweet, AsyncHttpResponseHandler handler) {
        Log.d("DEBUG", "Favoriting tweet with id " + String.valueOf(tweet.getUid()));
        String apiUrl = getApiUrl("favorites/create.json?id=" + String.valueOf(tweet.getUid()));
        getClient().post(apiUrl, null, handler);
    }

    // Unfavorite a tweet
    // https://api.twitter.com/1.1/favorites/destory.json?id=<id>
    public void postUnfavorite(Tweet tweet, AsyncHttpResponseHandler handler) {
        Log.d("DEBUG", "Unfavoriting tweet with id " + String.valueOf(tweet.getUid()));
        String apiUrl = getApiUrl("favorites/destroy.json?id=" + String.valueOf(tweet.getUid()));
        getClient().post(apiUrl, null, handler);
    }

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}