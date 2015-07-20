package com.hwtxframework.ioc.value;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hwtxframework.ioc.DependencyInfo.DependPath;
import com.hwtxframework.util.StringUtils;

public abstract class AbstractValue<T> implements Value<T> {

	protected Logger Logger = LoggerFactory.getLogger(getClass());

	protected String getVariable(Properties properties, String value) {
		if (StringUtils.isVariable(value)) {
			String vv = StringUtils.getVariableValue(properties, value);
			if (StringUtils.isEmpty(vv)) {
				Logger.warn("can not find variable " + value);
			} else {
				value = vv;
			}
		}
		return value;
	}

	class EmptyDependPath implements DependPath {

	}
}