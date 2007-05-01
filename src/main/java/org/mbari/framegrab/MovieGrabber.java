package org.mbari.framegrab;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.qd.Pict;
import quicktime.std.movies.Movie;

/**
 * Class for capturing images from a QuickTime Movie
 * @author brian
 */
public class MovieGrabber extends AbstractGrabber {
    
    private final Movie movie;

    /**
     * Constructor
     * @param movie 
     * @throws org.mbari.framegrab.GrabberException 
     */
    public MovieGrabber(Movie movie) throws GrabberException {
        if (movie == null) {
            throw new IllegalArgumentException("Movie argument can not be null");
        }
        this.movie = movie;
    }


    /**
     * @return 
     * @throws quicktime.QTException 
     */
    protected Pict grabPict() throws QTException {
        QTSession.open();
        // Stop movie to take a picture
        float rate = movie.getRate();
        if (rate > 0) {
            movie.stop();
        }

        // Take a PICT
        Pict pict = movie.getPict(movie.getTime());
        
        // Start movie back up
        movie.setRate(rate);
        QTSession.close();
        return pict;
    }


}
