package com.optimove.sdk.optimove_sdk.optipush.firebase;

import android.content.Context;
import android.content.res.Resources;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.optimove.sdk.optimove_sdk.R;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.optimove.sdk.optimove_sdk.optipush.firebase.OptimoveFirebaseInteractor.FirebaseInteractorConstants.*;

public class OptimoveFirebaseInteractor {

    private FirebaseApp sdkController;
    private FirebaseApp appController;
    private boolean hasClientFa;

    public OptimoveFirebaseInteractor(boolean hasClientFa) {

        this.hasClientFa = hasClientFa;
    }

    public void setup(JSONObject initData) throws JSONException {

        new InitCommand().execute(initData);
    }

    public boolean doesClientHaveFirebase() {

        return hasClientFa;
    }

    public String getAppControllerSenderId() {

        return appController.getOptions().getGcmSenderId();
    }

    private class InitCommand {

        private Context context;

        InitCommand() {

            this.context = Optimove.getInstance().getContext();
        }

        private void execute(JSONObject initData) throws JSONException {

            initAppControllerProject(FirebaseKeys.fromJson(initData));
            initSdkControllerProject();
        }

        private void initAppControllerProject(FirebaseKeys firebaseKeys) {

            String sdkFaName = hasClientFa ? APP_CONTROLLER_PROJECT_NAME : FirebaseApp.DEFAULT_APP_NAME;
            appController = FirebaseApp.initializeApp(context, firebaseKeys.toFirebaseOptions(), sdkFaName);
        }

        private void initSdkControllerProject() {

            FirebaseOptions masterFirebaseOptions = getSdkControllerProjectKeys(context.getResources()).toFirebaseOptions();
            sdkController = FirebaseApp.initializeApp(context, masterFirebaseOptions, SDK_CONTROLLER_PROJECT_NAME);
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

        private FirebaseKeys getSdkControllerProjectKeys(Resources resources) {

            return new FirebaseKeys.Builder()
                    .setApiKey(resources.getString(R.string.sdk_controller_project_api_key))
                    .setApplicationId(resources.getString(R.string.sdk_controller_project_application_id))
                    .setDatabaseUrl(resources.getString(R.string.sdk_controller_project_database_url))
                    .setGcmSenderId(resources.getString(R.string.sdk_controller_project_sender_id))
                    .setStorageBucket(resources.getString(R.string.sdk_controller_project_storage_bucket))
                    .setProjectId(resources.getString(R.string.sdk_controller_project_project_id))
                    .build();
        }
    }

    interface FirebaseInteractorConstants {

        String SDK_CONTROLLER_PROJECT_NAME = "sdk_controller_project";
        String APP_CONTROLLER_PROJECT_NAME = "app_controller_project";

        String FIREBASE_API_KEY_KEY = "apiKey";
        String FIREBASE_APP_ID_KEY = "appId";
        String FIREBASE_DB_URL_KEY = "dbUrl";
        String FIREBASE_SENDER_ID_KEY = "senderId";
        String FIREBASE_STORAGE_BUCKET_KEY = "storageBucket";
        String FIREBASE_PROJ_ID_KEY = "projectId";
    }
}
