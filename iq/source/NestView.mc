using Toybox.Graphics as Gfx;
using Toybox.System as Sys;
using Toybox.WatchUi as Ui;

class NestDelegate extends Ui.InputDelegate {
    function onKey(evt) {
        var mode = nestApi.getHvacMode();

        if ("off".equals(mode)) {
            // If the system is off, don't handle any input
            // TODO: maybe allow to turn on
            return;
        } else if (nestApi.isCurrentlyAway()) {
            // If we are away, look for the ENTER key
            if (evt.getKey() == Ui.KEY_ENTER) {
                nestApi.setAwayStatus(false);
            }
        } else {
            // In any other case, look for:
            //  -the enter key to enable away mode
            //  -the up/down key to change target temperature
            if (evt.getKey() == Ui.KEY_ENTER) {
                nestApi.setAwayStatus(true);
            } else if (evt.getKey() == Ui.KEY_UP) {
                nestApi.setTargetTemperature(nestApi.getTargetTemp() + 1);
            } else if (evt.getKey() == Ui.KEY_DOWN) {
                nestApi.setTargetTemperature(nestApi.getTargetTemp() - 1);
            }
        }

        Ui.requestUpdate();
    }
}

class NestView extends Ui.View {

    //! Restore the state of the app and prepare the view to be shown
    function onShow() {
        if (nestApi.isAuthenticated()) {
            nestApi.fetchUpdates();
        }
    }

    function drawHashMarks(dc, currentTemp) {
        // Range 40 - 90
        var radius = dc.getWidth() / 2;
        var numTicks = 50;
        var tickInterval = (2 * Math.PI - (4 * Math.PI / 12)) / numTicks;

        var cx = dc.getWidth() / 2;
        var cy = dc.getHeight() / 2;
        var lengthMultiplier = 7.0 / 8.0;
        var longLengthMultiplier = 6.0 / 8.0;
        for (var i = 0; i < numTicks / 2; i++) {
            dc.setPenWidth(2);
            var negativeMultiplier = lengthMultiplier;
            var positiveMultiplier = lengthMultiplier;

            if ((65 + i) == currentTemp) {
                negativeMultiplier = longLengthMultiplier;
            } else if ((65 - i) == currentTemp) {
                positiveMultiplier = longLengthMultiplier;
            }

            var dx = radius * Math.cos(Math.PI / 2 + tickInterval * i);
            var dy = radius * Math.sin(Math.PI / 2 + tickInterval * i);
            dc.drawLine(cx - negativeMultiplier * dx, cy - negativeMultiplier * dy, cx - dx, cy - dy);

            dx = radius * Math.cos(Math.PI / 2 - tickInterval * i);
            dy = radius * Math.sin(Math.PI / 2 - tickInterval * i);
            dc.drawLine(cx - positiveMultiplier * dx, cy - positiveMultiplier * dy, cx - dx, cy - dy);
        }
    }

    //! Update the view
    function onUpdate(dc) {
        if (nestApi.isAuthenticated()) {
            var width = dc.getWidth();
            var height = dc.getHeight();

            dc.setColor(Gfx.COLOR_WHITE, Gfx.COLOR_BLACK);
            dc.clear();

            // Draw the text in the center of the circle
            dc.setColor(Gfx.COLOR_WHITE, Gfx.COLOR_BLACK);
            var mode = nestApi.getHvacMode();
            if ("off".equals(mode)) {
                dc.drawText(width / 2, height / 2 - dc.getFontHeight(Gfx.FONT_LARGE) / 2,
                    Gfx.FONT_LARGE, "OFF", Gfx.TEXT_JUSTIFY_CENTER);
            } else if (nestApi.isCurrentlyAway()) {
                dc.drawText(width / 2, height / 2 - dc.getFontHeight(Gfx.FONT_LARGE) / 2,
                    Gfx.FONT_LARGE, "AWAY", Gfx.TEXT_JUSTIFY_CENTER);
            } else {
                dc.drawText(width / 2, height / 2 - dc.getFontHeight(Gfx.FONT_NUMBER_THAI_HOT) / 4,
                    Gfx.FONT_NUMBER_THAI_HOT, "" + nestApi.getTargetTemp(), Gfx.TEXT_JUSTIFY_CENTER);
                dc.drawText(width / 2, height / 2 - dc.getFontHeight(Gfx.FONT_NUMBER_THAI_HOT) / 3,
                    Gfx.FONT_SMALL, mode.toUpper() + " SET TO", Gfx.TEXT_JUSTIFY_CENTER);
            }

            // Draw the hash marks
            drawHashMarks(dc, nestApi.getCurrentTemp());
        } else {
            Ui.pushView(NestApi.sTextInput, new KeyboardListener(), Ui.SLIDE_DOWN);
        }
    }

    //! Called when this View is removed from the screen. Save the
    //! state of your app here.
    function onHide() {
    }
}
