/*
 * FileExistsException.java
 *
 * Created on February 27, 2007, 11:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.qt;

/**
 *
 * @author brian
 */
public class FileExistsException extends QT4JException {
    
    public FileExistsException(String message) {
        super(message);
    }
    
    public FileExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FileExistsException(Throwable cause) {
        super(cause);
    }
    
}
