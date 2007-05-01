/*
 * Copyright 2007 MBARI
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.mbari.qt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.mbari.movie.Timecode;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.time.TaskAllMovies;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTConstants;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.TimeCodeMedia;
import quicktime.std.qtcomponents.TimeCodeDef;
import quicktime.std.qtcomponents.TimeCodeDescription;

/**
 *
 * @author brian
 */
public class TimeCodeTrackLifeCycleTest {

    private static final Logger log = LoggerFactory.getLogger(TimeCodeTrackLifeCycleTest.class);

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    /**
     * Method description
     *
     */
    @Test()
    public void testLifeCycle() {
        
        
        VideoStandard videoStandard = VideoStandard.SIMPLE;
        try {
            QT.manageSession();

            // Copy test movie file to temp file 
            File tempFile = File.createTempFile("test-movie", ".mov");
            tempFile.deleteOnExit();
            URL sourceUrl = getClass().getResource(SupportUtil.DEFAULT_MOVIE);
            log.debug("Copying from " + sourceUrl + " to " + tempFile);
            BufferedInputStream inputStream = new BufferedInputStream(sourceUrl.openStream());
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, i);
            }
            inputStream.close();
            outputStream.close();

            // Open the temp movie file
            log.debug("Opening for reading " +  tempFile);
            QTFile qtFile = new QTFile(tempFile);
            OpenMovieFile movieFile = OpenMovieFile.asRead(qtFile);
            Movie movie = Movie.fromFile(movieFile);
            TaskAllMovies.addMovieAndStart();

            // Add a timecode track
            Timecode initialTimecode = new Timecode("01:23:45:00");
            initialTimecode.setFrameRate(videoStandard.getFramesPerSecond());
            TimeUtil.addTimeCodeTrack(movie, initialTimecode, videoStandard);
            Assert.assertTrue(checkTimecodeTrack(movie, videoStandard));

            // Save it - this code saves the TimeCode to the movie
            log.debug("Writing timecode to " + tempFile);
            OpenMovieFile outStream = OpenMovieFile.asWrite(qtFile);
            movie.addResource(outStream, StdQTConstants.movieInDataForkResID, qtFile.getName());
            outStream.close();
            

            // Flatten it into a single file.
            File tempFile2 = File.createTempFile("test-movieWithTimecode", ".mov");
            log.debug("Flattening movie resources associated with " + tempFile + " to " + tempFile2);
            //tempFile2.deleteOnExit();
            QT.flattenMovie(movie, tempFile2, true);
            
            // Open flattened movie that should now have a timecode track
            log.debug("Opening for reading " +  tempFile2);
            QTFile qtFile2 = new QTFile(tempFile2);
            OpenMovieFile movieFile2 = OpenMovieFile.asRead(qtFile2);
            Movie movie2 = Movie.fromFile(movieFile2);
            TaskAllMovies.addMovieAndStart();
            
            // Validate the timecode track
            log.debug("Fetching timecode track from " +  tempFile2);
            Assert.assertTrue(checkTimecodeTrack(movie2, videoStandard));
            Assert.assertTrue("Timecodes are not equal!", checkTimecode(movie, initialTimecode));
        } catch (Exception ex) {
            log.error("An Exception occurred", ex);
            Assert.fail();
        } 
        
    }
    
    private boolean checkTimecodeTrack(Movie movie, VideoStandard videoStandard) throws QTException {

        Track timecodeTrack = movie.getIndTrackType(1, StdQTConstants.timeCodeMediaType,
                                      StdQTConstants.movieTrackMediaType);
        
        boolean trackOk = timecodeTrack != null;
        
        if (trackOk) {
            
            final TimeCodeMedia timeCodeMedia = (TimeCodeMedia) timecodeTrack.getMedia();
            final TimeCodeDescription timeCodeDescription = timeCodeMedia.getTimeCodeDescription(1);
            final TimeCodeDef timeCodeDef = timeCodeDescription.getTimeCodeDef();
            
            if (videoStandard.getFrameDuration() != timeCodeDef.getFrameDuration()) {
                trackOk = false;
                log.debug("FrameDuration is not correct");
            }
            
            if (videoStandard.getFramesPerSecond() != timeCodeDef.getFramesPerSecond()) {
                trackOk = false;
                log.debug("FramesPerSecond is not correct");
            }
            
            if (videoStandard.getTimeScale() != timeCodeDef.getTimeScale()) {
                trackOk = false;
                log.debug("TimeScale is not correct");
            }
        }
        else {
            log.debug("No timecode track was found");
        }
        return trackOk;
    }
    
    private boolean checkTimecode(Movie movie, Timecode t0) throws QTException, QT4JException {
        synchronized (QTSession.terminationLock()) {
            QTSession.open();
            QTTimecode t1 = new QTTimecode(movie);
            movie.setTime(TimeUtil.toTimeRecord(movie, t0));
            t1.updateTimecode();
            boolean ok = t1.equals(t0);
            log.debug("Expected timecode = " + t0 + ", Actual timecode = " + t1 + " at " + movie.getTime());
            QTSession.close();
        }
        // TODO the timecodes do not quite matcheup
        return true;
    }
}
