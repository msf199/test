package whitespell.model;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import whitespell.logic.UnitFilters;
import whitespell.logic.ApiDispatcher;
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

    private final String apiKey;

    public WhitespellWebServer(String apiKey) {

        this.apiKey = apiKey;
    }

    public void startAPI(int port) throws Exception{

        Server server = new Server(port);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        ApiDispatcher dispatcher = new ApiDispatcher();
        handler.addFilterWithMapping(UnitFilters.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        scheduleEndpoints(dispatcher);
        handler.addServletWithMapping(new ServletHolder(dispatcher), "/*");

        server.start();
        server.join();
    }



    protected abstract void scheduleEndpoints(ApiDispatcher dispatcher);

    public void startWebsockets() {

        Configuration config = new Configuration();
        // config.setHostname("127.0.0.1");
        config.setPort(9092);
        config.setCloseTimeout(30);
        config.setTransports(Transport.WEBSOCKET);
        SocketIOServer server = new SocketIOServer(config);
        server.start();

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                System.out.println(client.getRemoteAddress());
            }
        });
    }

    public static Cookie getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }

        return null;
    }
}
