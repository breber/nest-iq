using Toybox.Application as App;
using Toybox.Communications as Comm;
using Toybox.System as Sys;
using Toybox.WatchUi as Ui;

enum
{
    ACCESS_TOKEN,
    THERMOSTAT,
    STRUCTURE,
    TARGET_TEMP,
    CURRENT_TEMP,
    IS_CURRENTLY_AWAY,
    HVAC_MODE
}

class KeyboardListener extends Ui.TextPickerDelegate {
    function onTextEntered(text, changed) {
        var requests = new WebRequests();
        requests.authenticate(text);
    }
}

const BASE_URL = "http://nestiqapi.appspot.com";
class WebRequests {
    function authenticate(code) {
        var url = BASE_URL + "/api/accesstoken/" + code;
        Comm.makeJsonRequest(url, {}, {}, method(:authenticateResponseCallback));
    }

    function authenticateResponseCallback(responseCode, data) {
        Sys.println("Response: (" + responseCode + ") " + data);

        if (responseCode == 200) {
            var app = App.getApp();

            // If we have an access token, we can go on with our requests
            if (data.hasKey("access_token")) {
                app.setProperty(ACCESS_TOKEN, data["access_token"]);

                NestApi.fetchUpdates();
            }

            Ui.requestUpdate();
        }
    }

    function refreshData() {
        var app = App.getApp();
        var token = app.getProperty(ACCESS_TOKEN);
        var url = BASE_URL + "/api/status/" + token;
        Sys.println(url);
        Comm.makeJsonRequest(url, {}, {}, method(:refreshDataResponseCallback));
    }

    function refreshDataResponseCallback(responseCode, data) {
        Sys.println("Refresh Response: (" + responseCode + ") " + data);

        if (responseCode == 200) {
            var app = App.getApp();

            // If we have an hvac_mode, we are responding to an update
            if (data.hasKey("thermostat")) {
                app.setProperty(THERMOSTAT, data.get("thermostat"));
                app.setProperty(STRUCTURE, data.get("structure"));
                app.setProperty(TARGET_TEMP, data.get("target_temp"));
                app.setProperty(CURRENT_TEMP, data.get("current_temp"));
                app.setProperty(IS_CURRENTLY_AWAY, data.get("away_status"));
                app.setProperty(HVAC_MODE, data.get("hvac_mode"));
            }

            Ui.requestUpdate();
        }
    }

    function setTargetTemperature(target) {
        var app = App.getApp();
        var token = app.getProperty(ACCESS_TOKEN);
        var thermostat = app.getProperty(THERMOSTAT);
        var url = BASE_URL + "/api/target/set/" + token + "/" + thermostat + "/" + target;
        Sys.println(url);
        Comm.makeJsonRequest(url, {}, {}, method(:updateDataResponseCallback));
    }

    function setAwayStatus(status) {
        var awayStatus = "away";
        if (!status) {
            awayStatus = "home";
        }
        var url = BASE_URL + "/api/away/set/" + awayStatus;
        Comm.makeJsonRequest(url, {}, {}, method(:updateDataResponseCallback));
    }

    function updateDataResponseCallback(responseCode, data) {
        Sys.println("Update Response: (" + responseCode + ") " + data);

        if ((responseCode == 200) &&
            data.hasKey("status") &&
            (data.get("status") == 200))
        {
            var app = App.getApp();
            var initialReq = data.get("req");

            if ("temp".equals(initialReq)) {
                app.setProperty(TARGET_TEMP, data.get("val"));
            } else if ("away".equals(initialReq)) {
                app.setProperty(IS_CURRENTLY_AWAY, data.get("val"));
            }

            Ui.requestUpdate();
        }
    }
}

class NestApi {
    static var sTextInput = new Ui.TextPicker("");

    static function isAuthenticated() {
        var app = App.getApp();
        var token = app.getProperty(ACCESS_TOKEN);
        return (token != null);
    }

    static function fetchUpdates() {
        var requests = new WebRequests();
        requests.refreshData();
    }

    static function getTargetTemp() {
        var app = App.getApp();
        var temp = app.getProperty(TARGET_TEMP);
        return (temp == null) ? 0 : temp;
    }

    static function getCurrentTemp() {
        var app = App.getApp();
        var temp = app.getProperty(CURRENT_TEMP);
        return (temp == null) ? 0 : temp;
    }

    static function isCurrentlyAway() {
        var app = App.getApp();
        var away = app.getProperty(IS_CURRENTLY_AWAY);
        return (away == null) ? false : away;
    }

    static function getHvacMode() {
        var app = App.getApp();
        var mode = app.getProperty(HVAC_MODE);
        return (mode == null) ? "heat" : mode;
    }

    static function setTargetTemperature(target) {
        if ((target > 50) && (target < 90)) {
            var requests = new WebRequests();
            requests.setTargetTemperature(target);
        }
    }

    static function setAwayStatus(status) {
        var requests = new WebRequests();
        requests.setAwayStatus(status);
    }
}
