package com.optimove.sdk.optimove_sdk.optipush.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.optimove.sdk.optimove_sdk.R;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.OptimoveComponentSetupListener;
import com.optimove.sdk.optimove_sdk.main.OptimoveComponentType;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.registration.OptiPushClientRegistrar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor.FirebaseInteractorConstants.*;

public class OptimoveFirebaseInteractor {

    private FirebaseApp masterFa;
    private FirebaseApp sdkFa;
    private boolean hasClientFa;

    public OptimoveFirebaseInteractor() {
    }

    public void setup(JSONObject initData, OptimoveComponentSetupListener setupListener) {

        Context context = Optimove.getInstance().getContext();
        FirebaseApp.initializeApp(context);
        hasClientFa = !FirebaseApp.getApps(context).isEmpty();
        new SdkFaInitCommand(context, setupListener).execute(initData);
    }

    private static DatabaseReference createRef(FirebaseApp app, String pathFormat, @NonNull Object... nodes) {

        String path = String.format(pathFormat, nodes);
        return FirebaseDatabase.getInstance(app).getReference().child(path);
    }

    public boolean doesClientHaveFirebase() {

        return hasClientFa;
    }

    public String getSdkFaSenderId() {

        return sdkFa.getOptions().getGcmSenderId();
    }

    private class SdkFaInitCommand {

        private Context context;
        private SharedPreferences sdkFaKeysSp;
        private OptimoveComponentSetupListener setupListener;

        SdkFaInitCommand(Context context, OptimoveComponentSetupListener setupListener) {

            this.context = context;
            this.sdkFaKeysSp = context.getSharedPreferences(SP_SDK_FA_KEYS_FILE, Context.MODE_PRIVATE);
            this.setupListener = setupListener;
        }

        void execute(JSONObject initData) {

            if (sdkFaKeysSp.contains(SDK_FA_APP_ID_KEY))
                initSdkFa(getSdkFaKeysFromLocalData());
            else
                initSdkFaFromInitData(initData);
        }

        private FirebaseKeys getSdkFaKeysFromLocalData() {

            return new FirebaseKeys.Builder()
                    .setApiKey(sdkFaKeysSp.getString(SDK_FA_API_KEY_KEY, null))
                    .setApplicationId(sdkFaKeysSp.getString(SDK_FA_APP_ID_KEY, null))
                    .setDatabaseUrl(sdkFaKeysSp.getString(SDK_FA_DB_URL_KEY, null))
                    .setGcmSenderId(sdkFaKeysSp.getString(SDK_FA_SENDER_ID_KEY, null))
                    .setStorageBucket(sdkFaKeysSp.getString(SDK_FA_STORAGE_BUCKET_KEY, null))
                    .setProjectId(sdkFaKeysSp.getString(SDK_FA_PROJ_ID_KEY, null))
                    .build();
        }

        private void initSdkFaFromInitData(JSONObject initData) {

            try {
                initSdkFa(FirebaseKeys.fromJson(initData));
            } catch (JSONException e) {
                OptiLogger.e(this, e);
                setupListener.onSetupFinished(OptimoveComponentType.OPTIPUSH, false);
            }
        }

        private void initSdkFa(FirebaseKeys firebaseKeys) {

            String sdkFaName = hasClientFa ? SDK_FA_NAME : FirebaseApp.DEFAULT_APP_NAME;
            sdkFa = FirebaseApp.initializeApp(context, firebaseKeys.toFirebaseOptions(), sdkFaName);
            initMasterFa(context);
            setupListener.onSetupFinished(OptimoveComponentType.OPTIPUSH, true);
            initFcmToken(firebaseKeys.getGcmSenderId());
            new OptiPushClientRegistrar(context).completeLastRegistrationIfFailed();
        }

        private void initMasterFa(Context context) {

            FirebaseOptions masterFirebaseOptions = getMasterFaKeys(context.getResources()).toFirebaseOptions();
            masterFa = FirebaseApp.initializeApp(context, masterFirebaseOptions, MASTER_FA_NAME);
        }

        private void initFcmToken(final String gcmSenderId) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String token = FirebaseInstanceId.getInstance().getToken(gcmSenderId, "FCM");
                        OptiLogger.d("COOL_TOKEN", token);
                    } catch (IOException e) {
                        OptiLogger.e(this, e);
                    }
                }
            }).start();
        }

        private FirebaseKeys getMasterFaKeys(Resources resources) {

            return new FirebaseKeys.Builder()
                    .setApiKey(resources.getString(R.string.optimove_sdk_master_fa_api_key))
                    .setApplicationId(resources.getString(R.string.optimove_sdk_master_fa_application_id))
                    .setDatabaseUrl(resources.getString(R.string.optimove_sdk_master_fa_database_url))
                    .setGcmSenderId(resources.getString(R.string.optimove_sdk_master_fa_sender_id))
                    .setStorageBucket(resources.getString(R.string.optimove_sdk_master_fa_storage_bucket))
                    .setProjectId(resources.getString(R.string.optimove_sdk_master_fa_project_id))
                    .build();
        }
    }

    interface FirebaseInteractorConstants {

        String MASTER_FA_NAME = "master_firebase_app";
        String SDK_FA_NAME = "sdk_firebase_app";

        String SP_SDK_FA_KEYS_FILE = "sdkFaKeysSp";

        String SDK_FA_API_KEY_KEY = "apiKey";
        String SDK_FA_APP_ID_KEY = "appId";
        String SDK_FA_DB_URL_KEY = "dbUrl";
        String SDK_FA_SENDER_ID_KEY = "senderId";
        String SDK_FA_STORAGE_BUCKET_KEY = "storageBucket";
        String SDK_FA_PROJ_ID_KEY = "projectId";
    }
}
