using Toybox.Application as App;
using Toybox.Communications as Comm;
using Toybox.System as Sys;
using Toybox.WatchUi as Ui;

enum
{
    ACCESS_TOKEN,
    TARGET_TEMP,
    CURRENT_TEMP,
    IS_CURRENTLY_AWAY,
    HVAC_MODE
}

class KeyboardListener extends Ui.TextPickerDelegate {
    function onTextEntered(text, changed) {
        NestApi.authenticate(text);
    }
}

class NestApi {
    static var sTextInput = new Ui.TextPicker("Test");

    hidden static function responseCallback(responseCode, data) {
        Sys.println("Response: (" + responseCode + ") " + data);

        if (responseCode == 200) {
            var app = App.getApp();

            // If we have an hvac_mode, we are responding to an update
            if (data.hasKey("hvac_mode")) {
                app.setProperty(TARGET_TEMP, data.get("target_temp"));
                app.setProperty(CURRENT_TEMP, data.get("current_temp"));
                app.setProperty(IS_CURRENTLY_AWAY, data.get("away_status"));
                app.setProperty(HVAC_MODE, data.get("hvac_mode"));
            }
            // If we have an access token, we can go on with our requests
            else if (data.hasKey("access_token")) {
                app.setProperty(ACCESS_TOKEN, data['access_token']);

                fetchUpdates();
            }

            Ui.requestUpdate();
        }
    }

    static function authenticate(code) {
        Sys.println("authenticate");
        var url = "https://nestiqapi.appspot.com/_ah/api/nestiq/v1/nest/accesstoken/" + code;
        Comm.makeJsonRequest(url, {}, {}, responseCallback);
    }

    static function isAuthenticated() {
        var app = App.getApp();
        var token = app.getProperty(ACCESS_TOKEN);
        return (token != null);
    }

    static function fetchUpdates() {
        Sys.println("fetchUpdates");
        var app = App.getApp();
        var token = app.getProperty(ACCESS_TOKEN);
        var url = "https://nestiqapi.appspot.com/_ah/api/nestiq/v1/nest/status/" + token;
        Comm.makeJsonRequest(url, {}, {}, responseCallback);
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
            var url = "https://nestiqapi.appspot.com/_ah/api/nestiq/v1/nest/targettemperature/set/" + target;
            Comm.makeJsonRequest(url, {}, {}, responseCallback);
        }
    }

    static function setAwayStatus(status) {
        var url = "https://nestiqapi.appspot.com/_ah/api/nestiq/v1/nest/away/set/" + status;
        Comm.makeJsonRequest(url, {}, {}, responseCallback);
    }
}
