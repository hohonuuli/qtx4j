package org.mbari.qt;


import java.util.Set;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quicktime.QTException;
import quicktime.std.movies.Movie;
import quicktime.io.QTFile;
import quicktime.io.OpenMovieFile;



/**
 * Simple class for allowing a user to select a movie and export it in a different format.
 * 
 * @author brian
 * @version $Id: SimpleMovieExport.java 195 2007-02-28 23:06:06Z brian $
 * @since Jan 19, 2007 1:35:28 PM PST
 */
public class SimpleMovieExport {

    private static final Logger log = LoggerFactory.getLogger(SimpleMovieExport.class);

    public SimpleMovieExport() throws QTException {
        // Empty constructor
    }

    public void doExport() throws QTException {
        QT.manageSession();
        QTFile file = QTFile.standardGetFilePreview(QTFile.kStandardQTFileTypes);
        OpenMovieFile openFile = OpenMovieFile.asRead(file);
        Movie movie = Movie.fromFile(openFile);

        //build choices
        Set<MovieExportFormat> choices = QT.listMovieExportFormats(movie);
        JComboBox comboBox = new JComboBox(new Vector<MovieExportFormat>(choices));
        JOptionPane.showMessageDialog(null, comboBox, "Choose export format", JOptionPane.PLAIN_MESSAGE);
        MovieExportFormat choice = (MovieExportFormat) comboBox.getSelectedItem();
        // TODO implement export
        //QT.exportMovie(movie, choice.getSubType());
    }

    public static void main(String[] args) {
        int exitcode = 0;
        try {
            SimpleMovieExport sme = new SimpleMovieExport();
            sme.doExport();
        }
        catch (Exception e) {
            log.error("Crashed and burned", e);
            exitcode = -1;
        }
        System.exit(exitcode);   
    }

}
