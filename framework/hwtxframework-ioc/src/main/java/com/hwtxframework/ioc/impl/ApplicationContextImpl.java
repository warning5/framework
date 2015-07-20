package com.hwtxframework.ioc.impl;


import com.hwtxframework.ioc.ApplicationContext;

public class ApplicationContextImpl extends AbstractApplicationContext {

    public ApplicationContextImpl(BaseCache readyServiceCache,
                                  BaseCache unresolvingServiceCache, ApplicationContext parent) {
        super(readyServiceCache, unresolvingServiceCache, parent);
    }

    public Object getComponent(String name) {
        if (unresolvingComponentCache.contain(name)) {
            String cause = unresolvingCausing.get(name);
            throw new RuntimeException("can't resolve component " + name + " by " + cause);
        }
        return readyComponentCache.getComponent(name);
    }
}
