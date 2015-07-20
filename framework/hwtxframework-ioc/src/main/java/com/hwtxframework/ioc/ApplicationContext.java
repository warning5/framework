package com.hwtxframework.ioc;

import com.hwtxframework.io.support.PropertiesLoaderSupport;
import com.hwtxframework.ioc.impl.BaseCache;

import java.util.Collection;
import java.util.Map;

public interface ApplicationContext {
    /**
     * get the service instance by component name
     *
     * @param name component name
     * @return
     */
    public Object getComponent(String name);

    /**
     * register the ServiceChangeListener instance to the context
     *
     * @param listener
     */
    public void registerServiceChangeListener(ComponentChangeListener... listener);

    /**
     * get the ServiceBundle objects can not be resolved for not all dependent
     * service can be find
     *
     * @return
     */
    public Collection<ComponentBundle> getUnResolvingComponentBundles();

    /**
     * get the ServiceBundle objects already be resolved
     *
     * @return
     */
    public Collection<ComponentBundle> getReadyComponentBundles();

    public BaseCache getReadyComponentCache();

    public BaseCache getUnresolvingComponentCache();

    public Map<String, String> getUnresolvingCausing();

    /**
     * remove the ServiceBundle with the spec name
     *
     * @param name
     */
    public void removeReadyComponentBundle(String name);

    public Collection<ComponentBundle> getDependencyI(String name);

    public void close();

    public void refreshComponent(String className, String fileName);

    public PropertiesLoaderSupport getPropertiesLoaderSupport();

    public void loadBundlesFromFilePath(String... files);

    public int loadComponentWithAnnotation(String path);
}
