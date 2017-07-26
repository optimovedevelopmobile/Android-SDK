package com.optimove.optimove_sdk.main.tools;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class BackendInteractor {

    private static final String BASE_URL = "https://optimovesdkmockendpoint.firebaseapp.com/";

    private Context context;

    public BackendInteractor(Context context) {

        this.context = context;
    }

    public RequestBuilder<JSONObject> getJson() {

        return new JsonRequestBuilder(null, Request.Method.GET);
    }

    public RequestBuilder<JSONObject> post(JSONObject data) {

        return new JsonRequestBuilder(data, Request.Method.POST);
    }

    public abstract class RequestBuilder<T> {

        protected T data;
        protected int method;
        protected String url;
        protected Response.Listener<T> successListener;
        protected Response.ErrorListener errorListener;

        protected RequestBuilder(T data, int method) {

            this.data = data;
            this.method = method;
            this.url = null;
            this.successListener = null;
            this.errorListener = null;
        }

        public RequestBuilder destination(String... pathComponents) {

            StringBuilder builder = new StringBuilder(BASE_URL);
            for (String comp : pathComponents)
                builder.append(comp).append("/");
            url = builder.deleteCharAt(builder.length() - 1).toString();
            return this;
        }

        public RequestBuilder successListener(Response.Listener<T> successListener) {

            this.successListener = successListener;
            return this;
        }

        public RequestBuilder errorListener(Response.ErrorListener errorListener) {

            this.errorListener = errorListener;
            return this;
        }

        protected boolean validateState() {
            return url != null && successListener != null;
        }

        public abstract void execute();
    }

    public class JsonRequestBuilder extends RequestBuilder<JSONObject> {

        protected JsonRequestBuilder(JSONObject data, int method) {
            super(data, method);
        }

        @Override
        public void execute() {

            if (!validateState())
                throw new IllegalStateException("Url and success listener must be set");
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JsonObjectRequest request = new JsonObjectRequest(method, url, data, successListener, errorListener);
            requestQueue.add(request);
        }
    }
}
