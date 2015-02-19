package com.codepath.apps.mysimpletweets.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mvince on 2/18/15.
 */
public class UsersArrayAdapter extends ArrayAdapter<User> {

    public UsersArrayAdapter(Context context, List<User> users) {
        super(context, android.R.layout.simple_list_item_1, users);
    }

    // Override and setup custom template
    // ViewHolder pattern

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Get the user
        final User u = getItem(position);

        // 2. Find or inflate the template
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }

        // 3. Find the subviews to fill with data in the template
        ImageView ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
        TextView tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);

        // 4. Populate data into the subviews
        tvName.setText(u.getName());
        tvUserName.setText("@" + u.getScreenName());
        ivProfileImage.setImageResource(android.R.color.transparent); // clear out the old image for a recycled view
        Picasso.with(getContext()).load(u.getProfileImageUrl()).into(ivProfileImage);
        tvDescription.setText(u.getDescription());

        // 5. Return the view to be inserted into the list
        return convertView;
    }
}
