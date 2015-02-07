using Toybox.Application as App;

enum
{
    LAST_KNOWN_TEMP,
    IS_CURRENTLY_AWAY,
    HVAC_MODE
}

class NestApi {
    static function getLastKnownTemp() {
        var app = App.getApp();
        var lastTemp = app.getProperty(LAST_KNOWN_TEMP);
        return (lastTemp == null) ? 0 : lastTemp;
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