package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.User;
import com.squareup.picasso.Picasso;

public class ComposeActivity extends ActionBarActivity {
    TextView tvName;
    TextView tvUserName;
    ImageView ivProfileImage;
    EditText etBody;
    Button btnTweet;
    TextView tvCharsLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        etBody = (EditText) findViewById(R.id.etBody);
        // Remove the under bar from the EditText and make hint lighter
        etBody.setBackground(null);
        etBody.setHintTextColor(getResources().getColor(R.color.lighter_gray));

        // There are extras if this is a reply
        String replyToUserName = getIntent().getStringExtra("replyToUserName");
        if (replyToUserName != null) {
            etBody.setText("@" + replyToUserName + " ");
            // Place cursor at the end of text
            etBody.setSelection(etBody.getText().length());
        }

        // Setup the custom action bar
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.action_bar_compose);
        View customView = actionBar.getCustomView();
        tvName = (TextView) customView.findViewById(R.id.tvName);
        tvUserName = (TextView) customView.findViewById(R.id.tvUserName);
        ivProfileImage = (ImageView) customView.findViewById(R.id.ivProfileImage);
        tvCharsLeft = (TextView) customView.findViewById(R.id.tvCharsLeft);
        btnTweet = (Button) customView.findViewById(R.id.btnTweet);

        User currentUser = User.getCurrentUser();
        tvName.setText(currentUser.getName());
        tvUserName.setText("@" + currentUser.getScreenName());
        ivProfileImage.setImageResource(android.R.color.transparent); // clear out the old image for a recycled view
        Picasso.with(this).load(currentUser.getProfileImageUrl()).into(ivProfileImage);
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("body", etBody.getText().toString());
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);

        // Setup character counter listener
        etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateActionBarControls();
            }
        });

        updateActionBarControls();

        // Add back button
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    // Updates character count and sets state of tweet button
    private void updateActionBarControls() {
        int count = 140 - etBody.getText().length();
        tvCharsLeft.setText(String.valueOf(count));
        // Update button state depending on text
        if (count < 0 || count == 140) {
            btnTweet.setEnabled(false);
            btnTweet.setTextColor(getResources().getColor(R.color.tab_underline));
        } else {
            btnTweet.setEnabled(true);
            btnTweet.setTextColor(Color.parseColor("white"));
        }
        btnTweet.setBackgroundColor(getResources().getColor(R.color.twitter_primary));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
