using Toybox.Application as App;
using Toybox.Communications as Comm;

enum
{
    TARGET_TEMP,
    CURRENT_TEMP,
    IS_CURRENTLY_AWAY,
    HVAC_MODE
}

class NestApi {
    function initialize() {
        Comm.setMailboxListener(method(:onMail));
    }

    function onMail(mailIter) {
        var mail = mailIter.next();

        while (mail != null) {
            // TODO: do something with the mail
            mail = mailIter.next();
        }

        Comm.emptyMailbox();
        Ui.requestUpdate();
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
            var listener = new CommListener();
            Comm.transmit("targetTemperature==" + target, null, listener);
        }
    }

    static function setAwayStatus(status) {
        var listener = new CommListener();
        Comm.transmit("awayStatus==" + status, null, listener);
    }
}

class CommListener extends Comm.ConnectionListener {
    function onComplete() {
        Sys.println( "Transmit Complete" );
    }

    function onError() {
        Sys.println( "Transmit Failed" );
    }
}