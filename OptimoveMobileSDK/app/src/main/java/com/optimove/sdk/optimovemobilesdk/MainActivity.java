package com.optimove.sdk.optimovemobilesdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optitrack.EventSentResult;
import com.optimove.sdk.optimove_sdk.optitrack.OptiTrackManager;
import com.optimove.sdk.optimove_sdk.optitrack.OptimoveEventSentListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void reportEvent(View view) {

        Optimove.getInstance().reportEvent(new MyEvent(), new OptimoveEventSentListener() {
            @Override
            public void onResponse(EventSentResult result) {
                OptiLogger.d(this, result.name());
            }
        });
        OptiTrackManager optiTrackManager = Optimove.getInstance().getOptiTrackManager();
        if (optiTrackManager != null) {
            optiTrackManager.dispatchAllEvents();
        }
    }

    public void updateUserId(View view) {

        String userId = Optimove.getInstance().getUserInfo().getUserId();
        if (userId == null)
            Optimove.getInstance().setUserId("TestUserID");
        else
            Optimove.getInstance().setUserId(null);
    }

    private class MyEvent implements OptimoveEvent {

        @Override
        public String getName() {
            return "Event1";
        }

        @Override
        public Map<String, Object> getParameters() {
            Map<String, Object> params = new HashMap<>();
            params.put("action_name", "Testing Module");
            params.put("action_value", 12);
            params.put("action_price", 24);
            return params;
        }
    }
}
