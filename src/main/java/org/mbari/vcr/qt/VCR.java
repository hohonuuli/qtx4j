/*
 * Copyright 2007 MBARI
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
VCR.java
 *
Created on February 21, 2007, 2:50 PM
 *
To change this template, choose Tools | Template Manager
and open the template in the editor.
 */

package org.mbari.vcr.qt;

import org.mbari.qt.QT4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.movie.Timecode;
import org.mbari.qt.TimeUtil;
import org.mbari.vcr.VCRAdapter;
import quicktime.QTException;
import quicktime.std.StdQTException;
import quicktime.std.movies.Movie;


/**
 *
 * @author brian
 */
public class VCR extends VCRAdapter {

    private static final float SHUTTLE_RATE_DENOMINATOR = 51.0F;
    /** Multiplier used to determine shuttle rates */
    private static final int SHUTTLE_RATE = 3;
    private static final Logger log = LoggerFactory.getLogger(VCR.class);
    private final Movie movie;

    /**
     * Creates a new instance of VCR 
     *
     * @param movie
     *
     * @throws QTException
     * @throws org.mbari.qt.QT4JException 
     */
    public VCR(final Movie movie) throws QTException, QT4JException {
        this(movie, TimeSource.AUTO);
    }

    public VCR(final Movie movie, TimeSource timeSource) throws QTException, QT4JException {
        this.movie = movie;
        vcrReply = new VCRReply(movie, timeSource);
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
    public void seekTimecode(final Timecode timecode) {
        try {
            float rate = movie.getRate();
            movie.stop();
            final TimeSource timeSource = ((VCRTimecode) getVcrTimecode()).getTimeSource();
            switch (timeSource) {
                case TIMECODETRACK:
                    //final TimeCoder timeCoder =  ((QTTimecode) getVcrTimecode().getTimecode()).getTimeCoder();
                    //TODO How do we seek based on timecode? 
                case RUNTIME:
                default:
                    movie.setTime(TimeUtil.toTimeRecord(movie, timecode));
                    break;
            }

            movie.setRate(rate);
        } catch (QTException ex) {
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
        } catch (QTException e) {
            log.error("Problem getting DefaultDataRef from movie", e);
        }
        catch (Exception e) {
            log.error("Problem parsing connection url", e);
        }
        return connectionName;
    }

    /** 
     * Method description
     *
     *
     * @return
     */
    public Movie getMovie() {
        return movie;
    }
}
