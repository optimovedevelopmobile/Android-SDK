package com.optimove.sdk.optimove_sdk.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.messaging.FirebaseMessaging;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.BackendInteractor;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.OptiPushManager;
import com.optimove.sdk.optimove_sdk.optitrack.OptiTrackManager;
import com.optimove.sdk.optimove_sdk.optitrack.OptimoveEventSentListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

        configure(application, tenantToken, configsListener, false);
    }

    public static void configure(Application application, TenantToken tenantToken, @Nullable OptimoveConfigurationListener configsListener, boolean hasDefaultFirebaseApp) {

        ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            if (configureSharedInstance(application)) return;
            application.registerActivityLifecycleCallbacks(shared.new OptimoveActivityLifecycleObserver());
            shared.tenantToken = tenantToken;
            shared.new Initializer(hasDefaultFirebaseApp, configsListener).initSdkComponents();
        } else {
            if (configsListener != null)
                configsListener.onConfigurationError(OptimoveConfigurationListener.Error.DISCONNECTED_FROM_NETWORK);
        }
    }

    private static boolean configureSharedInstance(Application application) {
        if (shared != null)
            return true;
        synchronized (Optimove.class) {
            if (shared == null)
                shared = new Optimove(application);
            else
                return true;
        }
        return false;
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
    private OptiPushManager optiPushManager;
    private OptiTrackManager optiTrackManager;

    private Optimove(Context context) {

        this.context = context;
        this.tenantToken = null;
        this.userInfo = UserInfo.newInstance(context);
        this.componentsMonitor = new ComponentsMonitor();
        this.optiPushManager = null;
        this.optiTrackManager = null;
    }

    public void setUserId(String userId) {

        if (userId == null)
            return;
        userInfo.setUserId(userId);
        optiTrackManager.userIdWasUpdated(userId);
        if (optiPushManager != null)
            optiPushManager.registerNewUserIfNeeded();
    }

    public void reportEvent(OptimoveEvent event, OptimoveEventSentListener listener) {

        if (componentsMonitor.getComponentState(OptimoveComponentType.OPTITRACK) == OptimoveComponentState.ACTIVE)
            optiTrackManager.reportEvent(event, listener);
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

    @Nullable
    public OptiPushManager getOptiPushManager() {
        if (optiPushManager == null)
            OptiLogger.w(this, "Attempt to access OptiPush when it's not properly initialized");
        return optiPushManager;
    }

    @Nullable
    public OptiTrackManager getOptiTrackManager() {
        if (optiTrackManager == null)
            OptiLogger.w(this, "Attempt to access OptiTrack when it's not properly initialized");
        return optiTrackManager;
    }

    public void setupOptiTrackFromLocalStorage() {

        if (optiTrackManager == null)
            optiTrackManager = OptiTrackManager.newInstanceFromLocalStorage(null);
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
                optiTrackManager.dispatchAllEvents();
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

            stateMap = new HashMap<>(OptimoveComponentType.COUNT);
            stateMap.put(OptimoveComponentType.OPTIPUSH, OptimoveComponentState.UNKNOWN);
            stateMap.put(OptimoveComponentType.OPTITRACK, OptimoveComponentState.UNKNOWN);
        }

        private void configureFromInitData(JSONObject initData) {

            OptimoveComponentState optitrackState = extractStateOfComponent(initData, OPTITRACK_ENABLED);
            OptimoveComponentState optipushState = extractStateOfComponent(initData, OPTIPUSH_ENABLED);
            componentsMonitor.stateMap.put(OptimoveComponentType.OPTITRACK, optitrackState);
            componentsMonitor.stateMap.put(OptimoveComponentType.OPTIPUSH, optipushState);
        }

        private OptimoveComponentState extractStateOfComponent(JSONObject jsonData, String component) {
            try {
                return jsonData.getBoolean(component) ? OptimoveComponentState.PERMITTED : OptimoveComponentState.DENIED;
            } catch (JSONException e) {
                OptiLogger.w(this, e);
                return OptimoveComponentState.DENIED;
            }
        }

        OptimoveComponentState getComponentState(OptimoveComponentType type) {

            return stateMap.get(type);
        }
    }

    private class Initializer implements OptimoveComponentSetupListener, Response.Listener<JSONObject>, Response.ErrorListener {

        private static final String INIT_END_POINT_URL = "https://mobile-sdk-mocked-mbaas.firebaseapp.com";

        private AtomicInteger componentsCounter;
        private Set<OptimoveConfigurationListener.Error> errors;
        private boolean hasDefaultFirebaseApp;
        @Nullable
        private OptimoveConfigurationListener configsListener;

        Initializer(boolean hasDefaultFirebaseApp, @Nullable OptimoveConfigurationListener configsListener) {

            this.hasDefaultFirebaseApp = hasDefaultFirebaseApp;
            this.componentsCounter = new AtomicInteger(OptimoveComponentType.COUNT);
            this.configsListener = configsListener;
            this.errors = new HashSet<>();
        }

        void initSdkComponents() {

            BackendInteractor backendInteractor = new BackendInteractor(context, INIT_END_POINT_URL);
            backendInteractor.getJson().successListener(this).errorListener(this)
                    .destination("%s/%s.json", tenantToken.getToken(), tenantToken.getConfigVersion())
                    .send();
        }

        @Override
        public void onResponse(JSONObject response) {

            componentsMonitor.configureFromInitData(response);
            if (optiTrackManager == null)
                optiTrackManager = OptiTrackManager.newInstance(extractOptiTrackData(response), this);
            if (optiPushManager == null)
                optiPushManager = OptiPushManager.newInstance(extractOptiPushData(response), hasDefaultFirebaseApp, this);
        }

        @Override
        public void onErrorResponse(VolleyError error) {

            OptiLogger.w(this, "Failed destination fetch initialization data %s", error.getMessage());
            if (configsListener != null) {
                configsListener.onConfigurationError(OptimoveConfigurationListener.Error.ERROR);
            }
        }

        @Override
        public void onSetupFinished(final OptimoveComponentType type, final boolean success, final OptimoveConfigurationListener.Error... errors) {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {

                    if (success) {
                        componentsMonitor.stateMap.put(type, OptimoveComponentState.ACTIVE);
                        OptiLogger.d(this, "Initialization finished for %s", type.name());
                    } else {
                        componentsMonitor.stateMap.put(type, OptimoveComponentState.BROKEN);
                        Collections.addAll(Initializer.this.errors, errors);
                        OptiLogger.w(this, "Failed to initialize %s", type.name());
                    }
                    if (componentsCounter.decrementAndGet() == 0)
                        performFinalConfigurationSteps();
                }
            });
        }

        private JSONObject extractOptiTrackData(JSONObject jsonObject) {

            JSONObject eventsJson = new JSONObject();
            try {
                eventsJson.put(InitDataJsonKeys.OPTITRACK_META_DATA, jsonObject.getJSONObject(InitDataJsonKeys.OPTITRACK_META_DATA));
                eventsJson.put(InitDataJsonKeys.EVENTS, jsonObject.getJSONObject(InitDataJsonKeys.EVENTS));
            } catch (JSONException e) {
                OptiLogger.e(this, e);
            }
            return eventsJson;
        }

        private JSONObject extractOptiPushData(JSONObject initData) {

            JSONObject firebaseConfigsJson = new JSONObject();
            try {
                firebaseConfigsJson = initData.getJSONObject(OPTIPUSH_META_DATA);
            } catch (JSONException e) {
                OptiLogger.e(this, e);
            }
            return firebaseConfigsJson;
        }

        private void performFinalConfigurationSteps() {

            boolean notificationsEnabled = optiPushManager.getClientRegistrar().checkAndActIfUserChangedNotificationPermission();
            if (!notificationsEnabled)
                errors.add(OptimoveConfigurationListener.Error.NOTIFICATIONS_NOT_GRANTED_BY_USER);
            if (configsListener != null)
                notifyConfigurationFinished();
        }

        @SuppressWarnings("ConstantConditions")
        private void notifyConfigurationFinished() {

            if (errors.isEmpty())
                configsListener.onConfigurationSuccess();
            else
                configsListener.onConfigurationError(errors.toArray(new OptimoveConfigurationListener.Error[errors.size()]));
        }
    }

    public interface InitDataJsonKeys {

        String OPTITRACK_ENABLED = "enableOptitrack";
        String OPTIPUSH_ENABLED = "enableOptipush";
        String OPTITRACK_META_DATA = "optitrackMetaData";
        String OPTIPUSH_META_DATA = "optipushMetaData";
        String EVENTS = "events";
    }
}
