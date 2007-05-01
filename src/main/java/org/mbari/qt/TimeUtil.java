/*
 * TimeUtil.java
 *
 * Created on February 27, 2007, 10:30 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.qt;

import org.mbari.movie.Timecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.qd.QDDimension;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.std.clocks.TimeRecord;
import quicktime.std.comp.ComponentDescription;
import quicktime.std.movies.Movie;
import quicktime.std.movies.TimeInfo;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.Media;
import quicktime.std.movies.media.MediaHandler;
import quicktime.std.movies.media.TimeCodeMedia;
import quicktime.std.qtcomponents.TimeCodeDef;
import quicktime.std.qtcomponents.TimeCodeDescription;
import quicktime.std.qtcomponents.TimeCodeInfo;
import quicktime.std.qtcomponents.TimeCodeTime;
import quicktime.std.qtcomponents.TimeCoder;
import quicktime.util.EndianOrder;
import quicktime.util.QTHandle;
import quicktime.util.QTUtils;

/**
 *
 * @author brian
 */
public class TimeUtil {
    
    private static final Logger log = LoggerFactory.getLogger(TimeUtil.class);
    
    /** Creates a new instance of TimeUtil */
    private TimeUtil() {
        // No instantiation
    }
    
     /**
     * Method description
     *
     *
     * @param movie
     * @param initialTimecode
     * @param videoStandard
     *
     * @return
     *
     * @throws QTException
     */
    public static Track addTimeCodeTrack(final Movie movie, final Timecode initialTimecode,
            final VideoStandard videoStandard)
            throws QTException {

        QT.manageSession();
        Track timecodeTrack = movie.getIndTrackType(1, StdQTConstants.timeCodeMediaType,
                                  StdQTConstants.movieTrackMediaType);

        if (timecodeTrack != null) {
            log.info("A Timecode track already exists in " + movie.getDefaultDataRef().getUniversalResourceLocator());
        }
        else {

            // Get the (first) visual track; this track determines the width of the new timecode track
            Track visualTrack = movie.getIndTrackType(1, StdQTConstants.visualMediaCharacteristic,
                                    StdQTConstants.movieTrackCharacteristic);

            // Create the timecode track and media
            if (visualTrack == null) {
                log.info("No visual track exists in " + movie.getDefaultDataRef().getUniversalResourceLocator());
            }
            else {

                // Get movie and track attributes
                int movieTimeScale = movie.getTimeScale();

                //Media theVisualTrackMedia = Media.fromTrack(visualTrack);
                QDDimension dim = visualTrack.getSize();

                timecodeTrack = movie.newTrack((float) dim.getWidth(), (float) dim.getHeight(), 0);
                TimeCodeMedia timeCodeMedia = new TimeCodeMedia(timecodeTrack, movieTimeScale);
                TimeCoder timeCoder = timeCodeMedia.getTimeCodeHandler();

                // Set up a TimeCodeDef
                TimeCodeDef timeCodeDef = videoStandard.createTimeCodeDef();

                // Make sure that the initial timecode has the same framerate.
                initialTimecode.setFrameRate(timeCodeDef.getFramesPerSecond());

                // Start the timecode at 0:0:0:0
                TimeCodeTime timeCodeTime = new TimeCodeTime(initialTimecode.getHour(), initialTimecode.getMinute(),
                                                initialTimecode.getSecond(), initialTimecode.getFrame());

                /*
                 * Add a sample to the timecode track
                 *
                 * Each sample in a timecode track provides timecode information for a span of movie time;
                 * here, we add a single sample that spans the entire movie duration
                 *
                 * The sample data contains a frame number that identifies one or more content frames
                 * that use the timecode; this value (a long integer) identifies the first frame that
                 * uses the timecode.  For our purposes this will probably always be zero, but it can't
                 * hurt to go the full 9.
                 */
                int frameNumber = timeCoder.toFrameNumber(timeCodeTime, timeCodeDef);
                
                /*
                 * The data in the timecode track must be big-endian. If the 
                 * EndianOrder flip is not used here, this will work on G5 Macs
                 * (big-endian) but won't on Windows or Intel Macs (little-endian)
                 */
                int[] frameNumberAr = { EndianOrder.flipNativeToBigEndian32(frameNumber) };
                QTHandle frameNumHandle = new QTHandle(4, false);
                frameNumHandle.copyFromArray(0, frameNumberAr, 0, 1);

                // Create and configure a new timecode description
                TimeCodeDescription timeCodeDescription = new TimeCodeDescription();
                timeCodeDescription.setTimeCodeDef(timeCodeDef);

                /*
                 * Edit the track media
                 *
                 * Since we created the track with the same timescale as the movie,
                 * we don't need to convert the duration
                 */
                timeCodeMedia.beginEdits();
                timeCodeMedia.addSample(frameNumHandle, 0, frameNumHandle.getSize(), movie.getDuration(),
                                        timeCodeDescription, 1, 0);
                timeCodeMedia.endEdits();
                timecodeTrack.insertMedia(0, 0, movie.getDuration(), 1.0F);
            }
        }

        return timecodeTrack;
    }
    
    /**
     * This is from http://lists.apple.com/archives/quicktime-java/2005/Jun/msg00054.html
     *
     * <quote><p>Finally found some relevant documentation on ADC. This is from
     * http://developer.apple.com/documentation/QuickTime/RM/Fundamentals/QTOverview/index.html.</p>
     *
     * <p>Because each chunk of sample data has its own duration, and a chunk can be as small as a single sample, a
     * QuickTime track may not have any fixed "frame rate." A video track might consist of a series of images that act
     * as a sideshow, for example, with each "slide" on screen for a different length of time.</p>
     *
     * <p>This can be very difficult to grasp if you are used to working in media with fixed frame rates, but it is a
     * powerful feature of QuickTime. A fixed frame rate would require images to be repeated periodically, perhaps many
     * times, to display them on screen for an extended period; in QuickTime, each image can be stored as a single
     * sample with its own unique duration.</p>
     *
     * <p>By extension, a QuickTime movie does not necessarily have a fixed frame rate. A 25-fps PAL video track may
     * play side by side with a 30-fps NTSC video track in the same movie, for example, perhaps with both tracks
     * composited on top of a still image that is displayed for the entire duration of the movie, or on top of a
     * "sideshow" track that changes at irregular intervals. This is possible because the display is created at runtime
     * by a programmable device, not mechanically projected by display hardware.</p>
     *
     * <p>Of course, a QuickTime track, or a QuickTime movie, may have a frame rate; it is very common for a video track
     * to contain a series of samples that all have the same duration, and it is also common for a movie to have a
     * single video track with a constant sample rate. But it is not a requirement.</p>
     *
     * <p>You can always compute a frame rate by dividing the duration of a track by the total number of video samples,
     * but be aware that the results of this calculation are not always predictive of the movie's behavior; the actual
     * frame rate could change abruptly at several points during the movie.</p></quote>
     *
     * @param movie
     * @return An estimate of the frame rate (in frames per second) for the visual movie track
     * @throws QTException
     */
    public static final float estimateFrameRate(final Movie movie) throws QTException {
        QT.manageSession();
        final Track videoTrack = movie.getIndTrackType(1, StdQTConstants.visualMediaCharacteristic,
                                     StdQTConstants.movieTrackCharacteristic);
        final Media media = videoTrack.getMedia();
        final double duration = media.getDuration(); // Units per movie
        final double timeScale = media.getTimeScale(); // Units per second
        double frameRate = -1;
                
        if (QT.isMpegMovie(movie)) {
            log.debug("Movie Duration = " + movie.getDuration() + ": Movie timeScale = " + movie.getTimeScale());
            // Trying to get framerate will hand a playing MPEG movie
            if (movie.getRate() != 0) {
                throw new QTException("Can't estimate frame rate from a playing MPEG movie");
            }
            TimeInfo timeInfo = new TimeInfo(0, 0);
            timeInfo = movie.getNextInterestingTime(StdQTConstants.nextTimeStep, null, timeInfo.time, 1.0f);
            log.debug(movie.getDefaultDataRef().getUniversalResourceLocator() + " is an MPEG." +
                    "timeInfo.time = " + timeInfo.time + " timeInfo.duration = " + timeInfo.duration);
            frameRate = 1.0 / (timeInfo.duration / timeScale);
        }
        else {
            final double sampleCount = media.getSampleCount(); // Frames per Movie
            frameRate = timeScale * sampleCount / duration;
        }

        return (float) frameRate;

    }
    
    
    
        /**
     * Gets the current runtime of the movie. i.e. A formated object that displays the current frame of the movie that
     * is supplied. The proper usage of this is:
     * <pre>
     * // Movie movie = ... <-- initialize movie somewhere
     * Timecode runtime = new Timecode();
     * // Set frame rate (frames per second)
     * runtime.setFrameRate(QT.estimateFrameRate(movie));
     * // play the movie and everyonce in the while call this method
     * // This will set the values of the runtime object to the current
     * // runtime of the movie.
     * QT.toRuntime(movie, runtime);
     * </pre>
     *
     * @param movie The movie whos runtime we want to track.
     * @param runtime The Timecode object representing the runtime
     * @throws QTException Thrown if QT barfs.
     */
    public static void queryRuntime(final Movie movie, final Timecode runtime) throws QTException {
        QT.manageSession();
        final int totalSeconds = movie.getTime() / movie.getTimeScale();
        final double frames = totalSeconds * runtime.getFrameRate();
        runtime.setFrames(frames);
    }

    /**
     * Gets the current timecode of the movie (if a timecode track is available). The proper usage of this is:
     * <pre>
     * // Movie movie = ... <-- initialize movie somewhere
     * Timecode timecode = new Timecode();
     * // Set frame rate (frames per second)
     * timecode.setFrameRate(QT.estimateFrameRate(movie));
     * // play the movie and everyonce in the while call this method
     * // This will set the values of the timecode object to the current
     * // timecode of the movie.
     * QT.toTimecode(movie, timecode);
     * </pre>
     *
     * @param movie The movie whos runtime we want to track.
     * @param timecode The Timecode object representing the runtime
     * @throws QTException Thrown if QT barfs.
     * @see QTTimecode
     */
    public static void queryTimecode(final Movie movie, final Timecode timecode) throws QTException {
        QT.manageSession();
        Track timecodeTrack = movie.getIndTrackType(1, StdQTConstants.timeCodeMediaType,
                                  StdQTConstants.movieTrackMediaType);
        

        if (timecodeTrack == null) {
            if (log.isDebugEnabled()) {
                log.debug("No timecode track was found in " + movie.getDefaultDataRef().getUniversalResourceLocator());
            }
            timecode.setTimecode(Timecode.EMPTY_TIMECODE_STRING);
        }
        else {
            final TimeCodeMedia timeCodeMedia = new TimeCodeMedia(timecodeTrack, movie.getTimeScale());
            final TimeCodeDescription timeCodeDescription = timeCodeMedia.getTimeCodeDescription(1);
            final TimeCodeDef timeCodeDef = timeCodeDescription.getTimeCodeDef();
            timecode.setFrameRate(timeCodeDef.getFramesPerSecond());
            final TimeCoder timeCoder = timeCodeMedia.getTimeCodeHandler();
            final TimeCodeInfo timeCodeInfo = timeCoder.getCurrent();
            long frame = timeCoder.toFrameNumber(timeCodeInfo.time, timeCodeInfo.definition);
            timecode.setFrames(frame);
        }
    }

    public static boolean hasTimeCodeTrack(Movie movie) throws QTException {
        QT.manageSession();
        Track timecodeTrack = movie.getIndTrackType(1, StdQTConstants.timeCodeMediaType,
                                  StdQTConstants.movieTrackMediaType);
        return timecodeTrack != null;
    }

    /**
     * Convert a timecode to a movie <i>time</i>> Use as:
     * <pre>
     *  Movie movie = ...
     *  Timecode timecode = new Timecode();
     *  timecode.setFrameRate(QT.estimateFrameRate(movie));
     *  timecode.setFrames(234); // Position to seek to
     *  long movieTime = QT.toTime(movie, timecode);
     * @param movie The movie that you want to find an index into
     * @param timecode The timecode that you want to get time for
     * @return The movie time to seek to in the movies time-scale units
     *
     * @throws StdQTException
     */
    public static long toTime(final Movie movie, final Timecode timecode) throws QTException {
        QT.manageSession();
        final double timeInSeconds = timecode.getFrames() / timecode.getFrameRate();

        return Math.round(timeInSeconds * movie.getTimeScale());
    }

    /**
     * Method description
     *
     *
     * @param movie
     * @param timecode
     *
     * @return
     *
     * @throws QTException
     */
    public static TimeRecord toTimeRecord(final Movie movie, final Timecode timecode) throws QTException {
        QT.manageSession();
        return new TimeRecord(movie.getTimeScale(), toTime(movie, timecode));
    }
    
    public static int timecodeToTime(final Movie movie, final Timecode timecode) throws QTException {
        // TODO implement: Look at Track.trackTimeToMediaTime(int) seconds = 
        return 0;
    }
    
}
