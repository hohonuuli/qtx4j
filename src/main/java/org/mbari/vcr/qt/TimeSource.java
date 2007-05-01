/*
 * TimeSource.java
 *
 * Created on March 14, 2007, 11:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.vcr.qt;

public enum TimeSource {
    TIMECODETRACK("Time-code Track"),
    RUNTIME("Elapsed Time"),
    AUTO("Automatic");

    private String description;

    TimeSource(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

}
