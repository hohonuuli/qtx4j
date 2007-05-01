package org.mbari.vcr.qt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.vcr.VCRStateAdapter;
import quicktime.std.movies.Movie;
import quicktime.std.StdQTException;
import quicktime.std.StdQTConstants;
import quicktime.std.clocks.RateCallBack;
import quicktime.QTException;

/**
 * @author brian
 * @version $Id: $
 * @since Feb 22, 2007 8:45:30 AM PST
 */
public class VCRState extends VCRStateAdapter {

    private static final Logger log = LoggerFactory.getLogger(VCRState.class);
    private final Movie movie;
    private final RateCallBack qtCallBack;

    public VCRState(final Movie movie) throws QTException {
        this.movie = movie;
        qtCallBack = new QTCallback(movie);
    }

    public void triggerNotify() {
        notifyObservers();
    }


    @Override
    public boolean isFastForwarding() {
        boolean out = false;
        try {
            out = movie.getRate() > movie.getPreferredRate();
        }
        catch (StdQTException e) {
            log.error("QTException thrown", e);
        }
        return out;
    }

    @Override
    public boolean isPlaying() {
        boolean out = false;
        try {
            out = movie.getRate() == movie.getPreferredRate();
        }
        catch (StdQTException e) {
            log.error("QTException thrown", e);
        }
        return out;
    }

    @Override
    public boolean isReverseDirection() {
        boolean out = false;
        try {
            out = movie.getRate() < 0.0;
        }
        catch (StdQTException e) {
            log.error("QTException thrown", e);
        }
        return out;
    }

    @Override
    public boolean isRewinding() {
        boolean out = false;
        try {
            out = movie.getRate() < 0.0;
        }
        catch (StdQTException e) {
            log.error("QTException thrown", e);
        }
        return out;
    }

    @Override
    public boolean isShuttling() {
        boolean out = false;
        try {
            out = movie.getRate() != 0.0 && movie.getRate() != movie.getPreferredRate();
        }
        catch (StdQTException e) {
            log.error("QTException thrown", e);
        }
        return out;
    }

    @Override
    public boolean isStandingBy() {
        return isStopped();
    }

    @Override
    public boolean isStill() {
        return isStopped();
    }

    @Override
    public boolean isStopped() {
        boolean out = false;
        try {
            out = movie.getRate() == 0.0;
        }
        catch (StdQTException e) {
            log.error("QTException thrown", e);
        }
        return out;
    }

    @Override
    public boolean isTapeEnd() {
        boolean out = false;
        try {
            out = movie.isDone();
        }
        catch (StdQTException e) {
            log.error("QTException thrown", e);
        }
        return out;
    }

    @Override
    public boolean isConnected() {
        return movie != null;
    }

    /**
     * State notifies Observers whenever the rate changes
     */
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
            notifyObservers();

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
