/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tools;

import java.io.File;
import java.net.URL;
import org.mbari.movie.Timecode;
import org.mbari.qt.QT;
import org.mbari.qt.TimeUtil;
import org.mbari.qt.VideoStandard;
import quicktime.std.movies.Movie;

/**
 *
 * @author brian
 */
public class AddTimecodeTrack {
    
    
    public static void main(String[] args) {
        
        if (args.length < 3) {
            System.out.println("Usage:");
            System.out.println("  AddTimecodeTrack [input] [output] [timecode]");
            System.out.println("");
            System.out.println("Arguments:");
            System.out.println("  input    = The QuickTime movie to read");
            System.out.println("  output   = The QuickTime movie to write");
            System.out.println("  timecode = The starting timecode for the movie as HH:MM:SS:FF");
            System.out.println("    For example: '01:23:45:27");
        }
        
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
                
        Timecode timecode = new Timecode(args[2]);
        int exitCode = 0;
        try {
            URL inputUrl = inputFile.toURI().toURL();
            Movie movie = QT.openMovieFromUrl(inputUrl);
            TimeUtil.addTimeCodeTrack(movie, timecode, VideoStandard.NTSC);
            QT.flattenMovie(movie, outputFile, false);
        }
        catch (Exception e) {
            System.out.println("An error occured while trying to add a timecode track to " + inputFile.getAbsoluteFile());
            e.printStackTrace();
            exitCode = 1;
        }
        System.exit(exitCode);
        
    }
}
