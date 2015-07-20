package com.hwtx.plugin;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hwtxframework.ioc.ApplicationContext;
import com.hwtxframework.ioc.ApplicationContextFactory;
import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.ComponentChangeListener;
import com.jfinal.core.ActionMapping;
import com.jfinal.core.Controller;
import com.jfinal.core.HwTx;
import com.jfinal.core.HwTxAutoBindRoutes;
import com.jfinal.kit.StrKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginState;
import ro.fortsoft.pf4j.PluginStateEvent;
import ro.fortsoft.pf4j.PluginStateListener;

import java.util.Map;
import java.util.Set;

/**
 * Created by panye on 2014/12/7.
 */
public class JFinalPluginStateListener implements PluginStateListener {

    ApplicationContext root;
    String pluginHome;
    ActionMapping actionMapping;
    Map<String, Set<String>> controllerMapping = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());

    public JFinalPluginStateListener(ApplicationContext applicationContext,
                                     ActionMapping actionMapping, String pluginHome) {
        this.root = applicationContext;
        this.pluginHome = pluginHome;
        this.actionMapping = actionMapping;
    }

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        PluginState pluginState = event.getPluginState();
        String pluginId = event.getPlugin().getPluginId();
        if (pluginState.equals(PluginState.CREATED)) {
            handlePluginCreate(event.getPlugin().getPluginId(), pluginHome + event.getPlugin().getPluginPath
                    (), event.getPlugin().getPluginClassLoader());
            logger.info("plugin " + pluginId + " loaded.");
        } else if (pluginState.equals(PluginState.STOPPED)) {
            final HwTxAutoBindRoutes routes = (HwTxAutoBindRoutes) root.getComponent(HwTx.getManagedBeanName(HwTxAutoBindRoutes.class));
            Set<String> mapping = controllerMapping.get(pluginId);
            if (mapping != null) {
                for (String path : mapping) {
                    actionMapping.removeActionMapping(path);
                    routes.getRouteMap().remove(path);
                }
            }
        }
    }

    private void handlePluginCreate(final String pluginId, String pluginPath, final ClassLoader classLoader) {
        ApplicationContext applicationContext = ApplicationContextFactory.createApplicationContext(root);
        final HwTxAutoBindRoutes routes = (HwTxAutoBindRoutes) root.getComponent(HwTx.getManagedBeanName(HwTxAutoBindRoutes.class));
        applicationContext.registerServiceChangeListener(new ComponentChangeListener() {
            @Override
            public void onComponentReady(ComponentBundle componentBundle) {
                if (componentBundle.getName().endsWith(routes.getSuffix())) {
                    Controller controller = Controller.class.cast(componentBundle.getInstance());
                    if (controller != null) {
                        String kk = routes.config(controller);
                        if (StrKit.notBlank(kk)) {
                            Set<String> mapping = controllerMapping.get(pluginId);
                            if (mapping == null) {
                                mapping = Sets.newHashSet();
                            }
                            mapping.add(kk);
                            actionMapping.addActionMapping(kk, controller);
                            controllerMapping.put(pluginId, mapping);
                        }
                    }
                }
            }
        });
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            applicationContext.loadComponentWithAnnotation(pluginPath);
        } catch (Throwable e) {
            PluginManager pluginManager = PluginManagerHolder.getPluginManager();
            pluginManager.unloadPlugin(pluginId);
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }
}
