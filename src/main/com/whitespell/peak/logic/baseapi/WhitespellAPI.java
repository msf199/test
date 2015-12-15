package main.com.whitespell.peak.logic.baseapi;

import main.com.whitespell.peak.logic.EndpointDispatcher;
import main.com.whitespell.peak.logic.Filters;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public abstract class WhitespellAPI {


    public void startAPI(int port) throws Exception {

        Server server = new Server(port);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        EndpointDispatcher dispatcher = new EndpointDispatcher();
        handler.addFilterWithMapping(Filters.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.addFilterWithMapping(Filters.class, "/*", EnumSet.of(DispatcherType.ASYNC));
        scheduleEndpoints(dispatcher);
        handler.addServletWithMapping(new ServletHolder(dispatcher), "/*");

        server.start();
        server.join();
    }

    protected abstract void scheduleEndpoints(EndpointDispatcher dispatcher);
}
