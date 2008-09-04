/*
 * TestUtil.java
 *
 * Created on February 22, 2007, 3:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.mbari.qt;

import java.net.URL;

/**
 *
 * @author brian
 */
public class SupportUtil {
    
    //public static final String DEFAULT_MOVIE = "/wonderwall.mov";
    public static final String DEFAULT_MOVIE = "/20060927T09250000Z-h264.mov";
    public static final String TIMECODE_MOVIE = "/20060927T09250000Z.mov";
    public static final float FRAMERATE_NETWORK = 29.97F;
    public static final float FRAMERATE_DEFAULT = 29.97F;
    public static final float FRAMERATE_TIMECODE = 29.97F;
            
    
    /** Creates a new instance of TestUtil */
    private SupportUtil() {
        // No instantiation
    }
    
    public static String getLocalMovieFile(String ref) {
        URL url = (new SupportUtil()).getClass().getResource(ref);
        /*
         * Java URL.toExternalForm() returns a url like:
         * file:/Users/brian/workspace/qt4j/target/test-classes/wonderwall.mov
         * but QuickTime expects a format like:
         * file:///Users/brian/workspace/qt4j/target/test-classes/wonderwall.mov
         *
         * We'll convert it to a path we like here
         */
        return url.getProtocol() + "://" + url.getPath();
    }
    
    /**
     * 
     * @return 
     */
    public static String getNetworkMovieFile() {
        return "https://oceana.mbari.org/projects/aved-db/tests/2613_02_23_59_20.results.mpeg";
    }
    
    public static String[] getTestMovies() {
        return new String[]{
            getLocalMovieFile(DEFAULT_MOVIE),
            getLocalMovieFile(TIMECODE_MOVIE),
            getNetworkMovieFile()
        };
    }
    
}
