package com.jfinal.ext.plugin.sqlinxml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement
class SqlValueItem {

	@XmlAttribute
	String id;

	@XmlValue
	String value;
}

@XmlRootElement
class SqlClauseItem {

	@XmlAttribute
	String id;

	@XmlElement(name = "exceptSelect")
	SqlExceptSelect sqlExceptSelect;
	@XmlElement(name = "select")
	SqlSelect sqlSelect;

	public SqlSelect getSqlSelect() {
		return sqlSelect;
	}

	public static class SqlExceptSelect {
		@XmlValue
		String express;

		public String getExpress() {
			return express;
		}

		void setExpress(String express) {
			this.express = express;
		}
	}

	public static class SqlSelect {
		@XmlValue
		String express;

		public String getExpress() {
			return express;
		}

		void setExpress(String express) {
			this.express = express;
		}
	}

}
