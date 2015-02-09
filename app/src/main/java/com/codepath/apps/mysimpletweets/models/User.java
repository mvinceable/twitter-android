package com.codepath.apps.mysimpletweets.models;

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

    // Deserialize the user json => User
    public static User fromJSON(JSONObject json) {
        User u = new User();
        try {
            // Extract and fill the values
            u.profileImageUrl = json.getString("profile_image_url");
            u.name = json.getString("name");
            u.uid = json.getLong("id");
            u.screenName = json.getString("screen_name");
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
}
