package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.fragments.SearchResultsFragment;

public class SearchActivity extends TimelineActivity implements MenuItemCompat.OnActionExpandListener {
    private SearchResultsFragment fragmentSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSubClassOnCreated = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState == null) {
            // retrieve extras
            String query = getIntent().getStringExtra("query");

            // Create the user timeline fragment
            fragmentSearchResults = SearchResultsFragment.newInstance(query);

            // Display user timeline fragment within this activity (dynamically)
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flContainer, fragmentSearchResults);
            ft.commit(); // changes the fragments

            // Setup ActionBar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getResources().getString(R.string.search_hint));

        styleSearchView();

        searchItem.expandActionView();

        // Override the searchItem collapse so it acts as the back button
        MenuItemCompat.setOnActionExpandListener(searchItem, this);

        // Set initial text
        setSearchText(getIntent().getStringExtra("query"));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Set text inside of the SearchView
    private void setSearchText(String s) {
        if (s != null) {
            mSearchView.setQuery(s, false);
        } else {
            mSearchView.setQuery("", false);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        setSearchText(s);
        // Update the search results
        fragmentSearchResults.updateWithQuery(s);
        return false;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true; // KEEP IT TO TRUE OR IT DOESN'T OPEN !!
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        // Close the Activity instead of collapsing
        setResult(RESULT_OK, new Intent());
        finish();
        return false;
    }
}