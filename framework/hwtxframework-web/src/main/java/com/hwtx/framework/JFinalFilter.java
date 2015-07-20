package com.hwtx.framework;

import com.hwtxframework.ioc.ApplicationContext;
import com.jfinal.core.Config;
import com.jfinal.core.JFinal;
import com.jfinal.handler.Handler;
import com.hwtxframework.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JFinal framework filter
 */
public final class JFinalFilter implements Filter {

    private Handler handler;
    private String encoding;
    private static final JFinal jfinal = JFinal.me();
    private static Logger logger = LoggerFactory.getLogger(JFinalFilter.class);
    private int contextPathLength;

    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext applicationContext = (ApplicationContext) filterConfig.getServletContext().getAttribute(Constants
                .ROOT_APPLICATION);
        handler = (Handler) applicationContext.getComponent("handler");
        encoding = Config.getConstants().getEncoding();
        String contextPath = filterConfig.getServletContext().getContextPath();
        contextPathLength = (contextPath == null || "/".equals(contextPath) ? 0 : contextPath.length());
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        request.setCharacterEncoding(encoding);

        String target = request.getRequestURI();
        if (contextPathLength != 0) {
            target = target.substring(contextPathLength);
        }

        int index = target.indexOf(";");
        if (index > 0) {
            target = target.substring(0, index);
        }

        boolean[] isHandled = {false};
        try {
            handler.handle(target, request, response, isHandled);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                String qs = request.getQueryString();
                logger.error(qs == null ? target : target + "?" + qs, e);
            }
        }

        if (isHandled[0] == false)
            chain.doFilter(request, response);
    }

    public void destroy() {
        jfinal.stopPlugins();
    }
}
