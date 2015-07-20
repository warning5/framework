/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
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

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;

/**
 * IocInterceptor.
 */
public class IocInterceptor implements Interceptor {

    public static ApplicationContext ctx;

    public void intercept(Invocation ai) {
        Controller controller = ai.getController();
        Field[] fields = controller.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object bean = null;
            if (field.isAnnotationPresent(Inject.BY_NAME.class))
                bean = ctx.getBean(field.getName());
            else if (field.isAnnotationPresent(Inject.BY_TYPE.class))
                bean = ctx.getBean(field.getType());
            else
                continue;

            try {
                if (bean != null) {
                    field.setAccessible(true);
                    field.set(controller, bean);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ai.invoke();
    }
}
