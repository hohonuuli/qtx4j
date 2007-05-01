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
TimeUtilTest.java
 *
Created on March 6, 2007, 10:56 AM
 *
To change this template, choose Tools | Template Manager
and open the template in the editor.
 */

package org.mbari.qt;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.time.TaskAllMovies;
import quicktime.std.StdQTConstants;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.DataRef;

/**
 *
 * @author brian
 */
public class TimeUtilTest {

    private static final Logger log = LoggerFactory.getLogger(TimeUtilTest.class);

    /** Creates a new instance of TimeUtilTest */
    public TimeUtilTest() {}

    private float estimateFrameRate(String url) throws QTException, MalformedURLException {
        Movie movie = QT.openMovieFromUrl(new URL(url));
        float frameRate = TimeUtil.estimateFrameRate(movie);
        return frameRate;
    }

    /**
     * Method description
     *
     */
    @Test()
    public void testMpeg() {
        String url = SupportUtil.getNetworkMovieFile();
        try {
            float frameRate = estimateFrameRate(url);
            Assert.assertEquals(SupportUtil.FRAMERATE_NETWORK, frameRate , 0.02);
        }
        catch (Exception e) {
            final String m = "Failed with movie " + url;
            log.debug(m, e);
            Assert.fail(m);
        }
    }
    
    @Test()
    public void testDefault() {
        String url = SupportUtil.getLocalMovieFile(SupportUtil.DEFAULT_MOVIE);
        try {
            float frameRate = estimateFrameRate(url);
            Assert.assertEquals(SupportUtil.FRAMERATE_DEFAULT, frameRate, 0.01);
        }
        catch (Exception e) {
            final String m = "Failed with movie " + url;
            log.debug(m, e);
            Assert.fail(m);
        }
    }
    
    @Test()
    public void testTimecode() {
        String url = SupportUtil.getLocalMovieFile(SupportUtil.TIMECODE_MOVIE);
        try {
            float frameRate = estimateFrameRate(url);
            Assert.assertEquals(SupportUtil.FRAMERATE_TIMECODE, frameRate, 0.01);
        }
        catch (Exception e) {
            final String m = "Failed with movie " + url;
            log.debug(m, e);
            Assert.fail(m);
        }
    }
}
