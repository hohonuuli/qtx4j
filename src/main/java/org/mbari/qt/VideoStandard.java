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

import quicktime.std.StdQTConstants;
import quicktime.std.qtcomponents.TimeCodeDef;

/**
 *
 * @author brian
 */
public enum VideoStandard {

    SIMPLE(StdQTConstants.tc24HourMax, 3000, 100, 30),                               // 30fps
    NTSC(StdQTConstants.tc24HourMax | StdQTConstants.tcDropFrame, 2997, 100, 30),    // 29.97 fps
    PAL(StdQTConstants.tc24HourMax, 2500, 100, 25),                                  // 25 fps
    SECAM(StdQTConstants.tc24HourMax, 2500, 100, 25);                                // 25 fps

    private final int flags;
    private final int frameDuration;
    private final int framesPerSecond;
    private final int timeScale;

    VideoStandard(int flags, int timeScale, int frameDuration, int framesPerSecond) {
        this.flags = flags;
        this.timeScale = timeScale;
        this.frameDuration = frameDuration;
        this.framesPerSecond = framesPerSecond;
    }

    /**
     * Returns a TimeCodeDef object suitable for creating a TimeCodeTrack
     *
     * @return
     */
    public TimeCodeDef createTimeCodeDef() {
        TimeCodeDef timeCodeDef = new TimeCodeDef();
        timeCodeDef.setFlags(flags);
        timeCodeDef.setTimeScale(getTimeScale());
        timeCodeDef.setFrameDuration(getFrameDuration());
        timeCodeDef.setFramesPerSecond(getFramesPerSecond());
        return timeCodeDef;
    }

    public int getFrameDuration() {
        return frameDuration;
    }

    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    public int getTimeScale() {
        return timeScale;
    }
}
