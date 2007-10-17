/*
 * QTPlayer.java
 * 
 * Created on Oct 16, 2007, 5:46:05 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mbari.qt.examples;

import java.io.File;
import java.net.URL;
import org.mbari.qt.QT;
import org.mbari.qt.QT4JException;
import org.mbari.qt.awt.QTMovieFrame;
import org.mbari.vcr.qt.VCR;
import org.mbari.vcr.ui.VCRFrame;
import quicktime.QTException;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.movies.Movie;

/**
 *
 * @author brian
 */
public class QTPlayer {

    QTMovieFrame movieFrame;
    VCRFrame vcrFrame;
    Movie movie;
    VCR vcr;
    
    public QTPlayer() throws QTException, QT4JException {
        // Open a dialog
        QT.manageSession();
        QTFile qtFile = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
        OpenMovieFile openMovieFile = OpenMovieFile.asRead(qtFile);
        Movie m = Movie.fromFile(openMovieFile);
        initialize(m);
    }

    public QTPlayer(Movie movie) throws QTException, QT4JException {
        initialize(movie);
    }
    
    private void initialize(Movie movie) throws QTException, QT4JException {
        this.movie = movie;
        movieFrame = new QTMovieFrame(movie);
        vcr = new VCR(movie);
        vcrFrame = new VCRFrame() {{
            setVcr(vcr);
        }};
        movieFrame.setVisible(true);
        vcrFrame.setVisible(true);
    }
    
    public static void main(String[] args) throws Exception {
        
        QTPlayer player = null;
        
        if (args.length > 0) {
            String filename = args[0];
            URL url = (new File(filename)).toURL();
            Movie movie = QT.openMovieFromUrl(url);
            player = new QTPlayer(movie);
        }
        else {
            player = new QTPlayer();
        }
    }


}
