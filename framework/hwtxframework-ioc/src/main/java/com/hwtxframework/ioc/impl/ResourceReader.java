package com.hwtxframework.ioc.impl;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hwtxframework.io.DefaultResourceLoader;
import com.hwtxframework.io.Resource;
import com.hwtxframework.io.ResourceLoader;
import com.hwtxframework.io.support.PathMatchingResourcePatternResolver;
import com.hwtxframework.io.support.ResourcePatternResolver;

public class ResourceReader {

	protected static Logger logger = LoggerFactory.getLogger(ResourceReader.class);

	private static ResourceLoader resourcePatternResolver = new PathMatchingResourcePatternResolver(
			new DefaultResourceLoader());

	public static Resource[] getResource(String location) throws IOException {
		ResourceLoader resourceLoader = getResourceLoader();
		if (resourceLoader == null) {
			throw new RuntimeException("no ResourceLoader available");
		}

		if (resourceLoader instanceof ResourcePatternResolver) {
			Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
			return resources;
		} else {
			Resource resource = resourceLoader.getResource(location);
			return Arrays.asList(resource).toArray(new Resource[0]);
		}
	}

	public static ResourceLoader getResourceLoader() {
		return resourcePatternResolver;
	}
}
