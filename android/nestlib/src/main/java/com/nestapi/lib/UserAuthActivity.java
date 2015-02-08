/**
 * Copyright 2014 Nest Labs Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software * distributed under
 * the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nestapi.lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.nestapi.lib.API.AccessToken;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.List;

public class UserAuthActivity extends Activity implements LoaderManager.LoaderCallbacks<AccessToken>{
    private final static String CLIENT_METADATA_KEY = "client_metadata_key";
    private final static String ACCESS_TOKEN_KEY = "access_token_key";

    public static final String TAG = "UserAuthFlow";

    private ProgressBar mProgressBar;
    private ClientMetadata mClientMetadata;
    private String mCode;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nest_auth_webview_layout);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.horizontalMargin = 50;
        params.verticalMargin = 50;
        getWindow().setAttributes(params);

        WebView clientWebView = (WebView)findViewById(R.id.auth_webview);
        mProgressBar = (ProgressBar)findViewById(R.id.webview_progress);
        mClientMetadata = getClientMetadata();

        if(!hasValidArgs(mClientMetadata.getRedirectURL(), mClientMetadata.getClientID(), mClientMetadata.getStateValue())) {
            log("invalid arguments...");
            setResult(RESULT_CANCELED);
            log("finishing");
            finish();
        }

        clientWebView.setWebChromeClient(new ProgressChromeClient());
        clientWebView.setWebViewClient(new RedirectClient());

        final String url = String.format(APIUrls.CLIENT_CODE_URL, mClientMetadata.getClientID(), mClientMetadata.getStateValue());
        log("setting url: " + url);
        WebSettings webSettings = clientWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        clientWebView.loadUrl(url);
    }

    private void finishWithResult(int result, AccessToken token) {
        final Intent intent = new Intent();
        intent.putExtra(ACCESS_TOKEN_KEY, token);
        setResult(result, intent);
        finish();
    }

    private String parseCodeQuery(String urlStr) {
        try{
            List<NameValuePair> params = URLEncodedUtils.parse(new URI(urlStr), "UTF-8");
            for(NameValuePair pair : params) {
                log("Parsed parameters: " + pair.getName() + " value: " + pair.getValue());
                if(pair.getName().equals("code")) {
                    return pair.getValue();
                }
            }
            return null;
        }
        catch (URISyntaxException excep) {
            return null;
        }
    }

    @Override
    public Loader<AccessToken> onCreateLoader(int id, Bundle args) {
        return new ObtainAccessTokenTask(this, mClientMetadata, mCode);
    }

    @Override
    public void onLoadFinished(Loader<AccessToken> loader, AccessToken data) {
        log("resolved access token is null: " + (data == null));
        if(data != null) {
            finishWithResult(RESULT_OK, data);
        } else {
            finishWithResult(RESULT_CANCELED, null);
        }
    }

    @Override
    public void onLoaderReset(Loader<AccessToken> loader) {
        //NO-OP
    }

    private ClientMetadata getClientMetadata() {
        final Intent intent = getIntent();
        return intent == null ? null : (ClientMetadata)intent.getParcelableExtra(CLIENT_METADATA_KEY);
    }

    private boolean hasValidArgs(String ... args) {
        if (args == null) {
            return true;
        }

        for(String s : args) {
            if(TextUtils.isEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    private static void log(String msg) {
        Log.v(TAG, msg);
    }

    private class ProgressChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            final int currentVisibility = mProgressBar.getVisibility();
            if(newProgress < 100 && currentVisibility != View.VISIBLE) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            else if(newProgress == 100 && currentVisibility != View.GONE) {
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    private class RedirectClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(!url.startsWith(mClientMetadata.getRedirectURL())) {
                mCode = null;
                return false;
            }
            mCode = parseCodeQuery(url);
            if(mCode == null) {
                finishWithResult(RESULT_CANCELED, null);
                return true;
            }
            getLoaderManager().restartLoader(0, null, UserAuthActivity.this);
            return true;
        }
    }

    static void launchAuthFlowForResult(Activity activity, int requestCode, ClientMetadata data) {
        final Intent authFlowIntent = new Intent(activity, UserAuthActivity.class);
        authFlowIntent.putExtra(CLIENT_METADATA_KEY, data);
        log("Launching auth flow Activity...");
        activity.startActivityForResult(authFlowIntent, requestCode);
    }

    static boolean hasAccessToken(Intent data) {
        return data != null && data.hasExtra(ACCESS_TOKEN_KEY);
    }

    static AccessToken getAccessToken(Intent data) {
        return data == null ? null : (AccessToken)data.getParcelableExtra(ACCESS_TOKEN_KEY);
    }

    private static class ObtainAccessTokenTask extends AsyncTaskLoader<AccessToken> {

        private static final int BUFFER_SIZE = 4096;

        private ClientMetadata mClientMetadata;
        private String mCode;

        public ObtainAccessTokenTask(Context context, ClientMetadata metadata, String code) {
            super(context);
            mClientMetadata = metadata;
            mCode = code;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public AccessToken loadInBackground() {
            try {
                String formattedUrl = String.format(APIUrls.ACCESS_URL, mCode, mClientMetadata.getClientID(), mClientMetadata.getClientSecret());
                log("Getting auth from: " + formattedUrl);
                URL url = new URL(formattedUrl);
                log("Created url...");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                log("Opened connection...");
                conn.setRequestMethod("POST");

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String result = readStream(in);
                JSONObject object = new JSONObject(result);

                return AccessToken.fromJSON(object);
            } catch (JSONException|IOException excep) {
                Log.e(TAG, "Unable to load access token.", excep);
                return null;
            }
        }

        private static String readStream(InputStream stream) throws IOException {
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            final byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = stream.read(buffer, 0, buffer.length)) != -1) {
                outStream.write(buffer, 0, read);
            }
            final byte[] data = outStream.toByteArray();
            return new String(data);
        }
    }
}
