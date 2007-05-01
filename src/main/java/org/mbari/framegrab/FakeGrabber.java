/*
 * FakeGrabber.java
 * 
 * Created on Apr 5, 2007, 1:40:10 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.framegrab;

import java.awt.Image;

/**
 * An IGrabber that does nothing
 * @author brian
 */
public class FakeGrabber implements IGrabber {


    /**
     * Constructor
     */
    public FakeGrabber() {
    }

    /**
     * @return null
     */
    public Image grab() throws GrabberException {
        return null;
    }

    public void dispose() {
        // Nothing to do
    }

}
