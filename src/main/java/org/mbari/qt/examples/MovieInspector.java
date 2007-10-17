/*
 * MovieInspector.java
 * 
 * Created on Oct 16, 2007, 7:43:38 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mbari.qt.examples;

import java.util.Set;
import org.mbari.qt.MovieExportFormat;
import org.mbari.qt.QT;
import org.mbari.qt.TimeUtil;
import quicktime.QTException;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTConstants;
import quicktime.std.comp.ComponentDescription;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.Media;
import quicktime.std.movies.media.MediaHandler;
import quicktime.util.QTUtils;

/**
 *
 * @author brian
 */
public class MovieInspector {

    private MovieInspector() {
        // No instantiation allowed
    }

    
    
    public static void inspect(Movie movie) throws QTException {
        QT.manageSession();
        final String url = movie.getDefaultDataRef().getUniversalResourceLocator();
        
        System.out.println("----- Inspecting " + url + " -----");
        System.out.println("\tIs MPEG = " + QT.isMpegMovie(movie));

        
        System.out.println("\tSupported Export formats:");
        Set<MovieExportFormat> componentIdentifiers = QT.listMovieExportFormats(movie);
        for (MovieExportFormat movieExportFormat : componentIdentifiers) {
            System.out.println("\t\t" + movieExportFormat + " [" + movieExportFormat.getSubType() + "]");
        }
        
        System.out.println("\tHas TimeCodeTrack = " + TimeUtil.hasTimeCodeTrack(movie));
        int trackCount = movie.getTrackCount();
        System.out.println("\tTrack count = " + trackCount);
        for (int i = 1; i <= trackCount; i++) {
            Track track = movie.getIndTrack(i);
            System.out.println("\tInspecting Track #" + i + " (" + track + ")");
            System.out.println("\t\tTrack Duration = " + track.getDuration());
            final Media media = track.getMedia();
            System.out.println("\t\tMedia Duration = " + media.getDuration());
            System.out.println("\t\tMedia TimeScale = " + media.getTimeScale());
            final MediaHandler mediaHandler = media.getHandler();
            final ComponentDescription info = mediaHandler.getInfo();
            System.out.println("\t\tDecoded by '" + info.getName() +
                      "' [type = " + QTUtils.fromOSType(info.getType()) + 
                      ": subtype = " + QTUtils.fromOSType(info.getSubType()) + "]");
            System.out.println("\t\tDecoder Manufacturer = " + info.getManufacturer());
            System.out.println("\t\tDecoder info = " + info.getInformationString());
        }

        //System.out.println("\tFrame Rate = " + TimeUtil.estimateFrameRate(movie));

    }
    
    
    public static void main(String[] args) {
        try {
        QT.manageSession();
        QTFile qtFile = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
        OpenMovieFile openMovieFile = OpenMovieFile.asRead(qtFile);
        Movie m = Movie.fromFile(openMovieFile);
        inspect(m);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    

}
