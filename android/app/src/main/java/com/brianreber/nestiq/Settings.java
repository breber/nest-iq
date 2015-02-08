package com.brianreber.nestiq;

import android.content.Context;
import android.content.SharedPreferences;
import com.nestapi.lib.API.AccessToken;

public class Settings {

    private static final String TOKEN_KEY = "token";
    private static final String EXPIRATION_KEY = "expiration";

    public static void saveAuthToken(Context context, AccessToken token) {
        getPrefs(context).edit()
                .putString(TOKEN_KEY, token.getToken())
                .putLong(EXPIRATION_KEY, token.getExpiresIn())
                .commit();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(AccessToken.class.getSimpleName(), 0);
    }

    public static AccessToken loadAuthToken(Context context) {
        final SharedPreferences prefs = getPrefs(context);
        final String token = prefs.getString(TOKEN_KEY, null);
        final long expirationDate = prefs.getLong(EXPIRATION_KEY, -1);

        if(token == null || expirationDate == -1) {
            return null;
        }

        return new AccessToken.Builder()
                .setToken(token)
                .setExpiresIn(expirationDate)
                .build();
    }
}
