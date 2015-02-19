package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
    ProfileInfoFragment fragmentProfileInfo;
    ProfileDescriptionFragment fragmentProfileDescription;
    private String name;
    private String userName;
    private String profileImageUrl;
    private String description;
    private View vDimmer;
    // Paging indicators
    private ImageView ivPageInfo;
    private ImageView ivPageDescription;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    /**
     * Callback to show following and followers lists
     */
    private UserTimelineCallback callback;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View header = inflater.inflate(R.layout.header_profile, null);
        lvTweets.addHeaderView(header);

        Bundle b = getArguments();
        name = b.getString("name");
        userName = b.getString("screen_name");
        profileImageUrl = b.getString("profile_image_url");
        description = b.getString("description");
        ImageView ivProfileBanner = (ImageView) v.findViewById(R.id.ivProfileBanner);
        TextView tvTweets = (TextView) v.findViewById(R.id.tvTweets);
        TextView tvFollowers = (TextView) v.findViewById(R.id.tvFollowers);
        TextView tvFollowing = (TextView) v.findViewById(R.id.tvFollowing);
        vDimmer = v.findViewById(R.id.vDimmer);
        ivPageInfo = (ImageView) v.findViewById(R.id.ivPageInfo);
        ivPageDescription = (ImageView) v.findViewById(R.id.ivPageDescription);

        String profileBannerUrl = b.getString("profile_banner_url");
        if (profileBannerUrl != null) {
            Picasso.with(getActivity()).load(profileBannerUrl).into(ivProfileBanner);
        }
        tvTweets.setText(User.getFriendlyCount(b.getInt("statuses_count")));
        tvFollowers.setText(User.getFriendlyCount((b.getInt("followers_count"))));
        tvFollowing.setText(User.getFriendlyCount(b.getInt("friends_count")));

        tvFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.showFollowers();
                }
            }
        });

        tvFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.showFollowing();
                }
            }
        });

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) v.findViewById(R.id.viewpager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View view, float position) {
                // Dim for description
                if (view == fragmentProfileDescription.getView()) {
                    vDimmer.setAlpha(1 - position);
                }
            }
        });
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    // Info
                    ivPageInfo.setAlpha((float)1);
                    ivPageDescription.setAlpha((float).5);
                } else {
                    // Description
                    ivPageInfo.setAlpha((float).5);
                    ivPageDescription.setAlpha((float)1);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Clicking on indicators should toggle
        ivPageInfo.setOnClickListener(new ProfileInfoToggler());
        ivPageDescription.setOnClickListener(new ProfileInfoToggler());

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
    public static UserTimelineFragment newInstance(String screenName, String name, int followersCount, int friendsCount, String profileImageUrl, String profileBannerUrl, int statusesCount, String description) {
        UserTimelineFragment userFragment = new UserTimelineFragment();
        Bundle args = new Bundle();
        args.putString("screen_name", screenName);
        args.putString("name", name);
        args.putInt("followers_count", followersCount);
        args.putInt("friends_count", friendsCount);
        args.putString("profile_image_url", profileImageUrl);
        args.putString("profile_banner_url", profileBannerUrl);
        args.putInt("statuses_count", statusesCount);
        args.putString("description", description);
        userFragment.setArguments(args);
        return userFragment;
    }

    // Send an API request to get the timeline json
    // Fill the listview by creating the tweet objects from the json
    protected void populateTimeline() {
        // populateTimeline is called before local onCreateView gets called, that's why we're not using userName
        client.getUserTimeline(getArguments().getString("screen_name"), new JsonHttpResponseHandler() {
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

    // Get more tweets older than the last item in the tweets array
    protected void loadMoreTweets() {
        Tweet lastTweet = getLastTweet();
        // Null when we get here the very first time
        if (lastTweet == null) {
            return;
        }
        client.getUserTimelineBeforeTweet(userName, lastTweet, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                addAll(Tweet.fromJSONArray(response));
                setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                setRefreshing(false);
//                Toast.makeText(TimelineActivity.this, "Failed loading more tweets", Toast.LENGTH_SHORT).show();
                if (errorResponse != null) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            }
        });
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (fragmentProfileInfo == null) {
                    fragmentProfileInfo = ProfileInfoFragment.newInstance(
                        name,
                        userName,
                        profileImageUrl);
                }
                return fragmentProfileInfo;
            } else if (position == 1) {
                if (fragmentProfileDescription == null) {
                    fragmentProfileDescription = ProfileDescriptionFragment.newInstance(description);
                }
                return fragmentProfileDescription;
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private class ProfileInfoToggler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mPager.getCurrentItem() == 0) {
                mPager.setCurrentItem(1);
            } else {
                mPager.setCurrentItem(0);
            }
        }
    }

    public void setCallback(UserTimelineCallback callback){
        this.callback = callback;
    }

    public interface UserTimelineCallback {
        public void showFollowing();
        public void showFollowers();
    }
}
