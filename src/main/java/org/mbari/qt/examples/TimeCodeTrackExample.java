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

import java.awt.Component;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import quicktime.Errors;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.view.QTComponent;
import quicktime.app.view.QTFactory;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.qd.QDColor;
import quicktime.qd.QDDimension;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.image.Matrix;
import quicktime.std.movies.Movie;
import quicktime.std.movies.MovieController;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.Media;
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
public class TimeCodeTrackExample extends Frame {

    MovieController mc;
    QTComponent qtc;
    QTFile qtf;
    Movie theMovie;

    TimeCodeTrackExample(String title) throws QTException {
        super(title);

        // prompt the user to select a movie file
        qtf = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);

        // open the selected file and make a Movie from it
        OpenMovieFile movieFile = OpenMovieFile.asRead(qtf);
        theMovie = Movie.fromFile(movieFile);

        initialize();
    }
    
    private void initialize() throws QTException {
        // make a MovieController from the resultant Movie
        // enabling the keys so the user can interact with the movie with the keyboard
        mc = new MovieController(theMovie);
        mc.setKeysEnabled(true);

        // make a QTComponent so that the MovieController has somewhere to draw
        // and add it to the Frame
        qtc = QTFactory.makeQTComponent(mc);
        add((Component) qtc);

        // add a file menu to add/remove time code to the movie
        new FileMenu(this);

        // add a Window Listener to this frame
        // that will close down the QTSession, dispose of the Frame
        // which will close the window - where we exit
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                QTSession.close();
                dispose();
            }

            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }

        });
    }

    /**
     * Method description
     *
     */
    public void addTimecodeToMovie() {
        try {
            Track myTrack = theMovie.getIndTrackType(1, StdQTConstants.timeCodeMediaType,
                                StdQTConstants.movieTrackMediaType);

            //only allow one time code track in movie
            if (myTrack != null) {
                return;
            }


            // Get the (first) visual track; this track determines the width of the new timecode track
            Track theVisualTrack = theMovie.getIndTrackType(1, StdQTConstants.visualMediaCharacteristic,
                                       StdQTConstants.movieTrackCharacteristic);

            QDDimension dim = null;

            // Get movie and track attributes
            int movieTimeScale = theMovie.getTimeScale();

            // Create the timecode track and media
            if (theVisualTrack == null) {
                QDRect r = mc.getBounds();
                dim = new QDDimension(r.getWidth(), r.getHeight());
            }
            else {
                Media theVisualTrackMedia = Media.fromTrack(theVisualTrack);
                dim = theVisualTrack.getSize();
            }

            Track theTCTrack = theMovie.newTrack((float) dim.getWidth(), (float) dim.getHeight(), 0);
            TimeCodeMedia theTCMedia = new TimeCodeMedia(theTCTrack, movieTimeScale);
            TimeCoder theTimeCoder = theTCMedia.getTimeCodeHandler();

            // Set up a TimeCodeDef
            TimeCodeDef myTCDef = new TimeCodeDef();

            //30 frames a second time code reading
            int tcdFlags = StdQTConstants.tc24HourMax;
            myTCDef.setFlags(tcdFlags);
            myTCDef.setTimeScale(3000);
            myTCDef.setFrameDuration(100);
            myTCDef.setFramesPerSecond(30);

            /*
             *       For drop frame 29.97 fps
             *       tcdFlags |= StdQTConstants.tcDropFrame;
             *       myTCDef.setTimeScale (2997);
             */

            // Start the timecode at 0:0:0:0
            TimeCodeTime myTCTime = new TimeCodeTime(0, 0, 0, 0);

            // Change the text options to Green on Black.
            String myTCString = theTimeCoder.timeCodeToString(myTCDef, myTCTime);
            TCTextOptions myTCTextOptions = theTimeCoder.getDisplayOptions();
            int textSize = myTCTextOptions.getTXSize();
            myTCTextOptions.setForeColor(QDColor.green);
            myTCTextOptions.setBackColor(QDColor.black);
            theTimeCoder.setDisplayOptions(myTCTextOptions);

            // Figure out the timecode track geometry
            QDDimension tcDim = theTCTrack.getSize();
            tcDim.setHeight(textSize + 2);
            theTCTrack.setSize(tcDim);

            if (dim.getHeight() > 0) {
                Matrix TCMatrix = theTCTrack.getMatrix();
                TCMatrix.translate(0, dim.getHeight());
                theTCTrack.setMatrix(TCMatrix);
            }

            // add a sample to the timecode track
            //
            // each sample in a timecode track provides timecode information for a span of movie time;
            // here, we add a single sample that spans the entire movie duration

            // the sample data contains a frame number that identifies one or more content frames
            // that use the timecode; this value (a long integer) identifies the first frame that
            // uses the timecode.  For our purposes this will probably always be zero, but it can't
            // hurt to go the full 9.
            int frameNumber = theTimeCoder.toFrameNumber(myTCTime, myTCDef);
            int[] frameNumberAr = { frameNumber };
            QTHandle myFrameNumHandle = new QTHandle(4, false);
            myFrameNumHandle.copyFromArray(0, frameNumberAr, 0, 1);

            // create and configure a new timecode description
            TimeCodeDescription myTCDescription = new TimeCodeDescription();
            myTCDescription.setTimeCodeDef(myTCDef);

            // edit the track media
            theTCMedia.beginEdits();

            // since we created the track with the same timescale as the movie,
            // we don't need to convert the duration
            theTCMedia.addSample(myFrameNumHandle, 0, myFrameNumHandle.getSize(), theMovie.getDuration(),
                                 myTCDescription, 1, 0);
            theTCMedia.endEdits();

            theTCTrack.insertMedia(0, 0, theMovie.getDuration(), 1.0F);

            // this code saves the TimeCode to the movie

            OpenMovieFile outStream = OpenMovieFile.asWrite (qtf);
            theMovie.addResource (outStream, StdQTConstants.movieInDataForkResID, qtf.getName());
            outStream.close();
             

            // Make the timecode visible
            int tcFlags = theTimeCoder.getFlags();
            tcFlags |= StdQTConstants.tcdfShowTimeCode;
            theTimeCoder.setFlags(tcFlags, StdQTConstants.tcdfShowTimeCode);

            changedMovie();
        }
        catch (QTException err) {
            err.printStackTrace();
        }
    }

    private void changedMovie() throws QTException {

        // tell the controller that we have changed the movie
        mc.movieChanged();
        repaint();

        // this will resize the frame to the current (new) size of the movie
        // the QTCanvas will be resized as a result of this call
        pack();
    }

    /**
     * Method description
     *
     */
    public void deleteTimeCodeTracks() {
        try {
            Track myTrack = null;
            do {
                myTrack = theMovie.getIndTrackType(1, StdQTConstants.timeCodeMediaType,
                                                   StdQTConstants.movieTrackMediaType);

                if (myTrack != null) {
                    theMovie.removeTrack(myTrack);
                }

            }
            while (myTrack != null);

            // if you previous saved the time code to the movie
            // removing the time code track you also need to update the movie file

            
            OpenMovieFile outStream = OpenMovieFile.asWrite (qtf);
            theMovie.addResource (outStream, StdQTConstants.movieInDataForkResID, qtf.getName());
            outStream.close();
             

            changedMovie();
        }
        catch (QTException err) {
            err.printStackTrace();
        }
    }

    /**
     * Method description
     *
     */
    public void goAway() {
        QTSession.close();
        dispose();
        System.exit(0);
    }

    /**
     * Method description
     *
     *
     * @param args
     */
    public static void main(String args[]) {
        try {
            QTSession.open();
            TimeCodeTrackExample tc = new TimeCodeTrackExample("QT in Java");

            // this will lay out and resize the Frame to the size of the Movie
            tc.pack();
            tc.setVisible(true);
            tc.toFront();
        }
        catch (QTException e) {

            // catch a userCanceledErr and just exit the program
            if (e.errorCode() != Errors.userCanceledErr) {
                e.printStackTrace();
            }
            else {
                System.out.println("UserCanceled : Application needs media file to run. Quitting....");
            }

            QTSession.close();
            System.exit(1);
        }
    }

    class FileMenu {

        TimeCodeTrackExample myTimeCode;

        /**
         * Constructs ...
         *
         *
         * @param src
         */
        public FileMenu(TimeCodeTrackExample src) {
            this.myTimeCode = src;

            // make the menu bar up
            MenuBar menuBar = new MenuBar();
            Menu fileMenu = new Menu("File");

            MenuItem addMenuItem = new MenuItem("Add TimeCode Track");
            MenuItem removeMenuItem = new MenuItem("Remove TimeCode Track");
            MenuItem quitMenuItem = new MenuItem("Quit");

            fileMenu.add(addMenuItem);
            fileMenu.add(removeMenuItem);
            fileMenu.addSeparator();
            fileMenu.add(quitMenuItem);

            menuBar.add(fileMenu);
            myTimeCode.setMenuBar(menuBar);

            addMenuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    myTimeCode.addTimecodeToMovie();
                }

            });
            removeMenuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    myTimeCode.deleteTimeCodeTracks();
                }

            });
            quitMenuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {

                    // closes down QT and quits
                    myTimeCode.goAway();
                }

            });
        }
    }
}
