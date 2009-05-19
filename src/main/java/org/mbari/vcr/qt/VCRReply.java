package org.mbari.vcr.qt;

import org.mbari.qt.QT4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mbari.vcr.VCRReplyAdapter;
import org.mbari.vcr.IVCRState;
import org.mbari.vcr.VCRErrorAdapter;
import org.mbari.vcr.VCRUserbitsAdapter;
import quicktime.QTException;
import quicktime.std.movies.Movie;

/**
 * @author brian
 * @version $Id: $
 * @since Feb 22, 2007 8:48:05 AM PST
 */
public class VCRReply extends VCRReplyAdapter {

    private static final Logger log = LoggerFactory.getLogger(VCRReply.class);
    
    public VCRReply(Movie movie) throws QTException, QT4JException {
        this(movie, TimeSource.AUTO);
    }
    
    public VCRReply(Movie movie, TimeSource timeSource) throws QTException, QT4JException {
        vcrError = new VCRErrorAdapter();
        vcrTimecode = new VCRTimecode(movie, timeSource);
        vcrUserbits = new VCRUserbitsAdapter();
        vcrState = new VCRState(movie);
    }

    @Override
    public IVCRState getVcrState() {
        return super.getVcrState();    //To change body of overridden methods use File | Settings | File Templates.
    }
    
    @Override
    public boolean isTimecodeReply() {
        return true;
    }

    @Override
    public boolean isStatusReply() {
        return true;
    }


}
