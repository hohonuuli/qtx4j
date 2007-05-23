/*
 * MovieChangeListener4QTMOVIEFRAME.java
 *
 * Created on May 22, 2007, 7:46:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.vars.annotation.ui.dispatchers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.mbari.qt.QT;
import org.mbari.util.Dispatcher;
import quicktime.std.movies.Movie;

/**
 *
 * @author brian
 */
public class MovieChangeListener4QTMOVIEFRAME implements PropertyChangeListener{
    
    private final Dispatcher qtMovieFrameDispatcher;
    private final Object defaultValue;
    
    /**
     * @param qtMovieFrameDispatcher Should be PredefinedDispatcher.QTMOVIEFRAME.getDispatcher()
     * @param defaultValue Should be PredefinedDispatcher.QTMOVIEFRAME.getDefaultValue()
     */
    public MovieChangeListener4QTMOVIEFRAME(final Dispatcher qtMovieFrameDispatcher, Object defaultValue) {
        this.qtMovieFrameDispatcher = qtMovieFrameDispatcher;
        this.defaultValue = defaultValue;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        Movie movie = (Movie) evt.getNewValue();
        if (movie != null) {
            
            // TODO Need to remove all refs to QuickTime Classes.
            qtMovieFrameDispatcher.setValueObject(QT.playMovie(movie));
            //PredefinedDispatcher.QTMOVIEFRAME.getDispatcher().setValueObject(QT.playMovie(movie));
        } else {
            qtMovieFrameDispatcher.setValueObject(defaultValue);
           // PredefinedDispatcher.QTMOVIEFRAME.reset();
        }
    }
    
}
