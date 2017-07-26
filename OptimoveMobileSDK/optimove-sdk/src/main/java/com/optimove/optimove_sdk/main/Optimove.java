package com.optimove.optimove_sdk.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.optimove.optimove_sdk.main.entities.OptimoveEvent;
import com.optimove.optimove_sdk.main.tools.BackendInteractor;
import com.optimove.optimove_sdk.main.tools.OptiLogger;
import com.optimove.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor;
import com.optimove.optimove_sdk.optitrack.OptimoveEventSentListener;
import com.optimove.optimove_sdk.optitrack.OptimoveAnalyticsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.optimove.optimove_sdk.main.Optimove.InitDataJsonKeys.*;
import static com.optimove.optimove_sdk.optitrack.OptitrackConstants.InitDataKeys.EVENTS;
import static com.optimove.optimove_sdk.optitrack.OptitrackConstants.InitDataKeys.OPTITRACK_META_DATA;

final public class Optimove {

    private static Optimove shared;

    public static Optimove getInstance() {

        if (shared == null)
            throw new IllegalStateException("Optimove.configure() must be called");
        return shared;
    }

    public static void configure(Application application, InitToken initToken, @Nullable ConfigurationFinishedListener configsListener) {

        if (shared != null)
            return;
        synchronized (Optimove.class) {
            if (shared == null)
                shared = new Optimove(application);
            else
                return;
        }
        application.registerActivityLifecycleCallbacks(shared.new OptimoveActivityLifecycleObserver());
        shared.new Initializer(initToken, configsListener).initSdkComponents();
    }

    public static void startTestMode() {

        if (shared == null)
            throw new IllegalStateException("Optimove.configure() must be called first");
        String packageName = getInstance().context.getPackageName();
        FirebaseMessaging.getInstance().subscribeToTopic("test_" + packageName);
    }

    public static void stopTestMode() {

        if (shared == null)
            throw new IllegalStateException("Optimove.configure() must be called first");
        String packageName = getInstance().context.getPackageName();
        FirebaseMessaging.getInstance().unsubscribeFromTopic("test_" + packageName);
    }

    // TODO: 7/18/2017 Potential memory leak, think about creating a custom Application subclass or moving Context everywhere
    private Context context;
    private ComponentsMonitor componentsMonitor;
    private OptimoveFirebaseInteractor firebaseInteractor;

    private OptimoveAnalyticsManager analyticsManager;

    private Optimove(Context context) {

        this.context = context;
        this.componentsMonitor = new ComponentsMonitor();
        this.firebaseInteractor = new OptimoveFirebaseInteractor();
        this.analyticsManager = new OptimoveAnalyticsManager(context);
    }

    public FirebaseApp getSdkFa() {

        return firebaseInteractor.getSdkFa();
    }

    public void updateUserSignIn(String userId) {

        this.updateUserSignIn(userId, null);
    }

    public void updateUserSignIn(String userId, String email) {

        analyticsManager.updateUserInfo(context, userId, email);
    }

    public void updateUserSignedOut() {

        analyticsManager.updateUserInfo(context, null, null);
    }

    public void setupOptiTrackFromLocalStorage() {

        analyticsManager.setupFromLocalStorage();
    }

    public void logEvent(OptimoveEvent event, OptimoveEventSentListener listener) {

        analyticsManager.logEvent(event, listener);
    }

    public Context getContext() {
        return context;
    }

    private class OptimoveActivityLifecycleObserver implements Application.ActivityLifecycleCallbacks {


        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

            analyticsManager.dispatchAllEvents();
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    private class ComponentsMonitor {

        private Map<OptimoveComponentType, OptimoveComponentState> stateMap;

        private ComponentsMonitor() {

            initComponentsMap();
        }

        private void initComponentsMap() {

            stateMap = new HashMap<>(3);
            stateMap.put(OptimoveComponentType.OPTIPUSH, OptimoveComponentState.UNKNOWN);
            stateMap.put(OptimoveComponentType.OPTITRACK, OptimoveComponentState.UNKNOWN);
            stateMap.put(OptimoveComponentType.REALTIME, OptimoveComponentState.UNKNOWN);
        }

        OptimoveComponentState getComponentState(OptimoveComponentType type) {

            return stateMap.get(type);
        }
    }

    private class Initializer implements OptimoveComponentSetupListener, Response.Listener<JSONObject>, Response.ErrorListener {

        private InitToken initToken;
        private AtomicInteger componentsCounter;
        @Nullable
        private ConfigurationFinishedListener configsListener;

        Initializer(InitToken initToken, @Nullable ConfigurationFinishedListener configsListener) {

            this.initToken = initToken;
            this.componentsCounter = new AtomicInteger(0);
            this.configsListener = configsListener;
        }

        void initSdkComponents() {

            BackendInteractor backendInteractor = new BackendInteractor(context);
            backendInteractor.getJson().successListener(this).errorListener(this)
                    .destination(initToken.getTenantToken(), initToken.getConfigVersion() + ".json")
                    .execute();
        }

        @Override
        public void onResponse(JSONObject response) {

            extractComponentsState(response);
            initOptiTrack(extractOptiTrackData(response));
            initOptiPush(extractOptiPushData(response));
        }

        @Override
        public void onErrorResponse(VolleyError error) {

            OptiLogger.w(this, "Failed destination fetch initialization data %s", error.getMessage());
        }

        @Override
        public void onSetupFinished(OptimoveComponentType type, boolean success) {

            if (!success) {
                componentsMonitor.stateMap.put(type, OptimoveComponentState.BROKEN);
                OptiLogger.w(this, "Failed to initialize %s", type.name());
                return;
            }

            componentsMonitor.stateMap.put(type, OptimoveComponentState.ACTIVE);
            if (componentsCounter.decrementAndGet() == 0 && configsListener != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        configsListener.onConfigurationFinished();
                    }
                });
            }
        }

        private void extractComponentsState(JSONObject response) {

            OptimoveComponentState optitrackState = OptimoveComponentState.DENIED;
            OptimoveComponentState optipushState = OptimoveComponentState.DENIED;
            OptimoveComponentState realtimeState = OptimoveComponentState.DENIED;
            try {
                optitrackState = response.getBoolean(OPTITRACK_ENABLED) ? OptimoveComponentState.PERMITTED : OptimoveComponentState.DENIED;
                optipushState = response.getBoolean(OPTIPUSH_ENABLED) ? OptimoveComponentState.PERMITTED : OptimoveComponentState.DENIED;
                realtimeState = response.getBoolean(REALTIME_ENABLED) ? OptimoveComponentState.PERMITTED : OptimoveComponentState.DENIED;
            } catch (JSONException e) {
                OptiLogger.w(this, e);
            }
            componentsMonitor.stateMap.put(OptimoveComponentType.OPTITRACK, optitrackState);
            componentsMonitor.stateMap.put(OptimoveComponentType.OPTIPUSH, optipushState);
            componentsMonitor.stateMap.put(OptimoveComponentType.REALTIME, realtimeState);
        }

        private JSONObject extractOptiTrackData(JSONObject jsonObject) {

            JSONObject eventsJson = new JSONObject();
            try {
                eventsJson.put(OPTITRACK_META_DATA, jsonObject.getJSONObject(OPTITRACK_META_DATA));
                eventsJson.put(EVENTS, jsonObject.getJSONObject(EVENTS));
            } catch (JSONException e) {
                OptiLogger.e(this, e);
            }
            return eventsJson;
        }

        private JSONObject extractOptiPushData(JSONObject initData) {

            JSONObject firebaseConfigsJson = new JSONObject();
            try {
                firebaseConfigsJson = initData.getJSONObject(FIREBASE_PROJECT_KEYS);
            } catch (JSONException e) {
                OptiLogger.e(this, e);
            }
            return firebaseConfigsJson;
        }

        private void initOptiTrack(JSONObject optiTrackData) {

            if (optiTrackData != null) {
                componentsCounter.incrementAndGet();
                analyticsManager.setup(optiTrackData, this);
            } else {
                componentsMonitor.stateMap.put(OptimoveComponentType.OPTITRACK, OptimoveComponentState.BROKEN);
            }
        }

        private void initOptiPush(JSONObject jsonObject) {

            componentsCounter.incrementAndGet();
            firebaseInteractor.setup(jsonObject, this);
        }
    }

    interface InitDataJsonKeys {

        String OPTITRACK_ENABLED = "enableOptitrack";
        String OPTIPUSH_ENABLED = "enableOptipush";
        String REALTIME_ENABLED = "enableRealtime";
        String FIREBASE_PROJECT_KEYS = "firebaseProjectKeys";
    }
}
