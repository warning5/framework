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

package com.jfinal.handler;

import java.util.List;

/**
 * HandlerFactory.
 */
public class HandlerFactory {

    private HandlerFactory() {

    }

    /**
     * Build handler chain
     */
    public static Handler getHandler(List<Handler> handlerList, Handler actionHandler) {
        Handler result = actionHandler;

        for (int i = handlerList.size() - 1; i >= 0; i--) {
            Handler temp = handlerList.get(i);
            temp.nextHandler = result;
            result = temp;
        }

        return result;
    }
}




