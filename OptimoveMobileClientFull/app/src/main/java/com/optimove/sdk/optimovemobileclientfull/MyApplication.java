package com.optimove.sdk.optimovemobileclientfull;

import android.app.Application;

import com.google.firebase.iid.FirebaseInstanceId;
import com.optimove.sdk.optimove_sdk.main.InitToken;
import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.OptimoveConfigurationListener;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;

import java.io.IOException;

public class MyApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        InitToken initToken = new InitToken("c10d36d2f267e295f7a43f631025a60b", "1.0.0");
        final long start = System.currentTimeMillis();
        Optimove.configure(this, initToken, new OptimoveConfigurationListener() {
            @Override
            public void onConfigurationFinished() {
                long end = System.currentTimeMillis();
                OptiLogger.d("DONE_CONFIGURE", "Loading took %d millis", (end - start));
                String id = FirebaseInstanceId.getInstance().getToken();
                if (id != null)
                    OptiLogger.d("ID_ID", id);
                String token = FirebaseInstanceId.getInstance(Optimove.getInstance().getSdkFa()).getToken();
                if (token != null)
                    OptiLogger.d("TOKEN_TOKEN", token);
                else
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                FirebaseInstanceId.getInstance().deleteInstanceId();
                            } catch (IOException e) {
                                OptiLogger.e(this, e);
                            }
                        }
                    });
            }
        });
    }
}
