package com.jfinal.core; /**
 * Copyright (c) 2011-2013, kidzhou 周磊 (zhouleib1412@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hwtxframework.util.ClassUtils;
import com.jfinal.ext.kit.Reflect;
import com.jfinal.log.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.Thread.currentThread;

public class HwTxClassSearcher {

    protected static final Logger LOG = Logger.getLogger(HwTxClassSearcher.class);
    private Map<ClassLoader, List<String>> classes = Maps.newHashMap();
    private Map<ClassLoader, List<String>> libs = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    private static <T> List<Class<? extends T>> extraction(Class<T> clazz, Map<ClassLoader, List<String>> classFileList) {
        List<Class<? extends T>> classList = Lists.newArrayList();
        ClassLoader old = null;
        for (Entry<ClassLoader, List<String>> entry : classFileList.entrySet()) {
            old = currentThread().getContextClassLoader();
            currentThread().setContextClassLoader(entry.getKey());
            try {
                for (String classFile : entry.getValue()) {
                    Class<?> classInFile = Reflect.on(classFile).get();
                    if (clazz.isAssignableFrom(classInFile) && clazz != classInFile) {
                        classList.add((Class<? extends T>) classInFile);
                    }
                }
            } finally {
                if (old != null) {
                    currentThread().setContextClassLoader(old);
                }
            }
        }

        return classList;
    }

    public HwTxClassSearcher(Map<ClassLoader, List<String>> mapping) {
        for (Entry<ClassLoader, List<String>> entry : mapping.entrySet()) {

            for (String path : entry.getValue()) {
                if (path.endsWith(".jar")) {
                    List<String> paths = libs.get(entry.getKey());
                    if (paths == null) {
                        paths = Lists.newArrayList();
                    }
                    paths.add(path);
                    libs.put(entry.getKey(), paths);
                } else {
                    List<String> paths = classes.get(entry.getKey());
                    if (paths == null) {
                        paths = Lists.newArrayList();
                    }
                    paths.add(path);
                    classes.put(entry.getKey(), paths);
                }
            }
        }
    }

    private boolean includeAllJarsInLib = false;

    private List<String> includeJars = Lists.newArrayList();

    public HwTxClassSearcher injars(List<String> jars) {
        if (jars != null) {
            includeJars.addAll(jars);
        }
        return this;
    }

    public HwTxClassSearcher inJars(String... jars) {
        if (jars != null) {
            for (String jar : jars) {
                includeJars.add(jar);
            }
        }
        return this;
    }

    private static Map<ClassLoader, List<String>> findFiles(Map<ClassLoader, List<String>> baseDirNames, String targetFileName) {
        Map<ClassLoader, List<String>> classFiles = Maps.newHashMap();
        for (Entry<ClassLoader, List<String>> entry : baseDirNames.entrySet()) {
            for (String path : entry.getValue()) {
                List<String> files = classFiles.get(entry.getKey());
                if (files == null) {
                    files = Lists.newArrayList();
                }
                files.addAll(ClassUtils.findFiles(path, targetFileName));
                classFiles.put(entry.getKey(), files);
            }
        }
        return classFiles;
    }

    private Map<ClassLoader, List<String>> findjarFiles(Map<ClassLoader, List<String>> baseDirNames,
                                                        final List<String> includeJars) {
        Map<ClassLoader, List<String>> classFiles = Maps.newHashMap();
        for (Entry<ClassLoader, List<String>> entry : baseDirNames.entrySet()) {
            for (String jar : entry.getValue()) {
                List<String> files = classFiles.get(entry.getKey());
                if (files == null) {
                    files = Lists.newArrayList();
                }
                files.addAll(findjarFiles(jar, includeJars));
                classFiles.put(entry.getKey(), files);
            }
        }
        return classFiles;
    }

    public <T> List<Class<? extends T>> findInClasspathAndJars(Class<T> clazz) {
        Map<ClassLoader, List<String>> classFileList = findjarFiles(libs, includeJars);
        for (Entry<ClassLoader, List<String>> entry : findFiles(classes, "*.class").entrySet()) {
            if (classFileList.containsKey(entry.getKey())) {
                classFileList.get(entry.getKey()).addAll(entry.getValue());
            } else {
                classFileList.put(entry.getKey(), entry.getValue());
            }
        }
        return extraction(clazz, classFileList);
    }

    /**
     * 查找jar包中的class
     *
     * @param jarUrl      jar路径
     * @param includeJars
     */
    private List<String> findjarFiles(String jarUrl, final List<String> includeJars) {
        List<String> classFiles = Lists.newArrayList();
        try {

            File baseDir = new File(jarUrl);
            if (!baseDir.exists() || baseDir.isDirectory()) {
                LOG.error("file serach error：" + jarUrl + " is a dir！");
            } else {
                if (includeAllJarsInLib || includeJars.contains(jarUrl)) {
                    return null;
                }
                JarFile localJarFile = new JarFile(new File(jarUrl));
                Enumeration<JarEntry> entries = localJarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String entryName = jarEntry.getName();
                    if (!jarEntry.isDirectory() && entryName.endsWith(".class")) {
                        String className = entryName.replaceAll("/", ".").substring(0, entryName.length() - 6);
                        classFiles.add(className);
                    }
                }
                localJarFile.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return classFiles;

    }

    public HwTxClassSearcher includeAllJarsInLib(boolean includeAllJarsInLib) {
        this.includeAllJarsInLib = includeAllJarsInLib;
        return this;
    }
}
