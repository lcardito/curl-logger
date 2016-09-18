package com.github.dzieciou.testing.curl;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Logs each HTTP request as CURL command in "curl" log.
 */
public class CurlLoggingInterceptor implements HttpRequestInterceptor {

    private final boolean logStacktrace;
    private Logger log = LoggerFactory.getLogger("curl");

    protected CurlLoggingInterceptor(Builder b) {
        this.logStacktrace = b.logStacktrace;
    }

    public static Builder defaultBuilder() {
        return new Builder();
    }

    private static void printStacktrace(StringBuffer sb) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement traceElement : trace) {
            sb.append("\tat " + traceElement + System.lineSeparator());
        }
    }

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        try {
            String curl = Http2Curl.generateCurl(request);
            StringBuffer message = new StringBuffer(curl);
            if (logStacktrace) {
                message.append(String.format("%n\tgenerated%n"));
                printStacktrace(message);
            }
            log.debug(message.toString());
        } catch (Exception e) {
            log.warn("Failed to generate CURL command for HTTP request", e);
        }
    }

    public static class Builder {

        private boolean logStacktrace = false;

        public Builder logStacktrace() {
            logStacktrace = true;
            return this;
        }

        public Builder dontLogStacktrace() {
            logStacktrace = false;
            return this;
        }

        public CurlLoggingInterceptor build() {
            return new CurlLoggingInterceptor(this);
        }

    }
}
