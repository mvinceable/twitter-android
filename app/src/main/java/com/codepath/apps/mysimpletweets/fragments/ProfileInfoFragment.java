package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;
import com.squareup.picasso.Picasso;

/**
 * Created by mvince on 2/16/15.
 */
public class ProfileInfoFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_info, container, false);

        ImageView ivProfileImage = (ImageView) v.findViewById(R.id.ivProfileImage);
        TextView tvName = (TextView) v.findViewById(R.id.tvName);
        TextView tvUserName = (TextView) v.findViewById(R.id.tvUserName);
        ImageView ivProfileBanner = (ImageView) v.findViewById(R.id.ivProfileBanner);

        Bundle b = getArguments();
        Picasso.with(getActivity()).load(b.getString("profile_image_url")).into(ivProfileImage);
        tvName.setText(b.getString("name"));
        tvUserName.setText("@" + b.getString("screen_name"));

        return v;
    }

    public static ProfileInfoFragment newInstance(String name, String screenName, String profileImageUrl) {
        ProfileInfoFragment fragmentProfileInfo = new ProfileInfoFragment();
        Bundle args = new Bundle();
        args.putString("screen_name", screenName);
        args.putString("name", name);
        args.putString("profile_image_url", profileImageUrl);
        fragmentProfileInfo.setArguments(args);
        return fragmentProfileInfo;
    }
}
