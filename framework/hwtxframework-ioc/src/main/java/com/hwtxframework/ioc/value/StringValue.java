package com.hwtxframework.ioc.value;

import java.util.Properties;

import com.hwtxframework.ioc.impl.BaseCache;

public class StringValue extends AbstractDefaultValue<String> {

	private String value;

	public StringValue(String value) {
		this.value = value;
	}

	@Override
	public String getValue(BaseCache readyServiceCache) {
		return getValue();
	}

	public String getValue() {
		return value;
	}

	public void merge(String content) {
		value += content;
	}

	@Override
	public void setVariable(Properties properties) {
		value = getVariable(properties, value);
	}
}