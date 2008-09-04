/*
 * Copyright 2007 MBARI
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.mbari.qt.awt;

import junit.framework.JUnit4TestAdapter;
import junit.textui.TestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mbari.qt.SupportUtil;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.std.StdQTConstants;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.DataRef;

/**
 * @author brian
 * @version $Id: QTMovieFrameTest.java 209 2007-03-01 19:15:59Z brian $
 * @since Jan 23, 2007 11:34:45 AM PST
 */
public class QTMovieFrameTest {

    private static final Logger log = LoggerFactory.getLogger(QTMovieFrameTest.class);

    /**
     * This test creates a QTMovieFrame, loads a file from a local URL, plays it for a few seconds then shuts down
     *
     * @param url
     *
     * @throws InterruptedException
     * @throws QTException
     */
    public void run(String url) throws QTException, InterruptedException {


        QTSession.open();
        DataRef dataRef = new DataRef(url);

        /*
         * QTConstants can be combined using OR (i.e. '|')
         */
        Movie movie = Movie.fromDataRef(dataRef, StdQTConstants.newMovieActive);

        QTMovieFrame movieFrame = new QTMovieFrame(movie);
        movieFrame.setVisible(true);
        movie.start();
        Thread.sleep(3000);
        movie.stop();
        movieFrame.setVisible(false);
        QTSession.close();
        movieFrame.dispose();

    }

    /**
     * Method description
     *
     */
    @Test 
    public void testAllMovies() {
        String[] urls = SupportUtil.getTestMovies();
        for (String url : urls) {
            try {
                run(url);
            }
            catch (Exception e) {
                final String m = "Failed with movie " + url;
                log.debug(m, e);
                Assert.fail(m);
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(QTMovieFrameTest.class);
    }
}
