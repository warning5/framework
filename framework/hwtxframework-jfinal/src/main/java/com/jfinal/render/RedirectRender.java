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

package com.jfinal.render;

import com.jfinal.core.JFinal;

import java.io.IOException;

/**
 * RedirectRender with status: 302 Found.
 */
public class RedirectRender extends Render {

    private static final String contextPath = getContxtPath();
    private String url;
    private boolean withQueryString;

    public RedirectRender(String url) {
        this.url = url;
        this.withQueryString = false;
    }

    public RedirectRender(String url, boolean withQueryString) {
        this.url = url;
        this.withQueryString = withQueryString;
    }

    static String getContxtPath() {
        String cp = JFinal.me().getContextPath();
        return ("".equals(cp) || "/".equals(cp)) ? null : cp;
    }

    public void render() {
        if (contextPath != null && url.indexOf("://") == -1)
            url = contextPath + url;

        if (withQueryString) {
            String queryString = request.getQueryString();
            if (queryString != null)
                if (url.indexOf("?") == -1)
                    url = url + "?" + queryString;
                else
                    url = url + "&" + queryString;
        }

        try {
            response.sendRedirect(url);    // always 302
        } catch (IOException e) {
            throw new RenderException(e);
        }
    }
}

