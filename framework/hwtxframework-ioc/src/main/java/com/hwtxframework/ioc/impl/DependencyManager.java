package com.hwtxframework.ioc.impl;

import com.hwtxframework.ioc.ApplicationContext;
import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.ComponentChangeListener;
import com.hwtxframework.ioc.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DependencyManager implements ComponentChangeListener {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext context;
    private ApplicationContext parent;

    public DependencyManager(ApplicationContext context, ApplicationContext parent) {
        this.context = context;
        this.parent = parent;
    }

    public void onComponentReady(ComponentBundle instance) {
        Set<ComponentBundle> bundles = new HashSet<>();
        BaseCache unResolvedCache = context.getUnresolvingComponentCache();
        BaseCache readyCache = context.getReadyComponentCache();
        Map<String, String> unresolvingCausing = context.getUnresolvingCausing();
        Collection<ComponentBundle> dependencyI = context.getDependencyI(instance.getName());

        for (ComponentBundle bundle : unResolvedCache.getServiceBundles()) {
            if (dependencyI.contains(bundle)) {
                bundles.add(unResolvedCache.findBundleByReference(bundle.getName()));
            }
        }
        for (ComponentBundle bundle : bundles) {
            String _available_field = BundleUtil.isAllReferenceAvailable(bundle, parent, readyCache);
            if (Constants.AVAILABLE.equals(_available_field)) {
                try {
                    BundleUtil.injectReference(bundle, parent, readyCache, context.getPropertiesLoaderSupport());
                    readyCache.add(bundle);
                    unResolvedCache.remove(bundle);
                    context.getUnresolvingCausing().remove(bundle.getName());
                } catch (Exception e) {
                    logger.error("inject " + bundle + "failure.", e);
                }
            } else {
                unresolvingCausing.put(bundle.getName(), _available_field);
            }
        }
    }
}
