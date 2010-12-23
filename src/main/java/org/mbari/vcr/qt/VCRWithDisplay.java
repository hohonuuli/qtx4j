/*
 * @(#)VCRWithDisplay.java   2010.11.29 at 04:16:50 PST
 *
 * Copyright 2009 MBARI
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.mbari.vcr.qt;

import java.awt.Frame;
import java.net.URL;
import org.mbari.framegrab.IGrabber;
import org.mbari.framegrab.MovieGrabber;
import org.mbari.movie.Timecode;
import org.mbari.qt.QT;
import org.mbari.qt.QT4JException;
import org.mbari.qt.QTTimecode;
import org.mbari.qt.TimeUtil;
import org.mbari.qt.awt.QTMovieFrame;
import javax.swing.SwingUtilities;
import org.mbari.vcr.VCRAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.std.StdQTException;
import quicktime.std.movies.Movie;
import quicktime.std.movies.MoviePrePreroll;

/**
 * @author Brian Schlining
 * @since 2010-11-29
 */
public class VCRWithDisplay extends VCRAdapter {

    /** Multiplier used to determine shuttle rates */
    private static final int SHUTTLE_RATE = 3;
    private static final float SHUTTLE_RATE_DENOMINATOR = 51.0F;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Frame currentVideoFrame;
    private IGrabber grabber;
    private Movie movie;
    private QTTimecode timecode;

    /**
     * Creates a new instance of VCR
     *
     *
     * @param movieRef
     * @param timeSource
     *
     */
    public VCRWithDisplay(String movieRef, TimeSource timeSource) {
        quickTimeCheck();
        openVideo(movieRef);
        try {
            vcrReply = new VCRReply(movie, timeSource);
        }
        catch (QTException e) {
           throw new QT4JException("An error occurred while initializing the VCRReply", e); 
        }
    }
    
    public void disconnect() {
        if (currentVideoFrame != null) {
            try {
                currentVideoFrame.dispose();
            }
            catch (Exception ex) {
                log.warn("An error occurred while trying to close a QuickTime movie Frame", ex);
            }
        }
        
        if (grabber != null) {
            try {
                grabber.dispose();   
            }
            catch (Exception ex) {
                log.warn("An error occurred while trying to close a QuickTime image grabber", ex);                
            }
        }
        
        timecode = null;
            
    }

    /**
     * Method description
     *
     */
    @Override
    public void fastForward() {
        try {
            movie.setRate(movie.getPreferredRate() * SHUTTLE_RATE);
        }
        catch (StdQTException ex) {
            log.error("Fastforward failed", ex);
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getConnectionName() {
        String connectionName = "";

        try {
            connectionName = movie.getDefaultDataRef().getUniversalResourceLocator();

            final String[] parts = connectionName.split("/");

            if (parts.length > 0) {
                connectionName = parts[parts.length - 1];
            }
        }
        catch (QTException e) {
            log.error("Problem getting DefaultDataRef from movie", e);
        }
        catch (Exception e) {
            log.error("Problem parsing connection url", e);
        }

        return connectionName;
    }

    /**
    * @return a {@link IGrabber} for grabbing images form the movie
     */
    public IGrabber getGrabber() {
        if ((grabber == null) && (movie != null)) {
            grabber = new MovieGrabber(movie);
        }

        return grabber;
    }

    private void openVideo(String movieRef) {
        URL url = null;

        try {
            url = new URL(movieRef);
            log.debug("Attempting to open the QuickTime movie at " + url.toExternalForm());
            movie = QT.openMovieFromUrl(url);
            movie.prePreroll(0, 1.0f, new PrepMovie());    // All the interesting stuff happens here.
        }
        catch (Exception e) {}
    }
    
    // public QTTimecode getQTTimecode() {
    //     if (timecode == null) {
    //         timecode = new QTTimecode(movie);
    //         timecode.updateTimecode();
    //     }
    //     return timecode;
    // }

    /**
     * Method description
     *
     */
    @Override
    public void pause() {
        stop();
    }

    /**
     * Method description
     *
     */
    @Override
    public void play() {
        try {
            movie.start();
        }
        catch (StdQTException ex) {
            log.error("Play failed", ex);
        }
    }

    private void quickTimeCheck() {
        try {

            // Check to see if quicktime is installed first
            Class.forName("quicktime.QTSession");
        }
        catch (final Throwable e) {
            String javaHome = System.getProperty("java.home");
            String javaVmName = System.getProperty("java.vm.name");

            throw new QT4JException("QuickTime for Java is not installed in '" + 
                    javaVmName + "' located at " + javaHome);
        }
    }

    /**
     * Method description
     *
     */
    @Override
    public void requestLTimeCode() {
        requestTimeCode();
    }

    /**
     * Method description
     *
     */
    @Override
    public void requestStatus() {

        // Trigger the VCRState to notify Observers
        ((org.mbari.vcr.qt.VCRState) getVcrState()).triggerNotify();
    }

    /**
     * Method description
     *
     */
    @Override
    public void requestTimeCode() {
        ((VCRTimecode) getVcrTimecode()).updateTimecode();
        //getQTTimecode().updateTimecode();
    }

    /**
     * Method description
     *
     */
    @Override
    public void requestVTimeCode() {
        requestTimeCode();
    }

    /**
     * Method description
     *
     */
    @Override
    public void rewind() {
        try {
            movie.setRate(-movie.getPreferredRate() * SHUTTLE_RATE);
        }
        catch (StdQTException ex) {
            log.error("Fastforward failed", ex);
        }
    }

    /**
     * Method description
     *
     *
     * @param timecode
     */
    @Override
    public void seekTimecode(final Timecode timecode) {
        try {
            float rate = movie.getRate();

            movie.stop();

            final TimeSource timeSource = ((VCRTimecode) getVcrTimecode()).getTimeSource();

            switch (timeSource) {
            case TIMECODETRACK:

            // final TimeCoder timeCoder =  ((QTTimecode) getVcrTimecode().getTimecode()).getTimeCoder();
            // TODO How do we seek based on timecode?
            case RUNTIME:
            default:
                movie.setTime(TimeUtil.toTimeRecord(movie, timecode));

                break;
            }

            movie.setRate(rate);
        }
        catch (QTException ex) {
            log.error("Failed to seek to " + timecode.toString());
        }
    }

    /**
     * Method description
     *
     *
     * @param speed
     */
    @Override
    public void shuttleForward(int speed) {
        try {
            movie.setRate((float) speed / SHUTTLE_RATE_DENOMINATOR);
        }
        catch (StdQTException e) {
            log.error("Failed to shuttle forward", e);
        }
    }

    /**
     * Method description
     *
     *
     * @param speed
     */
    @Override
    public void shuttleReverse(int speed) {
        try {
            movie.setRate((float) speed / -SHUTTLE_RATE_DENOMINATOR);
        }
        catch (StdQTException e) {
            log.error("Failed to shuttle forward", e);
        }
    }

    /**
     * Method description
     *
     */
    @Override
    public void stop() {
        try {
            movie.stop();
        }
        catch (StdQTException ex) {
            log.error("Stop failed", ex);
        }
    }

    private class PrepMovie implements MoviePrePreroll {

        /**
         *
         * @param movie
         * @param errorCode
         */
        public void execute(final Movie movie, int errorCode) {
            log.debug("Executing preroll tasks");

            try {

                /*
                 * Once we have enough movie loaded display it on the
                 * Swing event thread.
                 */
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        try {
                            currentVideoFrame = new QTMovieFrame(movie);
                            currentVideoFrame.pack();
                            currentVideoFrame.setVisible(true);

                            if (log.isDebugEnabled()) {
                                String msg = "Opened QuickTime movie:\n\tIVCR = " + this.toString() +
                                             "\n\tmovie = " + movie.toString();

                                log.debug(msg);
                            }
                        }
                        catch (Exception ex) {
                            throw new QT4JException("Failed to open " + movie.toString(), ex);
                        }
                    }

                });
            }
            catch (Exception ex) {
                throw new QT4JException("Failed to open " + movie.toString(), ex);
            }
        }
    }
}
