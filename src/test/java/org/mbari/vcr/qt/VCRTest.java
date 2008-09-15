/*
 * Copyright 2007 MBARI
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.mbari.vcr.qt;

import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.JUnit4TestAdapter;
import junit.textui.TestRunner;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mbari.movie.Timecode;
import org.mbari.qt.QT;
import org.mbari.qt.QT4JException;
import org.mbari.qt.SupportUtil;
import org.mbari.qt.TimeUtil;
import org.mbari.qt.awt.QTMovieFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.std.movies.Movie;

/**
 * @author brian
 * @version $Id: $
 * @since Feb 21, 2007 4:47:28 PM PST
 */
public class VCRTest {

    private static final Logger log = LoggerFactory.getLogger(VCRTest.class);

    /**
     * Method description
     *
     *
     * @param urlPath
     *
     *
     * @throws InterruptedException
     * @throws MalformedURLException
     * @throws QTException
     * @throws org.mbari.qt.QT4JException 
     */
    public void run(String urlPath) throws InterruptedException, MalformedURLException, QT4JException, QTException {
        QT.manageSession();


        // Create movie reference from URL
        // Create a QTMovieFrame
        Movie movie = QT.openMovieFromUrl(new URL(urlPath));

        // Create a QTMovieFrame
        QTMovieFrame movieFrame = new QTMovieFrame(movie);
        movieFrame.setVisible(true);

        //  Add movie to VCR
        VCR vcr = new VCR(movie);

        // control movie though VCR
        vcr.play();
        Thread.sleep(1000);
        vcr.fastForward();
        Thread.sleep(750);
        vcr.rewind();
        Thread.sleep(750);
        double frameRate = TimeUtil.estimateFrameRate(movie);
        Timecode timecode = new Timecode(frameRate * 15, frameRate);
        vcr.stop();
        vcr.seekTimecode(timecode);
        Thread.sleep(750);
        vcr.play();
        Thread.sleep(750);

        for (int i = 1; i < 255; i += 25) {
            vcr.shuttleForward(i);
            Thread.sleep(750);
        }

        for (int i = 1; i < 255; i += 25) {
            vcr.shuttleReverse(i);
            Thread.sleep(750);
        }

        vcr.stop();
        movieFrame.setVisible(false);
        movieFrame.dispose();


    }

    /**
     * Method description
     *
     */
    @Test 
    @Ignore
    public void testAllMovies() {
        String[] urls = {SupportUtil.getLocalMovieFile(SupportUtil.TIMECODE_MOVIE),
        SupportUtil.getLocalMovieFile(SupportUtil.DEFAULT_MOVIE)};
        for (String url : urls) {
            try {
                run(url);
            }
            catch (Exception e) {
                Assert.fail("Test failed on " + url);
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
        return new JUnit4TestAdapter(VCRTest.class);
    }
}
