package com.magic.chatai;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

public class x_UtilsSharedPref {
    private final String TAG = "genki";
    private final SharedPreferences sharedPref;
    private final SharedPreferences.Editor editor;
    private final String PREFS_NAME = "MyPrefs";

    private final String IS_CREATE_USER_DONE = "create_user";
    private final String PREFS_WRITING_STYLE = "writing_style";
    private final String IS_PREMIUM = "premium";
    private final String IS_FIRST_TIME_LAUNCH = "is_first_time_launch";

    x_UtilsSharedPref(Activity activity) {
        sharedPref = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public String getWritingStyle() {
        String writingStyle = sharedPref.getString(PREFS_WRITING_STYLE, "");
        return writingStyle;
    }
    public void putWritingStyle(String value) {
        editor.putString(PREFS_WRITING_STYLE, value);
        editor.apply();
    }

    public Boolean getBooleanCreateUser() {
        return sharedPref.getBoolean(IS_CREATE_USER_DONE, false);
    }

    public void putBooleanCreateUser(Boolean value) {
        editor.putBoolean(IS_CREATE_USER_DONE, value);
        editor.apply();
    }

    public Boolean getBooleanPremium() {
        return sharedPref.getBoolean(IS_PREMIUM, false);
    }

    public void putBooleanPremium(Boolean value) {
        editor.putBoolean(IS_PREMIUM, value);
        editor.apply();
    }

    public Boolean getBooleanFirstTimeLaunch() {
        return sharedPref.getBoolean(IS_FIRST_TIME_LAUNCH, false);
    }

    public void putBooleanFirstTimeLaunch(Boolean value) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, value);
        editor.apply();
    }

}
