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

package com.jfinal.render;

import com.jfinal.core.Const;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Render.
 */
public abstract class Render {

    private static String encoding = Const.DEFAULT_ENCODING;
    private static boolean devMode = Const.DEFAULT_DEV_MODE;
    protected String view;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    static final void init(String encoding, boolean devMode) {
        Render.encoding = encoding;
        Render.devMode = devMode;
    }

    public static final String getEncoding() {
        return encoding;
    }

    public static final boolean getDevMode() {
        return devMode;
    }

    public final Render setContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        return this;
    }

    public final Render setContext(HttpServletRequest request, HttpServletResponse response, String viewPath, String baseViewPath) {
        this.request = request;
        this.response = response;
        if (view != null && !view.startsWith("/"))
            view = viewPath + view;
        if (baseViewPath != null) {
            view = baseViewPath + view;
        }
        return this;
    }

    public Render setContext(HttpServletRequest request, HttpServletResponse response, String viewPath) {
        this.request = request;
        this.response = response;
        if (view != null && !view.startsWith("/"))
            view = viewPath + view;
        return this;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    /**
     * Render to client
     */
    public abstract void render();
}
