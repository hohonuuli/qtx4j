/*
 * Copyright 2007 MBARI
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.mbari.qt.examples;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.qt.QT;
import org.mbari.qt.awt.QTMovieFrame;
import quicktime.QTException;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.qd.QDColor;
import quicktime.qd.QDConstants;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.image.GraphicsMode;
import quicktime.std.image.Matrix;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.TimeCodeMedia;
import quicktime.std.qtcomponents.TCTextOptions;
import quicktime.std.qtcomponents.TimeCodeDef;
import quicktime.std.qtcomponents.TimeCodeDescription;
import quicktime.std.qtcomponents.TimeCodeTime;
import quicktime.std.qtcomponents.TimeCoder;
import quicktime.util.QTHandle;

/**
 *
 * @author brian
 */
public class TimeCodeTrackBuilder {
    
    private static final Logger log = LoggerFactory.getLogger(TimeCodeTrackBuilder.class);

    /** Field description */
    public static final int TIMECODE_TRACK_HEIGHT = 24;

    /** Field description */
    public static final int TIMECODE_TRACK_WIDTH = 120;

    /** Creates a new instance of TimeCodeTrackBuilder */
    public TimeCodeTrackBuilder() {}

    /**
     * Method description
     *
     *
     * @param movie
     *
     * @return
     *
     * @throws QTException
     */
    public static Track addTimeCodeTrack(Movie movie) throws QTException {
        int timescale = movie.getTimeScale();
        TimeCodeDef timeCodeDef = new TimeCodeDef();
        timeCodeDef.setTimeScale(2997);       // NTSC Drop Frame
        timeCodeDef.setFrameDuration(100);    // 1 frame in 30 fps dropframe
        timeCodeDef.setFramesPerSecond(30);
        timeCodeDef.setFlags(StdQTConstants.tcDropFrame);

        // First Record at 00:00:00:00 or 0 hours, 0 minutes, 0 seconds, 0 frames
        TimeCodeTime timeCodeTime = new TimeCodeTime(0, 0, 0, 0);

        // Create timecode track and media
        Track track = movie.addTrack(TIMECODE_TRACK_WIDTH, TIMECODE_TRACK_HEIGHT, 0);
        TimeCodeMedia timeCodeMedia = new TimeCodeMedia(track, timescale);
        TimeCoder timeCoder = timeCodeMedia.getTimeCodeHandler();

        // Turn on timecode display, set colors
        timeCoder.setFlags(timeCoder.getFlags() | StdQTConstants.tcdfShowTimeCode, StdQTConstants.tcdfShowTimeCode);
        TCTextOptions tcTextOptions = timeCoder.getDisplayOptions();
        tcTextOptions.setTXSize(14);
        tcTextOptions.setTXFace(QDConstants.bold);
        tcTextOptions.setForeColor(QDColor.yellow);
        tcTextOptions.setBackColor(QDColor.black);
        timeCoder.setDisplayOptions(tcTextOptions);

        // Setup a sample as a 4-byte array in a QTHandle
        int frameNumber = timeCoder.toFrameNumber(timeCodeTime, timeCodeDef);
        int[] frameNumbers = new int[1];
        frameNumbers[0] = frameNumber;
        QTHandle frameNumberHandle = new QTHandle(4, false);
        frameNumberHandle.copyFromArray(0, frameNumbers, 0, 1);

        // Create a timecode description (the sample to be added)
        TimeCodeDescription timeCodeDescription = new TimeCodeDescription();
        timeCodeDescription.setTimeCodeDef(timeCodeDef);

        // Add the sample to the TimeCodeMedia
        timeCodeMedia.beginEdits();
        timeCodeMedia.addSample(frameNumberHandle, 0, frameNumberHandle.getSize(), movie.getDuration(),
                                timeCodeDescription, 1, 0);
        timeCodeMedia.endEdits();

        // Insert this media into the track
        track.insertMedia(0,                              // trackStart
                          0,                              // mediaTime
                          timeCodeMedia.getDuration(),    // mediaDuration
                          1);                             // mediaRate
        
        /* 
         * Move the timecode to the bottom fo the movie and set a 
         * transparent background GraphicsMode
         */
        int x = (movie.getBox().getWidth() / 2 - TIMECODE_TRACK_WIDTH / 2);
        int y = (movie.getBox().getHeight() - TIMECODE_TRACK_HEIGHT);
        QDRect moveFrom = new QDRect(0, 0, TIMECODE_TRACK_WIDTH, TIMECODE_TRACK_HEIGHT);
        QDRect moveTo = new QDRect(x, y, TIMECODE_TRACK_WIDTH, TIMECODE_TRACK_HEIGHT);
        Matrix matrix = new Matrix();
        matrix.rect(moveFrom, moveTo);
        track.setMatrix(matrix);
        timeCoder.setGraphicsMode(new GraphicsMode(QDConstants.transparent, QDColor.black));
        
        return track;

    }

    // TODO implement this base on page 197 in QuickTime for Java Developer's Handbook

    /**
     * Method description
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            QT.manageSession();
            QTFile file = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
            OpenMovieFile omf = OpenMovieFile.asRead(file);
            Movie movie = Movie.fromFile(omf);
            addTimeCodeTrack(movie);
            
            // Create GUI
            final QTMovieFrame frame = new QTMovieFrame(movie);
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
            
        } catch (QTException e) {
            log.error("Failed", e);
        }
    }
}
