package com.codepath.apps.mysimpletweets.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mvince on 2/8/15.
 */
@Table(name = "user")
public class User extends Model {
    // List attributes
    @Column(name = "name")
    private String name;
    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long uid;
    @Column(name = "screen_name")
    private String screenName;
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    @Column(name = "description")
    private String description;
    @Column(name = "followers_count")
    private int followersCount;
    @Column(name = "friends_count")
    private int friendsCount;
    @Column(name = "profile_banner_url")
    private String profileBannerUrl;
    @Column(name = "statuses_count")
    private int statusesCount;

    private static User currentUser;

    public User() {
        super();
    }

    public String getName() {
        return name;
    }

    public long getUid() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public int getStatusesCount() {
        return statusesCount;
    }

    public String getProfileBannerUrl() {
        return profileBannerUrl;
    }

    // Deserialize the user json => User
    public static User fromJSON(JSONObject json) {
        User u = new User();
        try {
            // Extract and fill the values
            // Get bigger profile imagess
            u.profileImageUrl = json.getString("profile_image_url");
            u.name = json.getString("name");
            u.uid = json.getLong("id");
            u.screenName = json.getString("screen_name");
            u.description = json.getString("description");
            u.followersCount = json.getInt("followers_count");
            u.friendsCount = json.getInt("friends_count");
            if (json.has("profile_banner_url")) {
                // User mobile retina version (640x320)
                // https://dev.twitter.com/overview/general/user-profile-images-and-banners
                u.profileBannerUrl = json.getString("profile_banner_url") + "/mobile_retina";
            } else if (json.has("profile_background_image_url")) {
                // User profile background image instead
                u.profileBannerUrl = json.getString("profile_background_image_url");
            }
            u.statusesCount = json.getInt("statuses_count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Save the user
        u.save();
        // Return a user
        return u;
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
    }

    // Return current logged in user
    public static User getCurrentUser() {
        return User.currentUser;
    }

    // Return friendly count string
    public static String getFriendlyCount(int count) {
        Log.d("DEBUG", "count is " + String.valueOf(count));
        if (count >= 1000000) {         // millions
            return String.format("%.1fM", (double)count / 1000000);
        } else if (count >= 10000) {    // 10 thousands
            return String.format("%.1fK", (double)count / 1000);
        } else if (count >= 1000) {     // thousands
            return String.format("%d,%d", count / 1000, count % 1000);
        } else {
            return String.format("%d", count);
        }
    }
}
