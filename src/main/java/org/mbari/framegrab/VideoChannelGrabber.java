/*
 * VideoChannelGrabber.java
 * 
 * Created on Apr 5, 2007, 10:36:45 AM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.framegrab;

import org.mbari.qt.QT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.qd.Pict;
import quicktime.qd.QDGraphics;
import quicktime.std.StdQTException;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;

/**
 * Grabs frames from the current video channel. Use as:
 * <pre>
 * IGrabber grabber = new VideoChannelGrabber();
 * Image image = grabber.grab(); // repeast as needed
 * grabber.dispose(); // Cleanup when done.
 * </pre>
 * @author brian
 */
public class VideoChannelGrabber extends AbstractGrabber {
    
    private static final Logger log = LoggerFactory.getLogger(VideoChannelGrabber.class);
    
    /**
     * Initialized during open() by constructor
     */
    private SequenceGrabber sequenceGrabber;
    
    /**
     * Initialized during open() by constructor
     */
    private SGVideoChannel sgVideoChannel;
    

    /**
     * 
     * @throws org.mbari.framegrab.GrabberException 
     */
    public VideoChannelGrabber() throws GrabberException {
        try {
            QT.manageSession();
            sequenceGrabber = new SequenceGrabber();
            sgVideoChannel = new SGVideoChannel(sequenceGrabber);
        }
        catch (Exception e) {
            throw new GrabberException("Failed to initialize QuickTime components");
        }
    }

    protected Pict grabPict() throws QTException {
        QTSession.open();
        /*
         * Grab a Pict from the videoChannel
         */
        final QDGraphics gWorld = new QDGraphics(sgVideoChannel.getVideoRect());
        sequenceGrabber.setGWorld(gWorld, null);
        sgVideoChannel.setBounds(sgVideoChannel.getVideoRect());
        final Pict pict = sequenceGrabber.grabPict(sgVideoChannel.getVideoRect(), 0, 1);
        QTSession.close();
        return pict;
    }

    public void dispose() {
        
        try {
            if ((sequenceGrabber != null) && (sgVideoChannel != null)) {
                QT.manageSession();
                sequenceGrabber.disposeChannel(sgVideoChannel);
                sequenceGrabber.disposeQTObject();
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Trouble disposing sgVideoChannel", e);
            }
        }
    }
    
    public void showSettingsDialog() {
        try {
            sgVideoChannel.settingsDialog();
        } catch (Exception e) {
            log.warn("Unable to show QuickTime settings dialog", e);
        }
    }

}
