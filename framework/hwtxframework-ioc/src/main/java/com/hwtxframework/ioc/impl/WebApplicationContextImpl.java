package com.hwtxframework.ioc.impl;


import javax.servlet.ServletContext;

public class WebApplicationContextImpl extends ApplicationContextImpl {

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    private ServletContext servletContext;

    public WebApplicationContextImpl(BaseCache readyServiceCache, BaseCache unresolvingServiceCache) {
        super(readyServiceCache, unresolvingServiceCache, null);
    }
}
