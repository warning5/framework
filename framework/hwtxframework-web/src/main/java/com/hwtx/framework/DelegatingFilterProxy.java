package com.hwtx.framework;

import com.hwtxframework.ioc.ApplicationContext;
import com.hwtxframework.util.Constants;

import javax.servlet.*;
import java.io.IOException;

/**
 * Proxy for a standard Servlet 2.3 Filter, delegating to a Spring-managed
 * bean that implements the Filter interface. Supports a "targetBeanName"
 * filter init-param in {@code web.xml}, specifying the name of the
 * target bean in the Spring application context.
 * <p/>
 * <p>{@code web.xml} will usually contain a {@code DelegatingFilterProxy} definition,
 * with the specified {@code filter-name} corresponding to a bean name in
 * Spring's root application context. All calls to the filter proxy will then
 * be delegated to that bean in the Spring context, which is required to implement
 * the standard Servlet 2.3 Filter interface.
 * <p/>
 * <p>This approach is particularly useful for Filter implementation with complex
 * setup needs, allowing to apply the full Spring bean definition machinery to
 * Filter instances. Alternatively, consider standard Filter setup in combination
 * with looking up service beans from the Spring root application context.
 * <p/>
 * <p><b>NOTE:</b> The lifecycle methods defined by the Servlet Filter interface
 * will by default <i>not</i> be delegated to the target bean, relying on the
 * Spring application context to manage the lifecycle of that bean. Specifying
 * the "targetFilterLifecycle" filter init-param as "true" will enforce invocation
 * of the {@code Filter.init} and {@code Filter.destroy} lifecycle methods
 * on the target bean, letting the servlet container manage the filter lifecycle.
 * <p/>
 * <p>As of Spring 3.1, {@code DelegatingFilterProxy} has been updated to optionally accept
 * constructor parameters when using Servlet 3.0's instance-based filter registration
 * methods, usually in conjunction with Spring 3.1's
 * {@link org.springframework.web.WebApplicationInitializer} SPI. These constructors allow
 * for providing the delegate Filter bean directly, or providing the application context
 * and bean name to fetch, avoiding the need to look up the application context from the
 * ServletContext.
 * <p/>
 * <p>This class was originally inspired by Spring Security's {@code FilterToBeanProxy}
 * class, written by Ben Alex.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Chris Beams
 * @see javax.servlet.Filter#doFilter
 * @see javax.servlet.Filter#init
 * @see javax.servlet.Filter#destroy
 * @see javax.servlet.ServletContext#addFilter(String, Filter)
 * @since 1.2
 */
public class DelegatingFilterProxy implements Filter {

    private FilterConfig filterConfig;

    private Filter delegate;

    private final Object delegateMonitor = new Object();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lazily initialize the delegate if necessary.
        Filter delegateToUse;
        synchronized (this.delegateMonitor) {
            if (this.delegate == null) {
                ApplicationContext wac = (ApplicationContext) request.getServletContext().getAttribute(Constants
                        .ROOT_APPLICATION);
                if (wac == null) {
                    throw new IllegalStateException("No ApplicationContext found");
                }
                this.delegate = initDelegate(wac);
            }
            delegateToUse = this.delegate;
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(delegateToUse, request, response, filterChain);
    }

    public final void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }


    @Override
    public void destroy() {
        Filter delegateToUse;
        synchronized (this.delegateMonitor) {
            delegateToUse = this.delegate;
        }
        if (delegateToUse != null) {
            destroyDelegate(delegateToUse);
        }
    }

    /**
     * Initialize the Filter delegate, defined as bean the given Spring
     * application context.
     * <p>The default implementation fetches the bean from the application context
     * and calls the standard {@code Filter.init} method on it, passing
     * in the FilterConfig of this Filter proxy.
     *
     * @param wac the root application context
     * @return the initialized delegate Filter
     * @throws ServletException if thrown by the Filter
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    protected Filter initDelegate(ApplicationContext wac) throws ServletException {
        Filter delegate = (Filter) wac.getComponent(getFilterName());
        delegate.init(filterConfig);
        return delegate;
    }

    protected final String getFilterName() {
        return this.filterConfig.getFilterName();
    }

    protected void invokeDelegate(
            Filter delegate, ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        delegate.doFilter(request, response, filterChain);
    }

    protected void destroyDelegate(Filter delegate) {
        delegate.destroy();
    }

}
