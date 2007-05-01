/*
 * BasicAWTQTPlayer.java
 *
 * Created on January 11, 2007, 3:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.qt.examples;

import java.awt.Component;
import java.awt.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.qt.QT;
import quicktime.QTException;
import quicktime.app.view.QTComponent;
import quicktime.app.view.QTFactory;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.movies.Movie;

/**
 *
 * @author brian
 */
public class BasicQTPlayer extends Frame {
    
    private static final Logger log = LoggerFactory.getLogger(BasicQTPlayer.class);
    
    /** Creates a new instance of BasicAWTQTPlayer */
    public BasicQTPlayer(Movie m) throws QTException {
        super("Basic AWT QT Player");
        QTComponent qt = QTFactory.makeQTComponent(m);
        Component c = qt.asComponent();
        add(c);
    }
    
    public static void main(String[] args) {
        try {
            QT.manageSession();
            QTFile qtFile = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
            OpenMovieFile openMovieFile = OpenMovieFile.asRead(qtFile);
            Movie movie = Movie.fromFile(openMovieFile);
            Frame frame = new BasicQTPlayer(movie);
            frame.pack();
            frame.setVisible(true);
            movie.start();
        } catch (QTException ex) {
            log.error("An error occurred", ex);
        }
    }
    
}
