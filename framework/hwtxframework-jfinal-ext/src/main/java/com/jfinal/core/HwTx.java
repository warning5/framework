package com.jfinal.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.hwtxframework.ioc.ApplicationContext;
import com.hwtxframework.ioc.util.IocContextHolder;
import com.hwtxframework.util.Constants;
import com.jfinal.config.JFinalConfig;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwTx {

    private static Logger logger = LoggerFactory.getLogger(HwTx.class);

    public static ApplicationContext getApplicationContext() {
        return (ApplicationContext) JFinal.me().getServletContext().getAttribute(Constants.ROOT_APPLICATION);
    }

    public static <T> T getManagedBean(Class<T> clazz) {
        T result = IocContextHolder.getComponent(StrKit.firstCharToLowerCase(clazz.getSimpleName()));
        if (result == null) {
            throw new RuntimeException("can't find component by type " + clazz + ", please use by name to get.");
        }
        return result;
    }

    public static String getManagedBeanName(Class<?> cls) {
        return StrKit.firstCharToLowerCase(cls.getSimpleName());
    }

    public static HwTxClassSearcher getHwTxClassSearcherWithRootClassMapping() {
        Map<ClassLoader, List<String>> mapping = new HashMap<>();
        mapping.putAll(getRootClassMapping());
        mapping.putAll(getRootLibMapping());
        return new HwTxClassSearcher(mapping);
    }


    public static <T> T getComponent(String name) {
        return IocContextHolder.getComponent(name);
    }

    public static Map<ClassLoader, List<String>> getRootClassMapping() {

        Map<ClassLoader, List<String>> classMappping = new HashMap<>();
        List<String> path = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        path.add(Thread.currentThread().getContextClassLoader().getResource("").getPath());
        classMappping.put(classLoader, path);
        return classMappping;
    }

    public static Map<ClassLoader, List<String>> getRootLibMapping() {

        String libDir = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "lib";
        File dir = new File(libDir);
        final List<String> path = new ArrayList<>();
        String scanJar = JFinalConfig.getProperty("scan.jar");
        Map<ClassLoader, List<String>> libMappping = new HashMap<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!StrKit.isBlank(scanJar)) {
            List<String> paths = Lists.newArrayList();
            for (String jar : scanJar.split(",")) {
                paths.add(libDir + File.separator + jar);
            }
            libMappping.put(classLoader, paths);
        }
        return libMappping;
    }

    public static String suffix = "Controller";

    public static String getControllerKey(String moduleName, Class<? extends Controller> controller) {
        ControllerBind controllerBind = (ControllerBind) controller.getAnnotation(ControllerBind.class);
        String controllerKey = null;
        if (controllerBind == null) {
            controllerKey = moduleName + "_" + HwTx.controllerKey(controller, suffix);
        } else {
            String cKey = controllerBind.controllerKey();
            if (PathKit.isVariable(cKey)) {
                cKey = PathKit.getVariableValue(cKey);
            }
            controllerKey = moduleName + "_" + cKey;
        }
        if (!controllerKey.startsWith("/"))
            controllerKey = "/" + controllerKey;

        return controllerKey;
    }

    public static String controllerKey(Class<? extends Controller> clazz, String suffix) {
        Preconditions.checkArgument(clazz.getSimpleName().endsWith(suffix),
                " does not has a @ControllerBind annotation and it's name is not end with " + suffix);
        String controllerKey = "/" + StrKit.firstCharToLowerCase(clazz.getSimpleName());
        controllerKey = controllerKey.substring(0, controllerKey.indexOf(suffix));
        return controllerKey;
    }
}
