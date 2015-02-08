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

import android.app.Activity;
import android.content.Intent;
import com.nestapi.lib.API.AccessToken;

public final class AuthManager {
    private AuthManager() {
        throw new AssertionError("Utility class; do not instantiate.");
    }

    /**
     * Returns true if the Intent contains a valid AccessToken
     * @see #getAccessToken(android.content.Intent)
     * @param data the intent to parse the access token from
     */
    public static boolean hasAccessToken(Intent data) {
        return UserAuthActivity.hasAccessToken(data);
    }

    /**
     * Returns an AccessToken from the Intent
     * @param data the intent to parse the access token from
     * @return the AccessToken from the Intent; null if it could not be
     * retrieved or was invalid
     * @see #hasAccessToken(android.content.Intent)
     */
    public static AccessToken getAccessToken(Intent data) {
        return UserAuthActivity.getAccessToken(data);
    }

    /**
     * Start an activity which will guide a user through the authentication process.
     * @param activity the activity from which the authentication activity should be launched
     * @param requestCode the request code for which a result will be returned
     * @param clientMetadata the metadata about your client
     */
    public static void launchAuthFlow(Activity activity, int requestCode, ClientMetadata clientMetadata) {
        UserAuthActivity.launchAuthFlowForResult(activity, requestCode, clientMetadata);
    }
}
