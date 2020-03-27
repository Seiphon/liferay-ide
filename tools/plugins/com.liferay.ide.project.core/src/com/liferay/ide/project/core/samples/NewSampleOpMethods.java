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

package com.liferay.ide.project.core.samples;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.project.core.NewLiferayProjectProvider;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.modules.BaseModuleOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.platform.ProgressMonitorBridge;
import org.eclipse.sapphire.platform.StatusBridge;

/**
 * @author Terry Jia
 * @author Seiphon Wang
 */
public class NewSampleOpMethods {

	public static final Status execute(NewSampleOp newSampleOp, ProgressMonitor pm) {
		IProgressMonitor progressMonitor = ProgressMonitorBridge.create(pm);

		progressMonitor.beginTask("Creating Liferay sample project template files", 100);

		Status retval = null;

		Throwable errorStack = null;

		try {
			NewLiferayProjectProvider<BaseModuleOp> newSampleProjectProvider = _getter.get(
				newSampleOp.getProjectProvider());

			IStatus status = newSampleProjectProvider.createNewProject(newSampleOp, progressMonitor);

			retval = StatusBridge.create(status);

			if (retval.ok()) {
				_updateBuildAndVersionPrefs(newSampleOp);
			}
			else if ((retval.severity() == Severity.ERROR) && (retval.exception() != null)) {
				errorStack = retval.exception();
			}
		}
		catch (Exception e) {
			errorStack = e;
		}

		if (errorStack != null) {
			String readableStack = CoreUtil.getStackTrace(errorStack);

			ProjectCore.logError(readableStack);

			return Status.createErrorStatus(readableStack + "\t Please see Eclipse error log for more details.");
		}

		return retval;
	}

	private static void _updateBuildAndVersionPrefs(NewSampleOp op) {
		try {
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(ProjectCore.PLUGIN_ID);

			prefs.put(ProjectCore.PREF_DEFAULT_LIFERAY_VERSION_OPTION, SapphireUtil.getText(op.getLiferayVersion()));
			prefs.put(
				ProjectCore.PREF_DEFAULT_SAMPLE_PORTLET_PROJECT_BUILD_TYPE_OPTION,
				SapphireUtil.getText(op.getProjectProvider()));

			prefs.flush();
		}
		catch (Exception e) {
			String msg = "Error updating default project build type or version.";

			ProjectCore.logError(msg, e);
		}
	}

	private static final SapphireContentAccessor _getter = new SapphireContentAccessor() {
	};

}