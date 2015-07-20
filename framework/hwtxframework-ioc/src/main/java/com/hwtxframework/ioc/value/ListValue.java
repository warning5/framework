package com.hwtxframework.ioc.value;

import com.google.common.collect.Lists;
import com.hwtxframework.ioc.ComponentBundle;
import com.hwtxframework.ioc.Constants;
import com.hwtxframework.ioc.DependencyInfo;
import com.hwtxframework.ioc.impl.BaseCache;
import com.hwtxframework.ioc.impl.BundleUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * Created by panye on 2015/1/22.
 */
public class ListValue extends AbstractValue<List<Object>> {

    List<ListEntry> entries = new ArrayList<>();
    boolean isRef = false;

    public void addEntry(ListEntry entry) {
        if (entry.getRef() != null) {
            isRef = true;
        }
        entries.add(entry);
    }

    @Override
    public List<Object> getValue(BaseCache readyServiceCache) {
        List<Object> result = Lists.newArrayList();
        for (ListEntry entry : entries) {
            if (entry.getRef() != null) {
                result.add(readyServiceCache.findBundleByReference(entry.getRef().ref).getInstance());
            } else if (entry.getValue() != null) {
                result.add(entry.getValue().getValue());
            }
        }
        return result;
    }

    @Getter
    @AllArgsConstructor
    public class ListEntry {
        StringValue value;
        ReferenceValue ref;
    }

    @Override
    public boolean containRef() {
        return isRef;
    }

    @Override
    public String getUnResolved(BaseCache readyServiceCache) {
        for (ListEntry entry : entries) {
            if (entry.getRef() != null) {
                String resolve = entry.getRef().getUnResolved(readyServiceCache);
                if (!resolve.equals(Constants.RESOLVED)) {
                    return resolve;
                }
            }
        }
        return Constants.RESOLVED;
    }

    class ListDependPath implements DependencyInfo.DependPath {
        @Getter
        int index;

        public ListDependPath(int index) {
            this.index = index;
        }
    }

    @Override
    public Map<String, List<DependencyInfo.DependPath>> getDenpendRefs() {
        Map<String, List<DependencyInfo.DependPath>> result = new HashMap<>();
        int i = 0;
        for (ListEntry entry : entries) {
            if (entry.getRef() != null) {
                List<DependencyInfo.DependPath> paths = result.get(entry.getRef().ref);
                if (paths == null) {
                    paths = new ArrayList<>();
                }
                paths.add(new ListDependPath(i));
                result.put(entry.getRef().ref, paths);
            }
        }
        return result;
    }

    @Override
    public void refresh(ComponentBundle source, ComponentBundle target, String propertyName, List<? extends DependencyInfo.DependPath> dependPaths) {
        Object tObject = target.getInstance();
        @SuppressWarnings("unchecked")
        List<Object> listObject = (List<Object>) BundleUtil.getProperty(tObject, propertyName);
        for (DependencyInfo.DependPath dependPath : dependPaths) {
            ListDependPath mapDependPath = (ListDependPath) dependPath;
            listObject.set(mapDependPath.getIndex(), source);
        }
    }

    @Override
    public void setVariable(Properties properties) {
        for (ListEntry entry : entries) {
            if (entry.getValue() != null) {
                entry.getValue().setVariable(properties);
            } else {
                entry.getRef().setVariable(properties);
            }
        }
    }
}
