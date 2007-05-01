/*
 * IGrabber.java
 * 
 * Created on Apr 5, 2007, 10:08:36 AM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.framegrab;

import java.awt.Image;

/**
 *
 * @author brian
 */
public interface IGrabber {
    
    /**
     * 
     * @return THe image captured from the QuickTime source
     * @throws org.mbari.framegrab.GrabberException 
     */
    Image grab() throws GrabberException;
    
    /**
     * Cleanup resources
     */
    void dispose();

}
