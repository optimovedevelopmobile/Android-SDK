package com.optimove.sdk.optimovemobileclientfull;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        Optimove.getInstance().logEvent(new FirstEvent(), this);
    }

    public void failEvent(View view) {

        Optimove.getInstance().logEvent(new MyBadEvent(), this);
    }

    public void sendSecondEvent(View view) {
        Optimove.getInstance().logEvent(new SecondEvent(), this);
    }

    @Override
    public void onResponse(EventSentResult eventSentResult) {

        OptiLogger.d("EVENT_RESPONSE!!!", eventSentResult.name());
    }

    private class FirstEvent implements OptimoveEvent {

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

    private class SecondEvent implements OptimoveEvent {

        @Override
        public String getName() {
            return "Event2";
        }

        @Override
        public Map<String, Object> getParameters() {

            Map<String, Object> params = new HashMap<>(3);
            params.put("action_name", "TWO_Bananas");
            return params;
        }
    }

    private class MyBadEvent implements OptimoveEvent {

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
