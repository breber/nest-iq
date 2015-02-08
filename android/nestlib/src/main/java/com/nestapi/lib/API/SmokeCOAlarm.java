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

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public final class SmokeCOAlarm extends BaseDevice {
    private final String mBatteryHealth;
    private final String mCOAlarmState;
    private final String mSmokeAlarmState;
    private final String mUIColorState;

    private SmokeCOAlarm(Builder builder) {
        super(builder);
        mBatteryHealth = builder.mBatteryHealth;
        mCOAlarmState = builder.mCOAlarmState;
        mSmokeAlarmState = builder.mSmokeAlarmState;
        mUIColorState = builder.mUIColorState;
    }

    private SmokeCOAlarm(Parcel in) {
        super(in);
        mBatteryHealth = in.readString();
        mCOAlarmState = in.readString();
        mSmokeAlarmState = in.readString();
        mUIColorState = in.readString();
    }

    public static class Builder extends BaseDeviceBuilder<SmokeCOAlarm> {
        private String mBatteryHealth;
        private String mCOAlarmState;
        private String mSmokeAlarmState;
        private String mUIColorState;

        public SmokeCOAlarm fromJSON(JSONObject json) {
            super.fromJSON(json);
            setBatteryHealth(json.optString(Keys.SMOKE_CO_ALARM.BATTERY_HEALTH));
            setCOAlarmState(json.optString(Keys.SMOKE_CO_ALARM.CO_ALARM_STATE));
            setSmokeAlarmState(json.optString(Keys.SMOKE_CO_ALARM.SMOKE_ALARM_STATE));
            setUIColorState(json.optString(Keys.SMOKE_CO_ALARM.UI_COLOR_STATE));
            return build();
        }

        public Builder setBatteryHealth(String batteryHealth) {
            mBatteryHealth = batteryHealth;
            return this;
        }

        public Builder setCOAlarmState(String coAlarmState) {
            mCOAlarmState = coAlarmState;
            return this;
        }

        public Builder setSmokeAlarmState(String smokeAlarmState) {
            mSmokeAlarmState = smokeAlarmState;
            return this;
        }

        public Builder setUIColorState(String uiColorState) {
            mUIColorState = uiColorState;
            return this;
        }

        public SmokeCOAlarm build() {
            return new SmokeCOAlarm(this);
        }
    }

    @Override
    protected void save(JSONObject json) throws JSONException {
        super.save(json);
        json.put(Keys.SMOKE_CO_ALARM.BATTERY_HEALTH, mBatteryHealth);
        json.put(Keys.SMOKE_CO_ALARM.CO_ALARM_STATE, mCOAlarmState);
        json.put(Keys.SMOKE_CO_ALARM.SMOKE_ALARM_STATE, mSmokeAlarmState);
        json.put(Keys.SMOKE_CO_ALARM.UI_COLOR_STATE, mUIColorState);
    }

    public String getBatteryHealth() {
        return mBatteryHealth;
    }

    public String getCOAlarmState() {
        return mCOAlarmState;
    }

    public String getSmokeAlarmState() {
        return mSmokeAlarmState;
    }

    public String getUIColorState() {
        return mUIColorState;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mBatteryHealth);
        dest.writeString(mCOAlarmState);
        dest.writeString(mSmokeAlarmState);
        dest.writeString(mUIColorState);
    }

    public static final Parcelable.Creator<SmokeCOAlarm> CREATOR = new Parcelable.Creator<SmokeCOAlarm>() {
        @Override
        public SmokeCOAlarm createFromParcel(Parcel source) {
            return new SmokeCOAlarm(source);
        }

        @Override
        public SmokeCOAlarm[] newArray(int size) {
            return new SmokeCOAlarm[size];
        }
    };
}
