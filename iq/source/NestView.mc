using Toybox.Graphics as Gfx;
using Toybox.System as Sys;
using Toybox.WatchUi as Ui;

class NestView extends Ui.View {

    //! Restore the state of the app and prepare the view to be shown
    function onShow() {
    }

    function drawHashMarks(dc) {
        var width = dc.getWidth();
        var height = dc.getHeight();
        var coords = [ 0, width / 4, (3 * width) / 4, width ];

        Sys.println("drawHashMarks: " + coords[1]);

        for (var i = 0; i < coords.size(); i++) {
            var dx = ((width / 2.0) - coords[i]) / (height / 2.0);
            var upperX = coords[i] + (dx * 10);
            // Draw the upper hash marks
            dc.fillPolygon([ [coords[i] - 1, 2], [upperX - 1, 12], [upperX + 1, 12], [coords[i] + 1, 2] ]);
            // Draw the lower hash marks
            dc.fillPolygon([ [coords[i] - 1, height-2], [upperX - 1, height - 12], [upperX + 1, height - 12], [coords[i] + 1, height - 2]]);
        }
    }

    //! Update the view
    function onUpdate(dc) {
        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_BLACK);
        dc.clear();

        var width = dc.getWidth();
        var height = dc.getHeight();

        // Draw the text in the center of the circle
        dc.setColor(Gfx.COLOR_WHITE, Gfx.COLOR_BLACK);
        var mode = NestApi.getHvacMode();
        if ("off".equals(mode)) {
            dc.drawText(width / 2, height / 2 - dc.getFontHeight(Gfx.FONT_LARGE) / 2,
                Gfx.FONT_LARGE, "OFF", Gfx.TEXT_JUSTIFY_CENTER);
        } else if (NestApi.isCurrentlyAway()) {
            dc.drawText(width / 2, height / 2 - dc.getFontHeight(Gfx.FONT_LARGE) / 2,
                Gfx.FONT_LARGE, "AWAY", Gfx.TEXT_JUSTIFY_CENTER);
        } else {
            dc.drawText(width / 2, height / 2 - dc.getFontHeight(Gfx.FONT_NUMBER_THAI_HOT) / 4,
                Gfx.FONT_NUMBER_THAI_HOT, "" + NestApi.getLastKnownTemp(), Gfx.TEXT_JUSTIFY_CENTER);
            dc.drawText(width / 2, height / 2 - dc.getFontHeight(Gfx.FONT_NUMBER_THAI_HOT) / 3,
                Gfx.FONT_SMALL, mode.toUpper() + " SET TO", Gfx.TEXT_JUSTIFY_CENTER);
        }

        // Draw the hash marks
        drawHashMarks(dc);
    }

    //! Called when this View is removed from the screen. Save the
    //! state of your app here.
    function onHide() {
    }

}