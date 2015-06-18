package whitespell.model.baseapi;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import whitespell.logic.EndpointDispatcher;
import whitespell.logic.Filters;
import whitespell.net.websockets.socketio.Configuration;
import whitespell.net.websockets.socketio.SocketIOClient;
import whitespell.net.websockets.socketio.SocketIOServer;
import whitespell.net.websockets.socketio.Transport;
import whitespell.net.websockets.socketio.listener.ConnectListener;

import javax.servlet.DispatcherType;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.EnumSet;

public abstract class WhitespellWebServer {


    public WhitespellWebServer(String apiKey) {

        this.apiKey = apiKey;
    }

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
