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


package org.mbari.framegrab;

import org.mbari.qt.QT4JException;


/**
 * <p>Exception that is throws when an attempt frame-capture fails.</p>
 *
 * @author <a href="http://www.mbari.org">MBARI</a>
 * @version $Id: GrabFrameException.java 209 2007-03-01 19:15:59Z brian $
 */
public class GrabberException extends QT4JException {

    /**
     *
     */
    private static final long serialVersionUID = -2835613917581593561L;


    /**
     *     @param string
     */
    public GrabberException(String string) {
        super(string);
    }
    
    /**
     * 
     * @param string 
     * @param cause 
     */
    public GrabberException(String string, Throwable cause) {
        super(string, cause);
    }
    
}
