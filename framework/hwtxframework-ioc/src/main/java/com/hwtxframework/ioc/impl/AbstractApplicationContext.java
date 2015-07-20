package com.hwtxframework.ioc.impl;

import com.google.common.collect.Lists;
import com.hwtxframework.io.Resource;
import com.hwtxframework.io.support.PropertiesLoaderSupport;
import com.hwtxframework.ioc.*;
import com.hwtxframework.ioc.exceptions.ComponentDefinitionException;
import com.hwtxframework.util.ClassUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class AbstractApplicationContext implements ApplicationContext {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    @Getter
    protected BaseCache readyComponentCache;
    @Getter
    protected BaseCache unresolvingComponentCache;

    final Map<String, List<DependencyInfo>> iDependencies = new HashMap<>();
    final Map<String, List<DependencyInfo>> dependenciesI = new HashMap<>();
    @Getter
    final PropertiesLoaderSupport propertiesLoaderSupport = new PropertiesLoaderSupport();

    @Getter
    final Map<String, String> unresolvingCausing = new HashMap<String, String>();

    private ApplicationContext parent;

    @Override
    public Collection<ComponentBundle> getDependencyI(String name) {
        List<ComponentBundle> bundles = new ArrayList<ComponentBundle>();
        List<DependencyInfo> infos = dependenciesI.get(name);
        if (infos != null) {
            for (DependencyInfo depencyInfo : infos) {
                bundles.add(depencyInfo.getComponentBundle());
            }
        }
        return bundles;
    }

    public AbstractApplicationContext(BaseCache readyServiceCache, BaseCache unresolvingServiceCache,
                                      ApplicationContext parent) {
        this.readyComponentCache = readyServiceCache;
        this.unresolvingComponentCache = unresolvingServiceCache;
        this.parent = parent;
    }

    public void addBundles(Map<String, ComponentBundle> bundles, ApplicationContext applicationContext) {
        for (ComponentBundle bundle : bundles.values()) {
            resolve(bundle, applicationContext);
        }
    }

    public void loadBundlesFromFilePath(String... locations) {
        for (String location : locations) {
            try {
                loadComponentDefinitions(location);
            } catch (ComponentDefinitionException e) {
                logger.error("{}", e);
            }
        }
    }

    private int loadComponentDefinitions(String location) throws ComponentDefinitionException {
        try {
            Resource[] resources = ResourceReader.getResource(location);
            int loadCount = loadComponentDefinitions(resources);
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + loadCount + " component definitions from location pattern [" + location + "]");
            }
            return loadCount;
        } catch (IOException ex) {
            throw new ComponentDefinitionException("Could not resolve component definition resource pattern ["
                    + location + "]", ex);
        }
    }

    public int loadComponentDefinitions(Resource... resources) {
        ConfigParser parser = new ConfigParser();
        parser.setPropertiesLoaderSupport(propertiesLoaderSupport);
        Map<String, ComponentBundle> bundles = new HashMap<>();
        for (Resource resource : resources) {
            try {
                parser.validate(resource.getInputStream());
                parser.loadConfig(resource.getInputStream());
                bundles.putAll(parser.getBundles());
                dependenciesI.putAll(parser.getDependenciesI());
            } catch (Exception e) {
                logger.error("load file " + resource.getFilename() + " failure.", e);
            }
        }
        addBundles(bundles, null);
        afterLoadBundle(null);
        return readyComponentCache.cache.size();
    }

    public int loadComponentWithAnnotation(String path) {
        ConfigParser parser = new ConfigParser();
        parser.setPropertiesLoaderSupport(propertiesLoaderSupport);
        Map<String, ComponentBundle> bundles = new HashMap<>();
        for (String file : ClassUtils.findFiles(path, "*.class")) {
            String fileName = file.substring(file.lastIndexOf(".") + 1);
            try {
                ComponentBundle componentBundle = BundleUtil.getAnnotationBundle(file, fileName);
                if (componentBundle != null) {
                    bundles.put(componentBundle.getName(), componentBundle);
                    parser.handleDependency(componentBundle, dependenciesI);
                }
            } catch (Exception e) {
                logger.error("load class " + fileName + " failure.", e);
            }
        }
        if (bundles.size() != 0) {
            addBundles(bundles, parent);
            afterLoadBundle(parent);
            return readyComponentCache.cache.size();
        }
        return 0;
    }

    @Override
    public void refreshComponent(String className, String fileName) {
        ComponentBundle bundle = null;
        try {
            bundle = BundleUtil.getAnnotationBundle(className, fileName);
            if (bundle == null) {
                return;
            }
            resolve(bundle, null);
            injectDependi(bundle);
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

    private void resolve(ComponentBundle bundle, ApplicationContext applicationContext) {
        String _available_field = BundleUtil.isAllReferenceAvailable(bundle, applicationContext, readyComponentCache);
        if (Constants.AVAILABLE.equals(_available_field)) {
            try {
                BundleUtil.injectReference(bundle, applicationContext, readyComponentCache, propertiesLoaderSupport);
                readyComponentCache.add(bundle);
                unresolvingComponentCache.remove(bundle);
                unresolvingCausing.remove(bundle.getName());
            } catch (Exception e) {
                logger.error("inject " + bundle + "failure.", e);
            }
        } else {
            unresolvingComponentCache.add(bundle);
            unresolvingCausing.put(bundle.getName(), _available_field);
        }
    }

    private void injectDependi(ComponentBundle bundle) {
        List<DependencyInfo> infos = dependenciesI.get(bundle.getName());
        if (infos == null || infos.size() == 0) {
            return;
        }
        for (DependencyInfo dependencyInfo : infos) {
            dependencyInfo.refresh(bundle);
        }
    }

    private void afterLoadBundle(ApplicationContext applicationContext) {

        for (ComponentBundle bundle : readyComponentCache.getServiceBundles()) {

            Object instance = bundle.getInstance();
            if (ApplicationContextAware.class.isAssignableFrom(instance.getClass())) {
                ((ApplicationContextAware) instance).setApplicationContext(this);
            }
        }

        List<ComponentBundle> removed = Lists.newArrayList();
        //处理存在的循环依赖
        for (ComponentBundle bundle : unresolvingComponentCache.getServiceBundles()) {
            try {
                BundleUtil.injectCycleReference(bundle, applicationContext, unresolvingComponentCache,
                        readyComponentCache, propertiesLoaderSupport);
                readyComponentCache.add(bundle);
                removed.add(bundle);
                unresolvingCausing.remove(bundle.getName());
            } catch (Exception e) {
                logger.error("component:" + bundle.getName() + " can't resolved by " + unresolvingCausing.get(bundle
                        .getName()), e);
            }
        }
        for (ComponentBundle componentBundle : removed) {
            unresolvingComponentCache.remove(componentBundle);
        }
    }

    public void registerServiceChangeListener(ComponentChangeListener... listener) {
        readyComponentCache.registerComponentChangeListener(listener);
    }

    public Collection<ComponentBundle> getUnResolvingComponentBundles() {
        return unresolvingComponentCache.getServiceBundles();
    }

    public Collection<ComponentBundle> getReadyComponentBundles() {
        return readyComponentCache.getServiceBundles();
    }

    public void removeReadyComponentBundle(String name) {
        readyComponentCache.removeBundleByName(name);
    }

    @Override
    public void close() {
        dependenciesI.clear();
        unresolvingCausing.clear();
        unresolvingComponentCache.close();
        for (ComponentBundle bundle : readyComponentCache.cache.values()) {
            if (bundle.getPreDestroy() != null) {
                try {
                    Method method = bundle.getInstance().getClass().getMethod(bundle.getPreDestroy(), new Class[]{});
                    method.invoke(bundle.getInstance(), new Object[]{});
                } catch (Exception e) {
                }
            }
        }
        readyComponentCache.close();
    }
}