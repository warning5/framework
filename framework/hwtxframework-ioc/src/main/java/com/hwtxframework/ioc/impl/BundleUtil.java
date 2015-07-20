package com.hwtxframework.ioc.impl;

import com.hwtxframework.io.Resource;
import com.hwtxframework.io.support.PropertiesLoaderSupport;
import com.hwtxframework.ioc.*;
import com.hwtxframework.ioc.annotation.Component;
import com.hwtxframework.ioc.annotation.Dependon;
import com.hwtxframework.ioc.exceptions.LoadConfigException;
import com.hwtxframework.ioc.exceptions.SetRefereceException;
import com.hwtxframework.ioc.exceptions.UnResolveException;
import com.hwtxframework.ioc.value.ListValue;
import com.hwtxframework.ioc.value.MapValue;
import com.hwtxframework.ioc.value.ReferenceValue;
import com.hwtxframework.ioc.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BundleUtil {

    protected static Logger logger = LoggerFactory.getLogger(BundleUtil.class);

    public static String firstCharToLowerCase(String str) {
        Character firstChar = str.charAt(0);
        String tail = str.substring(1);
        str = Character.toLowerCase(firstChar) + tail;
        return str;
    }

    public static String firstCharToUpperCase(String str) {
        Character firstChar = str.charAt(0);
        String tail = str.substring(1);
        str = Character.toUpperCase(firstChar) + tail;
        return str;
    }

    public static ComponentBundle scanFieldAndMethod(ComponentBundle bundle, Class<?> clazz) {

        for (Field field : transferClassField(clazz)) {
            javax.annotation.Resource resource = field.getAnnotation(javax.annotation.Resource.class);
            if (resource == null) {
                continue;
            }

            Property property = new Property();
            property.setName(BundleUtil.firstCharToLowerCase(field.getName()));
            String refName = resource.name();
            if ("".equals(refName) || refName == null) {
                refName = firstCharToLowerCase(field.getType().getSimpleName());
            }
            property.setValue(new ReferenceValue(refName));
            bundle.getProperties().add(property);
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (bundle.getPostConstruct() != null) {
                    throw new RuntimeException("only one method can be annotation with @PostConstruct");
                }
                bundle.setPostConstruct(method.getName());
            } else if (method.isAnnotationPresent(PreDestroy.class)) {
                if (bundle.getPreDestroy() != null) {
                    throw new RuntimeException("only one method can be annotation with @PreDestroy");
                }
                bundle.setPreDestroy(method.getName());
            }
        }
        return bundle;
    }

    private static List<Field> transferClassField(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class sClass = clazz.getSuperclass();
        while (!sClass.isInterface() && sClass != Object.class) {
            fields.addAll(Arrays.asList(sClass.getDeclaredFields()));
            sClass = sClass.getSuperclass();
        }
        return fields;
    }

    public static ComponentBundle getAnnotationBundle(String clazz_path, String className) throws Exception {
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(clazz_path);
        Component component = clazz.getAnnotation(Component.class);
        if (component == null) {
            return null;
        }
        ComponentBundle bundle = new ComponentBundle();
        String name = component.value();
        if (name == null || "".equals(name)) {
            name = BundleUtil.firstCharToLowerCase(className);
        }
        bundle.setName(name);
        bundle.setClassName(clazz_path);

        Dependon dependon = clazz.getAnnotation(Dependon.class);
        if (dependon != null) {
            bundle.setDependons(new HashSet<String>(Arrays.asList(dependon.value())));
        }
        scanFieldAndMethod(bundle, clazz);
        return bundle;
    }

    public static Object buildInstance(Class<?> clazz, String bundleName) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new LoadConfigException("the component " + bundleName + " create failed!", e);
        }
    }

    private static Object createPropertyValueObject(String value, Method method) {
        Object ret = null;
        String type = method.getParameterTypes()[0].getName();
        if (value != null && !value.equals("")) {
            if (type.equals("java.lang.String")) {
                ret = value;
            } else if (type.equals("int") || type.equals("java.lang.Integer")) {
                ret = new Integer(value);
            } else if (type.equals("long") || type.equals("java.lang.Long")) {
                ret = new Long(value);
            } else if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
                ret = Boolean.valueOf(value);
            } else if (type.equals("java.util.Date")) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date;
                try {
                    date = format.parse(value);
                } catch (ParseException e) {
                    throw new LoadConfigException(e);
                }
                ret = date;
            }
        }
        return ret;

    }

    public static void setProperty(Object obj, String name, Object value) {
        Class<?> c = obj.getClass();
        Method[] methods = c.getMethods();
        String setMethodName = "set" + firstCharToUpperCase(name);
        for (Method method : methods) {
            if (method.getName().equals(setMethodName)) {
                Class<?> type = method.getParameterTypes()[0];
                if (Resource.class.isAssignableFrom(type) && value instanceof String) {
                    try {
                        Resource[] resources = ResourceReader.getResource((String) value);
                        if (resources != null & resources.length == 1) {
                            value = resources[0];
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Object valueObject = (value instanceof String) ? createPropertyValueObject((String) value, method)
                            : value;
                    method.invoke(obj, new Object[]{valueObject});
                } catch (Exception e) {
                    throw new SetRefereceException(e);
                }
            }
        }
    }

    public static Object getProperty(Object obj, String name) {
        Class<?> c = obj.getClass();
        Method[] methods = c.getMethods();
        String getMethodName = "get" + firstCharToUpperCase(name);
        for (Method method : methods) {
            if (method.getName().equals(getMethodName)) {
                try {
                    return method.invoke(obj);
                } catch (Exception e) {
                    throw new SetRefereceException(e);
                }
            }
        }
        return null;
    }

    public static String isAllReferenceAvailable(ComponentBundle bundle, ApplicationContext applicationContext,
                                                 BaseCache readyServiceCache) {
        for (Property prop : bundle.getProperties()) {
            if (prop.containRef()) {
                String resolve = prop.getUnResolved(readyServiceCache);
                if (!resolve.equals(Constants.RESOLVED)) {
                    if (applicationContext != null) {
                        resolve = prop.getUnResolved(applicationContext.getReadyComponentCache());
                        if (!resolve.equals(Constants.RESOLVED)) {
                            return resolve;
                        }
                    } else {
                        return resolve;
                    }
                }
            }
        }

        if (bundle.getDependons() != null) {
            for (String component : bundle.getDependons()) {
                if (readyServiceCache.getComponent(component) == null) {
                    if (applicationContext != null) {
                        if (applicationContext.getComponent(component) == null) {
                            return component;
                        }
                    } else {
                        return component;
                    }
                }
            }
        }
        return Constants.AVAILABLE;
    }

    public static void setPropertyByString(Object obj, Property pro) {
        setProperty(obj, pro.getName(), pro.getPropertyValue(StringValue.class).getValue());
    }

    /**
     * @param bundle
     * @return the true value means all the references of the bundle have been injected
     * @throws Exception
     */
    public static void injectReference(ComponentBundle bundle, ApplicationContext applicationContext,
                                       BaseCache readyServiceCache, PropertiesLoaderSupport propertiesLoaderSupport) throws Exception {
        //处理循环依赖时出现先创建实例的情况
        if (bundle.getInstance() == null) {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(bundle.getClassName());
            bundle.setInstance(buildInstance(clazz, bundle.getName()));
        }
        for (Property prop : bundle.getProperties()) {
            prop.setVariable(propertiesLoaderSupport.getProps());
            if (prop.containRef()) {
                Object instance = prop.getValue().getValue((readyServiceCache));
                if (instance == null && applicationContext != null) {
                    instance = prop.getValue().getValue(applicationContext.getReadyComponentCache());
                }
                setReference(bundle.getInstance(), prop.getName(), instance);
            } else {
                if (prop.getPropertyValue() instanceof ListValue) {
                    setProperty(bundle.getInstance(), prop.getName(), prop.getPropertyValue(ListValue.class).getValue(readyServiceCache));
                } else if (prop.getPropertyValue() instanceof MapValue) {
                    setProperty(bundle.getInstance(), prop.getName(), prop.getPropertyValue(MapValue.class).getValue(readyServiceCache));
                } else {
                    setPropertyByString(bundle.getInstance(), prop);
                }
            }
        }

        String postConstruct = bundle.getPostConstruct();
        if (postConstruct != null & !"".equals(postConstruct)) {
            try {
                Method method = bundle.getInstance().getClass().getDeclaredMethod(postConstruct, new Class[]{});
                if (method != null) {
                    method.invoke(bundle.getInstance());
                }
            } catch (Exception e) {
                throw new SetRefereceException(e);
            }
        }

        if (FactoryBean.class.isAssignableFrom(bundle.getInstance().getClass())) {
            try {
                bundle.setInstance(((FactoryBean<?>) bundle.getInstance()).getObject());
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }
    }

    public static void injectCycleReference(ComponentBundle bundle, ApplicationContext applicationContext,
                                            BaseCache unresolvingComponentCache, BaseCache readyComponentCache, PropertiesLoaderSupport propertiesLoaderSupport) throws Exception {

        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(bundle.getClassName());
        bundle.setInstance(buildInstance(clazz, bundle.getName()));
        for (Property prop : bundle.getProperties()) {
            prop.setVariable(propertiesLoaderSupport.getProps());
            if (prop.containRef()) {
                ComponentBundle componentBundle = unresolvingComponentCache.findBundleByReference(prop.getName());
                Object instance;
                if (componentBundle != null) {
                    instance = componentBundle.getInstance();
                    if (instance == null) {
                        instance = buildInstance(Thread.currentThread().getContextClassLoader().loadClass(componentBundle.getClassName()), componentBundle.getName());
                        componentBundle.setInstance(instance);
                    }
                } else {
                    instance = readyComponentCache.getComponent(prop.getName());
                }
                if (instance == null && applicationContext != null) {
                    instance = prop.getValue().getValue(applicationContext.getReadyComponentCache());
                }
                if (instance == null) {
                    throw new UnResolveException();
                }
                setReference(bundle.getInstance(), prop.getName(), instance);
            } else {
                if (prop.getPropertyValue() instanceof ListValue) {
                    setProperty(bundle.getInstance(), prop.getName(), prop.getPropertyValue(ListValue.class).getValue(readyComponentCache));
                } else if (prop.getPropertyValue() instanceof MapValue) {
                    setProperty(bundle.getInstance(), prop.getName(), prop.getPropertyValue(MapValue.class).getValue(readyComponentCache));
                } else {
                    setPropertyByString(bundle.getInstance(), prop);
                }
            }
        }

        String postConstruct = bundle.getPostConstruct();
        if (postConstruct != null & !"".equals(postConstruct)) {
            try {
                Method method = bundle.getInstance().getClass().getDeclaredMethod(postConstruct, new Class[]{});
                if (method != null) {
                    method.invoke(bundle.getInstance());
                }
            } catch (Exception e) {
                throw new SetRefereceException(e);
            }
        }

        if (FactoryBean.class.isAssignableFrom(bundle.getInstance().getClass())) {
            try {
                bundle.setInstance(((FactoryBean<?>) bundle.getInstance()).getObject());
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }
    }

    private static void setReference(Object obj, String ref, Object serviceIns) {
        Class<?> c = obj.getClass();
        Method[] methods = c.getMethods();
        String setMethodName = "set" + firstCharToUpperCase(ref);
        boolean ok = false;
        for (Method method : methods) {
            if (method.getName().equals(setMethodName)) {
                try {
                    method.invoke(obj, new Object[]{serviceIns});
                    ok = true;
                    break;
                } catch (Exception e) {
                    throw new SetRefereceException(e);
                }
            }
        }
        if (!ok) {
            try {
                Field field = getField(c, ref);
                field.setAccessible(true);
                field.set(obj, serviceIns);
                ok = true;
            } catch (Exception e) {
                throw new SetRefereceException(e);
            }
        }

        if (!ok) {
            throw new RuntimeException("can't get field " + ref + " and method " + setMethodName + "in " + obj);
        }
    }

    private static Field getField(Class<?> c, String ref) {
        Field field = null;
        while (field == null) {
            try {
                field = c.getDeclaredField(ref);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        return field;
    }
}
