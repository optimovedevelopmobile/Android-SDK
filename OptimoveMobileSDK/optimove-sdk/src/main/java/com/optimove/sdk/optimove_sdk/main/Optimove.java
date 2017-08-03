package com.optimove.sdk.optimove_sdk.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.messaging.FirebaseMessaging;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.BackendInteractor;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptiPushClientRegistrar;
import com.optimove.sdk.optimove_sdk.optitrack.OptimoveAnalyticsManager;
import com.optimove.sdk.optimove_sdk.optitrack.OptimoveEventSentListener;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.optimove.sdk.optimove_sdk.main.Optimove.InitDataJsonKeys.*;

final public class Optimove {

    private static Optimove shared;

    public static Optimove getInstance() {

        if (shared == null)
            throw new IllegalStateException("Optimove.configure() must be called");
        return shared;
    }

    public static void configure(Application application, TenantToken tenantToken, @Nullable OptimoveConfigurationListener configsListener) {

        if (shared != null)
            return;
        synchronized (Optimove.class) {
            if (shared == null)
                shared = new Optimove(application);
            else
                return;
        }
        application.registerActivityLifecycleCallbacks(shared.new OptimoveActivityLifecycleObserver());
        shared.tenantToken = tenantToken;
        shared.new Initializer(configsListener).initSdkComponents();
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

    private Context context;
    private TenantToken tenantToken;
    private UserInfo userInfo;
    private ComponentsMonitor componentsMonitor;
    private OptimoveFirebaseInteractor firebaseInteractor;
    private OptimoveAnalyticsManager analyticsManager;

    private Optimove(Context context) {

        this.context = context;
        this.tenantToken = null;
        this.userInfo = UserInfo.newInstance(context);
        this.componentsMonitor = new ComponentsMonitor();
        this.firebaseInteractor = new OptimoveFirebaseInteractor();
        this.analyticsManager = new OptimoveAnalyticsManager();
    }

    public boolean clientHasFirebase() {

        return firebaseInteractor.doesClientHaveFirebase();
    }

    public String getSdkFaSenderId() {

        return firebaseInteractor.getSdkFaSenderId();
    }

    public void updateUserSignIn(String userId) {

        this.updateUserSignIn(userId, null);
    }

    public void updateUserSignIn(String userId, String email) {

        userInfo.setUserId(userId);
        userInfo.setEmail(email);
        analyticsManager.userIdWasUpdated(userId);
        OptiPushClientRegistrar registrar = new OptiPushClientRegistrar(context);
        if (registrar.isFirstConversion())
            registrar.registerNewUser();
    }

    public void updateUserSignedOut() {

        userInfo.setUserId(null);
        userInfo.setEmail(null);
        analyticsManager.userIdWasUpdated(null);
    }

    public void setupOptiTrackFromLocalStorage() {

        analyticsManager.setupFromLocalStorage();
    }

    public void logEvent(OptimoveEvent event, OptimoveEventSentListener listener) {

        if (componentsMonitor.getComponentState(OptimoveComponentType.OPTITRACK) == OptimoveComponentState.ACTIVE)
            analyticsManager.logEvent(event, listener);
    }

    public Context getContext() {
        return context;
    }

    public TenantToken getTenantToken() {
        return tenantToken;
    }

    public UserInfo getUserInfo() {
        return userInfo;
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

            if (componentsMonitor.getComponentState(OptimoveComponentType.OPTITRACK) == OptimoveComponentState.ACTIVE)
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

        private AtomicInteger componentsCounter;
        @Nullable
        private OptimoveConfigurationListener configsListener;

        Initializer(@Nullable OptimoveConfigurationListener configsListener) {

            this.componentsCounter = new AtomicInteger(0);
            this.configsListener = configsListener;
        }

        void initSdkComponents() {

            BackendInteractor backendInteractor = new BackendInteractor(context);
            backendInteractor.getJson().successListener(this).errorListener(this)
                    .destination(tenantToken.getToken(), tenantToken.getConfigVersion() + ".json")
                    .send();
        }

        @Override
        public void onResponse(JSONObject response) {

            int permittedComponents = extractComponentsState(response);
            componentsCounter = new AtomicInteger(permittedComponents);
            if (componentsMonitor.getComponentState(OptimoveComponentType.OPTITRACK) == OptimoveComponentState.PERMITTED)
                initOptiTrack(extractOptiTrackData(response));
            if (componentsMonitor.getComponentState(OptimoveComponentType.OPTIPUSH) == OptimoveComponentState.PERMITTED)
                initOptiPush(extractOptiPushData(response));
        }

        @Override
        public void onErrorResponse(VolleyError error) {

            OptiLogger.w(this, "Failed destination fetch initialization data %s", error.getMessage());
        }

        @Override
        public void onSetupFinished(OptimoveComponentType type, boolean success) {

            if (success) {
                componentsMonitor.stateMap.put(type, OptimoveComponentState.ACTIVE);
                OptiLogger.d(this, "Initialization finished for %s", type.name());
                if (componentsCounter.decrementAndGet() == 0 && configsListener != null)
                    notifyConfigurationFinished(configsListener);
            } else {
                componentsMonitor.stateMap.put(type, OptimoveComponentState.BROKEN);
                OptiLogger.w(this, "Failed to initialize %s", type.name());
            }
        }

        private int extractComponentsState(JSONObject response) {

            OptimoveComponentState optitrackState = extractStateOfComponent(response, OPTITRACK_ENABLED);
            OptimoveComponentState optipushState = extractStateOfComponent(response, OPTIPUSH_ENABLED);
            OptimoveComponentState realtimeState = extractStateOfComponent(response, REALTIME_ENABLED);
            componentsMonitor.stateMap.put(OptimoveComponentType.OPTITRACK, optitrackState);
            componentsMonitor.stateMap.put(OptimoveComponentType.OPTIPUSH, optipushState);
            componentsMonitor.stateMap.put(OptimoveComponentType.REALTIME, realtimeState);
            return countPermittedComponents(new OptimoveComponentState[]{optitrackState, optipushState, realtimeState});
        }

        private OptimoveComponentState extractStateOfComponent(JSONObject jsonData, String component) {
            try {
                return jsonData.getBoolean(component) ? OptimoveComponentState.PERMITTED : OptimoveComponentState.DENIED;
            } catch (JSONException e) {
                OptiLogger.w(this, e);
                return OptimoveComponentState.DENIED;
            }
        }

        private int countPermittedComponents(OptimoveComponentState[] states) {

            int permittedComps = 0;
            for (OptimoveComponentState state : states) {
                permittedComps += state == OptimoveComponentState.PERMITTED ? 1 : 0;
            }
            return permittedComps;
        }

        private JSONObject extractOptiTrackData(JSONObject jsonObject) {

            JSONObject eventsJson = new JSONObject();
            try {
                eventsJson.put(OptitrackConstants.InitDataKeys.OPTITRACK_META_DATA, jsonObject.getJSONObject(OptitrackConstants.InitDataKeys.OPTITRACK_META_DATA));
                eventsJson.put(OptitrackConstants.InitDataKeys.EVENTS, jsonObject.getJSONObject(OptitrackConstants.InitDataKeys.EVENTS));
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

            if (optiTrackData != null)
                analyticsManager.setup(optiTrackData, this);
            else
                componentsMonitor.stateMap.put(OptimoveComponentType.OPTITRACK, OptimoveComponentState.BROKEN);

        }

        private void initOptiPush(JSONObject jsonObject) {

            final int servicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
            if (servicesAvailable != ConnectionResult.SUCCESS) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
//                        if (configsListener != null)
//                            configsListener.googlePlayServicesFailed(servicesAvailable);
                    }
                });
                return;
            }
            firebaseInteractor.setup(jsonObject, this);
        }

        private void notifyConfigurationFinished(@NonNull final OptimoveConfigurationListener configsListener) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    configsListener.onConfigurationFinished();
                }
            });
        }
    }

    interface InitDataJsonKeys {

        String OPTITRACK_ENABLED = "enableOptitrack";
        String OPTIPUSH_ENABLED = "enableOptipush";
        String REALTIME_ENABLED = "enableRealtime";
        String FIREBASE_PROJECT_KEYS = "firebaseProjectKeys";
    }
}
