package org.mbari.qt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates information from quicktime about an export format. Useful for UI components; in general though, it's
 * better to use QuickTime's ComponentIdentifier.
 * @author brian
 * @version $Id: MovieExportFormat.java 195 2007-02-28 23:06:06Z brian $
 * @since Jan 19, 2007 2:10:03 PM PST
 * @see org.mbari.qt.QT
 */
public class MovieExportFormat implements Comparable {

    private static final Logger log = LoggerFactory.getLogger(MovieExportFormat.class);
    private final String name;
    private final int subType;

    public MovieExportFormat(String name, int subType) {
        this.name = name;
        this.subType = subType;
    }

    /**
     * Return the human readable name of the export format
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return The QT subType code for the export format
     */
    public int getSubType() {
        return subType;
    }

    public String toString() {
        return name;
    }

    public int compareTo(Object o) {
        return toString().compareToIgnoreCase(((MovieExportFormat) o).toString());
    }

    public boolean equals(Object obj) {
        return compareTo(obj) == 0;
    }

    public int hashCode() {
        return toString().hashCode();
    }
}
