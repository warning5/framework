/**
 * Copyright (c) 2011-2013, James Zhan 詹波 (jfinal@126.com).
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.core;

import com.jfinal.config.Constants;
import com.jfinal.config.JFinalConfig;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.render.RenderFactory;
import com.jfinal.token.ITokenCache;
import com.jfinal.token.TokenManager;
import com.jfinal.upload.OreillyCos;

import javax.servlet.ServletContext;
import java.util.List;

/**
 * JFinal
 */
public final class JFinal {

    private static final JFinal me = new JFinal();
    private Constants constants;
    private ServletContext servletContext;
    private String contextPath = "";

    private JFinal() {
    }

    public static JFinal me() {
        return me;
    }

    public boolean init(JFinalConfig jfinalConfig, ServletContext servletContext) {
        this.servletContext = servletContext;
        this.contextPath = servletContext.getContextPath();

        initPathUtil();

        Config.configJFinal(jfinalConfig); // start plugin and init logger
        // factory in this method
        constants = Config.getConstants();

//        initActionMapping();
        initRender();
        initOreillyCos();

        initTokenManager();

        return true;
    }

    private void initTokenManager() {
        ITokenCache tokenCache = constants.getTokenCache();
        if (tokenCache != null)
            TokenManager.init(tokenCache);
    }


    private void initOreillyCos() {

        OreillyCos.init(constants.getUploadedFileSaveDirectory(), constants.getMaxPostSize(), constants.getEncoding());

    }

    private void initPathUtil() {
        String path = servletContext.getRealPath("/");
        PathKit.setWebRootPath(path);
    }

    private void initRender() {
        RenderFactory renderFactory = RenderFactory.me();
        renderFactory.init(constants, servletContext);
    }

    public void stopPlugins() {
        List<IPlugin> plugins = Config.getPlugins().getPluginList();
        if (plugins != null) {
            for (int i = plugins.size() - 1; i >= 0; i--) { // stop plugins
                boolean success = false;
                try {
                    success = plugins.get(i).stop();
                } catch (Exception e) {
                    success = false;
                    e.printStackTrace();
                }
                if (!success) {
                    System.err.println("Plugin stop error: " + plugins.get(i).getClass().getName());
                }
            }
        }
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public Constants getConstants() {
        return Config.getConstants();
    }

    public String getContextPath() {
        return contextPath;
    }
}
