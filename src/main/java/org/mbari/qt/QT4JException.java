/*
 * QT4JException.java
 *
 * Created on February 12, 2007, 4:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.qt;

/**
 *
 * @author brian
 */
public class QT4JException extends Exception {
    
    /** Creates a new instance of QT4JException */
    public QT4JException(String message) {
        super(message);
    }
    
    public QT4JException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public QT4JException(Throwable cause) {
        super(cause);
    }
        
}
