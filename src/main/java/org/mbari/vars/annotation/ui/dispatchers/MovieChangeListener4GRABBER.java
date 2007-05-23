/*
 * MovieChangeListener4GRABBER.java
 *
 * Created on May 22, 2007, 7:56:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.vars.annotation.ui.dispatchers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.mbari.framegrab.GrabberException;
import org.mbari.framegrab.IGrabber;
import org.mbari.framegrab.MovieGrabber;
import org.mbari.util.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.std.movies.Movie;

/**
 *
 * @author brian
 */
public class MovieChangeListener4GRABBER implements PropertyChangeListener{
    
    private final Dispatcher grabberDispatcher;
    private final Object defaultValue;
    
    private static final Logger log = LoggerFactory.getLogger(MovieChangeListener4GRABBER.class);
    
    /**
     * @param grabberDispatcher Should be PredefinedDispatcher.GRABBER.getDispatcher()
     * @param defaultValue Should be PredefinedDispatcher.GRABBER.getDefaultValue()
     */
    public MovieChangeListener4GRABBER(final Dispatcher grabberDispatcher, final Object defaultValue) {
        this.grabberDispatcher = grabberDispatcher;
        this.defaultValue = defaultValue;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        Movie movie = (Movie) evt.getNewValue();
        if (movie != null) {
            //Dispatcher dispatcher = PredefinedDispatcher.GRABBER.getDispatcher();
            try {
                IGrabber grabber = new MovieGrabber(movie);
                grabberDispatcher.setValueObject(grabber);
            } catch (GrabberException ex) {
                //AppFrameDispatcher.showErrorDialog("Unable to intialize framegrabber");
                log.warn("Unable to intialize framegrabber");
                grabberDispatcher.setValueObject(defaultValue);
                //PredefinedDispatcher.GRABBER.reset();
            }
        }
    }
    
}
