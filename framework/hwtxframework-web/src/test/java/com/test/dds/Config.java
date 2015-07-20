package com.test.dds;

import com.hwtx.config.HwTxDebugPluginConfig;
import com.jfinal.ext.kit.JaxbKit;

import java.io.File;

/**
 * Created by panye on 14-12-18.
 */
public class Config {

    public static void main(String[] args) {
        String path = "/home/panye/ideaworkspace/hwtxframework/hwtxframework-app/src/main" +
                "/resources/config/debug-config.xml";
        HwTxDebugPluginConfig hwTxDebugPluginConfig = JaxbKit.unmarshal(new File(path),
                HwTxDebugPluginConfig.class);
        System.out.println();
    }
}
