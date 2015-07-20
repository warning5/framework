package com.hwtxframework.ioc.value;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.DependencyInfo.DependPath;
import com.hwtxframework.ioc.impl.BaseCache;

public interface Value<T> {

	public T getValue(BaseCache readyServiceCache);

	public boolean containRef();

	public String getUnResolved(BaseCache readyServiceCache);

	public Map<String, List<DependPath>> getDenpendRefs();

	public void refresh(ComponentBundle source, ComponentBundle target, String propertyName,
			List<? extends DependPath> dependPaths);

	public void setVariable(Properties properties);
}
