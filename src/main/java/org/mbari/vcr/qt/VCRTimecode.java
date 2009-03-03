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
VCRTimecode.java
 *
Created on March 14, 2007, 9:26 AM
 *
To change this template, choose Tools | Template Manager
and open the template in the editor.
 */

package org.mbari.vcr.qt;

import org.mbari.movie.Timecode;
import org.mbari.qt.QT4JException;
import org.mbari.qt.QTTimecode;
import org.mbari.qt.TimeUtil;
import org.mbari.util.IObserver;
import org.mbari.util.ObservableSupport;
import org.mbari.vcr.IVCRTimecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.std.movies.Movie;

/**
 *
 * @author brian
 */
public class VCRTimecode implements IVCRTimecode {

    private final ObservableSupport os = new ObservableSupport();
    private Timecode timecode;
    private TimeSource timeSource;
    private final Movie movie;

    private static final Logger log = LoggerFactory.getLogger(VCRTimecode.class);
    /**
     * Constructs ...
     *
     * @param movie
     *
     * @throws QT4JException
     * @throws QTException
     */
    public VCRTimecode(final Movie movie) throws QTException, QT4JException {
        this(movie, TimeSource.AUTO);
    }

    /**
     * Creates a new instance of VCRTimecode 
     *
     * @param movie
     * @param timeSource
     *
     * @throws QT4JException
     * @throws QTException
     */
    public VCRTimecode(final Movie movie, final TimeSource timeSource) throws QTException, QT4JException {
        this.movie = movie;
        initTimecode(movie, timeSource);
    }

    /**
     * Add an a IObserver to monitor timecode changes
     *
     * @param observer
     */
    public void addObserver(IObserver observer) {
        os.add(observer);
    }

    /**
     * @return The frame of the current timecode
     */
    public int getFrame() {
        return timecode.getFrame();
    }

    /**
     * @return The hour of the current timecode
     */
    public int getHour() {
        return timecode.getHour();
    }

    /**
     * @return The minute of the current timecode
     */
    public int getMinute() {
        return timecode.getMinute();
    }

    /**
     * @return The second of the current timecode
     */
    public int getSecond() {
        return timecode.getSecond();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Timecode getTimecode() {
        return timecode;
    }
    
    /**
     * Updates the timecode to the current time in the movie
     */
    public void updateTimecode() {
        try {
            if (timeSource == TimeSource.RUNTIME) {
                TimeUtil.queryRuntime(movie, timecode);
            }
            else {
                ((QTTimecode) timecode).updateTimecode();
            }
        } catch (QTException ex) {
            log.error("QTException occured", ex);
        }
    }

    private void initTimecode(final Movie movie, TimeSource timeSource) throws QTException, QT4JException {

        // If AUTO, use a TimeCodeTrack if found. Otherwise use runtime
        if (timeSource == TimeSource.AUTO) {
            if (TimeUtil.hasTimeCodeTrack(movie)) {
                timeSource = TimeSource.TIMECODETRACK;
            }
            else {
                timeSource = TimeSource.RUNTIME;
            }
        }

        // Create a timecode using the selected TimeSource
        if (timeSource == TimeSource.RUNTIME) {
            timecode = new Timecode();
            timecode.setFrameRate(TimeUtil.estimateFrameRate(movie));
        }
        else {
            timecode = new QTTimecode(movie);
        }

        this.timeSource = timeSource;
        timecode.addObserver(new Observer());
    }

    /** Notifies registered observers when the this objects state has changed. */
    protected void notifyObservers() {
        os.notify(this, null);
    }

    /**
     * Method description
     *
     */
    public void removeAllObservers() {
        os.clear();
    }
    
    public TimeSource getTimeSource() {
        return timeSource;
    }

    /**
     * Method description
     *
     *
     * @param observer
     */
    public void removeObserver(IObserver observer) {
        os.remove(observer);
    }

    @Override
    public String toString() {
        return timecode == null ? Timecode.EMPTY_TIMECODE_STRING : timecode.toString();
    }

    /**
     *     Class that relays notification to registered observers when the
     *     Timecode object is updated.
     */
    private class Observer implements IObserver {

        /**
         * Method description
         *
         *
         * @param obj
         * @param changeCode
         */
        public void update(Object obj, Object changeCode) {
            notifyObservers();
        }
    }
    
    
}
