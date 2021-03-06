/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.upgrade.problems.core.internal.liferay72;

import com.liferay.ide.upgrade.problems.core.AutoFileMigrator;
import com.liferay.ide.upgrade.problems.core.FileMigrator;
import com.liferay.ide.upgrade.problems.core.internal.JavaImportsMigrator;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

/**
 * @author Terry Jia
 */
@Component(
	property = {
		"file.extensions=java,jsp,jspf", "problem.title=Classes in portal-service.jar moved 7.2",
		"problem.summary=Many classes from former portal-service.jar from Liferay Portal 6.x have been moved into application and framework API modules.",
		"problem.tickets=", "problem.section=#classes-in-portal-service-jar-moved", "auto.correct=import", "version=7.2"
	},
	service = {AutoFileMigrator.class, FileMigrator.class}
)
public class PortalServiceImports72 extends JavaImportsMigrator {

	public PortalServiceImports72() {
		Map<String, String> importFixes = new HashMap<>();

		for (String modularizationPropertiesFileName : _modularizationPropertiesFileNames) {
			Class<?> clazz = getClass();

			InputStream inputStream = clazz.getResourceAsStream(modularizationPropertiesFileName);

			Properties properties = new Properties();

			try {
				properties.load(inputStream);
			}
			catch (IOException ioe) {
			}

			String moduleLayout = properties.getProperty("module.layout");

			Set<Object> keys = properties.keySet();

			for (Object key : keys) {
				String value = (String)properties.get(key);

				if ((value != null) && !value.isEmpty()) {
					value = value + "," + moduleLayout;

					importFixes.put((String)key, value);
				}
			}
		}

		setImportFixes(importFixes);
	}

	private final String[] _modularizationPropertiesFileNames = {
		"modularization-comment-api.properties", "modularization-document-library-file-rank-service.properties",
		"modularization-petra-function.properties", "modularization-asset-list-service.properties",
		"modularization-exportimport-test-util.properties", "modularization-petra-process.properties",
		"modularization-portal-cache-multiple.properties", "modularization-portal-search.properties",
		"modularization-portal-vulcan-api.properties"
	};

}