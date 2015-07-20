package com.hwtxframework.ioc.impl;

import com.hwtxframework.io.support.PropertiesLoaderSupport;
import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.Constants;
import com.hwtxframework.ioc.DependencyInfo;
import com.hwtxframework.ioc.DependencyInfo.DependPath;
import com.hwtxframework.ioc.Property;
import com.hwtxframework.ioc.exceptions.LoadConfigException;
import com.hwtxframework.ioc.value.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class ConfigParser {

    private static Logger logger = LoggerFactory.getLogger(ConfigParser.class);
    final Map<String, ComponentBundle> bundles = new HashMap<String, ComponentBundle>();
    @Getter
    final Map<String, List<DependencyInfo>> dependenciesI = new HashMap<>();
    @Setter
    PropertiesLoaderSupport propertiesLoaderSupport;

    class Elementhnd extends DefaultHandler {
        ComponentBundle currentBundle = null;
        Property currentProperty = null;
        private String preTag = null;
        private String handlerTag = null;

        private void dealComponentTag(Attributes attrs) {
            currentBundle = new ComponentBundle();
            String name = attrs.getValue(Constants.NAME);
            if (name != null) {
                currentBundle.setName(name);
            } else {
                throw new LoadConfigException("the component must be set name.");
            }
            String className = attrs.getValue(Constants.CLASS);
            if (className != null) {
                try {
                    className = className.trim();
                    Class<?> clazz = Class.forName(className);
                    currentBundle.setClassName(className);
                    BundleUtil.scanFieldAndMethod(currentBundle, clazz);
                } catch (Exception e) {
                    throw new LoadConfigException("the component " + className + " create failed!", e);
                }
            }
        }

        private void dealPropertyTag(Attributes attrs) {
            currentProperty = new Property();
            String name = attrs.getValue(Constants.NAME);
            currentProperty.setName(name);
            String value = attrs.getValue(Constants.VALUE);
            String ref = attrs.getValue(Constants.PROPERTY_REF);
            if (value != null && ref != null) {
                throw new LoadConfigException("property " + name + " can't include value and ref at same time.");
            } else if (value != null && ref == null) {
                currentProperty.setValue(new StringValue(value));
            } else if (value == null && ref != null) {
                currentProperty.setValue(new ReferenceValue(ref));
            }
        }

        private void dealMapTag(Attributes attrs) {
            MapValue mapValue = new MapValue();
            currentProperty.setValue(mapValue);
        }

        private void dealListTag(Attributes attrs) {
            ListValue listValue = new ListValue();
            currentProperty.setValue(listValue);
        }

        private void dealEntry(Attributes attrs) {
            MapValue mapValue = currentProperty.getPropertyValue(MapValue.class);
            String key = attrs.getValue(Constants.KEY);
            String value = attrs.getValue(Constants.VALUE);
            String ref = attrs.getValue(Constants.VALUE_REF);
            MapValue.MapEntry entry = null;
            if (value != null && ref != null) {
                throw new LoadConfigException("property " + key + " can't include value and value-ref at same time.");
            } else if (value != null && ref == null) {
                entry = mapValue.new MapEntry(key, new StringValue(value), null);
            } else if (value == null && ref != null) {
                entry = mapValue.new MapEntry(key, null, new ReferenceValue(ref));
            } else {
                throw new LoadConfigException("property " + key + " must include value or value-ref at last one");
            }
            mapValue.addEntry(entry);
        }

        private void doRef(Attributes attrs) {
            ListValue listValue = currentProperty.getPropertyValue(ListValue.class);
            ListValue.ListEntry entry;
            String ref = attrs.getValue(Constants.NAME);
            if (ref != null) {
                entry = listValue.new ListEntry(null, new ReferenceValue(ref));
            } else {
                throw new LoadConfigException("ref element'attr name must exist.");
            }
            listValue.addEntry(entry);
        }

        public void startElement(String uri, String sName, String qName, Attributes attrs) {
            if (qName.equals(Constants.COMPONENT)) {
                dealComponentTag(attrs);
                handlerTag = Constants.COMPONENT;
            } else if (qName.equals(Constants.PROPERTY)) {
                dealPropertyTag(attrs);
                handlerTag = Constants.PROPERTY;
            } else if (qName.equals(Constants.MAP)) {
                dealMapTag(attrs);
                handlerTag = Constants.MAP;
            } else if (qName.equals(Constants.ENTRY)) {
                dealEntry(attrs);
                handlerTag = Constants.ENTRY;
            } else if (qName.equals(Constants.COMPONENT_SCAN)) {
                scanComponents(attrs);
                handlerTag = Constants.COMPONENT_SCAN;
            } else if (qName.equals(Constants.PROPERTY_PLACEHOLDER)) {
                dealProperty(attrs);
                handlerTag = Constants.PROPERTY_PLACEHOLDER;
            } else if (qName.equals(Constants.LIST)) {
                dealListTag(attrs);
                handlerTag = Constants.LIST;
            } else if (qName.equals(Constants.PROPERTY_REF)) {
                doRef(attrs);
                handlerTag = Constants.PROPERTY_REF;
            }
            preTag = qName;
        }

        public void endElement(String uri, String localName, String qName) {
            if (qName.equals(Constants.COMPONENT)) {
                bundles.put(currentBundle.getName(), currentBundle);
                handleDependency(currentBundle, dependenciesI);
            }
            if (qName.equals(Constants.PROPERTY)) {
                currentBundle.getProperties().add(currentProperty);
            }
            preTag = null;
            handlerTag = null;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (preTag != null) {
                String content = new String(ch, start, length);
                if (Constants.VALUE.equals(preTag)) {
                    if (currentProperty != null) {
                        Value<?> value = currentProperty.getValue();
                        if (value != null) {
                            if (value instanceof ListValue) {
                                ListValue listValue = (ListValue) value;
                                listValue.addEntry(listValue.new ListEntry(new StringValue(content), null));
                            } else {
                                ((StringValue) value).merge(content);
                            }
                        } else {
                            if (handlerTag.equals(Constants.LIST)) {
                                ListValue listValue = new ListValue();
                                listValue.addEntry(listValue.new ListEntry(new StringValue(content), null));
                                currentProperty.setValue(listValue);
                            } else {
                                currentProperty.setValue(new StringValue(content));
                            }
                        }
                    }
                }
            }
        }
    }

    private void dealProperty(Attributes attrs) {
        String location = attrs.getValue(Constants.LOCATION);
        if (location != null) {
            String[] locations = location.split(",");
            for (String l : locations) {
                try {
                    Properties properties = propertiesLoaderSupport.loadProperties(ResourceReader.getResource(l));
                    propertiesLoaderSupport.mergeProperties(properties);
                } catch (IOException e) {
                    logger.error("{}", e);
                }
            }
        }
    }

    public void handleDependency(ComponentBundle bundle, Map<String, List<DependencyInfo>> dependenciesI) {
        for (Property property : bundle.getProperties()) {
            if (!property.containRef()) {
                continue;
            }
            for (Entry<String, List<DependPath>> entry : property.getDenpendRefs().entrySet()) {
                List<DependencyInfo> infos = dependenciesI.get(entry.getKey());
                if (infos == null) {
                    infos = new ArrayList<>();
                }
                infos.add(new DependencyInfo(bundle, property, entry.getValue()));
                dependenciesI.put(entry.getKey(), infos);
            }
        }
    }

    public void validate(InputStream configIs) throws Exception {
        String strLang = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        SchemaFactory factory = SchemaFactory.newInstance(strLang);
        InputStream isSchema = ConfigParser.class.getClassLoader().getResourceAsStream("hwtx-ioc.xsd");
        StreamSource ss = new StreamSource(isSchema);
        Schema schema = factory.newSchema(ss);
        Validator validator = schema.newValidator();
        StreamSource source = new StreamSource(configIs);
        validator.validate(source);
    }

    public void loadConfig(InputStream configIs) throws Exception {
        Elementhnd handler = new Elementhnd();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(configIs, handler);
        configIs.close();
    }

    public Map<String, ComponentBundle> getBundles() {
        return bundles;
    }

    private void scanComponents(Attributes attrs) {
        String _base_package = attrs.getValue(Constants.BASE_PACKAGE);
        if (_base_package != null) {
            String[] packages = _base_package.split(",");
            for (String p : packages) {
                scanClasses(p);
            }
        }
    }

    /**
     * 从包package中获取所有的Class
     */
    private void scanClasses(String pack) {

        boolean recursive = true;
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.charAt(0) == '/') {
                                name = name.substring(1);
                            }
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                if (idx != -1) {
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                if ((idx != -1) || recursive) {
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        handleClass(packageName, className);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        logger.error("{}", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("{}", e);
        }
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     */
    private void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                handleClass(packageName, className);
            }
        }
    }

    public void handleClass(String packageName, String className) {
        try {
            ComponentBundle bundle = BundleUtil.getAnnotationBundle(packageName + '.' + className, className);
            if (bundle != null) {
                bundles.put(bundle.getName(), bundle);
                handleDependency(bundle, dependenciesI);
            }
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }
}
