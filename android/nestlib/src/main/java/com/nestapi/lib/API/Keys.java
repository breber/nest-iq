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

class Keys {
    static final String DEVICES = "devices";
    static final String THERMOSTATS = "thermostats";
    static final String STRUCTURES = "structures";
    static final String SMOKE_CO_ALARMS = "smoke_co_alarms";
    static final String DEVICE_ID = "device_id";
    static final String LOCALE = "locale";
    static final String SOFTWARE_VERSION = "software_version";
    static final String STRUCTURE_ID = "structure_id";
    static final String NAME = "name";
    static final String NAME_LONG = "name_long";
    static final String LAST_CONNECTION = "last_connection";
    static final String IS_ONLINE = "is_online";

    static class ACCESS_TOKEN {
        static final String TOKEN = "access_token";
        static final String EXPIRES_IN = "expires_in";
    }

    static class THERMOSTAT {
        static final String CAN_COOL = "can_cool";
        static final String CAN_HEAT = "can_heat";
        static final String IS_USING_EMERGENCY_HEAT = "is_using_emergency_heat";
        static final String HAS_FAN = "has_fan";
        static final String FAN_TIMER_ACTIVE = "fan_timer_active";
        static final String FAN_TIMER_TIMEOUT = "fan_timer_timeout";
        static final String HAS_LEAF = "has_leaf";
        static final String TEMP_SCALE = "temperature_scale";
        static final String TARGET_TEMP_F = "target_temperature_f";
        static final String TARGET_TEMP_C = "target_temperature_c";
        static final String TARGET_TEMP_HIGH_F = "target_temperature_high_f";
        static final String TARGET_TEMP_HIGH_C = "target_temperature_high_c";
        static final String TARGET_TEMP_LOW_F = "target_temperature_low_f";
        static final String TARGET_TEMP_LOW_C = "target_temperature_low_c";
        static final String AWAY_TEMP_HIGH_F = "away_temperature_high_f";
        static final String AWAY_TEMP_HIGH_C = "away_temperature_high_c";
        static final String AWAY_TEMP_LOW_F = "away_temperature_low_f";
        static final String AWAY_TEMP_LOW_C = "away_temperature_low_c";
        static final String HVAC_MODE = "hvac_mode";
        static final String AMBIENT_TEMP_F = "ambient_temperature_f";
        static final String AMBIENT_TEMP_C = "ambient_temperature_c";
    }

    static class SMOKE_CO_ALARM {
        static final String BATTERY_HEALTH = "battery_health";
        static final String CO_ALARM_STATE = "co_alarm_state";
        static final String SMOKE_ALARM_STATE = "smoke_alarm_state";
        static final String UI_COLOR_STATE = "ui_color_state";
    }

    static class STRUCTURE {
        static final String THERMOSTATS = "thermostats";
        static final String SMOKE_CO_ALARMS = "smoke_co_alarms";
        static final String AWAY = "away";
        static final String NAME = "name";
        static final String COUNTRY_CODE = "country_code";
        static final String PEAK_PERIOD_START_TIME = "peak_period_start_time";
        static final String PEAK_PERIOD_END_TIME = "peak_period_end_time";
        static final String TIME_ZONE = "time_zone";
        static final String ETA = "eta";
    }

    static class ETA {
        static final String TRIP_ID = "trip_id";
        static final String ESTIMATED_ARRIVAL_WINDOW_BEGIN = "estimated_arrival_window_begin";
        static final String ESTIMATED_ARRIVAL_WINDOW_END = "estimated_arrival_window_end";
    }
}
