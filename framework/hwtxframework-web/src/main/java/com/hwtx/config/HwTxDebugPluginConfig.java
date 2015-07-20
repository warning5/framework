package com.hwtx.config;

import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.Set;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class HwTxDebugPluginConfig {

    @Getter
    @XmlElementWrapper(name = "plugins")
    @XmlElement(name = "plugin")
    Set<Plugin> plugins;

    public static class Plugin {

        @Getter
        @XmlAttribute(name = "id")
        private String id;

        @Getter
        @XmlAttribute(name = "version")
        private String version;

        @Getter
        @XmlValue
        private String path;
    }
}
