package main.com.whitespell.peak.logic;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         2/14/15
 *         whitespell.model
 */
public class Filters implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * doFilter adds the required filters for the API to function as an API.
     *
     * @param servletRequest  The request we're dealing with
     * @param servletResponse The response we're generating
     * @param filterChain     //todo
     * @throws IOException      If the request is closed and you can't write to it anymore
     * @throws ServletException //todo
     */

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        if (((HttpServletRequest) servletRequest).getRequestURI().contains("favicon.ico")) {
            ((HttpServletResponse) servletResponse).setStatus(404);
            servletResponse.getWriter().close();
        }

        // These headers allow CORS requests.
        ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, x-Authentication");
        ((HttpServletResponse) servletResponse).addHeader("Server", "Whitespell Server");
        //((HttpServletResponse) servletResponse).addHeader("WWW-Authenticate", "Basic realm=\"0\"");
        servletResponse.setContentType("application/json");
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
