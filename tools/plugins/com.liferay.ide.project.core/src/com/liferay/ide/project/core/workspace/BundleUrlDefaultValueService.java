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

package com.liferay.ide.project.core.workspace;

import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.core.workspace.WorkspaceConstants;

import org.eclipse.sapphire.DefaultValueService;

/**
 * @author Seiphon Wang
 */
public class BundleUrlDefaultValueService extends DefaultValueService implements SapphireContentAccessor {

	@Override
	protected String compute() {
		NewLiferayWorkspaceOp op = _op();

		NewLiferayWorkspaceProjectProvider<NewLiferayWorkspaceOp> workspaceProjectProvider = get(
			op.getProjectProvider());

		String buildType = workspaceProjectProvider.getDisplayName();

		if (buildType.equals("Gradle")) {
			return null;
		}

		String liferayVersion = get(op.getLiferayVersion());

		switch (liferayVersion) {
			case "7.3":
				return WorkspaceConstants.BUNDLE_URL_CE_7_3;

			case "7.2":
				return WorkspaceConstants.BUNDLE_URL_CE_7_2;

			case "7.1":
				return WorkspaceConstants.BUNDLE_URL_CE_7_1;

			case "7.0":
				return WorkspaceConstants.BUNDLE_URL_CE_7_0;

			default:
				return WorkspaceConstants.BUNDLE_URL_CE_7_3;
		}
	}

	private NewLiferayWorkspaceOp _op() {
		return context(NewLiferayWorkspaceOp.class);
	}

}