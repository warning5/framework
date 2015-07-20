package com.hwtxframework.ioc;

import com.hwtxframework.ioc.impl.ApplicationContextImpl;
import com.hwtxframework.ioc.impl.BaseCache;
import com.hwtxframework.ioc.impl.DependencyManager;

public class ApplicationContextFactory {

    public static ApplicationContext createApplicationContext(ApplicationContext parent) {
        ApplicationContextImpl applicationContext = new ApplicationContextImpl(new BaseCache(), new BaseCache(),parent);
        applicationContext.registerServiceChangeListener(new DependencyManager(applicationContext, parent));
        return applicationContext;
    }

    public static ApplicationContext createApplicationContext() {
        return createApplicationContext(null);
    }
}
