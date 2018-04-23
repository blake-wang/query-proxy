package com.ijunhai;

import com.ijunhai.exception.Exceptions;
import com.ijunhai.util.*;
import org.apache.commons.cli.CommandLine;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Proxy {

    private static final Logger logger = LoggerFactory.getLogger(Proxy.class);

    public static void main(String[] args) throws Exception {
        CommandLine commandLine = CommandLineUtils.genCommandLine(args);

        if (commandLine.hasOption('l')) {
            String filePath = commandLine.getOptionValue('l');
            logger.info("logback file [{}]", filePath);
            LogBackConfigLoader.load(filePath);
        }

        int port = 8089;
        if (commandLine.hasOption('p')) {
            String portStr = commandLine.getOptionValue('p', "8089");
            port = Integer.parseInt(portStr);
        }

        Proxy proxy = new Proxy();
        proxy.start(port);
    }


    private void start(int port) {
        ResourceConfig config = new ResourceConfig();
        Exceptions.initExceptionMappers(config);
        config.packages("com.ijunhai.resource");

        ServletHolder servlet = new ServletHolder(new ServletContainer(config));

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");

        try {
            logger.info("start proxy [port: {}] ...", port);
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.destroy();
        }
    }


}
