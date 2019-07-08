package com.soaesps.core.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.BaseOperation.Filter.PredictionFilter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeSynchronizer {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(TimeSynchronizer.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private PredictionFilter<TimeMsg, Instant> filter;

    private ObjectMapper mapper = new ObjectMapper();

    private String fullUrl;

    private ConcurrentLinkedQueue<TimeMsg> queue = new ConcurrentLinkedQueue<>();

    @NotBlank
    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(@NotBlank final String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public void syncronize() {
        final TimeMsg msg = getTimeMsg();
        try {
            msg.setDelayEst(filter.getPrediction());
            Instant value = httpClient.send(fullUrl, mapper.writeValueAsString(msg));
            filter.setUpInSignal(msg);
            filter.setUpOutSignal(value);
        }
        catch (final IOException ex) {
            if(logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "[TimeSynchronizer/IOException]: ", ex);
            }
        }

    }

    public TimeMsg getTimeMsg() {
        return this.new TimeMsg();
    }

    private class TimeMsg {//implements  {
        private ZonedDateTime timeStamp;

        private Instant delayEst;

        public TimeMsg() {
            this.timeStamp = DateTimeHelper.getCurrentTimeWithTimeZone("UTC");
        }

        public TimeMsg(final Instant delayEst) {
            this.timeStamp = DateTimeHelper.getCurrentTimeWithTimeZone("UTC");
            this.delayEst = delayEst;
        }

        public ZonedDateTime getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(ZonedDateTime timeStamp) {
            this.timeStamp = timeStamp;
        }

        public Instant getDelayEst() {
            return delayEst;
        }

        public void setDelayEst(Instant delayEst) {
            this.delayEst = delayEst;
        }
    }
}