package com.optimove.optimove_sdk.optipush.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.optimove.optimove_sdk.main.Optimove;

public class NotificationInteractionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String messageId = intent.getStringExtra(OptiPushConstants.MESSAGE_ID_KEY);
        boolean isDelete = intent.getBooleanExtra(OptiPushConstants.IS_DELETE_KEY, false);
        if (isDelete)
            notificationWasDeleted(messageId);
        else
            notificationWasOpened(messageId, context, intent);
    }

    private void notificationWasDeleted(String messageId) {

        Optimove.getInstance().setupOptiTrackFromLocalStorage();
        new NotificationLifecycleObserver(messageId).onDeleted();
    }

    private void notificationWasOpened(String messageId, Context context, Intent intent) {

        new NotificationLifecycleObserver(messageId).onOpened();
        if (intent.hasExtra(OptiPushConstants.DYNAMIC_LINK_KEY))
            openActivityWithDynamicLink(context, intent);
        else
            openMainActivity(context);
    }

    private void openActivityWithDynamicLink(Context context, Intent intent) {
        String dynamicLink = intent.getStringExtra(OptiPushConstants.DYNAMIC_LINK_KEY);
        Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dynamicLink));
        context.startActivity(linkIntent);
    }

    private void openMainActivity(Context context) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        context.startActivity(launchIntent);
    }
}
