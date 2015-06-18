package whitespell.model.baseapi;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import whitespell.logic.EndpointDispatcher;
import whitespell.logic.Filters;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public abstract class WhitespellWebServer {


    public void startAPI(int port) throws Exception{

        Server server = new Server(port);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        EndpointDispatcher dispatcher = new EndpointDispatcher();
        handler.addFilterWithMapping(Filters.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        scheduleEndpoints(dispatcher);
        handler.addServletWithMapping(new ServletHolder(dispatcher), "/*");

        server.start();
        server.join();
    }



    protected abstract void scheduleEndpoints(EndpointDispatcher dispatcher);
}
