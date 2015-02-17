package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;

/**
 * Created by mvince on 2/16/15.
 */
public class ProfileDescriptionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_description, container, false);

        TextView tvDescription = (TextView) v.findViewById(R.id.tvDescription);
        tvDescription.setText(getArguments().getString("description"));

        return v;
    }

    public static ProfileDescriptionFragment newInstance(String description) {
        ProfileDescriptionFragment fragmentProfileDescription = new ProfileDescriptionFragment();
        Bundle args = new Bundle();
        args.putString("description", description);
        fragmentProfileDescription.setArguments(args);
        return fragmentProfileDescription;
    }
}
