package com.hwtx.framework;

import com.google.common.collect.Maps;
import com.hwtx.config.HwTxDebugPluginConfig;
import com.hwtx.plugin.PluginManagerHolder;
import com.jfinal.ext.kit.JaxbKit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.gescobar.jmx.annotation.ManagedOperation;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by panye on 14-12-18.
 */
public class PluginDebugContainer {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Getter
    PluginManager pluginManager = null;
    private Map<String, DeployTask> tasks = Maps.newHashMap();

    @Getter
    @AllArgsConstructor
    class DeployTask {
        String pluginId;
        String devPath;
        String version;
    }

    @ManagedOperation
    public void deploy() {
        for (Map.Entry<String, DeployTask> entry : tasks.entrySet()) {
            String pluginId = entry.getValue().getPluginId();
            PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
            File target = new File(PluginManagerHolder.getPluginsHome(), pluginId + "-" + entry.getValue().getVersion());
            try {
                if (pluginWrapper != null) {
                    pluginManager.unloadPlugin(pluginId);
                }
                if (target.exists()) {
                    FileUtils.forceDelete(target);
                }
                FileUtils.copyDirectory(new File(entry.getValue().getDevPath(), "plugin-classes"), new File(target, "classes"));
                FileUtils.copyDirectory(new File(entry.getValue().getDevPath(), "plugin-classes" + File.separator + "page"),
                        new File(target, "page"));
                File source = new File(entry.getValue().getDevPath(), "lib");
                if (source.exists()) {
                    FileUtils.copyDirectory(source, new File(target, "lib"));
                }
                pluginManager.loadExpandPlugin(target);
                pluginManager.startPlugin(pluginId);
            } catch (IOException e) {
                logger.error("{}", e);
                return;
            }
        }
    }

    public PluginDebugContainer(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public void start() {
        URL configFile = PluginDebugContainer.class.getClassLoader().getResource
                ("config/debug-config.xml");
        if (configFile != null) {
            try {
                HwTxDebugPluginConfig hwTxDebugConfig = JaxbKit.unmarshal(Paths.get(configFile.toURI())
                        .toFile(), HwTxDebugPluginConfig.class);

                for (HwTxDebugPluginConfig.Plugin plugin : hwTxDebugConfig.getPlugins()) {
                    if (pluginManager.getPlugin(plugin.getId()) != null) {
                        logger.warn("already contain plugin " + plugin.getId());
                    }

                    String devPath = plugin.getPath().trim();
                    if (devPath.equals(PluginManagerHolder.getPluginsHome())) {
                        logger.warn("already contain plugin " + plugin.getId());
                        continue;
                    }

                    tasks.put(plugin.getId(), new DeployTask(plugin.getId(), plugin.getPath(), plugin.getVersion()));
                }
            } catch (URISyntaxException e) {
                logger.error("{}", e);
            }
        }
    }
}
