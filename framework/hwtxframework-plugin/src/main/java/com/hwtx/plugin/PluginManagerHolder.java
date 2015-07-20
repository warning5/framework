package com.hwtx.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginStateListener;

import java.io.File;

public class PluginManagerHolder {

    public static final String PLUGIN_DIR = "pf4j.pluginsDir";
    /**
     * The plugin manager.
     */
    private static PluginManager pluginManager = null;

    /**
     * The Constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(PluginManagerHolder.class);

    /**
     * Inits the.
     *
     * @param pluginsHome the plugins home
     */
    public static void init(String pluginsHome) {
        if (pluginsHome != null) {
            pluginManager = new DefaultPluginManager(new File(pluginsHome));
        } else {
            pluginManager = new DefaultPluginManager();
        }
    }

    public static void addPluginStateListener(PluginStateListener listener) {
        if (pluginManager == null) {
            throw new RuntimeException("You must init PluginManager first");
        }
        pluginManager.addPluginStateListener(listener);
    }

    /**
     * Gets the plugin manager.
     *
     * @return the plugin manager
     */
    public static PluginManager getPluginManager() {
        if (pluginManager == null) {
            throw new RuntimeException("You must init PluginManager first");
        }
        return pluginManager;
    }

    public static String getPluginsHome() {
        return PluginManagerHolder.class.getClassLoader().getResource("../plugins").getFile();
    }
}
