using Toybox.Application as App;
using Toybox.Communications as Comm;
using Toybox.System as Sys;
using Toybox.WatchUi as Ui;

const BASE_URL = "http://nestiqapi.appspot.com";

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

var nestApi = new NestApi();

class KeyboardListener extends Ui.TextPickerDelegate {
    function onTextEntered(text, changed) {
        var url = BASE_URL + "/api/accesstoken/" + text;
        Comm.makeJsonRequest(url, {}, {}, nestApi.method(:authenticateResponseCallback));
    }
}

class NestApi {
    static var sTextInput = new Ui.TextPicker("");

    function isAuthenticated() {
        var app = App.getApp();
        var token = app.getProperty(ACCESS_TOKEN);
        return (token != null);
    }

    function fetchUpdates() {
        var app = App.getApp();
        var token = app.getProperty(ACCESS_TOKEN);
        var url = BASE_URL + "/api/status/" + token;
        Sys.println(url);
        Comm.makeJsonRequest(url, {}, {}, method(:refreshDataResponseCallback));
    }

    function getTargetTemp() {
        var app = App.getApp();
        var temp = app.getProperty(TARGET_TEMP);
        return (temp == null) ? 0 : temp;
    }

    function getCurrentTemp() {
        var app = App.getApp();
        var temp = app.getProperty(CURRENT_TEMP);
        return (temp == null) ? 0 : temp;
    }

    function isCurrentlyAway() {
        var app = App.getApp();
        var away = app.getProperty(IS_CURRENTLY_AWAY);
        return (away == null) ? false : "away".equals(away);
    }

    function getHvacMode() {
        var app = App.getApp();
        var mode = app.getProperty(HVAC_MODE);
        return (mode == null) ? "heat" : mode;
    }

    function setTargetTemperature(target) {
        if ((target > 50) && (target < 90)) {
            var app = App.getApp();
            var token = app.getProperty(ACCESS_TOKEN);
            var thermostat = app.getProperty(THERMOSTAT);
            var url = BASE_URL + "/api/target/set/" + token + "/" + thermostat + "/" + target;
            Sys.println(url);
            Comm.makeJsonRequest(url, {}, {}, method(:refreshDataResponseCallback));
        }
    }

    function setAwayStatus(status) {
        var awayStatus = "away";
        if (!status) {
            awayStatus = "home";
        }

        var app = App.getApp();
        var token = app.getProperty(ACCESS_TOKEN);
        var structure = app.getProperty(STRUCTURE);
        var url = BASE_URL + "/api/away/set/" + token + "/" + structure + "/" + awayStatus;
        Sys.println(url);
        Comm.makeJsonRequest(url, {}, {}, method(:refreshDataResponseCallback));
    }

    function authenticateResponseCallback(responseCode, data) {
        Sys.println("Response: (" + responseCode + ") " + data);

        if (responseCode == 200) {
            var app = App.getApp();

            // If we have an access token, we can go on with our requests
            if (data.hasKey("access_token")) {
                app.setProperty(ACCESS_TOKEN, data["access_token"]);

                fetchUpdates();
            }

            Ui.requestUpdate();
        }
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
}
