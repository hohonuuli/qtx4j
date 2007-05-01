package org.mbari.qt.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.qt.QT;
import quicktime.std.clocks.RateCallBack;
import quicktime.std.movies.Movie;
import quicktime.std.StdQTConstants;
import quicktime.QTException;
import quicktime.io.QTFile;
import quicktime.io.OpenMovieFile;

import java.awt.*;

/**
 * Quicktime player that illustrates the usage of RateCallBack
 * 
 * @author brian
 * @version $Id: BasicQTController1.java 140 2007-02-17 00:41:37Z brian $
 * @since Jan 16, 2007 1:01:03 PM PST
 */
public class BasicQTController1 extends BasicQTController {

    private static final Logger log = LoggerFactory.getLogger(BasicQTController1.class);

    QTCallback callback;

    public BasicQTController1(Movie movie) throws QTException {
        super(movie);
        callback = new QTCallback(movie);
    }

    public static void main(String[] args) {
        try {
            QT.manageSession();
            QTFile qtFile = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
            OpenMovieFile openMovieFile = OpenMovieFile.asRead(qtFile);
            Movie m = Movie.fromFile(openMovieFile);
            Frame f = new BasicQTController1(m);
            f.setVisible(true);
        } catch (QTException ex) {
            log.error("An error occurred", ex);
        }
    }

    class QTCallback extends RateCallBack {

        QTCallback(Movie movie) throws QTException {
            /*
             * triggerRateChange tells the callback to execute anytime the rate changes
             */
            super(movie.getTimeBase(), 0, StdQTConstants.triggerRateChange);
            // Register callback
            callMeWhen();
        }

        public void execute() {
            boolean isStopped = rateWhenCalled == 0.0;
            getStartButton().setEnabled(isStopped);
            getStopButton().setEnabled(!isStopped);

            try {
                /*
                 * Callbacks are executed only once. We must reregister tha callback everytime it it
                 * executed.
                 */
                callMeWhen();
            }
            catch (QTException e) {
                log.error("Error in callback", e);
            }
        }
    }

}
