/*
 * Copyright 2005 MBARI
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1 
 * (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * The Monterey Bay Aquarium Research Institute (MBARI) provides this
 * documentation and code 'as is', with no warranty, express or
 * implied, of its quality or consistency. It is provided without support and
 * without obligation on the part of MBARI to assist in its use, correction,
 * modification, or enhancement. This information should not be published or
 * distributed to third parties without specific written permission from MBARI
 */
package org.mbari.framegrab;

import ij.IJ;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.view.GraphicsImporterDrawer;
import quicktime.app.view.QTImageProducer;
import quicktime.io.QTFile;
import quicktime.qd.Pict;
import quicktime.qd.QDGraphics;
import quicktime.std.StdQTException;
import quicktime.std.image.GraphicsImporter;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;

//~--- classes ----------------------------------------------------------------

/**
 * <p>
 * Class for triggering a frame grab and saving it to disk as a PICT file. The
 * correct usage of this class is:
 * </p>
 *
 * <pre>
 *
 *  Framegrabber fg = new Framegrabber();
 *  if (Framegrabber.isAvailable()) {
 *      try {
 *          fg.capture(new File(&quot;somefile.pict&quot;);
 *      }
 *      catch (Exception e) {
 *          System.out.println(&quot;Failed to create a pict image&quot;);
 *      }
 *  }
 *  fg.close();
 *
 * </pre>
 *
 * <p>This is a thread-safe class. You can keep a single instance open and reuse
 * it to capture multiple frames.</p>
 *
 *@author     <a href="http://www.mbari.org">MBARI </a>
 *@created    September 21, 2004
 *@version    $Id: Framegrabber.java 451 2007-04-05 23:58:59Z brian $
 */
public class Framegrabber {

    /**
     * A lock object used for synchronizing methods. Because it is
     * static it acts as a class level lock. i.e. Only one instance
     * of a class can execute the method at any given time.
     */
    private final static byte[] LOCK = new byte[1];
    private static final Logger log = LoggerFactory.getLogger(Framegrabber2.class);

    /**
     * Indicates if a frame-grab board is available
     */
    private static volatile boolean available;

    /**
     * Initialized during open() by constructor
     */
    private static SequenceGrabber sequenceGrabber;

    /**
     * Initialized during open() by constructor
     */
    private static SGVideoChannel sgVideoChannel;

    //~--- constructors -------------------------------------------------------

    /**
     * Default constructor. The constructor automatically opens a Quicktime
     * session if one is not already available.
     */
    public Framegrabber() {
        if (!isAvailable()) {
            open();
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Captures a frame grab and saves it to a disk as a PICT file.
     *
     * @param  pictFile             Description of the Parameter
     * @throws  IOException         Thrown if unable to write the pict file to the disk.
     * @throws  QTException         Thrown if there is a problem capturing the image.
     * @throws  GrabFrameException  Thrown if a frame-capture card is not available.
     */
    public void capture(final File pictFile)
            throws QTException, IOException, GrabberException {
        synchronized (LOCK) {
            if (!isAvailable()) {
                throw new GrabberException (
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

            if (sgVideoChannel == null) {
                if (log.isErrorEnabled()) {
                    log.error("SGVideoChannel is null, can't grab frame.");
                }

                close();
                throw new GrabberException(
                        "The SQVideoChannel is null. Is the " +
                            "frame-capture card installed?");
            }
            

            QDGraphics qdGraphics = new QDGraphics(
                sgVideoChannel.getVideoRect());
            sequenceGrabber.setGWorld(qdGraphics, null);
            sgVideoChannel.setBounds(sgVideoChannel.getVideoRect());
            Pict myPict = sequenceGrabber.grabPict(sgVideoChannel.getVideoRect(),
                0, 1);
            myPict.writeToFile(pictFile);

            if (log.isDebugEnabled()) {
                log.debug("Captured pict file: " + pictFile.getAbsolutePath());
            }

            // Dispose of all created objects that descend from quicktime.QTObject
            qdGraphics.disposeQTObject();
        }
    }

    /**
     * Closes all Channels and disposes of the QTSession.
     */
    public final void close() {
        synchronized (LOCK) {
            synchronized (QTSession.terminationLock()) {
                if (QTSession.isInitialized()) {
                    try {
                        if ((sequenceGrabber != null) &&
                                (sgVideoChannel != null)) {
                            sequenceGrabber.disposeChannel(sgVideoChannel);
                        }
                    } catch (StdQTException e) {
                        if (log.isWarnEnabled()) {
                            log.warn("Trouble disposing sgVideoChannel", e);
                        }
                    }
                }
            }

            QTSession.close();
            sequenceGrabber = null;
            sgVideoChannel = null;
            available = false;
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
            String[] overlayText) {
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
        BufferedImage bi = new BufferedImage(image.getWidth(null),
            image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = bi.getGraphics();
        g.drawImage(ip.createImage(), 0, 0, null);
        g.dispose();

        // Save as a jpg using ImageIO
        try {
            ImageIO.write(bi, "jpg", jpg);
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to create " + jpg.getAbsolutePath(), e);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Created " + jpg.getAbsolutePath());
        }

        // new ImagePlus(fileName, ip).show();
    }

    /**
     * Converts an image object to PNG.
     *
     * @param  image
     * @param  png
     * @throws  QTException
     * @throws  IOException
     */
    public void imageToPng(Image image, File png)
            throws QTException, IOException {
        BufferedImage bufferedImage = null;
        synchronized (LOCK) {
            if (!isAvailable()) {
                return;
            }

            // Make into a BufferedImage and use JRE's ImageIO conversion
            // utility
            // instead of JIMI
            bufferedImage = new BufferedImage(
                    sgVideoChannel.getVideoRect().getWidth(),
                        sgVideoChannel.getVideoRect().getHeight(),
                            BufferedImage.TYPE_3BYTE_BGR);
        }

        Graphics g = bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        ImageIO.write(bufferedImage, "png", png);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Indicates if the frame grab board is available for use.
     *
     * @return    true if it's availabel, false otherwise.
     */
    public static boolean isAvailable() {
        return available;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Startup frame grabbing session by beginning QTSession and getting a
     * VideoChannel. By default you should not need to call this since it is called
     * in the constructor.
     */
    public final void open() {

        // Open Quicktime session
        synchronized (LOCK) {
            synchronized (QTSession.terminationLock()) {
                if (!QTSession.isInitialized()) {
                    try {
                        QTSession.open();
                    } catch (QTException e) {
                        if (log.isWarnEnabled()) {
                            log.warn("Unable to open a QuickTime session.", e);
                        }

                        available = false;
                    }
                }
            }

            if (sequenceGrabber == null) {
                try {
                    sequenceGrabber = new SequenceGrabber();
                } catch (QTException e1) {
                    if (log.isWarnEnabled()) {
                        log.warn(
                                "Unable to instantiate a SequenceGrabber object",
                                e1);
                    }

                    close();
                }
            }

            // Get video channel
            if (sgVideoChannel == null) {
                try {
                    sgVideoChannel = new SGVideoChannel(sequenceGrabber);
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

            /*
             *  A for-sure check. mVideoChannel may be null even if exception is
             *  thrown above
             */
            if (sgVideoChannel == null) {
                if (log.isWarnEnabled()) {
                    log.warn(
                            " SGVideoChannel is null. Frame grabbing will not work on this system.");
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
    }

    /**
     * Converts a pict file to a png file.
     *
     * @param  pict          The pict file to  read
     * @param  png           The png file to write
     * @throws  QTException  If there is a problem with the QTSession.
     * @throws  IOException  If unable to read or write to the pict or png
     */
    public void pictToPng(File pict, File png)
            throws QTException, IOException {
        Image image = readPict(pict);
        if (image != null) {
            imageToPng(image, png);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Unable to read the pict file.");
            }
        }
    }

    /**
     * Reads a pict file and returns it as an Image
     *
     * @param  pict          The pict to be read. null is returned if Quicktime is not
     *          available.
     * @return               An image of the pict file
     * @throws  QTException
     */
    public Image readPict(File pict) throws QTException {
        Image image = null;
        synchronized (LOCK) {
            if (!isAvailable()) {
                return image;
            }

            QTFile qtFile = new QTFile(pict);
            GraphicsImporter gi = new GraphicsImporter(qtFile);
            GraphicsImporterDrawer myDrawer = new GraphicsImporterDrawer(gi);
            Dimension dim = new Dimension(
                sgVideoChannel.getVideoRect().getWidth(),
                sgVideoChannel.getVideoRect().getHeight());
            QTImageProducer qtProducer = new QTImageProducer(myDrawer, dim);
            image = Toolkit.getDefaultToolkit().createImage(qtProducer);
            gi.disposeQTObject();
        }

        return image;
    }
}
