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
package com.nestapi.lib.API;

import android.util.Log;
import com.firebase.client.*;
import com.nestapi.lib.APIUrls;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class NestAPI implements ValueEventListener {
    private static final String TAG = NestAPI.class.getSimpleName();
    private static final StringObjectMapIndicator MAP_INDICATOR = new StringObjectMapIndicator();

    public interface AuthenticationListener {
        void onAuthenticationSuccess();
        void onAuthenticationFailure(int errorCode);
    }

    public interface CompletionListener {
        void onComplete();
        void onError(int errorCode);
    }

    private static NestAPI sFirebaseManager = null;

    private final List<WeakReference<Listener>> mListeners;
    private Firebase mFirebaseRef;

    public static NestAPI getInstance() {
        if(sFirebaseManager == null) {
            sFirebaseManager = new NestAPI();
        }
        return sFirebaseManager;
    }

    private NestAPI() {
        Firebase.goOffline();
        Firebase.goOnline();
        Config defaultConfig = Firebase.getDefaultConfig();
        defaultConfig.setLogLevel(Logger.Level.DEBUG);
        mFirebaseRef = new Firebase(APIUrls.NEST_FIREBASE_URL);
        mListeners = new ArrayList<>();
    }

    /**
     * Request authentication with an AccessToken.
     * @param token the token to authenticate with
     * @param listener a listener to be notified when authentication succeeds or fails
     */
    public void authenticate(AccessToken token, AuthenticationListener listener) {
        Log.v(TAG, "authenticating with token: " + token.getToken());
        mFirebaseRef.auth(token.getToken(), new NestFirebaseAuthListener(listener));
    }

    /**
     * Send a request to update the target temperature of the thermostat in Fahrenheit. This value
     * is not relevant if the thermostat is in "Heat and Cool" mode. Instead, see
     * {@link #setTargetTemperatureHighF(String, Long, com.nestapi.lib.API.NestAPI.CompletionListener)}
     * and {@link #setTargetTemperatureLowF(String, Long, com.nestapi.lib.API.NestAPI.CompletionListener)}
     * @param thermostatID the identifier for the thermostat to adjust
     * @param tempF the new temperature, in fahrenheit (in the range of 50 to 90)
     * @param listener an optional listener for success or failure
     */
    public void setTargetTemperatureF(String thermostatID, Long tempF, CompletionListener listener) {
        final String path = buildThermostatFieldPath(thermostatID, Keys.THERMOSTAT.TARGET_TEMP_F);
        sendRequest(path, tempF, listener);
    }

    /**
     * Send a request to update the target temperature of the thermostat in Celsius. This value
     * is not relevant if the thermostat is in "Heat and Cool" mode. Instead, see
     * {@link #setTargetTemperatureHighC(String, Long, com.nestapi.lib.API.NestAPI.CompletionListener)}
     * and {@link #setTargetTemperatureLowC(String, Long, com.nestapi.lib.API.NestAPI.CompletionListener)}
     * @param thermostatID the identifier for the thermostat to adjust
     * @param tempC the new temperature, in celsius (in the range of 9 to 32)
     * @param listener an optional listener for success or failure
     */
    public void setTargetTemperatureC(String thermostatID, Long tempC, CompletionListener listener) {
        final String path = buildThermostatFieldPath(thermostatID, Keys.THERMOSTAT.TARGET_TEMP_C);
        sendRequest(path, tempC, listener);
    }

    /**
     * Send a request to update the target cooling temperature of the thermostat in Fahrenheit.
     * This value is only relevant when in "Heat and Cool" mode. Otherwise, see
     * {@link #setTargetTemperatureF(String, Long, com.nestapi.lib.API.NestAPI.CompletionListener)}
     * @param thermostatID the identifier for the thermostat to adjust
     * @param tempF the new temperature, in fahrenheit
     * @param listener an optional listener for success or failure
     */
    public void setTargetTemperatureHighF(String thermostatID, Long tempF, CompletionListener listener) {
        final String path = buildThermostatFieldPath(thermostatID, Keys.THERMOSTAT.TARGET_TEMP_HIGH_F);
        sendRequest(path, tempF, listener);
    }

    /**
     * Send a request to update the target cooling temperature of the thermostat in Celsius.
     * This value is only relevant when in "Heat and Cool" mode. Otherwise, see
     * {@link #setTargetTemperatureC(String, Long, com.nestapi.lib.API.NestAPI.CompletionListener)}
     * @param thermostatID the identifier for the thermostat to adjust
     * @param tempC the new temperature, in celsius
     * @param listener an optional listener for success or failure
     */
    public void setTargetTemperatureHighC(String thermostatID, Long tempC, CompletionListener listener) {
        final String path = buildThermostatFieldPath(thermostatID, Keys.THERMOSTAT.TARGET_TEMP_HIGH_C);
        sendRequest(path, tempC, listener);
    }

    /**
     * Send a request to update the target heating temperature of the thermostat in Fahrenheit.
     * This value is only relevant when in "Heat and Cool" mode. Otherwise, see
     * {@link #setTargetTemperatureF(String, Long, com.nestapi.lib.API.NestAPI.CompletionListener)}
     * @param thermostatID the identifier for the thermostat to adjust
     * @param tempF the new temperature, in fahrenheit
     * @param listener an optional listener for success or failure
     */
    public void setTargetTemperatureLowF(String thermostatID, Long tempF, CompletionListener listener) {
        final String path = buildThermostatFieldPath(thermostatID, Keys.THERMOSTAT.TARGET_TEMP_LOW_F);
        sendRequest(path, tempF, listener);
    }

    /**
     * Send a request to update the target heating temperature of the thermostat in Celsius.
     * This value is only relevant when in "Heat and Cool" mode. Otherwise, see
     * {@link #setTargetTemperatureC(String, Long, com.nestapi.lib.API.NestAPI.CompletionListener)}
     * @param thermostatID the identifier for the thermostat to adjust
     * @param tempC the new temperature, in celsius
     * @param listener an optional listener for success or failure
     */
    public void setTargetTemperatureLowC(String thermostatID, Long tempC, CompletionListener listener) {
        final String path = buildThermostatFieldPath(thermostatID, Keys.THERMOSTAT.TARGET_TEMP_LOW_C);
        sendRequest(path, tempC, listener);
    }

    /**
     * Send a request to update the operating mode of the thermostat.
     * @see com.nestapi.lib.API.Thermostat.HVACMode
     * @param thermostatID the identifier for the thermostat to adjust
     * @param hvacMode the new operating mode for the thermostat
     * @param listener an optional listener for success or failure
     */
    public void setHVACMode(String thermostatID, Thermostat.HVACMode hvacMode, CompletionListener listener) {
        final String path = buildThermostatFieldPath(thermostatID, Keys.THERMOSTAT.HVAC_MODE);
        sendRequest(path, hvacMode.getKey(), listener);
    }

    /**
     * Send a request to change the Away status of a structure.
     * @see com.nestapi.lib.API.Structure.AwayState
     * @param structureID the identifier of the structure
     * @param awayType the new away status for the structure
     * @param listener an optional listener for success or failure
     */
    public void setStructureAway(String structureID, Structure.AwayState awayType, CompletionListener listener) {
        final String path = buildStructureFieldPath(structureID, Keys.STRUCTURE.AWAY);
        sendRequest(path, awayType.getKey(), listener);
    }

    /**
     * Add a listener to receive updates when data changes
     * @param listener the listener to receive changes
     */
    public void addUpdateListener(Listener listener) {
        mFirebaseRef.addValueEventListener(this);
        mListeners.add(new WeakReference<>(listener));
    }

    /**
     * Remove a previously registered update listener
     * @param listener the listener to remove
     * @return true if the listener was removed
     */
    public boolean removeUpdateListener(Listener listener) {
        if (listener == null) {
            return false;
        }

        for (int i = 0, count = mListeners.size(); i < count; i++) {
            WeakReference<Listener> listenerRef = mListeners.get(i);
            Listener l = listenerRef.get();
            if (l == listener) {
                mListeners.remove(i);
                if (mListeners.isEmpty()) {
                    mFirebaseRef.removeEventListener(this);
                }
                return true;
            }
        }
        return false;
    }

    private void sendRequest(String path, Object value, CompletionListener listener) {
        mFirebaseRef.child(path).setValue(value, new NestCompletionListener(listener));
    }

    private String buildStructureFieldPath(String structureID, String fieldName) {
        return new PathBuilder()
                .append(Keys.STRUCTURES)
                .append(structureID)
                .append(fieldName).build();
    }

    private String buildThermostatFieldPath(String thermostatID, String fieldName) {
        return new PathBuilder()
                .append(Keys.DEVICES)
                .append(Keys.THERMOSTATS)
                .append(thermostatID)
                .append(fieldName).build();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        final Map<String, Object> values = dataSnapshot.getValue(MAP_INDICATOR);

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            notifyUpdateListeners(entry, mListeners);
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        // Do nothing
    }

    private static List<Listener> listenersFromReferences(List<WeakReference<Listener>> listenerRefs) {
        final List<Listener> listeners = new ArrayList<>();
        for (int i = listenerRefs.size() - 1; i >= 0; i--) {
            final WeakReference<Listener> listenerRef = listenerRefs.get(i);
            final Listener listener = listenerRef.get();

            if (listener != null) {
                // Add any remaining references
                listeners.add(listener);
            } else {
                // Clear out any obsolete references
                listenerRefs.remove(i);
            }
        }
        return listeners;
    }

    @SuppressWarnings("unchecked")
    private static void notifyUpdateListeners(Map.Entry<String, Object> entry, List<WeakReference<Listener>> listenerRefs) {
        final List<Listener> listeners = listenersFromReferences(listenerRefs);

        if (listeners.isEmpty()) {
            return;
        }

        final Map<String, Object> value = (Map<String, Object>) entry.getValue();
        switch (entry.getKey()) {
            case Keys.DEVICES:
                updateDevices(value, listeners);
                break;
            case Keys.STRUCTURES:
                updateStructures(value, listeners);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private static void updateDevices(Map<String, Object> devices, List<Listener> listeners) {
        for (Map.Entry<String, Object> entry : devices.entrySet()) {
            final Map<String, Object> value = (Map<String, Object>) entry.getValue();
            switch (entry.getKey()) {
                case Keys.THERMOSTATS:
                    updateThermostats(value, listeners);
                    break;
                case Keys.SMOKE_CO_ALARMS:
                    updateSmokeCOAlarms(value, listeners);
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void updateThermostats(Map<String, Object> thermostatsMap, List<Listener> listeners) {
        for (Map.Entry<String, Object> entry : thermostatsMap.entrySet()) {
            final Map<String, Object> value = (Map<String, Object>) entry.getValue();
            final Thermostat thermostat = new Thermostat.Builder().fromMap(value);

            if (thermostat != null) {
                for (Listener listener : listeners) {
                    if (listener.getThermostatListener() != null) {
                        listener.getThermostatListener().onThermostatUpdated(thermostat);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void updateSmokeCOAlarms(Map<String, Object> smokeCOAlarms, List<Listener> listeners) {
        for (Map.Entry<String, Object> entry : smokeCOAlarms.entrySet()) {
            final Map<String, Object> value = (Map<String, Object>) entry.getValue();
            final SmokeCOAlarm alarm = new SmokeCOAlarm.Builder().fromMap(value);

            if (alarm != null) {
                for (Listener listener : listeners) {
                    if (listener.getSmokeCOAlarmListener() != null) {
                        listener.getSmokeCOAlarmListener().onSmokeCOAlarmUpdated(alarm);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void updateStructures(Map<String, Object> structures, List<Listener> listeners) {
        for (Map.Entry<String, Object> entry : structures.entrySet()) {
            final Map<String, Object> value = (Map<String, Object>) entry.getValue();
            final Structure structure = new Structure.Builder().fromMap(value);

            if (structure != null) {
                for (Listener listener : listeners) {
                    if (listener.getStructureListener() != null) {
                        listener.getStructureListener().onStructureUpdated(structure);
                    }
                }
            }
        }
    }

    private class NestCompletionListener implements Firebase.CompletionListener {
        private CompletionListener mCompletionListener;

        public NestCompletionListener(CompletionListener listener) {
            mCompletionListener = listener;
        }

        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
            if (mCompletionListener == null) {
                return;
            }

            if (firebaseError == null) {
                Log.v(TAG, "Request successful");
                mCompletionListener.onComplete();
            } else {
                Log.w(TAG, "Error: " + firebaseError.getCode() + " " + firebaseError.getMessage());
                mCompletionListener.onError(firebaseError.getCode());
            }
        }
    }

    private class NestFirebaseAuthListener implements Firebase.AuthListener {
        private AuthenticationListener mListener;

        public NestFirebaseAuthListener (AuthenticationListener listener) {
            mListener = listener;
        }

        @Override
        public void onAuthError(FirebaseError firebaseError) {
            if (firebaseError != null) {
                Log.w(TAG, "Error during authentication: " + firebaseError.toString());
            }

            if (mListener != null) {
                final int errorCode = firebaseError == null ? 0 : firebaseError.getCode();
                mListener.onAuthenticationFailure(errorCode);
            }
        }

        @Override
        public void onAuthSuccess(Object o) {
            Log.v(TAG, "auth success: " + String.valueOf(o));
            if (mListener != null) {
                mListener.onAuthenticationSuccess();
            }
        }

        @Override
        public void onAuthRevoked(FirebaseError firebaseError) {
            onAuthError(firebaseError);
        }
    }

    private static class PathBuilder {
        private StringBuilder mBuilder;

        public PathBuilder() {
            mBuilder = new StringBuilder();
        }

        public PathBuilder append(String entry) {
            mBuilder.append("/").append(entry);
            return this;
        }

        public String build() {
            return mBuilder.toString();
        }
    }

    // Marker for Firebase to retrieve a strongly-typed collection
    private static class StringObjectMapIndicator extends GenericTypeIndicator<Map<String, Object>> {}
}
