package org.mbari.qt.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.qt.QT;

import java.awt.*;

import quicktime.app.view.QTComponent;
import quicktime.app.view.QTFactory;
import quicktime.std.movies.Movie;
import quicktime.std.movies.MovieController;
import quicktime.std.movies.media.DataRef;
import quicktime.std.StdQTConstants;
import quicktime.QTException;

import javax.swing.*;

/**
 * This class demostrates how to play a QuickTime movie from a URL in a AWT componenet.
 *
 * @author brian
 * @version $Id: BasicQTURLController.java 140 2007-02-17 00:41:37Z brian $
 * @since Jan 17, 2007 2:00:19 PM PST
 */
public class BasicQTURLController extends Frame {

    private static final Logger log = LoggerFactory.getLogger(BasicQTURLController.class);

    QTComponent qtComponent;

    public BasicQTURLController() throws QTException {
        super("Basic QT DataRef/Controller");
        Movie dummyMovie = new Movie();
        qtComponent = QTFactory.makeQTComponent(dummyMovie);
        Component c = qtComponent.asComponent();
        add(c);
        pack();
    }

    public QTComponent getQTComponent() {
        return qtComponent;
    }

    /**
     * file:///Users/brian/workspace/qt4j/src/test/resources/wonderwall.mov
     */

    public static void main(String[] args){
        try {
            QT.manageSession();
            BasicQTURLController f= new BasicQTURLController();
            String url = JOptionPane.showInputDialog(f, "Enter URL");

            /*
             * DataRef is a general purpose media locator.
             */
            DataRef dataRef = new DataRef(url);

            /*
             * QTConstants can be combined using OR (i.e. '|')
             */
            Movie movie = Movie.fromDataRef(dataRef, StdQTConstants.newMovieActive);
            MovieController movieController = new MovieController(movie);
            //f.getQTComponent().setMovie(movie);  <-- This call is NOT needed.
            f.getQTComponent().setMovieController(movieController);

            /*
             * Allocate movie-playing resources upfront, reducing jitter and dropped frames when the movie starts
             * playing. These methods take the same 2 arguments: the movie time and the rate that the program intends to
             * start playing at.
             */
            movie.prePreroll(0, 1.0f);
            movie.preroll(0, 1.0f);
            f.pack();
            f.setVisible(true);

            /*
             * We're assuming that the movie downloads faster than it plays. This is NOT a safe assumption
             */
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
}
