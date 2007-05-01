/*
 * Framegrabber2.java
 *
 * Created on February 9, 2006, 3:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.framegrab;

import ij.IJ;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.mbari.qt.QT4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.io.QTFile;
import quicktime.qd.Pict;
import quicktime.qd.QDGraphics;
import quicktime.std.StdQTConstants4;
import quicktime.std.StdQTException;
import quicktime.std.image.GraphicsExporter;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;

/**
 *
 * @author brian
 */
public class Framegrabber2 {
    
    
    private static final Logger log = LoggerFactory.getLogger(Framegrabber2.class);
    
    /**
     * Indicates if a frame-grab board is available
     * @uml.property  name="available"
     */
    private volatile boolean available;
    
    /**
     * Initialized during open() by constructor
     * @uml.property  name="sequenceGrabber"
     * @uml.associationEnd
     */
    private SequenceGrabber sequenceGrabber;
    
    /**
     * Initialized during open() by constructor
     * @uml.property  name="sgVideoChannel"
     * @uml.associationEnd
     */
    private SGVideoChannel sgVideoChannel;
    
    
    
    
    
    /** Creates a new instance of Framegrabber2 */
    public Framegrabber2() {
        openQuicktimeSession();
    }
    
    /**
     * Indicates if the frame grab board is available for use.
     *
     * @return    true if it's availabel, false otherwise.
     */
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * @return  the sequenceGrabber
     * @uml.property  name="sequenceGrabber"
     */
    public SequenceGrabber getSequenceGrabber() {
        if (isAvailable() && sequenceGrabber == null) {
            try {
                sequenceGrabber = new SequenceGrabber();
            } catch (QTException e1) {
                if (log.isWarnEnabled()) {
                    log.warn("Unable to instantiate a SequenceGrabber object", e1);
                }
                
                close();
            }
        }
        return sequenceGrabber;
    }
    
    /**
     * @return  the sgVideoChannel
     * @uml.property  name="sgVideoChannel"
     */
    public SGVideoChannel getSgVideoChannel() {
        if (isAvailable() && sgVideoChannel == null) {
            final SequenceGrabber sg = getSequenceGrabber();
            if (sg != null) {
                try {
                    sgVideoChannel = new SGVideoChannel(sg);
                } catch (StdQTException e2) {
                    if (log.isWarnEnabled()) {
                        log.warn(
                                "Failed to get a video channel. This system " +
                                " most likely does not have a frame grabbing " +
                                "card installed.");
                    }
                    close();
                }
            }
        }
        return sgVideoChannel;
    }
    
    
    /**
     * Startup frame grabbing session by beginning QTSession and getting a
     * VideoChannel. By default you should not need to call this since it is called
     * in the constructor.
     */
    private synchronized final void openQuicktimeSession() {
        
        // Open Quicktime session
        
        if (!QTSession.isInitialized()) {
            try {
                QTSession.open();
                available = true;
            } catch (QTException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Unable to open a QuickTime session.", e);
                }
                available = false;
            }
        }
        
        
        /*
         *  A for-sure check. mVideoChannel may be null even if exception is
         *  thrown above
         */
        if (getSgVideoChannel() == null) {
            if (log.isWarnEnabled()) {
                log.warn("SGVideoChannel is null. Frame grabbing will not work on this system.");
            }
            available = false;
        } else {
            available = true;
            
            if (log.isDebugEnabled()) {
                try {
                    log.debug(
                            "Got video channel, sourceVideoBounds = " +
                            sgVideoChannel.getSrcVideoBounds() +
                            ", videoRect = " +
                            sgVideoChannel.getVideoRect());
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Problem with SGVideoChannel", e);
                    }
                    close();
                }
            }
        }
        
    }
    
    /**
     * Captures a frame grab and returns it as a Java Image.
     *
     * @param  pictFile             Description of the Parameter
     * @throws  IOException         Thrown if unable to write the pict file to the disk.
     * @throws  QTException         Thrown if there is a problem capturing the image.
     * @throws  GrabFrameException  Thrown if a frame-capture card is not available.
     */
    public void capture(final File png) throws QT4JException {
        capture(png, StdQTConstants4.kQTFileTypePNG);
    }
    
    /**
     *
     * @param file The file to save to.
     * @param kQTFileType The type of the file. See {@link StdQTConstants4} for acceptable parameters. If you call
     *  the capture(File) method it is the same as calling capture(file, StdQTConstants4.kQTFileTypePNG).
     * @throws IOException
     * @throws QTException
     * @throws GrabFrameException
     */
    public void capture(final File file, final int kQTFileType) throws QT4JException {
        
        if (!isAvailable()) {
            throw new QT4JException(
                    "A Quicktime Session is not available. " +
                    "Is the frame-capture card installed or did you close the " +
                    "framegrabber object?");
        }
        
        /*
         *  From other example
         *  (http://www.cs.hut.fi/~samarin/T-126.103/lesson4.pdf)... Working
         *  with the RawEncodedImage object as in this example always gave me
         *  a black (blank) image. The grabPict() method does work. Just need
         *  to grab that and convert it to a .png file.
         */
        if (log.isDebugEnabled()) {
            log.debug("Grabbing a frame");
        }
        
        try {
            /*
             * Grab a Pict from the videoChannel
             */
            final QDGraphics gWorld = new QDGraphics(sgVideoChannel.getVideoRect());
            sequenceGrabber.setGWorld(gWorld, null);
            sgVideoChannel.setBounds(sgVideoChannel.getVideoRect());
            final Pict pict = sequenceGrabber.grabPict(sgVideoChannel.getVideoRect(), 0, 1);
            
            /*
             * Tip from http://www.oreillynet.com/cs/user/view/cs_msg/42209
             */
            final GraphicsExporter graphicsExporter = new GraphicsExporter(kQTFileType);
            graphicsExporter.setOutputFile(new QTFile(file.getAbsolutePath()));
            graphicsExporter.setInputPicture(pict);
            graphicsExporter.doExport();
            graphicsExporter.disposeQTObject();
            
            // Dispose of all created objects that descend from quicktime.QTObject
            gWorld.disposeQTObject();
        } catch (QTException ex) {
            throw new QT4JException(ex);
        }
    }
    
    
    
    
    /**
     * Add overlay text to the image and save as a .jpg file.
     * Show the image in a popup frame too.  Uses ImageJ API
     * for the text overlay code, specifically {@link
     * ij.process.ImageProcessor#drawString ij.process.ImageProcessor#drawString}.
     *
     * @param  image        a java.awt.Image to add the text overlay to
     * @param  jpg          Description of the Parameter
     * @param  overlayText  Description of the Parameter
     */
    public static void createJpgWithOverlay(final Image image, final File jpg,
            final String[] overlayText) {
        if (IJ.versionLessThan("1.17s")) {
            if (log.isWarnEnabled()) {
                log.warn(
                        "Unable to complete this operation. You are running a " +
                        "version of imagej less than 1.17s. Upgrade imagej!");
            }
            
            return;
        }
        
        final ImageProcessor ip = new ColorProcessor(image);
        ip.setColor(Color.cyan);
        int x = 1;
        int y = 1;
        ip.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        for (int i = 0; i < overlayText.length; i++) {
            y += 14;
            ip.moveTo(x, y);
            ip.drawString(overlayText[i] + "");
        }
        
        // Get BufferedImage and set .jpg file name
        final BufferedImage bi = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        final Graphics g = bi.getGraphics();
        g.drawImage(ip.createImage(), 0, 0, null);
        g.dispose();
        
        // Save as a jpg using ImageIO
        try {
            ImageIO.write(bi, "jpg", jpg);
            if (log.isDebugEnabled()) {
                log.debug("Created " + jpg.getAbsolutePath());
            }
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to create " + jpg.getAbsolutePath(), e);
            }
        }
    }
    
    /**
     * Closes all Channels and disposes of the QTSession.
     */
    public synchronized final void close() {
        if (QTSession.isInitialized()) {
            try {
                if ((sequenceGrabber != null) && (sgVideoChannel != null)) {
                    sequenceGrabber.disposeChannel(sgVideoChannel);
                }
            } catch (StdQTException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Trouble disposing sgVideoChannel", e);
                }
            }
        }
        
        QTSession.close();
        sequenceGrabber = null;
        sgVideoChannel = null;
        available = false;
    }
    
    public void showSettingsDialog() {
        try {
            getSgVideoChannel().settingsDialog();
        } catch (final Exception e) {
            if (log.isInfoEnabled()) {
                log.info("Unable to show QuickTime settings dialog", e);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        final Framegrabber2 g = new Framegrabber2();
        g.capture(new File("trashme.png"));
    }
    
}
