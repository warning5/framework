package com.hwtxframework.ioc.impl;

import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.ComponentChangeListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BaseCache {
    protected ConcurrentMap<String, ComponentBundle> cache = new ConcurrentHashMap<String, ComponentBundle>();
    protected Set<ComponentChangeListener> listeners = new HashSet<ComponentChangeListener>();

    protected void notifyListenersServiceReady(ComponentBundle obj) {
        for (ComponentChangeListener listen : listeners) {
            listen.onComponentReady(obj);
        }
    }

    public boolean contain(String name) {
        return cache.containsKey(name);
    }

    public void add(ComponentBundle obj) {

        boolean ntf = false;
        synchronized (cache) {
            if (!cache.containsKey(obj.getName())) {
                ntf = (cache.put(obj.getName(), obj) == null);
            }
        }
        if (ntf) {
            notifyListenersServiceReady(obj);
        }
    }

    public void remove(ComponentBundle obj) {
        cache.remove(obj.getName());
    }

    public Object getComponent(String name) {
        ComponentBundle bundle = cache.get(name);
        if (bundle != null) {
            return bundle.getInstance();
        }
        return null;
    }

    public void registerComponentChangeListener(ComponentChangeListener... listener) {
        listeners.addAll(Arrays.asList(listener));
    }

    public ComponentBundle findBundleByReference(String name) {
        return cache.get(name);
    }

    public Collection<ComponentBundle> getServiceBundles() {
        return cache.values();
    }

    public void removeBundleByName(String name) {
        synchronized (cache) {
            cache.remove(name);
        }
    }

    public void close() {
        cache = null;
        listeners = null;
    }
}
