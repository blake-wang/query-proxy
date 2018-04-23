package com.ijunhai.util;

import org.apache.commons.cli.*;

public final class CommandLineUtils {

    private CommandLineUtils() {}

    private final static CommandLineParser parser = new DefaultParser();

    public static CommandLine genCommandLine(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("p", "port", true, "service port");
        options.addOption("l", "logback", true, "logback config file");
        return parser.parse(options, args);
    }
}
