/*
 * GrabUtil.java
 * 
 * Created on Apr 5, 2007, 12:54:19 PM
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
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.mbari.awt.image.ImageUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brian
 */
public class GrabUtil {
    
    private static final Logger log = LoggerFactory.getLogger(GrabUtil.class);

    private GrabUtil() {
        // No instantiation
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
     * @throws java.io.IOException
     */
    public static void createJpgWithOverlay(final Image image, final File jpg,
            final String[] overlayText) throws IOException {
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
        ImageIO.write(bi, "jpg", jpg);

    }
    

    /**
     * Capture and image from the grabber. The image is written to disk in a 
     * background thread so the method doesn't block while an image is written.
     * 
     * @param grabber The grabber to use to capture the image
     * @param file The name of the file to save the image to.
     * @return An AWT image object of the captured image.
     * @throws org.mbari.framegrab.GrabberException 
     */
    public static Image capture(final IGrabber grabber, final File file) throws GrabberException {
        
        Image image = null;

        if (log.isDebugEnabled()) {
            log.debug("Grabbing a frame");
        }
        
        try {
            
            /*
             * Grab a Pict from the videoChannel
             */
            image = grabber.grab();
            
            /*
             * Get the file extension to determine the image type. Split on '.'
             * Remember Java Regex is an escape nightmare so we to double escape 
             * it
             */
            final String[] fileParts = file.getName().split("\\.");
            final String ext = fileParts[fileParts.length - 1];
            
            /*
             * Save the image to disk in a seperate thread.
             */
            final Image finalImage = image;
            Runnable saveRunnable = new Runnable() {

                public void run() {
                    if (log.isDebugEnabled()) {
                        log.debug("Saving image to " + file.getAbsolutePath());
                    }
                    try {
                        //RenderedOp renderedImage = JAI.create("AWTImage", finalImage);
                        RenderedImage renderedImage = ImageUtilities.toBufferedImage(finalImage);
                        ImageIO.write(renderedImage, ext, file);
                    } catch (IOException ex) {
                        throw new RuntimeException("An error occured while trying to write to " + file.getAbsolutePath(), ex);
                    }
                }
                
            };
            
            (new Thread(saveRunnable, "ImageIO-" + file.getName())).run();

        } catch (Exception ex) {
            throw new GrabberException("Failed to create " + file.getAbsolutePath(), ex);
        }
        
        return image;
    }

}
