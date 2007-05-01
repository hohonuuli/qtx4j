/*
 * BasicSwingQTPlayer.java
 *
 * Created on January 11, 2007, 2:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.qt.swing;

import javax.swing.JComponent;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.qt.*;
import quicktime.QTException;
import quicktime.app.view.MoviePlayer;
import quicktime.app.view.QTFactory;
import quicktime.app.view.QTJComponent;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.movies.Movie;

/**
 *
 * @author brian
 */
public class BasicQTPlayer extends JFrame {
    
    private static Logger log = LoggerFactory.getLogger(BasicQTPlayer.class);
    
    /** Creates a new instance of BasicSwingQTPlayer */
    public BasicQTPlayer(Movie m) throws QTException {
        super("Basic Swing QT Player");
        MoviePlayer mp = new MoviePlayer (m);
        QTJComponent qc = QTFactory.makeQTJComponent(mp);
        JComponent jc = qc.asJComponent();
        add(jc);
    }
    
    public static void main(String[] args) {
        try {
            QT.manageSession();
            QTFile file = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
            OpenMovieFile omFile = OpenMovieFile.asRead(file);
            Movie m = Movie.fromFile(omFile);
            JFrame f = new BasicQTPlayer(m);
            f.pack();
            f.setVisible(true);
            m.start();
        }
        catch (Exception e){
            log.error("An error occurred", e);
        }
    }   
    
}
