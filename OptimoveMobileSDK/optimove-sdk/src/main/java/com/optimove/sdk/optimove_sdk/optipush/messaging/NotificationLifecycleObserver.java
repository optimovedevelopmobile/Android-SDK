package com.optimove.sdk.optimove_sdk.optipush.messaging;

import android.content.Context;
import android.content.SharedPreferences;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;

import java.util.HashMap;
import java.util.Map;

public class NotificationLifecycleObserver {

    private SharedPreferences notificationLifecycleSp;
    private String notificationId;

    public NotificationLifecycleObserver(String notificationId) {
        Context context = Optimove.getInstance().getContext();
        this.notificationId = notificationId;
    }

    public void onReceived() {
        Optimove.getInstance().reportEvent(new NotificationEvent("received"), null);
    }

    public void onDeleted() {
        Optimove.getInstance().reportEvent(new NotificationEvent("deleted"), null);
    }

    public void onOpened() {
        Optimove.getInstance().reportEvent(new NotificationEvent("opened"), null);
    }

    private class NotificationEvent implements OptimoveEvent {

        private String name;

        public NotificationEvent(String name) {
            this.name = String.format("notification_%s", name);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Map<String, Object> getParameters() {

            Map<String, Object> params = new HashMap<>(1);
            params.put("timestamp", System.currentTimeMillis());
            return params;
        }
    }
}
