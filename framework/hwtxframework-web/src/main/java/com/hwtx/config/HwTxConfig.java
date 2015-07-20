package com.hwtx.config;

import com.google.common.collect.Maps;
import com.hwtxframework.ioc.annotation.Component;
import com.jfinal.config.*;
import com.jfinal.core.HwTxAutoTableBindPlugin;
import com.jfinal.ext.interceptor.LogInterceptor;
import com.jfinal.ext.plugin.sqlinxml.SqlInXmlPlugin;
import com.jfinal.plugin.activerecord.IContainerFactory;
import com.jfinal.plugin.activerecord.ModelRecordElResolver;
import com.jfinal.plugin.activerecord.tx.TxByMethods;
import com.jfinal.plugin.activerecord.tx.TxByRegex;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.ViewType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class HwTxConfig extends JFinalConfig {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public static final String adminPath = "adminPath";

    public void configConstant(Constants me) {
        loadPropertyFile("jeesite.properties");
        me.setDevMode(getPropertyToBoolean("devMode", false));
        me.setViewType(ViewType.JSP);
        ModelRecordElResolver.setWorking(false);
        String baseViewPath = getProperty("baseViewPath");
        if (baseViewPath != null && !baseViewPath.equals(""))
            me.setBaseViewPath(baseViewPath);
    }

    public void configPlugin(Plugins me) {
        DruidPlugin druid = new DruidPlugin(getProperty("jdbc.url"), getProperty("jdbc.username"),
                getProperty("jdbc.password"));
        me.add(druid);
        me.add(new SqlInXmlPlugin());
        HwTxAutoTableBindPlugin hwTxAutoTableBindPlugin = new HwTxAutoTableBindPlugin(druid);
        hwTxAutoTableBindPlugin.setContainerFactory(new IContainerFactory() {
            public Map<String, Object> getAttrsMap() {
                return new HashMap<>();
            }

            public Map<String, Object> getColumnsMap() {
                return Maps.newLinkedHashMap();
            }

            public Set<String> getModifyFlagSet() {
                return new HashSet<>();
            }
        });
        me.add(hwTxAutoTableBindPlugin);
//        me.add(new ModuleIocPlugin("ioc/*.xml"));
//        me.add(new ModuleSqlInXmlPlugin());
    }

    public void configInterceptor(Interceptors me) {
        me.add(new TxByRegex("save*", false));
        me.add(new TxByRegex("update*", false));
        me.add(new TxByRegex("delete*", false));
        me.add(new TxByMethods("save", "update"));
        me.add(new LogInterceptor());
    }

    public void configHandler(Handlers me) {
    }

    protected static long start;

    @Override
    public void afterJFinalStart() {
        logger.info("start framework spend " + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void beforeJFinalStart() {
        start = System.currentTimeMillis();
    }

    @Override
    public void configRoute(Routes me) {
    }
}
