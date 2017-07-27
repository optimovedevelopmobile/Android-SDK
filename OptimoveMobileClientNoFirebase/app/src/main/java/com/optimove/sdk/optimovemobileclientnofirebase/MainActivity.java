package com.optimove.sdk.optimovemobileclientnofirebase;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.entities.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optitrack.EventSentResult;
import com.optimove.sdk.optimove_sdk.optitrack.OptimoveEventSentListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OptimoveEventSentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendEvent(View view) {

        Optimove.getInstance().logEvent(new MyEvent(), this);
    }

    @Override
    public void onResponse(EventSentResult eventSentResult) {

        OptiLogger.d("EVENT_RESPONSE!!!", eventSentResult.name());
    }

    private class MyEvent implements OptimoveEvent {

        @Override
        public String getName() {
            return "Event1";
        }

        @Override
        public Map<String, Object> getParameters() {

            Map<String, Object> params = new HashMap<>(3);
            params.put("action_name", "Banana");
            params.put("action_value", 100);
            params.put("action_price", 200);
            return params;
        }
    }
}
