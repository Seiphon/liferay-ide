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

package com.liferay.ide.upgrade.problems.core.internal.liferay73;

import com.liferay.ide.upgrade.problems.core.FileMigrator;
import com.liferay.ide.upgrade.problems.core.internal.DeprecatedMethodsMigrator;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import org.osgi.service.component.annotations.Component;

/**
 * @author Simon Jiang
 */
@Component(property = {"file.extensions=java,jsp,jspf", "version=7.3"}, service = FileMigrator.class)
public class Liferay73DeprecatedMethodsMigrator extends DeprecatedMethodsMigrator {

	public Liferay73DeprecatedMethodsMigrator() {
		super(_getDeprecatedMethods(), "7.3");
	}

	private static JSONArray[] _getDeprecatedMethods() {
		List<JSONArray> deprecatedMethodsList = new ArrayList<>();

		String fqn = "/com/liferay/ide/upgrade/problems/core/internal/liferay73/";

		String[] jsonFilePaths = {fqn + "deprecatedMethods72.json", fqn + "deprecatedMethodsNoVersion.json"};

		Class<? extends DeprecatedMethodsMigrator> clazz = Liferay73DeprecatedMethodsMigrator.class;

		for (String path : jsonFilePaths) {
			try {
				String jsonContent = read(clazz.getResourceAsStream(path));

				deprecatedMethodsList.add(new JSONArray(jsonContent));
			}
			catch (IOException ioe) {
			}
			catch (JSONException jsone) {
			}
		}

		return deprecatedMethodsList.toArray(new JSONArray[0]);
	}

}