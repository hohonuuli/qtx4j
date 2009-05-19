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
QTTimecode.java
 *
Created on February 26, 2007, 4:27 PM
 *
To change this template, choose Tools | Template Manager
and open the template in the editor.
 */

package org.mbari.qt;

import org.mbari.movie.Timecode;
import org.mbari.movie.Timecode.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.std.StdQTConstants;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.TimeCodeMedia;
import quicktime.std.qtcomponents.TimeCodeDef;
import quicktime.std.qtcomponents.TimeCodeDescription;
import quicktime.std.qtcomponents.TimeCodeInfo;
import quicktime.std.qtcomponents.TimeCoder;

/**
 *
 * @author brian
 */
public class QTTimecode extends Timecode {

    private static final Logger log = LoggerFactory.getLogger(QTTimecode.class);
    private final TimeCodeMedia timeCodeMedia;
    private final TimeCoder timeCoder;

    /**
     * Creates a new instance of QTTimecode 
     *
     * @param movie
     *
     * @throws QT4JException
     * @throws QTException
     */
    public QTTimecode(final Movie movie) throws QTException, QT4JException {
        final Track timecodeTrack = movie.getIndTrackType(1, StdQTConstants.timeCodeMediaType,
                                        StdQTConstants.movieTrackMediaType);

        if (timecodeTrack == null) {
            throw new QT4JException("No timecode track was found on " + QT.resolveName(movie));
        }
        

        setRepresentation(Representation.TIMECODE);
        //final TimeCodeMedia timeCodeMedia = new TimeCodeMedia(timecodeTrack, movie.getTimeScale());
        timeCodeMedia = (TimeCodeMedia) timecodeTrack.getMedia();
        /*
         * Search for the timeCodeDescription. We don't know the index so we
         * have to quess. TODO is this correct?
         */
        TimeCodeDescription timeCodeDescription = null;
        for (int i = 0; i < 10; i++) {
            try {
                timeCodeDescription = timeCodeMedia.getTimeCodeDescription(i);
                if (timeCodeDescription != null) {
                    break;
                }
            } catch (QTException ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Call to timeCodeMedia.getTimeCodeDescription(" +
                            i + ") failed");
                }
            }
        }
        
        if (timeCodeDescription == null) {
            throw new QT4JException("Unable to find TimeCodeDescription for TimeCode track in " +
                    QT.resolveName(movie));
        }

        final TimeCodeDef timeCodeDef = timeCodeDescription.getTimeCodeDef();
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Timecode track settings from movie: ");
            sb.append("timeScale = ").append(timeCodeDef.getTimeScale());
            sb.append("frameDuration = ").append(timeCodeDef.getFrameDuration());
            sb.append("framesPerSecond = ").append(timeCodeDef.getFramesPerSecond());
        }
        setFrameRate(timeCodeDef.getFramesPerSecond());
        timeCoder = timeCodeMedia.getTimeCodeHandler();

        updateTimecode();
    }

    /**
     * Method description
     *
     *
     * @throws QTException
     */
    public void updateTimecode() throws QTException {
        QT.manageSession();
        try {
            TimeCodeInfo timeCodeInfo = timeCoder.getCurrent();
            final long frame = timeCoder.toFrameNumber(timeCodeInfo.time, timeCodeInfo.definition);
            setFrames(frame);
        }
        catch (QTException e) {
            log.warn("Could not read timecode data", e);
        }
    }
   
}
