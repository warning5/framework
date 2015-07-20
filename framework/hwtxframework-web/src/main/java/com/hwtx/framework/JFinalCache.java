package com.hwtx.framework;

import com.hwtxframework.ioc.annotation.Component;
import com.jfinal.plugin.ehcache.CacheKit;
import lombok.Getter;
import net.sf.ehcache.CacheManager;

import javax.annotation.Resource;

@Component
public class JFinalCache {

    @Resource
    @Getter
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        CacheKit.init(cacheManager);
    }

}
