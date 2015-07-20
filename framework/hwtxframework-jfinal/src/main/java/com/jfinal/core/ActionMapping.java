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

import com.hwtx.annotation.RequestMethod;
import com.jfinal.aop.Interceptor;
import com.jfinal.config.Interceptors;
import com.jfinal.config.Routes;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

/**
 * ActionMapping
 */
public final class ActionMapping {

    private static final String SLASH = "/";
    private final Map<String, Action> mapping = new HashMap<String, Action>();
    private Routes routes;
    private Interceptors interceptors;

    public ActionMapping(Routes routes, Interceptors interceptors) {
        this.routes = routes;
        this.interceptors = interceptors;
    }

    private static final String buildMsg(String actionKey, Class<? extends Controller> controllerClass, Method method) {
        StringBuilder sb = new StringBuilder("The action \"")
                .append(controllerClass.getName()).append(".")
                .append(method.getName()).append("()\" can not be mapped, ")
                .append("actionKey \"").append(actionKey).append("\" is already in use.");

        String msg = sb.toString();
        System.err.println("\nException: " + msg);
        return msg;
    }

    private Set<String> buildExcludedMethodName() {
        Set<String> excludedMethodName = new HashSet<String>();
        Method[] methods = Controller.class.getMethods();
        for (Method m : methods) {
            if (m.getParameterTypes().length == 0)
                excludedMethodName.add(m.getName());
        }
        return excludedMethodName;
    }

    public void removeActionMapping(String key) {
        Iterator<String> it = mapping.keySet().iterator();
        while (it.hasNext()) {
            if (it.next().indexOf(key) >= 0) {
                it.remove();
            }
        }
    }

    public void buildActionMapping() {
        mapping.clear();
        Set<String> excludedMethodName = buildExcludedMethodName();
        ActionInterceptorBuilder interceptorBuilder = new ActionInterceptorBuilder();
        Interceptor[] globalInters = interceptors.getGlobalActionInterceptor();
        interceptorBuilder.addToInterceptorsMap(globalInters);
        for (Entry<String, Controller> entry : routes.getEntrySet()) {
            buildInternal(excludedMethodName, interceptorBuilder, globalInters, entry.getKey(), entry.getValue());
        }

        // support url = controllerKey + urlParas with "/" of controllerKey
        Action actoin = mapping.get("/");
        if (actoin != null)
            mapping.put("", actoin);
    }

    public void addActionMapping(String bind, Controller controller) {
        Set<String> excludedMethodName = buildExcludedMethodName();
        ActionInterceptorBuilder interceptorBuilder = new ActionInterceptorBuilder();
        Interceptor[] globalInters = interceptors.getGlobalActionInterceptor();
        interceptorBuilder.addToInterceptorsMap(globalInters);
        buildInternal(excludedMethodName, interceptorBuilder, globalInters, bind, controller);
    }

    private void buildInternal(Set<String> excludedMethodName, ActionInterceptorBuilder interceptorBuilder, Interceptor[] globalInters,
                               String controllerKey, Controller controller) {
        Class<? extends Controller> controllerClass = controller.getClass();
        Interceptor[] controllerInters = interceptorBuilder.buildControllerInterceptors(controllerClass);
        Method[] methods = controllerClass.getMethods();
        boolean sonOfController = (controllerClass.getSuperclass() == Controller.class);
        for (Method method : methods) {
            String methodName = method.getName();
            if (excludedMethodName.contains(methodName) || method.getParameterTypes().length != 0)
                continue;
            if (sonOfController && !Modifier.isPublic(method.getModifiers()))
                continue;

            Interceptor[] methodInters = interceptorBuilder.buildMethodInterceptors(method);
            Interceptor[] actionInters = interceptorBuilder.buildActionInterceptors(globalInters, controllerInters, methodInters, method);

            ActionKey ak = method.getAnnotation(ActionKey.class);
            if (ak != null) {
                String[] actionKeys = ak.value();
                for (String actionKey : actionKeys) {
                    actionKey = actionKey.trim();
                    if (PathKit.isVariable(actionKey)) {
                        actionKey = PathKit.getVariableValue(actionKey);
                        if (StrKit.isBlank(actionKey)) {
                            throw new RuntimeException("can't fing any variable of " + ak.value());
                        }
                    }

                    if ("".equals(actionKey))
                        throw new IllegalArgumentException(controllerClass.getName() + "." + methodName
                                + "(): The argument of ActionKey can not be blank.");

                    if (!actionKey.startsWith(SLASH))
                        actionKey = SLASH + actionKey;

                    RequestMethod requestMethod = ak.method();
                    String copyKey = actionKey;

                    if (requestMethod != RequestMethod.NONE) {
                        copyKey += "_" + requestMethod.name().toLowerCase();
                    }

                    if (!ak.self()) {
                        copyKey = controllerKey + copyKey;
                    }

                    if (mapping.containsKey(copyKey)) {
                        buildMsg(copyKey, controllerClass, method);
                        continue;
                    }
                    Action action = new Action(controllerKey, actionKey, controller, method, methodName, actionInters,
                            routes.getViewPath(controllerKey), routes.getBaseViewPath());
                    mapping.put(copyKey, action);
                }
            } else if (methodName.equals("index")) {
                String actionKey = controllerKey;

                Action action = new Action(controllerKey, actionKey, controller, method, methodName, actionInters,
                        routes.getViewPath(controllerKey), routes.getBaseViewPath());
                action = mapping.put(actionKey, action);

                if (action != null) {
                    buildMsg(action.getActionKey(), controllerClass, action.getMethod());
                }
            } else {
                String actionKey = controllerKey.equals(SLASH) ? SLASH + methodName : controllerKey + SLASH + methodName;

                if (mapping.containsKey(actionKey)) {
                    buildMsg(actionKey, controllerClass, method);
                    continue;
                }

                Action action = new Action(controllerKey, actionKey, controller, method, methodName, actionInters,
                        routes.getViewPath(controllerKey), routes.getBaseViewPath());
                if (mapping.put(actionKey, action) != null)
                    throw new RuntimeException(buildMsg(actionKey, controllerClass, method));
            }
        }
        // support url = controllerKey + urlParas with "/" of controllerKey
        Action actoin = mapping.get("/");
        if (actoin != null)
            mapping.put("", actoin);
    }

    /**
     * Support four types of url
     * 1: http://abc.com/controllerKey                 ---> 00
     * 2: http://abc.com/controllerKey/para            ---> 01
     * 3: http://abc.com/controllerKey/method          ---> 10
     * 4: http://abc.com/controllerKey/method/para     ---> 11
     */
    Action getAction(String url, String[] urlPara) {
        Action action = mapping.get(url);
        if (action != null) {
            return action;
        }

        int i = url.lastIndexOf(SLASH);
        if (i != -1) {
            action = mapping.get(url.substring(0, i));
            urlPara[0] = url.substring(i + 1);
        }

        return action;
    }

    List<String> getAllActionKeys() {
        List<String> allActionKeys = new ArrayList<String>(mapping.keySet());
        Collections.sort(allActionKeys);
        return allActionKeys;
    }
}
