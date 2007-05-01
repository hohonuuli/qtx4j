/*
 * BasicQTController.java
 *
 * Created on January 11, 2007, 4:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.qt.examples;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.qt.QT;
import quicktime.QTException;
import quicktime.app.view.QTComponent;
import quicktime.app.view.QTFactory;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTException;
import quicktime.std.movies.Movie;

/**
 *
 * @author brian
 */
public class BasicQTController extends Frame {
    
    private static final Logger log = LoggerFactory.getLogger(BasicQTController.class);
    
    private Button reverseButton;
    private Button stopButton;
    private Button startButton;
    private Button forwardButton;
    Panel buttonPanel;

    
    private Movie movie;
    
    /** Creates a new instance of BasicQTController */
    public BasicQTController(Movie movie) throws QTException {
        super("Basic QT Player with Controller");
        this.movie = movie;
        initialize();
    }
    
    void initialize() throws QTException {
        QTComponent qc = QTFactory.makeQTComponent(movie);
        Component c = qc.asComponent();
        setLayout(new BorderLayout());
        add(c, BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);

        pack();
    }
    
    Panel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new Panel();
            buttonPanel.add(getReverseButton());
            buttonPanel.add(getStopButton());
            buttonPanel.add(getStartButton());
            buttonPanel.add(getForwardButton());
        }
        return buttonPanel;
    }
    
    Button getReverseButton() {
        if (reverseButton == null) {
            reverseButton = new Button("<");
            reverseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        movie.setRate(movie.getRate() - 0.5f);
                    } catch (StdQTException ex) {
                        log.error("Reverse failed", ex);
                    }
                }
            });
        }
        return reverseButton;
    }

    Button getStopButton() {
        if (stopButton == null) {
            stopButton = new Button("Stop");
            stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        movie.stop();
                    }
                    catch (StdQTException ex) {
                        log.error("Stop failed", ex);
                    }
                }
            });
        }
        return stopButton;
    }

    Button getStartButton() {
        if (startButton == null) {
            startButton = new Button("Start");
            startButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        movie.start();
                    }
                    catch (StdQTException ex) {
                        log.error("Start failed", ex);
                    }
                }
            });
        }
        return startButton;
    }

    Button getForwardButton() {
        if (forwardButton == null) {
            forwardButton = new Button(">");
            forwardButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        movie.setRate(movie.getRate() + 0.5f);
                    }
                    catch (StdQTException ex) {
                        log.error("Forward failed", ex);
                    }
                }
            });
        }
        return forwardButton;
    }

    public Movie getMovie() {
        return movie;
    }
    
    public static void main(String[] args) {
        try {
            
            QT.manageSession();
            QTFile qtFile = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
            OpenMovieFile openMovieFile = OpenMovieFile.asRead(qtFile);
            Movie m = Movie.fromFile(openMovieFile);
            Frame f = new BasicQTController(m);
            f.setVisible(true);
        } catch (QTException ex) {
            log.error("An error occurred", ex);
        }
    }

}
