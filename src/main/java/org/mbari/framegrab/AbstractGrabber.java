/*
 * AbstractGrabber.java
 * 
 * Created on Apr 5, 2007, 11:32:56 AM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.framegrab;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import org.mbari.qt.QT;
import quicktime.QTException;
import quicktime.app.view.QTImageProducer;
import quicktime.app.view.GraphicsImporterDrawer;
import quicktime.qd.Pict;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.image.GraphicsImporter;
import quicktime.std.movies.media.DataRef;

/**
 *
 * @author brian
 */
public abstract class AbstractGrabber  implements IGrabber {
    
    private GraphicsImporter graphicsImporter;
    private GraphicsImporterDrawer graphicsImporterDrawer;

    /**
     * 
     * @throws org.mbari.framegrab.GrabberException 
     */
    public AbstractGrabber() throws GrabberException {
        try {
            QT.manageSession();
            graphicsImporter = new GraphicsImporter(StdQTConstants.kQTFileTypePicture);
            graphicsImporterDrawer = new GraphicsImporterDrawer(graphicsImporter);
        }
        catch (QTException e) {
            throw new GrabberException("Unable to intialize QuickTime objects", e);
        }
    }

    public Image grab() throws GrabberException {
        Image image = null;
        
        try {
            QT.manageSession();
            Pict pict = grabPict(); // Grab pict from child class
            
            // Add a 512-byte header that PICT would have as a file
            byte[] pictBuf = new byte[pict.getSize() + 512];
            pict.copyToArray(0, pictBuf, 512, pictBuf.length - 512);
            pict = new Pict(pictBuf);

            // Export it
            DataRef dataRef = new DataRef(pict, StdQTConstants.kDataRefQTFileTypeTag, "PICT");
            graphicsImporter.setDataReference(dataRef);
            QDRect rect = graphicsImporter.getSourceRect();
            Dimension d = new Dimension(rect.getWidth(), rect.getHeight());
            QTImageProducer imageProducer = new QTImageProducer(graphicsImporterDrawer, d);

            // Convert from MoviePlayer to java.awt.Image
            image = Toolkit.getDefaultToolkit().createImage(imageProducer);
            dataRef.disposeQTObject();
        }
        catch (Exception e) {
            throw new GrabberException("Failed to grab image from QuickTime source", e);
        }
        return image;
    }
    
    public void dispose() {
        // Nothing to do.
    }
    
    protected abstract Pict grabPict() throws QTException;

}
