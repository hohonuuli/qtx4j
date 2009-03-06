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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.movie.Timecode;
import org.mbari.qt.QT;
import org.mbari.qt.QT4JException;
import org.mbari.qt.QTTimecode;
import org.mbari.qt.TimeUtil;
import org.mbari.util.IObserver;
import quicktime.QTException;
import quicktime.app.view.QTComponent;
import quicktime.app.view.QTFactory;
import quicktime.std.StdQTConstants;
import quicktime.std.movies.Movie;
import quicktime.std.movies.MovieController;
import quicktime.std.movies.media.DataRef;

/**
 * Frame that displays a QuickTime movie and the scrubber. Also displays the time-code (if available) and the
 * run-time. Also provides hooks for applications to get information. THis is to be used as a standalone window.
 * Since a movie must be provided in the constructor, this application assumes you have already opened (and not closed)
 * a QTSession.
 * @author brian
 * @version $Id: QTMovieFrame.java 534 2007-04-20 18:51:56Z brian $
 * @since Jan 23, 2007 10:15:03 AM PST
 */
public class QTMovieFrame extends Frame {

    private static final Logger log = LoggerFactory.getLogger(QTMovieFrame.class);
    Panel infoPanel;
    Label lbl1;
    Label lbl2;
    Component movieComponent;
    Timecode runtime;
    TimecodeLabel runtimeLabel;
    QTTimecode timecode;
    TimecodeLabel timecodeLabel;
    private final Movie movie;

    /**
     * Constructs ...
     *
     *
     * @param movie
     *
     * @throws QTException
     */
    public QTMovieFrame(Movie movie) throws QTException {
        super(QT.resolveName(movie));
        
        /*
         * This call is important even if QTSession.open() has already been called. Because we use
         * QuickTime components in timers in this class we need to make sure that QTSession.close() is
         * called after the timers are completed. QTSession requires one close called for each open, but
         * you can call open and close as often as you like.
         */
        QT.manageSession();
        this.movie = movie;

        try {
            timecode = new QTTimecode(movie);
            timecode.setRepresentation(Timecode.Representation.TIMECODE);
        }
        catch (QT4JException ex) {
            log.debug("Not able to monitor timecode track", ex);
            timecode = null;
        }

        runtime = new Timecode();
        runtime.setRepresentation(Timecode.Representation.RUNTIME);
        initialize();
        //movie.prePreroll(0, 1.0f);
        //movie.preroll(0, 1.0f);
    }

    Panel getInfoPanel() {
        if (infoPanel == null) {
            infoPanel = new Panel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
            infoPanel.add(Box.createHorizontalGlue());
            infoPanel.add(lbl1);
            infoPanel.add(Box.createHorizontalStrut(5));
            infoPanel.add(getTimecodeLabel());
            infoPanel.add(Box.createHorizontalStrut(15));
            infoPanel.add(lbl2);
            infoPanel.add(Box.createHorizontalStrut(5));
            infoPanel.add(getRuntimeLabel());
            infoPanel.add(Box.createHorizontalGlue());
        }

        return infoPanel;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Movie getMovie() {
        return movie;
    }

    Component getMovieComponent() throws QTException {
        if (movieComponent == null) {
            MovieController movieController = new MovieController(movie);
            movieController.setKeysEnabled(true);
            QTComponent qtComponent = QTFactory.makeQTComponent(movieController);
            movieComponent = qtComponent.asComponent();
        }

        return movieComponent;
    }

    TimecodeLabel getRuntimeLabel() {
        if (runtimeLabel == null) {
            runtimeLabel = new TimecodeLabel();
            runtimeLabel.setText(Timecode.EMPTY_TIMECODE_STRING);
            runtime.addObserver(runtimeLabel);

            try {
                float frameRate = TimeUtil.estimateFrameRate(movie);
                runtime.setFrameRate(frameRate);
            }
            catch (QTException e) {
                log.error("Unable to estimate frame rate from movie");
            }

            Timer timer = new Timer(100, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (movie != null) {
                        try {
                            TimeUtil.queryRuntime(movie, runtime);
                        }
                        catch (QTException ex) {
                            runtimeLabel.setText(Timecode.EMPTY_TIMECODE_STRING);
                            log.error("Problem fetching time from movie", ex);
                        }
                        catch (Exception ex) {
                            runtimeLabel.setText(Timecode.EMPTY_TIMECODE_STRING);
                            log.error("Problem fetching time from movie", ex);
                        }
                    }
                }

            });
            timer.start();
        }

        return runtimeLabel;
    }

    TimecodeLabel getTimecodeLabel() {
        if (timecodeLabel == null) {
            timecodeLabel = new TimecodeLabel();
            timecodeLabel.setText(Timecode.EMPTY_TIMECODE_STRING);
            if (timecode != null && movie != null) {
                timecode.addObserver(timecodeLabel);

                Timer timer = new Timer(100, new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        try {
                            timecode.updateTimecode();
                        } catch (QTException ex) {
                            log.debug("Failed to update timecode", ex);
                        }
                    }
                });
                timer.start();
            }
        }

        return timecodeLabel;
    }

    private void initialize() throws QTException {
        lbl1 = new Label("Time-code:");
        lbl2 = new Label("Run-time:");
        setLayout(new BorderLayout());
        add(getMovieComponent(), BorderLayout.CENTER);
        add(getInfoPanel(), BorderLayout.SOUTH);
        pack();
        setResizable(false);
    }

    /**
     * Method description
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            QT.manageSession();

            String url = JOptionPane.showInputDialog(null, "Enter URL");

            /*
             * DataRef is a general purpose media locator.
             */
            DataRef dataRef = new DataRef(url);

            /*
             * QTConstants can be combined using OR (i.e. '|')
             */
            Movie movie = Movie.fromDataRef(dataRef, StdQTConstants.newMovieActive);
            movie.prePreroll(0, 1.0f);
            movie.preroll(0, 1.0f);
            QTMovieFrame f = new QTMovieFrame(movie);
            f.setVisible(true);
            movie.start();

        }
        catch (Exception e) {

            /*
             * If no movie is selected:
             * java.lang.NullPointerException
             *      at quicktime.std.movies.media.DataRef.<init>(DataRef.java:144)
             *      at org.mbari.qt.awt.BasicQTURLController.main(BasicQTURLController.java:50)
             *
             * If a bogus URL is given:
             * quicktime.std.StdQTException[QTJava:6.1.6g],-2012=invalidDataRef,QT.vers:7138000
             *      at quicktime.std.movies.Movie.fromDataRef(Movie.java:346)
             *      at org.mbari.qt.awt.BasicQTURLController.main(BasicQTURLController.java:51)
             */
            log.error("Failed", e);
        }
    }

    class TimecodeLabel extends Label implements IObserver {

        /**
         * Method description
         *
         *
         * @param obj
         * @param changeCode
         */
        public void update(Object obj, Object changeCode) {
            if (obj instanceof Timecode) {
                setText(obj.toString());
            }
        }
    }
}
