package com.hwtxframework.ioc.util;

import com.hwtxframework.ioc.ApplicationContext;
import com.hwtxframework.ioc.ApplicationContextAware;
import com.jfinal.kit.StrKit;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IocContextHolder implements ApplicationContextAware {

	private static ApplicationContext applicationContext = null;

	private static Logger logger = LoggerFactory.getLogger(IocContextHolder.class);

	/**
	 * 取得存储在静态变量中的ApplicationContext.
	 */
	public static ApplicationContext getApplicationContext() {
		assertContextInjected();
		return applicationContext;
	}
	
	/**
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getComponent(String name) {
		assertContextInjected();
		return (T) applicationContext.getComponent(StrKit.firstCharToLowerCase(name));
	}

	/**
	 * 清除SpringContextHolder中的ApplicationContext为Null.
	 */
	public static void clearHolder() {
		if (logger.isDebugEnabled()){
			logger.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
		}
		applicationContext = null;
	}

	/**
	 * 实现ApplicationContextAware接口, 注入Context到静态变量中.
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
//		logger.debug("注入ApplicationContext到SpringContextHolder:{}", applicationContext);

		if (IocContextHolder.applicationContext != null) {
			logger.info("SpringContextHolder中的ApplicationContext被覆盖, 原有ApplicationContext为:" + IocContextHolder.applicationContext);
		}

		IocContextHolder.applicationContext = applicationContext; // NOSONAR
	}

	/**
	 * 检查ApplicationContext不为空.
	 */
	private static void assertContextInjected() {
		Validate.validState(applicationContext != null, "applicaitonContext属性未注入, 请在applicationContext.xml中定义SpringContextHolder.");
	}
}