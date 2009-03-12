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

import java.awt.Frame;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.mbari.qt.awt.QTMovieFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.io.IOConstants;
import quicktime.io.QTFile;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTConstants4;
import quicktime.std.StdQTException;
import quicktime.std.comp.ComponentDescription;
import quicktime.std.comp.ComponentIdentifier;
import quicktime.std.image.ImageDescription;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.DataRef;
import quicktime.std.movies.media.Media;
import quicktime.std.movies.media.MediaHandler;
import quicktime.std.movies.media.SampleDescription;
import quicktime.std.qtcomponents.MovieExporter;
import quicktime.util.QTUtils;

/**
 * Convienince utilities for working with QuickTime for java
 * @author brian
 */
public class QT {

    private static final Logger log = LoggerFactory.getLogger(QT.class);
    private static QT instance;

    /** Creates a new instance of QTSessionCheck */
    QT() throws QTException {
        super();
        QTSession.open();

        // create shutdown handler
        final Thread shutdownHook = new Thread() {
            @Override
            public void run() {
                try {
                    QTSession.close();
                }
                catch (Exception e) {
                    log.error("An error occurred while closing the QuickTime session", e);
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Export a movie using the specified export format. This method shows some user interface elements such as user
     * settings, file to save to and a progress bar. SInce a Movie object is supplied as an argument this methods assumes that you are
     * already working in an open quicktime session (either using QTSession.open() or QT.manageSession())
     *
     * @param movie The movie to convert
     * @param file
     * @param componentIdentifier The componenet used for the export
     * @throws QTException if something bad happends.
     */
    public static void exportMovie(Movie movie, File file, ComponentIdentifier componentIdentifier) throws QTException {
        exportMovie(movie, file, componentIdentifier.getInfo().getSubType());
    }

    /**
     * Export a movie using the specified export format. This method shows some
     * user interface elements such as user settings, file to save to and a
     * progress bar. Since a Movie object is supplied as an argument this
     * methods assumes that you are already working in an open quicktime session
     * (either using QTSession.open() or QT.manageSession())
     *
     * @param movie The movie to convert
     * @param file
     * @param subType The format to save it as. Such as componentIdentifier.getInfo().getSubType();
     * @throws QTException if something bad happends.
     */
    public static void exportMovie(Movie movie, File file, int subType) throws QTException {
        manageSession();
        MovieExporter exporter = new MovieExporter(subType);
        QTFile qtFile = new QTFile(file);
        movie.setProgressProc();

        /*
         * convertToFile([Track to export, null for all tracks],
         *               [QTFile to export to],
         *               [A file type],
         *               [A creator],
         *               [A script tag, this is typically IOConstants.smSystemScript],
         *               [Behavior flags],
         *               [The movie exporter used to export]
         */
        movie.convertToFile(null, 
                qtFile, 
                StdQTConstants.kQTFileTypeMovie, 
                StdQTConstants.kMoviePlayer,
                IOConstants.smSystemScript,
                (StdQTConstants.movieToFileOnlyExport | StdQTConstants.movieFileSpecValid), 
                exporter);
        // The lines below show an export settings dialog
//        movie.convertToFile(null, qtFile, StdQTConstants.kQTFileTypeMovie, StdQTConstants.kMoviePlayer,
//                            IOConstants.smSystemScript,
//                            (StdQTConstants.showUserSettingsDialog | StdQTConstants.movieToFileOnlyExport
//                             | StdQTConstants.movieFileSpecValid), exporter);
        exporter.disposeQTObject();
    }

    /**
     * Export a movie into a single (flat) file. Also configures the movie for
     * <i>fast start</i>
     *
     * @param movie The movie to flatten
     * @param file The target that the flattened movie will be saved to. If it
     *      already exists and exception will be thrown
     * @param overwrite
     *
     *
     * @throws FileExistsException
     * @throws QTException
     */
    public static void flattenMovie(Movie movie, File file, boolean overwrite) throws FileExistsException, QTException {
        if (!overwrite && file.exists()) {
            throw new FileExistsException("File " + file.getAbsolutePath() + " exists. You must delete if first.");
        }

        manageSession();
        QTFile qtFile = new QTFile(file);
        movie.setProgressProc();
        movie.flatten((StdQTConstants.flattenAddMovieToDataFork
                       | StdQTConstants.flattenForceMovieResourceBeforeMovieData), 
                      qtFile,    // file out
                      StdQTConstants.kMoviePlayer,                    // creator
                      IOConstants.smSystemScript,                     // scriptTag
                      (StdQTConstants.createMovieFileDeleteCurFile
                      | StdQTConstants.createMovieFileDontCreateResFile),    // Delete existing file at target location
                      StdQTConstants.movieInDataForkResID,            // resId
                      null);                                          // resName
    }
        

    private static QT getInstance() throws QTException {
        if (instance == null) {
            instance = new QT();
        }

        return instance;
    }

    /**
     * @see https://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap3/chapter_4_section_2.html#//apple_ref/doc/uid/TP40000939-CH205-74522
     *
     * @param movie
     *
     * @return
     *
     * @throws QTException
     */
    public static boolean isMpegMovie(Movie movie) throws QTException {
        manageSession();
        final Track videoTrack = movie.getIndTrackType(1, StdQTConstants.visualMediaCharacteristic,
                                     StdQTConstants.movieTrackCharacteristic);
        final Media media = videoTrack.getMedia();
        final MediaHandler mediaHandler = media.getHandler();
        final ComponentDescription info = mediaHandler.getInfo();
        if (log.isDebugEnabled()) {
            log.debug(resolveName(movie) + " is decoded by '" + info.getName() +
                      "' [type = " + QTUtils.fromOSType(info.getType()) + ": subtype = " +
                      QTUtils.fromOSType(info.getSubType()) + "]");
        }

        boolean isMpeg = false;
        SampleDescription sd = media.getSampleDescription(1);
        if (sd instanceof ImageDescription) {
            ImageDescription id = (ImageDescription) sd;
            int ctype = id.getCType();
            String cName = QTUtils.fromOSType(ctype);
            if (log.isDebugEnabled()) {
                log.debug(resolveName(movie) + " compresses it's image frames using " + cName);
            }
            isMpeg = cName.equalsIgnoreCase("mpeg");
        }
        else {
            isMpeg = QTUtils.fromOSType(info.getSubType()).equalsIgnoreCase("m1v ") || 
                    info.getSubType() == StdQTConstants.MPEGMediaType;
        }
        
        if (log.isDebugEnabled() && isMpeg) {
            log.debug(resolveName(movie) + " is an MPEG.");
        }

        return isMpeg;

    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws QTException
     */
    public static Set<ComponentIdentifier> listAllComponentIdentifiers() throws QTException {
        manageSession();
        Set<ComponentIdentifier> identifiers = new HashSet<ComponentIdentifier>();
        ComponentDescription description = new ComponentDescription();
        ComponentIdentifier identifier = null;
        while ((identifier = ComponentIdentifier.find(identifier, description)) != null) {
            identifiers.add(identifier);

            if (log.isInfoEnabled()) {
                ComponentDescription cd = identifier.getInfo();
                log.info("Found " + cd.getName() + " (" + QTUtils.fromOSType(cd.getType()) + "/" +
                         QTUtils.fromOSType(cd.getSubType()) + ") ");
            }
        }

        return identifiers;
    }

    /**
     * Retrieve a Set of ComponentIdentifiers that can be used to export a particular movie. The resulting formats can be
     * used as follows:
     * <pre>
     * // Get all the exportable formats
     * Set<ComponentIdentifier> set = QT.listMovieExportComponents(movie);
     * // Let's use the first format for exporting
     * ComponentIdentifier choice = set.iterator().next();
     * // Export to 'somefile'
     * MovieExporter exporter = new MovieExporter(choice.getInfo().getSubType());
     * QTFile qtFile = new QTFile(new File("somefile"));
     * movie.setProgressProc();
     * movie.convertToFile(null,
     *                     qtFile,
     *                     StdQTConstants.kQTFileTypeMovie,
     *                     StdQTConstants.kMoviePlayer,
     *                     IOConstants.smSystemScript,
     *                     StdQTConstants.showUserSettngsDialog |
     *                     StdQTConstants.movieToFileOnlyExport |
     *                     StdQTConstants.movieFileSpecValid,
     *                     exporter);
     *
     * </pre>
     *
     * This method assumes that you've already started a session, either by using QTSession.open() or QT.manageSession()
     *
     * @param movie The source movie that you want to save in a different format
     * @return A set of exceptable export formats. An empty set is returned if none are found
     * @throws QTException If something bombs.
     */
    public static Set<ComponentIdentifier> listMovieExportComponentIdentifiers(Movie movie) throws QTException {
        manageSession();
        Set<ComponentIdentifier> identifiers = new HashSet<ComponentIdentifier>();
        ComponentDescription description = new ComponentDescription(StdQTConstants.movieExportType);
        ComponentIdentifier identifier = null;
        while ((identifier = ComponentIdentifier.find(identifier, description)) != null) {

            /*
             * Check that the movie can be exported with this component.
             */
            try {
                MovieExporter movieExporter = new MovieExporter(identifier);
                if (movieExporter.validate(movie, null)) {
                    identifiers.add(identifier);
                }
            }
            catch (StdQTException e) {
                if (log.isInfoEnabled()) {
                    log.info("The movie type '" + identifier.getInfo().getName() + "' can not be used for exporting");
                }
            }
        }

        return identifiers;
    }

    /**
     * Retrieve a Set of MovieExportFormats that can be used to export a particular movie. The resulting formats can be
     * used as follows:
     * <pre>
     * // Get all the exportable formats
     * Set<MovieExportFormat> set = QT.listMovieExportFormats(movie);
     * // Let's use the first format for exporting
     * MovieExportFormat choice = set.iterator().next();
     * // Export to 'somefile'
     * MovieExporter exporter = new MovieExporter(choice.getSubType());
     * QTFile qtFile = new QTFile(new File("somefile"));
     * movie.setProgressProc();
     * movie.convertToFile(null,
     *                     qtFile,
     *                     StdQTConstants.kQTFileTypeMovie,
     *                     StdQTConstants.kMoviePlayer,
     *                     IOConstants.smSystemScript,
     *                     StdQTConstants.showUserSettngsDialog |
     *                     StdQTConstants.movieToFileOnlyExport |
     *                     StdQTConstants.movieFileSpecValid,
     *                     exporter);
     *
     * </pre>
     *
     * This method assumes that you've already started a session, either by using QTSession.open() or QT.manageSession()
     *
     * @param movie The source movie that you want to save in a different format
     * @return A set of exceptable export formats.
     * @throws QTException If something bombs.
     */
    public static Set<MovieExportFormat> listMovieExportFormats(Movie movie) throws QTException {
        manageSession();
        Set<MovieExportFormat> formats = new TreeSet<MovieExportFormat>();
        Set<ComponentIdentifier> identifiers = listMovieExportComponentIdentifiers(movie);
        for (ComponentIdentifier identifier : identifiers) {
            formats.add(new MovieExportFormat(identifier.getInfo().getName(), identifier.getInfo().getSubType()));
        }

        return formats;
    }

    /**
     * <p>This method will open a quicktime session. It also adds a shutdown
     * hook to close the session when the JVM is exited. This is useful if
     * you do not want to manage QuickTime sessions on your own.</p>
     *
     * <p>Usage:
     * <pre>
     *  QT.manageSession()
     *  // Do your quicktime processing
     * </pre></p>
     *
     * <p>The above snippet can be used to replace this boiler plate code:
     * <pre>
     *  QTSession.open();
     *  //Do your quicktime processing
     *  QTSession.close();
     * </pre>
     *
     *
     * @throws QTException
     */
    public static void manageSession() throws QTException {
        getInstance();
    }

    /**
     * Open a movie using a url as a source reference. You will need to call
     * movie.prePreroll and movie.preroll after opening the movie You can
     * implement a MoviePrePreroll interface to execute other tasks when the
     * prePreroll is completed.
     *
     * @param url
     *
     * @return
     *
     * @throws QTException
     */
    public static Movie openMovieFromUrl(URL url) throws QTException {
        manageSession();
        String refUrl = toDataRefURL(url);
        if (log.isDebugEnabled()) {
            log.debug("Opening " + refUrl);
        }
        DataRef dataRef = new DataRef(refUrl);
        Movie movie = Movie.fromDataRef(dataRef, StdQTConstants.newMovieActive);
        return movie;
    }

    /**
     * Java returns local URL's in the form of file:/some/path/to/file.mov but
     * QuickTime expects file:///some/path/to/file.mov. This method will
     * convert those URL's to a fiel that Qt can consume.
     *
     * @param url The url to convert. If it's in an exceptable form no conversion
     * is done
     *
     * @return A String suitable for use with DataRef.
     */
    public static String toDataRefURL(URL url) {
        return url.toExternalForm().replaceFirst("^file:/(?=[a-zA-Z0-9])", "file:///");
    }
    
    
    /**
     * Attempt to resolve the URL of the movie file. Some files types can't
     * be resolved (e.g. AVI's). If a name can't be resolved an empty string, "",
     * is returned.
     * @param movie The movie whose URL we want to resolve
     * @return The String URL, or an empty string, "", if resolution is not possible.
     */
    public static String resolveName(Movie movie) {
        String name = "";
        try {
            name = movie.getDefaultDataRef().getUniversalResourceLocator();
        }
        catch (Exception e) {
            log.info("Unable to resolve the URL for the movie", e);
        }
        return name;
    }
    
    /**
     * Opens a movie player for playing the movie.
     * @param movie The movie file to play
     * @return A Frame that is displaying the current movie.
     */
    public static Frame playMovie(Movie movie) {
            // Create and show a frame for movie        
        Frame frame = null;
        try {
            frame = new QTMovieFrame(movie);
            frame.setVisible(true);
        }
        catch (QTException ex) {
            log.error("Unable to open movie frame", ex);
        }
        return frame;

    }
    
    /**
     * Opens a movie player for playing the movie.
     * @param url The url that points to a movie file
     * @return A Frame that is displaying the current movie.
     */
    public static Frame playMovie(URL url) {
        Frame frame = null;
        try {
            frame = playMovie(QT.openMovieFromUrl(url));
        }
        catch (QTException ex) {
            log.error("Unable to open movie frame for " + url, ex);
        }
        return frame;
    }
    
}
