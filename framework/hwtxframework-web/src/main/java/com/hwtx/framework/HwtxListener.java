package com.hwtx.framework;

import com.hwtx.config.HwTxConfig;
import com.hwtx.plugin.JFinalPluginStateListener;
import com.hwtx.plugin.PluginManagerHolder;
import com.hwtxframework.ioc.ApplicationContext;
import com.hwtxframework.ioc.ApplicationContextFactory;
import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.ComponentChangeListener;
import com.hwtxframework.util.Constants;
import com.jfinal.core.*;
import net.gescobar.jmx.Management;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginManager;

import javax.management.InstanceAlreadyExistsException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by panye on 2014/12/4.
 */
@WebListener
public class HwtxListener implements ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        logger.info("start framework");
        long start = System.currentTimeMillis();
        JFinal jFinal = JFinal.me();
        if (!jFinal.init(new HwTxConfig(), servletContextEvent.getServletContext()))
            throw new RuntimeException("JFinal init error!");
        ApplicationContext applicationContext = ApplicationContextFactory.createApplicationContext();

        final HwTxAutoBindRoutes routes = new HwTxAutoBindRoutes();
        applicationContext.registerServiceChangeListener(new ComponentChangeListener() {
            @Override
            public void onComponentReady(ComponentBundle componentBundle) {
                if (componentBundle.getName().endsWith(routes.getSuffix())) {
                    Controller controller = Controller.class.cast(componentBundle.getInstance());
                    if (controller != null) {
                        routes.config(controller);
                    }
                }
            }
        });
        applicationContext.loadBundlesFromFilePath("ioc/*.xml");
        ComponentBundle componentBundle = new ComponentBundle();
        componentBundle.setInstance(routes);
        componentBundle.setName(HwTx.getManagedBeanName(HwTxAutoBindRoutes.class));
        applicationContext.getReadyComponentCache().add(componentBundle);
        ActionMapping actionMapping = new ActionMapping(routes, Config.getInterceptors());
        HwTxActionHandler hwTxActionHandler = new HwTxActionHandler(actionMapping, JFinal.me().getConstants());
        componentBundle = new ComponentBundle();
        componentBundle.setInstance(hwTxActionHandler);
        componentBundle.setName("handler");
        applicationContext.getReadyComponentCache().add(componentBundle);
        servletContextEvent.getServletContext().setAttribute(Constants.ROOT_APPLICATION, applicationContext);
        logger.info("start framework completion,over " + (System.currentTimeMillis() - start) / 1000 + "s");

        logger.info("Initializing plugin manager.");
        PluginManagerHolder.init(PluginManagerHolder.getPluginsHome());
        final PluginManager pluginManager = PluginManagerHolder.getPluginManager();
        pluginManager.addPluginStateListener(new JFinalPluginStateListener(applicationContext,
                actionMapping, PluginManagerHolder.getPluginsHome()));
        logger.info("Plugin Manager initialized.");
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        actionMapping.buildActionMapping();
        logger.info("Plugins loaded.");
        logger.info("load " + pluginManager.getPlugins().size() + " plugins");
        PluginDebugContainer pluginDebugContainer = new PluginDebugContainer(pluginManager);
        try {
            Management.register(pluginDebugContainer, "org.hwtx.framework:type=gov");
        } catch (InstanceAlreadyExistsException e) {
            logger.error("{}", e);
        }
        pluginDebugContainer.start();
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ApplicationContext applicationContext = (ApplicationContext) servletContextEvent.getServletContext().getAttribute(Constants
                .ROOT_APPLICATION);
        applicationContext.close();
        logger.info("close ApplicationContext");
        logger.debug("get plugin manager");
        final PluginManager pluginManager = PluginManagerHolder.getPluginManager();
        logger.info("Stopping plugins...");
        pluginManager.stopPlugins();
        logger.info("Plugins stopped.");
    }
}
