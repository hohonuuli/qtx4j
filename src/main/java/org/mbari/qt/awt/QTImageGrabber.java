package org.mbari.qt.awt;

import org.mbari.qt.QT;
import org.mbari.movie.Timecode;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.mbari.qt.TimeUtil;

import quicktime.QTException;
import quicktime.qd.QDRect;
import quicktime.qd.Pict;
import quicktime.app.view.*;
import quicktime.std.movies.Movie;
import quicktime.std.movies.MovieController;
import quicktime.std.movies.media.DataRef;
import quicktime.std.StdQTConstants;
import quicktime.std.image.GraphicsImporter;
import quicktime.io.QTFile;
import quicktime.io.OpenMovieFile;

import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author brian
 * @version $Id: QTImageGrabber.java 195 2007-02-28 23:06:06Z brian $
 * @since Jan 22, 2007 1:49:14 PM PST
 */
public class QTImageGrabber extends Frame {

    private static final Logger log = LoggerFactory.getLogger(QTImageGrabber.class);
    private Movie movie;
    private MoviePlayer player;
    private MovieController controller;
    private QTComponent qtComponent;
    private GraphicsImporter graphicsImporter;
    private GraphicsImporterDrawer graphicsImporterDrawer;
    static int nextFrameX;
    static int nextFrameY;


    public QTImageGrabber() throws QTException {
        super("QuickTime Movie");
        QT.manageSession();
        graphicsImporter = new GraphicsImporter(StdQTConstants.kQTFileTypePicture);
        graphicsImporterDrawer = new GraphicsImporterDrawer(graphicsImporter);

        // Build the UI
        initialize();
    }

    private void initialize() throws QTException {
        // Get a movie and UI parts
        QTFile file = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
        OpenMovieFile omFile = OpenMovieFile.asRead(file);
        movie = Movie.fromFile(omFile);
        player = new MoviePlayer(movie);
        controller = new MovieController(movie);
        qtComponent = QTFactory.makeQTComponent(movie);
        Component c = qtComponent.asComponent();

        // Layout UI
        setLayout(new BorderLayout());
        add(c, BorderLayout.CENTER);
        Button imageButton = new Button("Grab Image");
        add(imageButton, BorderLayout.SOUTH);
        imageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    grab();
                }
                catch (QTException e1) {
                    log.error("Failed to capture an image", e1);
                }
            }
        });
        movie.start();
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                try {
                    movie.stop();
                    graphicsImporter.disposeQTObject();
                    movie.disposeQTObject();
                }
                catch (QTException e1) {
                    log.error("Problem with cleanup of QT", e1);
                }
                System.exit(0);
            }
        });
    }

    public void grab() throws QTException {
        // Stop movie to take a picture
        float rate = movie.getRate();
        if (rate > 0) {
            movie.stop();
        }

        // Take a PICT
        Pict pict = movie.getPict(movie.getTime());

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
        Image image = Toolkit.getDefaultToolkit().createImage(imageProducer);
        dataRef.disposeQTObject();

        // Display it
        ImageIcon icon = new ImageIcon(image);
        JLabel label = new JLabel(icon);
        Timecode runtime = new Timecode();
        runtime.setFrameRate(TimeUtil.estimateFrameRate(movie));
        TimeUtil.queryRuntime(movie, runtime);
        JFrame frame = new JFrame("Java Image at " + runtime.toString());
        frame.add(label);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocation(nextFrameX += 10, nextFrameY += 10);
        frame.setVisible(true);

        movie.setRate(rate);
    }

    public static void main(String[] args){
        try {
            QTImageGrabber grabber = new QTImageGrabber();
            grabber.pack();
            Rectangle bounds = grabber.getBounds();
            nextFrameX = bounds.x + bounds.width;
            nextFrameY = bounds.y + bounds.height;
            grabber.setVisible(true);
        }
        catch (QTException e) {
            log.error("Failed", e);
        }
        catch (Exception e) {
            log.error("Really really bombed", e);
        }

    }


}
