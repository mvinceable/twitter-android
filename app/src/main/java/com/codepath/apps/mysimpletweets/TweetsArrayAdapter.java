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
        User u = tweet.getUser();
        // 4. Populate data into the subviews
        tvName.setText(u.getName());
        tvUserName.setText("@" + u.getScreenName());
        tvTime.setText(tweet.getRelativeCreatedTime());
        tvBody.setText(tweet.getBody());
        ivProfileImage.setImageResource(android.R.color.transparent); // clear out the old image for a recycled view
        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).into(ivProfileImage);
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.showProfile(tweet.getUser());
                }
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.showDetails(tweet);
                }
            }
        });
        // 5. Return the view to be inserted into the list
        return convertView;
    }

    public void setCallback(TweetDetailsCallback callback){
        this.callback = callback;
    }

    public interface TweetDetailsCallback {
        public void showDetails(Tweet tweet);
        public void showProfile(User user);
    }
}
