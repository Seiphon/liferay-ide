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

package com.liferay.ide.upgrade.tasks.core.internal;

import com.liferay.ide.upgrade.tasks.core.ImportSDKProjectsOp;

import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;

/**
 * @author Terry Jia
 */
public class ImportSDKProjectsOpMethods {

	public static final Status execute(ImportSDKProjectsOp sdkProjectsImportOp, ProgressMonitor progressMonitor) {
		return Status.createOkStatus();
	}

}