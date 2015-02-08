using Toybox.Application as App;

enum
{
    TARGET_TEMP,
    CURRENT_TEMP,
    IS_CURRENTLY_AWAY,
    HVAC_MODE
}

class NestApi {
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
}