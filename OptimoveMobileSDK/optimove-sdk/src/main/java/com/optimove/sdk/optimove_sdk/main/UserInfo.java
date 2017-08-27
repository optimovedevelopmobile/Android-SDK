package com.optimove.sdk.optimove_sdk.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import static com.optimove.sdk.optimove_sdk.main.UserInfo.UserInfoConstants.*;

public class UserInfo {

    @Nullable
    private String userId;
    private String visitorId;

    private SharedPreferences userIdsSp;

    private UserInfo() {

        this.userId = null;
        this.visitorId = null;
    }

    @Nullable
    public String getUserId() {
        return userId;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public void setUserId(@Nullable String userId) {

        this.userId = userId;
        userIdsSp.edit().putString(USER_ID_KEY, userId).apply();
    }

    public void setVisitorId(String visitorId) {

        this.visitorId = visitorId;
        userIdsSp.edit().putString(VISITOR_ID_KEY, visitorId).apply();
    }

    static UserInfo newInstance(Context context) {

        UserInfo userInfo = new UserInfo();
        userInfo.userIdsSp = context.getSharedPreferences(USER_IDS_SP, Context.MODE_PRIVATE);
        userInfo.userId = userInfo.userIdsSp.getString(USER_ID_KEY, null);
        userInfo.visitorId = userInfo.userIdsSp.getString(VISITOR_ID_KEY, null);
        return userInfo;
    }

    interface UserInfoConstants {

        String USER_IDS_SP = "user_ids";
        String USER_ID_KEY = "userId";
        String VISITOR_ID_KEY = "visitorId";
    }
}
