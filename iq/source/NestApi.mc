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
    static function responseCallback(responseCode, data) {
        Sys.println("Response: (" + responseCode + ") " + data);

        if (responseCode == 200) {
            //if (data
        }
    }

    static function getUserCode() {
        Sys.println("getUserCode");
        var url = "https://nestiqapi.appspot.com/_ah/api/nestiq/v1/nest/usercode";
        Comm.makeJsonRequest(url, {}, {}, responseCallback);
    }

    static function getTargetTemp() {
        var app = App.getApp();
        var temp = app.getProperty(TARGET_TEMP);
        return (temp == null) ? 0 : temp;
    }

    static function getCurrentTemp() {
        getUserCode();

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
            var listener = new CommListener();
            Comm.transmit("targetTemperature==" + target, null, listener);
        }
    }

    static function setAwayStatus(status) {
        var listener = new CommListener();
        Comm.transmit("awayStatus==" + status, null, listener);
    }
}
