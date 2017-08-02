package com.optimove.sdk.optimovemobileclientoptitrack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.optimove.sdk.optimove_sdk.main.tools.OptiLogger;
import com.optimove.sdk.optimove_sdk.optipush.dynamic_link.DynamicLinkHandler;
import com.optimove.sdk.optimove_sdk.optipush.dynamic_link.LinkDataError;
import com.optimove.sdk.optimove_sdk.optipush.dynamic_link.LinkDataExtractedListener;

import java.util.Map;

public class PromoActivity extends AppCompatActivity implements LinkDataExtractedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo);
        new DynamicLinkHandler(this).extractLinkData(this);
    }

    @Override
    public void onDataExtracted(Map<String, String> map) {

        TextView outputTv = (TextView) findViewById(R.id.outputTextView);
        StringBuilder builder = new StringBuilder(map.size());
        for (String key : map.keySet())
            builder.append(map.get(key)).append("\n");
        outputTv.setText(builder.toString());
    }

    @Override
    public void onErrorOccurred(LinkDataError linkDataError) {

        OptiLogger.d("NOOOOOOOOOOOOOO", linkDataError.name());
    }
}
