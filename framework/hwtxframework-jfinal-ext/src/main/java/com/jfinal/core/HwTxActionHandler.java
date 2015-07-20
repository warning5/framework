package com.jfinal.core; /**
 * Copyright (c) 2011-2013, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.jfinal.aop.Invocation;
import com.jfinal.config.Constants;
import com.jfinal.handler.Handler;
import com.jfinal.log.Logger;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;
import com.jfinal.render.RenderFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class HwTxActionHandler extends Handler {

    private boolean devMode = JFinal.me().getConstants().getDevMode();
    private ActionMapping actionMapping = null;
    private static final RenderFactory renderFactory = RenderFactory.me();
    private static final Logger log = Logger.getLogger(HwTxActionHandler.class);

    public HwTxActionHandler(ActionMapping actionMapping, Constants constants) {
        this.actionMapping = actionMapping;
        this.devMode = constants.getDevMode();
    }

    public HwTxActionHandler() {
    }

    /**
     * handle 1: Action action = actionMapping.getAction(target) 2: new
     * ActionIindexnvocation(...).invoke() 3: render(...)
     */
    public final void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
        if (target.indexOf('.') != -1) {
            return;
        }
        isHandled[0] = true;
        String[] urlPara = {null};
        Action action = actionMapping.getAction(target, urlPara);
        if (action == null) {
            String method = request.getMethod();
            action = actionMapping.getAction(target + "_" + method.toLowerCase(), urlPara);
        }
        if (action == null) {
            if (log.isWarnEnabled()) {
                String qs = request.getQueryString();
                log.warn("404 Action Not Found: " + (qs == null ? target : target + "?" + qs));
            }
            renderFactory.getErrorRender(404).setContext(request, response).render();
            return;
        }
        Controller controller = action.getController();
        try {
            controller.init(request, response, urlPara[0]);
            if (devMode) {
                boolean isMultipartRequest = ActionReporter.reportCommonRequest(controller, action);
                new Invocation(action, controller).invoke();
                if (isMultipartRequest)
                    ActionReporter.reportMultipartRequest(controller, action);
            } else {
                new Invocation(action, controller).invoke();
            }

            Render render = controller.getRender();
            if (render instanceof ActionRender) {
                String actionUrl = ((ActionRender) render).getActionUrl();
                if (target.equals(actionUrl))
                    throw new RuntimeException("The forward action url is the same as before.");
                else
                    handle(actionUrl, request, response, isHandled);
                return;
            }

            String view_path = action.getViewPath();
            String base_view = action.getBaseViewPath();
            if (render == null) {
                render = renderFactory.getDefaultRender(view_path);
            }

            if (target.indexOf("plugin") > 0) {
                view_path = view_path.substring(base_view.length());
                render.setContext(request, response, "/WEB-INF/plugins" + view_path).render();
            } else {
                render.setContext(request, response, view_path, base_view).render();
            }
        } catch (RenderException e) {
            if (log.isErrorEnabled()) {
                String qs = request.getQueryString();
                log.error(qs == null ? target : target + "?" + qs, e);
            }
        } catch (ActionException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == 404 && log.isWarnEnabled()) {
                String qs = request.getQueryString();
                log.warn("404 Not Found: " + (qs == null ? target : target + "?" + qs));
            } else if (errorCode == 401 && log.isWarnEnabled()) {
                String qs = request.getQueryString();
                log.warn("401 Unauthorized: " + (qs == null ? target : target + "?" + qs));
            } else if (errorCode == 403 && log.isWarnEnabled()) {
                String qs = request.getQueryString();
                log.warn("403 Forbidden: " + (qs == null ? target : target + "?" + qs));
            } else if (log.isErrorEnabled()) {
                String qs = request.getQueryString();
                log.error(qs == null ? target : target + "?" + qs, e);
            }
            e.getErrorRender().setContext(request, response).render();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                String qs = request.getQueryString();
                log.error(qs == null ? target : target + "?" + qs, e);
            }
            if (controller != null) {
                controller.getRender().setContext(request, response).render();
            }
            renderFactory.getErrorRender(500).setContext(request, response).render();
        }
    }
}