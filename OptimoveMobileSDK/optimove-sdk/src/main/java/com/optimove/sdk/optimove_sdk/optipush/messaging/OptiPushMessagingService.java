package com.optimove.sdk.optimove_sdk.optipush.messaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optimove.sdk.optimove_sdk.main.Optimove;

import java.util.Map;

import static com.optimove.sdk.optimove_sdk.optipush.messaging.OptiPushConstants.*;

public class OptiPushMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);
        if (!remoteMessage.getData().containsKey(IS_OPTIPUSH_MESSAGE))
            return;
        if (Boolean.parseBoolean(remoteMessage.getData().get(IS_OPTIPUSH_MESSAGE)))
            new OptiPushMessageCommand(remoteMessage).execute();
        else
            new ServerMessageCommand(remoteMessage).execute();
    }

    private class OptiPushMessageCommand {

        private RemoteMessage remoteMessage;

        private OptiPushMessageCommand(RemoteMessage remoteMessage) {
            this.remoteMessage = remoteMessage;
        }

        private void execute() {

            Optimove.getInstance().setupOptiTrackFromLocalStorage();
            new NotificationLifecycleObserver(remoteMessage.getMessageId()).onReceived();
            Notification notification = generateNotificationFromMessage();
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, notification);
        }

        private Notification generateNotificationFromMessage() {

            NotificationData notificationData = new NotificationData(remoteMessage.getData());
            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder = new Notification.Builder(OptiPushMessagingService.this, getApplicationInfo().name);
            else
                builder = new Notification.Builder(OptiPushMessagingService.this);
            return builder
                    .setContentTitle(notificationData.title)
                    .setContentText(notificationData.body)
                    .setDeleteIntent(getPendingIntent(remoteMessage.getMessageId(), true, null))
                    .setContentIntent(getPendingIntent(remoteMessage.getMessageId(), false, notificationData.dynamicLink))
                    .setSmallIcon(getApplicationInfo().icon)
                    .setAutoCancel(true)
                    .build();
        }

        private PendingIntent getPendingIntent(String messageId, boolean isDelete, @Nullable String dynamicLink) {

            Intent intent = new Intent(OptiPushMessagingService.this, NotificationInteractionReceiver.class);
            intent.putExtra(MESSAGE_ID_KEY, messageId);
            intent.putExtra(IS_DELETE_KEY, isDelete);
            if (dynamicLink != null)
                intent.putExtra(DYNAMIC_LINK_KEY, dynamicLink);
            int rc = isDelete ? PENDING_INTENT_DELETE_RC : PENDING_INTENT_OPEN_RC;
            return PendingIntent.getBroadcast(OptiPushMessagingService.this, rc, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        private class NotificationData {

            String title;
            String body;
            String dynamicLink;

            NotificationData(Map<String, String> data) {
                title = data.get(TITLE_KEY);
                body = data.get(BODY_KEY);
                dynamicLink = data.get(DYNAMIC_LINK_KEY);
            }
        }
    }

    private class ServerMessageCommand {

        private RemoteMessage remoteMessage;

        public ServerMessageCommand(RemoteMessage remoteMessage) {
            this.remoteMessage = remoteMessage;
        }

        private void execute() {

        }
    }
}
