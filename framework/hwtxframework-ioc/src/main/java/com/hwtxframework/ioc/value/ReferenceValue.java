package com.hwtxframework.ioc.value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.DependencyInfo.DependPath;
import com.hwtxframework.ioc.impl.BaseCache;
import com.hwtxframework.ioc.impl.BundleUtil;

public class ReferenceValue extends AbstractDefaultValue<Object> {

	String ref;

	public ReferenceValue(String ref) {
		this.ref = ref;
	}

	@Override
	public boolean containRef() {
		return true;
	}

	@Override
	public String getUnResolved(BaseCache readyServiceCache) {
		if (readyServiceCache.getComponent(ref) == null) {
			return ref;
		}
		return super.getUnResolved(readyServiceCache);
	}

	@Override
	public Object getValue(BaseCache readyServiceCache) {
		ComponentBundle bundle = readyServiceCache.findBundleByReference(ref);
		if (bundle != null) {
			return bundle.getInstance();
		}
		return null;
	}

	@Override
	public Map<String, List<DependPath>> getDenpendRefs() {
		Map<String, List<DependPath>> result = new HashMap<String, List<DependPath>>();
		List<DependPath> paths = new ArrayList<DependPath>();
		paths.add(new EmptyDependPath());
		result.put(ref, paths);
		return result;
	}

	@Override
	public void refresh(ComponentBundle source, ComponentBundle target, String propertyName,
			List<? extends DependPath> dependPaths) {
		Object tObject = target.getInstance();
		BundleUtil.setProperty(tObject, propertyName, source.getInstance());
	}

	@Override
	public void setVariable(Properties properties) {
		ref = getVariable(properties, ref);
	}
}
