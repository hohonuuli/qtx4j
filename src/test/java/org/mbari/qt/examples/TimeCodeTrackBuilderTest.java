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
TimeCodeTrackBuilderTest.java
JUnit based test
 *
Created on February 26, 2007, 9:50 AM
 */

package org.mbari.qt.examples;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.mbari.qt.SupportUtil;
import org.mbari.qt.awt.QTMovieFrame;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.std.StdQTConstants;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.DataRef;

/**
 *
 * @author brian
 */
public class TimeCodeTrackBuilderTest {
    
    private static final Logger log = LoggerFactory.getLogger(TimeCodeTrackBuilderTest.class);

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    private void run(String urlPath) throws QTException, InterruptedException {
        QTSession.open();
        DataRef dataRef = new DataRef(urlPath);

        // Create a QTMovieFrame
        Movie movie = Movie.fromDataRef(dataRef, StdQTConstants.newMovieActive);

        TimeCodeTrackBuilder.addTimeCodeTrack(movie);

        // Create GUI
        //MoviePlayer moviePlayer = new MoviePlayer(movie);
        final QTMovieFrame frame = new QTMovieFrame(movie);
        frame.setVisible(true);
        movie.start();
        Thread.sleep(3000);
        movie.stop();
        frame.setVisible(true);
        frame.dispose();
        
    }

    /**
     * Method description
     *
     */
    @Test 
    @Ignore
    public void testAllMovies() {
        //String[] urls = SupportUtil.getTestMovies();
        String[] urls = {SupportUtil.getLocalMovieFile(SupportUtil.DEFAULT_MOVIE)};
        for (String url : urls) {
            try {
                log.debug("Testing " + url);
                run(url);
            }
            catch (Exception e) {
                log.error("Test Failed", e);
                Assert.fail("Test failed on " + url);
            }
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TimeCodeTrackBuilderTest.class);
    }
}
