package com.hwtxframework.ioc.value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.Constants;
import com.hwtxframework.ioc.DependencyInfo.DependPath;
import com.hwtxframework.ioc.impl.BaseCache;

public abstract class AbstractDefaultValue<T> extends AbstractValue<T> {

	protected Logger Logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean containRef() {
		return false;
	}

	@Override
	public String getUnResolved(BaseCache readyServiceCache) {
		return Constants.RESOLVED;
	}

	@Override
	public Map<String, List<DependPath>> getDenpendRefs() {
		return Collections.emptyMap();
	}

	@Override
	public void refresh(ComponentBundle source, ComponentBundle target, String propertyName,
			List<? extends DependPath> dependPaths) {
	}
}