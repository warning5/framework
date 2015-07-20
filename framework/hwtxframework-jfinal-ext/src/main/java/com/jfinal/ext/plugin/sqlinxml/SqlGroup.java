package com.jfinal.ext.plugin.sqlinxml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement
class SqlGroup {

	@XmlAttribute
	String name;
	@XmlElement(name = "sql")
	List<SqlValueItem> sqlValues = Lists.newArrayList();
	@XmlElement(name = "sql-clause")
	List<SqlClauseItem> sqlClauseItems = Lists.newArrayList();
	@XmlElement(name = "model-mapping")
	ModelMapping modelMapping;

	static class ModelMapping {
		@XmlElement(name = "mapping")
		List<ModelItem> modelItems;
	}

	static class ModelItem {
		@XmlAttribute
		String model;
		@XmlAttribute
		String alias;
		@XmlAttribute
		String name;
	}

}
