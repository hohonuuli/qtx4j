/*
 * MovieChangeListener4VCR.java
 *
 * Created on May 22, 2007, 7:31:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.vars.annotation.ui.dispatchers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.mbari.util.Dispatcher;
import org.mbari.vcr.IVCR;
import org.mbari.vcr.StateMonitoringVCR;
import org.mbari.vcr.qt.TimeSource;
import org.mbari.vcr.qt.VCR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.std.movies.Movie;

/**
 *
 * @author brian
 */
public class MovieChangeListener4VCR implements PropertyChangeListener {
    
    private final Dispatcher vcrDispatcher;
    private final IVCR defaultVcr;
    
    private static final Logger log = LoggerFactory.getLogger(MovieChangeListener4VCR.class);
    /**
     * @param vcrDispatcher Should be PredefinedDispatcher.VCR.getDispatcher()
     * @param defaultVcr Should be PredefinedDispatcher.VCR.getDefaultValue()
     */
    public MovieChangeListener4VCR(Dispatcher vcrDispatcher, IVCR defaultVcr) {
        this.vcrDispatcher = vcrDispatcher;
        this.defaultVcr = defaultVcr;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        Movie movie = (Movie) evt.getNewValue();
        
        if (movie != null) {
            // Close old VCR -> equivalent to calling PredefinedDispatcher.VCR.reset();
            vcrDispatcher.setValueObject(defaultVcr);
            
            try {
                IVCR vcr = new StateMonitoringVCR(new VCR(movie, TimeSource.TIMECODETRACK));
                vcrDispatcher.setValueObject(vcr);
                //PredefinedDispatcher.VCR.getDispatcher().setValueObject(vcr);
            } catch (Exception e) {
                log.error("Error occurred while creating VCR", e);
                vcrDispatcher.setValueObject(defaultVcr);
                //PredefinedDispatcher.VCR.reset();
            }
            
        }
    }
    
}
