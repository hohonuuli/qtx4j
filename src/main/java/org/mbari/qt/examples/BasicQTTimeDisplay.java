/*
 * BasicQTTimeDisplay.java
 *
 * Created on January 12, 2007, 1:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.qt.examples;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.movie.Timecode;
import org.mbari.qt.QT;
import org.mbari.qt.TimeUtil;
import quicktime.QTException;
import quicktime.io.QTFile;
import quicktime.io.OpenMovieFile;
import quicktime.app.view.QTComponent;
import quicktime.app.view.QTFactory;
import quicktime.std.movies.Movie;
import quicktime.std.movies.MovieController;

/**
 *
 * @author brian
 */
public class BasicQTTimeDisplay extends Frame{
    
    private final Movie movie;
    private Label timeLabel;
    private static final Logger log = LoggerFactory.getLogger(BasicQTTimeDisplay.class);
    private Timecode timecode = new Timecode();
    private static final String BOGUS_TIMECODE = "--:--:--:--";


    /** Creates a new instance of BasicQTTimeDisplay */
    public BasicQTTimeDisplay(Movie movie) throws QTException {
        super("Basic QT Controller");
        this.movie = movie;
        initialize();
    }
    
    private void initialize() throws QTException {
        MovieController mc = new MovieController(movie);
        QTComponent qc = QTFactory.makeQTComponent(mc);
        Component c = qc.asComponent();
        setLayout(new BorderLayout());
        add(c, BorderLayout.CENTER);
        timeLabel = new Label(BOGUS_TIMECODE, Label.CENTER);
        add(timeLabel, BorderLayout.SOUTH);
        Timer timer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (movie != null) {
                    try {
                        TimeUtil.queryRuntime(movie, timecode);
                        timeLabel.setText(timecode.toString());
                    }
                    catch (QTException ex) {
                        timeLabel.setText(BOGUS_TIMECODE);
                        log.error("Problem fetching time from movie", ex);
                    }
                }
            }
        });
        timer.start();
        pack();
    }

    public static void main(String[] args){
        try {
            QT.manageSession();
            QTFile qtFile = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
            OpenMovieFile openMovieFile = OpenMovieFile.asRead(qtFile);
            Movie movie = Movie.fromFile(openMovieFile);
            Frame f = new BasicQTTimeDisplay(movie);
            f.pack();
            f.setVisible(true);
            movie.start();
        }
        catch (QTException e) {
            log.error("Bombed out", e);
        }

    }
    
    
}
