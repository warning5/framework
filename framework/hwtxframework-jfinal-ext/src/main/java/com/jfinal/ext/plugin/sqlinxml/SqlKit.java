/**
 * Copyright (c) 2011-2013, kidzhou 周磊 (zhouleib1412@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jfinal.ext.plugin.sqlinxml;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.jfinal.ext.kit.JaxbKit;
import com.jfinal.ext.plugin.sqlinxml.SqlGroup.ModelItem;
import com.jfinal.ext.plugin.sqlinxml.SqlGroup.ModelMapping;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Model;

public class SqlKit {

	protected static final Logger LOG = Logger.getLogger(SqlKit.class);

	private static Map<String, Object> sqlMap = new HashMap<String, Object>();
	private static Map<String, Pair<Class<?>, String>> modelMapping = new HashMap<String, Pair<Class<?>, String>>();

	public static String sql(String groupNameAndsqlId) {
		if (sqlMap == null) {
			throw new NullPointerException("SqlInXmlPlugin not start");
		}
		return ((SqlValueItem) sqlMap.get(groupNameAndsqlId)).value;
	}

	public static SqlClauseItem.SqlExceptSelect sqlExceptSelect(String groupNameAndsqlId) {
		if (sqlMap == null) {
			throw new NullPointerException("SqlInXmlPlugin not start");
		}
		return ((SqlClauseItem) sqlMap.get(groupNameAndsqlId)).sqlExceptSelect;
	}

	public static SqlClauseItem.SqlSelect sqlSelect(String groupNameAndsqlId) {
		if (sqlMap == null) {
			throw new NullPointerException("SqlInXmlPlugin not start");
		}
		return ((SqlClauseItem) sqlMap.get(groupNameAndsqlId)).sqlSelect;
	}

	public static Map<String, Pair<Class<?>, String>> getModelMapping() {
		return modelMapping;
	}

	public static void clearSqlMap() {
		sqlMap.clear();
	}

	public static void parse(File xmlFile) {
		parseSql(xmlFile, false, sqlMap, modelMapping);
	}

	public static void reload(File xmlFile, boolean newOne) {
		if (!newOne) {
			removeSqlWithPrefix(xmlFile);
		}
		parseSql(xmlFile, true, sqlMap, modelMapping);
	}

	public static void removeSqlWithPrefix(File xmlFile) {
		String name = xmlFile.getName();
		int start = name.indexOf("-");
		String prefix = null;
		if (start > -1) {
			prefix = name.substring(0, start);
		}

		if (prefix == null) {
			LOG.info("sql file " + xmlFile + " name is invalidate.");
			return;
		}

		Iterator<String> it = sqlMap.keySet().iterator();
		while (it.hasNext()) {
			if (it.next().startsWith(prefix)) {
				it.remove();
			}
		}
	}

	public static void init() {
		File file = new File(SqlKit.class.getClassLoader().getResource("").getFile());
		Collection<File> files = FileUtils.listFiles(file, new IOFileFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("sql.xml");
			}

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith("sql.xml");
			}
		}, TrueFileFilter.INSTANCE);

		for (File xmlfile : files) {
			parseSql(xmlfile, false, sqlMap, modelMapping);
		}
		LOG.debug("sqlMap" + sqlMap);
	}

	@SuppressWarnings("unchecked")
	private static void parseSql(File xmlfile, boolean override, Map<String, Object> sqlMap,
			Map<String, Pair<Class<?>, String>> modelMapping) {
		SqlGroup group = JaxbKit.unmarshal(xmlfile, SqlGroup.class);
		String name = group.name;
		if (name == null || name.trim().equals("")) {
			name = xmlfile.getName();
		}

		for (SqlValueItem sqlItem : group.sqlValues) {
			String sqlKey = name + "." + sqlItem.id;
			if (!override) {
				if (!sqlMap.containsKey(sqlKey)) {
					sqlMap.put(sqlKey, sqlItem);
					LOG.debug("load sql " + sqlKey);
				} else {
					LOG.error("already contain sql " + sqlKey);
				}
			} else {
				sqlMap.put(sqlKey, sqlItem);
				LOG.info("load sql " + sqlKey);
			}
		}

		for (SqlClauseItem sqlItem : group.sqlClauseItems) {
			String sqlKey = name + "." + sqlItem.id;
			if (!override) {
				if (!sqlMap.containsKey(sqlKey)) {
					sqlMap.put(sqlKey, sqlItem);
					LOG.debug("load sql " + sqlKey);
				} else {
					LOG.error("already contain sql " + sqlKey);
				}
			} else {
				sqlMap.put(sqlKey, sqlItem);
				LOG.info("load sql " + sqlKey);
			}
		}

		ModelMapping mMapping = group.modelMapping;
		if (mMapping != null) {
			for (ModelItem item : mMapping.modelItems) {
				Class<? extends Model<?>> modelClaz;
				try {
					modelClaz = (Class<Model<?>>) Class.forName(item.model, true, Thread.currentThread().getContextClassLoader());
					String alias = item.alias;
					if (alias != null) {
						for (String al : alias.split(",")) {
							modelMapping.put(al, new ImmutablePair<Class<?>, String>(modelClaz, item.name));
						}
					} else {
						LOG.error("model " + item.model + "'s alias can't be null");
					}
				} catch (ClassNotFoundException e) {
					LOG.error("{}", e);
				}
			}
		}
	}
}
