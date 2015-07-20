package com.hwtxframework.ioc;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class DependencyInfo {

	ComponentBundle componentBundle;
	Property property;
	List<? extends DependPath> dependPaths;

	public interface DependPath {

	}

	public void refresh(ComponentBundle source) {
		property.refresh(source, componentBundle, property.getName(), dependPaths);
	}
}
