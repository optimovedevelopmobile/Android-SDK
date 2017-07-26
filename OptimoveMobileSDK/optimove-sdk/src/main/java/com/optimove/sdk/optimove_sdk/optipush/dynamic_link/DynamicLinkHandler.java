package com.optimove.sdk.optimove_sdk.optipush.dynamic_link;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

import java.util.HashMap;
import java.util.Map;

public class DynamicLinkHandler implements OnSuccessListener<PendingDynamicLinkData>, OnFailureListener {

    private FragmentActivity activity;
    private LinkDataExtractedListener linkDataExtractedListener;

    public DynamicLinkHandler(FragmentActivity activity) {

        this.activity = activity;
    }

    public void extractLinkData(LinkDataExtractedListener linkDataExtractedListener) {

        this.linkDataExtractedListener = linkDataExtractedListener;
        FirebaseDynamicLinks.getInstance().getDynamicLink(activity.getIntent())
                .addOnSuccessListener(this).addOnFailureListener(this);
    }

    @Override
    public void onSuccess(PendingDynamicLinkData dynamicLink) {

        if (dynamicLink == null || dynamicLink.getLink() == null) {
            linkDataExtractedListener.onErrorOccurred(LinkDataError.NO_DATA_FOUND);
            return;
        }
        Intent launchIntent = dynamicLink.getUpdateAppIntent(activity);
        if (launchIntent != null) {
            activity.startActivity(launchIntent);
            OptiLogger.d(this, "Launching UPGRADE flow");
            return;
        }
        Uri deepLink = dynamicLink.getLink();
        buildCarryOverData(deepLink);
    }

    @Override
    public void onFailure(@NonNull Exception e) {

        OptiLogger.e(this, e);
        linkDataExtractedListener.onErrorOccurred(LinkDataError.NO_DYNAMIC_LINK);
    }

    private void buildCarryOverData(Uri deepLink) {

        Map<String, String> resultData = new HashMap<>();
        for (String name : deepLink.getQueryParameterNames())
            resultData.put(name, deepLink.getQueryParameter(name));
        linkDataExtractedListener.onDataExtracted(resultData);
    }
}
