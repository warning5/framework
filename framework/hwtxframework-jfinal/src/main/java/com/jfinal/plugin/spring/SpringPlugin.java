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

package com.jfinal.plugin.spring;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.IPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * SpringPlugin.
 */
public class SpringPlugin implements IPlugin {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private String[] configurations;
    private ApplicationContext ctx;
    private volatile boolean started = false;

    /**
     * Use configuration under the path of WebRoot/WEB-INF.
     */
    public SpringPlugin() {
    }

    public SpringPlugin(String... configurations) {
        this.configurations = configurations;
    }

    public SpringPlugin(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    public boolean start() {

        if (started) {
            logger.info("spring plugin has started.");
            return started;
        }
        if (ctx != null)
            IocInterceptor.ctx = ctx;
        else if (configurations != null)
            IocInterceptor.ctx = new FileSystemXmlApplicationContext(configurations);
        else
            IocInterceptor.ctx = new FileSystemXmlApplicationContext(PathKit.getWebRootPath() + "/WEB-INF/applicationContext.xml");

        started = true;
        return true;
    }

    public boolean stop() {
        started = false;
        return true;
    }
}
