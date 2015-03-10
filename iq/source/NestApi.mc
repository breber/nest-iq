using Toybox.Application as App;
using Toybox.Communications as Comm;
using Toybox.System as Sys;

enum
{
    ACCESS_TOKEN,
    TARGET_TEMP,
    CURRENT_TEMP,
    IS_CURRENTLY_AWAY,
    HVAC_MODE
}

class NestApi {
    static var sUserCode = "";
    hidden static var sDeviceCode = null;

    hidden static function responseCallback(responseCode, data) {
        Sys.println("Response: (" + responseCode + ") " + data);

        if (responseCode == 200) {
            var app = App.getApp();

            // If it has a 'user_code' item in the response, we need
            // to initiate a request to get the access token
            if (data.hasKey("user_code")) {
                sDeviceCode = data['device_code'];
                sUserCode = data['user_code'];
                // TODO: request confirmation
                Ui.requestUpdate();
            }
            // If we have an access token, we can go on with our requests
            else if (data.hasKey("access_token")) {
                app.setProperty(ACCESS_TOKEN, data['access_token']);
                Ui.requestUpdate();
            }
        }
    }

    hidden static function strReplace(str, search, repl) {
        var result = str;
        while (result.find(search) != null) {
            var index = str.find(search);
            var tmp = str.substring(0, index);
            tmp += repl;
            tmp += str.substring(index + search.length(), str.length());
            result = tmp;
        }
        return result;
    }

    static function authenticate() {
        Sys.println("authenticate");
        if (sDeviceCode == null) {
            Sys.println("getUserCode");
            var url = "https://nestiqapi.appspot.com/_ah/api/nestiq/v1/nest/usercode";
            Comm.makeJsonRequest(url, {}, {}, responseCallback);
        } else {
            Sys.println("getAccessToken");
            var deviceCode = strReplace(sDeviceCode, "/", "%2F");

            var url = "https://nestiqapi.appspot.com/_ah/api/nestiq/v1/nest/accesstoken/" + deviceCode;
            Comm.makeJsonRequest(url, {}, {}, responseCallback);
        }
    }

    static function isAuthenticated() {
        var app = App.getApp();
        var token = app.getProperty(ACCESS_TOKEN);
        return (token != null);
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
