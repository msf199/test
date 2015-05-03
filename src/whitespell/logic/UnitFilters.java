package whitespell.logic;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/14/15
 *         whitespell.model
 */
public class UnitFilters implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * doFilter adds the required filters for the API to function as an API.
     * It also handles the session checking and creation, and gives out a Request ID for each request
     * so that a request response for an ad can be mapped to a websocket connection.
     *
     * @param servletRequest                The request we're dealing with
     * @param servletResponse               The response we're generating
     * @param filterChain                   //todo
     * @throws IOException                  If the request is closed and you can't write to it anymore
     * @throws ServletException             //todo
     */

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (((HttpServletRequest) servletRequest).getRequestURI().contains("favicon.ico")) {
            ((HttpServletResponse) servletResponse).setStatus(404);
            servletResponse.getWriter().close();
        }

        /*addSessionHeader((HttpServletResponse) servletResponse,
                WhitespellWebServer.getCookie(((HttpServletRequest) servletRequest), "Session"));*/


        ((HttpServletResponse) servletResponse).addHeader("Server", "Whitespell API Server");
        ((HttpServletResponse) servletResponse).addHeader("Request-id", UUID.randomUUID().toString());
        servletResponse.setContentType("application/json");

        System.out.println("request: " + servletRequest.getRemoteAddr());
        System.out.println("response: " + servletResponse.getContentType());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * addSessionHeader adds the proper session header to the response and sets the right cookie with the user.
     * This cookie is used to maintain your session throughout your entire history.
     * @param response      the HTTPServletResponse to write to
     * @param cookie        The current session cookie, this can be null if no cookie is found, or the cookie could be outdated or not properly set.
     */

    private void addSessionHeader(HttpServletResponse response, Cookie cookie) {
        if (cookie == null) {
            // no cookie was set
            String session = SessionHandler.generateSessionId();
            response.addHeader("Session", session);
            cookie = new Cookie("Session", session);
            cookie.setMaxAge(-1);
            cookie.setDomain("localhost");
            cookie.setPath("/");
            response.addCookie(cookie);
            System.out.println("Generating new cookie fresh" + cookie.getValue());
        } else {
            if(cookie.getValue() != null) {
                // cookie was set but forgotten
                if(SessionHandler.sessionExists(cookie.getValue())) {
                   System.out.println("Successfully authenticated session" + cookie.getValue());
                } else {
                    System.out.println("Generating new session because cookie is expired");
                    String session = SessionHandler.generateSessionId();
                    cookie.setValue(session);
                }
            }
        }
        response.addHeader("Session", cookie.getValue());
    }

    @Override
    public void destroy() {

    }
}
