/*
 * BombsAway.java
 * 
 * Created on Oct 18, 2007, 8:58:42 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mbari.qt.examples;

import quicktime.QTSession;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTConstants;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.Media;

/**
 *
 * @author brian
 */
public class BombsAway {

    public static void main(String[] args) {
        try {
            QTSession.open();
            QTFile qtFile = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
            OpenMovieFile openMovieFile = OpenMovieFile.asRead(qtFile);
            Movie movie = Movie.fromFile(openMovieFile);
            final Track videoTrack = movie.getIndTrackType(1, 
                    StdQTConstants.visualMediaCharacteristic,
                    StdQTConstants.movieTrackCharacteristic);
            final Media media = videoTrack.getMedia();
            media.getDuration();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
