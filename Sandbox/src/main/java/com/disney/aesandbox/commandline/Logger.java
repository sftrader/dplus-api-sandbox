package com.disney.aesandbox.commandline;

/**
 * Simple logger
 */

public class Logger {

    private enum Mode { BASIC, VERBOSE};

    private Mode mode;

    private Logger(Mode mode) {
        this.mode = mode;
    }

    public static Logger newVerboseLogger() {
        return new Logger(Mode.VERBOSE);
    }

    public static Logger newBasicLogger() {
        return new Logger(Mode.BASIC);
    }

    public void log(String message) {
        System.console().writer().println(message);
    }

    public void logVerbose(String message) {
        if (mode.equals(Mode.VERBOSE)) {
            log(message);
        }
    }
}
