/*
 * VideoArchiveListener4VIDEOARCHIVE.java
 *
 * Created on May 22, 2007, 8:05:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.vars.annotation.ui.dispatchers;

import java.net.URL;
import org.mbari.framegrab.GrabberException;
import org.mbari.framegrab.IGrabber;
import org.mbari.framegrab.VideoChannelGrabber;
import org.mbari.qt.QT;
import org.mbari.qt.QT4JException;
import org.mbari.util.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brian
 */
public class VideoArchiveChangeAction4VIDEOARCHIVE  {
    
    private final Dispatcher movieDispatcher;
    private final Object defaultMovie;
    private final Dispatcher grabberDispatcher;
    private final Object defaultGrabber;
    
    private static final Logger log = LoggerFactory.getLogger(VideoArchiveChangeAction4VIDEOARCHIVE.class);
    
    /**
     * @param movieDispatcher Should be PredefinedDispatcher.MOVIE.getDispatcher()
     * @param defaultMovie PredefinedDispatcher.MOVIE.getDefaultValue()
     * @param grabberDispatcher PredefinedDispatcher.GRABBER.getDispatcher()
     * @param defaultGrabber PredefinedDispatcher.GRABBER.getDefaultValue()
     */
    public VideoArchiveChangeAction4VIDEOARCHIVE(Dispatcher movieDispatcher,
            Object defaultMovie, Dispatcher grabberDispatcher, Object defaultGrabber) {
        this.movieDispatcher = movieDispatcher;
        this.grabberDispatcher = grabberDispatcher;
        this.defaultMovie = defaultMovie;
        this.defaultGrabber = defaultGrabber;
    }
    
    /**
     * @param url The url refering to a Movie file to be opened. <b>null</b> is
     * acceptable
     * @throws org.mbari.qt.QT4JException 
     */
    public void doAction(URL url) throws QT4JException {
        /*
         * Open a movie if the videoArchive refers to a URL.
         */
        if (url != null) {
            try {
                movieDispatcher.setValueObject(QT.openMovieFromUrl(url));
            } 
            catch (Exception ex) {
                log.error("Failed to open " + url.toExternalForm() + " in a movie player", ex);
                movieDispatcher.setValueObject(defaultMovie);
                throw new QT4JException("Unable to open the movie '" + url.toExternalForm() + "'", ex);
            }
        } else {
            movieDispatcher.setValueObject(defaultMovie);
        }
        
        /*
         * Open a VideoChannelGrabber if the videoArchive does not
         * refer to a URL
         */
        if (url == null) {
            try {
                IGrabber grabber = (IGrabber) grabberDispatcher.getValueObject();
                if (grabber instanceof VideoChannelGrabber) {
                    // Do nothing
                } else {
                    grabber = new VideoChannelGrabber();
                    grabberDispatcher.setValueObject(grabber);
                }
                
            } 
            catch (GrabberException ex) {
                log.warn("Unable to intialize the QuickTime components need for frame capture", ex);
                grabberDispatcher.setValueObject(defaultGrabber);
            }
        }
    }
    
}
