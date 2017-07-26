package com.optimove.sdk.optimove_sdk.optipush.messaging;

import android.content.Context;
import android.content.SharedPreferences;

import com.optimove.sdk.optimove_sdk.main.Optimove;

import static com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationLifecycleObserver.NotificationLifecycleConstants.RECEIVED_STAMP_KEY;
import static com.optimove.sdk.optimove_sdk.optipush.messaging.NotificationLifecycleObserver.NotificationLifecycleConstants.SP_NAME;

public class NotificationLifecycleObserver {

    private SharedPreferences notificationLifecycleSp;
    private String notificationId;

    public NotificationLifecycleObserver(String notificationId) {
        Context context = Optimove.getInstance().getContext();
        this.notificationLifecycleSp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        this.notificationId = notificationId;
    }

    public void onReceived() {
        long currentTime = System.currentTimeMillis();
        notificationLifecycleSp.edit().putLong(getKeyForProperty(RECEIVED_STAMP_KEY), currentTime).apply();
        // TODO: 7/16/2017 Send Event
//        Optimove.getInstance().logEvent();
    }

    public void onDeleted() {
        long responseTime = elapsedResponseTime();
        // TODO: 7/16/2017 Send Event
//        Optimove.getInstance().logEvent();
    }

    public void onOpened() {
        long responseTime = elapsedResponseTime();
        // TODO: 7/16/2017 Send Event
//        Optimove.getInstance().logEvent();
    }

    private long elapsedResponseTime() {

        long receivedTime = notificationLifecycleSp.getLong(getKeyForProperty(RECEIVED_STAMP_KEY), -1);
        long respondedTime = System.currentTimeMillis();
        if (receivedTime == -1 || respondedTime == -1)
            return -1;
        clear();
        return respondedTime - receivedTime;
    }

    private void clear() {
        notificationLifecycleSp.edit()
                .remove(getKeyForProperty(RECEIVED_STAMP_KEY))
                .apply();
    }

    private String getKeyForProperty(String property) {
        return String.format("%s_%s", notificationId, property);
    }

    interface NotificationLifecycleConstants {

        String SP_NAME = "notification_lifecycle";
        String RECEIVED_STAMP_KEY = "received_stamp";
    }
}
