/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.config;

import com.jfinal.core.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Routes.
 */
public abstract class Routes {

    private static String baseViewPath;
    private final Map<String, Controller> map = new HashMap<>();
    private final Map<String, String> viewPathMap = new HashMap<String, String>();

    /**
     * you must implement config method and use add method to config route
     */
    public abstract void config();

    public Routes add(Routes routes) {
        if (routes != null) {
            routes.config();    // very important!!!
            map.putAll(routes.map);
            viewPathMap.putAll(routes.viewPathMap);
        }
        return this;
    }

    /**
     * Add route
     *
     * @param controllerKey A key can find controller
     * @param controller    Controller
     * @param viewPath      View path for this Controller
     */
    public Routes add(String controllerKey, Controller controller, String viewPath) {
        if (controllerKey == null)
            throw new IllegalArgumentException("The controllerKey can not be null");
        // if (controllerKey.indexOf(".") != -1)
        // throw new IllegalArgumentException("The controllerKey can not contain dot character: \".\"");
        controllerKey = controllerKey.trim();
        if ("".equals(controllerKey))
            throw new IllegalArgumentException("The controllerKey can not be blank");
        if (controller == null)
            throw new IllegalArgumentException("The controller can not be null");
        if (!controllerKey.startsWith("/"))
            controllerKey = "/" + controllerKey;
        if (map.containsKey(controllerKey))
            throw new IllegalArgumentException("The controllerKey already exists: " + controllerKey);

        map.put(controllerKey, controller);

        if (viewPath == null || "".equals(viewPath.trim()))    // view path is controllerKey by default
            viewPath = controllerKey;

        viewPath = viewPath.trim();
        if (!viewPath.startsWith("/"))                    // "/" added to prefix
            viewPath = "/" + viewPath;

        if (!viewPath.endsWith("/"))                    // "/" added to postfix
            viewPath = viewPath + "/";

        if (baseViewPath != null)                        // support baseViewPath
            viewPath = baseViewPath + viewPath;

        viewPathMap.put(controllerKey, viewPath);
        return this;
    }

    /**
     * Add url mapping to controller. The view path is controllerKey
     *
     * @param controllerkey A key can find controller
     * @param controller    Controller
     */
    public Routes add(String controllerkey, Controller controller) {
        return add(controllerkey, controller, controllerkey);
    }

    public Set<Entry<String, Controller>> getEntrySet() {
        return map.entrySet();
    }

    public Map<String, Controller> getRouteMap() {
        return map;
    }

    public String getViewPath(String key) {
        return viewPathMap.get(key);
    }

    public String getBaseViewPath() {
        return baseViewPath;
    }

    /**
     * Set the base path for all views
     */
    static void setBaseViewPath(String baseViewPath) {
        if (baseViewPath == null)
            throw new IllegalArgumentException("The baseViewPath can not be null");
        baseViewPath = baseViewPath.trim();
        if ("".equals(baseViewPath))
            throw new IllegalArgumentException("The baseViewPath can not be blank");

        if (!baseViewPath.startsWith("/"))            // add prefix "/"
            baseViewPath = "/" + baseViewPath;

        if (baseViewPath.endsWith("/"))                // remove "/" in the end of baseViewPath
            baseViewPath = baseViewPath.substring(0, baseViewPath.length() - 1);

        Routes.baseViewPath = baseViewPath;
    }
}






