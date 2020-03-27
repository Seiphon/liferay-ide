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

package com.liferay.ide.gradle.core;

import com.liferay.ide.core.AbstractLiferayProjectProvider;
import com.liferay.ide.core.ILiferayProject;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.core.workspace.LiferayWorkspaceUtil;
import com.liferay.ide.project.core.NewLiferayProjectProvider;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.modules.BladeCLI;
import com.liferay.ide.project.core.samples.NewSampleOp;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sapphire.modeling.Path;

/**
 * @author Seiphon Wang
 */
public class GradleSampleProjectProvider
	extends AbstractLiferayProjectProvider implements NewLiferayProjectProvider<NewSampleOp>, SapphireContentAccessor {

	public GradleSampleProjectProvider() {
		super(null);
	}

	@Override
	public IStatus createNewProject(NewSampleOp newSampleOp, IProgressMonitor monitor)
		throws CoreException, InterruptedException {

		IStatus status = Status.OK_STATUS;

		String projectName = get(newSampleOp.getProjectName());

		Path location = get(newSampleOp.getLocation());

		String liferayVersion = get(newSampleOp.getLiferayVersion());

		String sampleName = get(newSampleOp.getSampleName());

		Path targetDirPath = location.removeLastSegments(1);

		File targetDir = targetDirPath.toFile();

		StringBuffer sb = new StringBuffer();

		sb.append("samples");
		sb.append(" ");
		sb.append("-b ");
		sb.append("gradle ");
		sb.append(" ");
		sb.append("-v ");
		sb.append(liferayVersion);
		sb.append(" ");
		sb.append("-d ");
		sb.append(targetDir.getAbsolutePath());
		sb.append(" ");
		sb.append(sampleName);

		try {
			BladeCLI.execute(sb.toString());

			Path oldPath = targetDirPath.append(sampleName);

			File oldFile = oldPath.toFile();

			File newFile = location.toFile();

			oldFile.renameTo(newFile);

			Job job = new Job("Openning project " + projectName) {

				@Override
				protected IStatus run(IProgressMonitor progressMonitor) {
					org.eclipse.core.runtime.Path projectLocation = new org.eclipse.core.runtime.Path(
						newFile.getPath());

					try {
						CoreUtil.openProject(projectName, projectLocation, progressMonitor);

						GradleUtil.synchronizeProject(projectLocation, monitor);
					}
					catch (CoreException ce) {
						return ProjectCore.createErrorStatus(ce);
					}

					return Status.OK_STATUS;
				}

			};

			job.schedule();
		}
		catch (Exception e) {
		}

		return status;
	}

	@Override
	public ILiferayProject provide(Class<?> type, Object adaptable) {
		return null;
	}

	@Override
	public IStatus validateProjectLocation(String projectName, IPath path) {
		IStatus retval = Status.OK_STATUS;

		if (LiferayWorkspaceUtil.isValidGradleWorkspaceLocation(path)) {
			retval = LiferayGradleCore.createErrorStatus("Invalid location for workspace project.");
		}

		return retval;
	}

}