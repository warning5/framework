package com.hwtxframework.io.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hwtxframework.io.Resource;
import com.hwtxframework.util.CollectionUtils;

public class PropertiesLoaderSupport {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private boolean ignoreResourceNotFound = false;

	private String fileEncoding;

	@Setter
	@Getter
	private Properties props = new Properties();

	/**
	 * Set if failure to find the property resource should be ignored.
	 * <p>
	 * "true" is appropriate if the properties file is completely optional. Default is "false".
	 */
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		this.ignoreResourceNotFound = ignoreResourceNotFound;
	}

	/**
	 * Set the encoding to use for parsing properties files.
	 * <p>
	 * Default is none, using the <code>java.util.Properties</code> default encoding.
	 * <p>
	 * Only applies to classic properties files, not to XML files.
	 * 
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setFileEncoding(String encoding) {
		this.fileEncoding = encoding;
	}

	public void mergeProperties(Properties properties) {
		CollectionUtils.mergePropertiesIntoMap(properties, props);
	}

	/**
	 * Load properties into the given instance.
	 * 
	 * @param props
	 *            the Properties instance to load into
	 * @throws java.io.IOException
	 *             in case of I/O errors
	 * @see #setLocations
	 */
	public Properties loadProperties(Resource[] locations) throws IOException {
		Properties result = new Properties();
		if (locations != null) {
			for (Resource location : locations) {
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties file from " + location);
				}
				InputStream is = null;
				try {
					is = location.getInputStream();
					if (this.fileEncoding != null) {
						result.load(new InputStreamReader(is, this.fileEncoding));
					} else {
						result.load(is);
					}
				} catch (IOException ex) {
					if (this.ignoreResourceNotFound) {
						if (logger.isWarnEnabled()) {
							logger.warn("Could not load properties from " + location + ": " + ex.getMessage());
						}
					} else {
						throw ex;
					}
				} finally {
					if (is != null) {
						is.close();
					}
				}
			}
		}
		return result;
	}
}