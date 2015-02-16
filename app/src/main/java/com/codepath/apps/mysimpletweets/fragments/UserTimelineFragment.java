package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by mvince on 2/15/15.
 */
public class UserTimelineFragment extends TweetsListFragment {
    private TwitterClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View header = inflater.inflate(R.layout.header_profile, null);
        lvTweets.addHeaderView(header);

        Bundle b = getArguments();
        ImageView ivProfileImage = (ImageView) v.findViewById(R.id.ivProfileImage);
        TextView tvName = (TextView) v.findViewById(R.id.tvName);
        TextView tvUserName = (TextView) v.findViewById(R.id.tvUserName);
        ImageView ivProfileBanner = (ImageView) v.findViewById(R.id.ivProfileBanner);
        TextView tvTweets = (TextView) v.findViewById(R.id.tvTweets);
        TextView tvFollowers = (TextView) v.findViewById(R.id.tvFollowers);
        TextView tvFollowing = (TextView) v.findViewById(R.id.tvFollowing);

        Picasso.with(getActivity()).load(b.getString("profile_image_url")).into(ivProfileImage);
        tvName.setText(b.getString("name"));
        tvUserName.setText("@" + b.getString("screen_name"));

        String profileBannerUrl = b.getString("profile_banner_url");
        if (profileBannerUrl != null) {
            Picasso.with(getActivity()).load(profileBannerUrl + "/mobile_retina").into(ivProfileBanner);
        }
        tvTweets.setText(User.getFriendlyCount(b.getInt("statuses_count")));
        tvFollowers.setText(User.getFriendlyCount((b.getInt("followers_count"))));
        tvFollowing.setText(User.getFriendlyCount(b.getInt("friends_count")));

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the client
        client = TwitterApplication.getRestClient(); // singleton client
        populateTimeline();
    }

    // Creates a new fragment given a screenName
    // UserTimelineFragment.newInstance("username");
    public static UserTimelineFragment newInstance(String screenName, String name, int followersCount, int friendsCount, String profileImageUrl, String profileBannerUrl, int statusesCount) {
        UserTimelineFragment userFragment = new UserTimelineFragment();
        Bundle args = new Bundle();
        args.putString("screen_name", screenName);
        args.putString("name", name);
        args.putInt("followers_count", followersCount);
        args.putInt("friends_count", friendsCount);
        args.putString("profile_image_url", profileImageUrl);
        args.putString("profile_banner_url", profileBannerUrl);
        args.putInt("statuses_count", statusesCount);
        userFragment.setArguments(args);
        return userFragment;
    }

    // Send an API request to get the timeline json
    // Fill the listview by creating the tweet objects from the json
    protected void populateTimeline() {
        String screenName = getArguments().getString("screen_name");
        client.getUserTimeline(screenName, new JsonHttpResponseHandler() {
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                Log.d("DEBUG", json.toString());
                // DESERIALIZE JSON
                // CREATE MODELS AND ADD THEM TO THE ADAPTER
                // LOAD THE MODEL DATA INTO LISTVIEW
                clear();
                addAll(Tweet.fromJSONArray(json));
                setRefreshing(false);
            }

            // FAILURE
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                setRefreshing(false);
//                Toast.makeText(TimelineActivity.this, "Failed populating timeline", Toast.LENGTH_SHORT).show();
                if (errorResponse != null) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            }
        });
    }
}
