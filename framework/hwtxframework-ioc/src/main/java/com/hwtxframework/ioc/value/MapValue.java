package com.hwtxframework.ioc.value;

import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.Constants;
import com.hwtxframework.ioc.DependencyInfo.DependPath;
import com.hwtxframework.ioc.impl.BaseCache;
import com.hwtxframework.ioc.impl.BundleUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

public class MapValue extends AbstractValue<Map<Object, Object>> {

	List<MapEntry> entries = new ArrayList<MapEntry>();
	boolean isRef = false;

	public boolean containRef() {
		return isRef;
	}

	public void addEntry(MapEntry entry) {
		if (entry.getValue_ref() != null) {
			isRef = true;
		}
		entries.add(entry);
	}

	@Override
	public String getUnResolved(BaseCache readyServiceCache) {
		for (MapEntry entry : entries) {
			if (entry.getValue_ref() != null) {
				String resolve = entry.getValue_ref().getUnResolved(readyServiceCache);
				if (!resolve.equals(Constants.RESOLVED)) {
					return resolve;
				}
			}
		}
		return Constants.RESOLVED;
	}

	@Override
	public Map<Object, Object> getValue(BaseCache readyServiceCache) {
		Map<Object, Object> result = new HashMap<Object, Object>();
		for (MapEntry entry : entries) {
			if (entry.getValue_ref() != null) {
				result.put(entry.key, readyServiceCache.findBundleByReference(entry.getValue_ref().ref).getInstance());
			} else if (entry.getValue() != null) {
				result.put(entry.key, entry.getValue().getValue());
			}
		}
		return result;
	}

	@Getter
	@AllArgsConstructor
	public class MapEntry {
		String key;
		StringValue value;
		ReferenceValue value_ref;
	}

	class MapDependPath implements DependPath {
		String key;
		public MapDependPath(String key) {
			this.key = key;
		}
		public String getKey() {
			return key;
		}
	}

	@Override
	public Map<String, List<DependPath>> getDenpendRefs() {
		Map<String, List<DependPath>> result = new HashMap<String, List<DependPath>>();
		for (MapEntry entry : entries) {
			if (entry.getValue_ref() != null) {
				List<DependPath> paths = result.get(entry.getValue_ref().ref);
				if (paths == null) {
					paths = new ArrayList<DependPath>();
				}
				paths.add(new MapDependPath(entry.getKey()));
				result.put(entry.getValue_ref().ref, paths);
			}
		}
		return result;
	}

	@Override
	public void refresh(ComponentBundle source, ComponentBundle target, String propertyName,
			List<? extends DependPath> dependPaths) {
		Object tObject = target.getInstance();
		@SuppressWarnings("unchecked")
		Map<Object, Object> mapObject = (Map<Object, Object>) BundleUtil.getProperty(tObject, propertyName);
		for (DependPath dependPath : dependPaths) {
			MapDependPath mapDependPath = (MapDependPath) dependPath;
			mapObject.put(mapDependPath.getKey(), source);
		}
	}

	@Override
	public void setVariable(Properties properties) {
		for (MapEntry entry : entries) {
			if (entry.getValue() != null) {
				entry.getValue().setVariable(properties);
			} else {
				entry.getValue_ref().setVariable(properties);
			}
		}
	}
}
