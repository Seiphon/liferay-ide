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

package com.liferay.ide.project.core.util;

import com.liferay.ide.core.workspace.WorkspaceConstants;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.modules.BladeCLI;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Seiphon Wang
 */
public class SampleProjectUtil {

	public static final QualifiedName LIFERAY_PROJECT_DOWNLOAD_JOB = new QualifiedName(
		ProjectCore.PLUGIN_ID, "LIFERAY_PROJECT_DOWNLOAD_JOB");

	public static void downloadAllSampleArchive() {
		for (String liferayVersion : WorkspaceConstants.liferayTargetPlatformVersions.keySet()) {
			executeSampleCommand("samples", liferayVersion, null, false);
		}
	}

	public static String[] executeSampleCommand(String command, String liferayVersion, String dir, boolean ignore) {
		String[] lines = null;

		try {
			if (isBladeRepoArchiveExist(liferayVersion)) {
				if (dir != null) {
					if (ignore) {
						lines = BladeCLI.execute(command + " -v " + liferayVersion + " --base " + dir + " --ignore");
					}
					else {
						lines = BladeCLI.execute(command + " -v " + liferayVersion + " --base " + dir);
					}
				}
				else {
					if (ignore) {
						lines = BladeCLI.execute(command + " -v " + liferayVersion + " --ignore");
					}
					else {
						lines = BladeCLI.execute(command + " -v " + liferayVersion);
					}
				}
			}
			else {
				Job job = new Job("Downloading liferay blade samples archive for " + liferayVersion) {

					@Override
					protected IStatus run(IProgressMonitor progressMonitor) {
						try {
							BladeCLI.execute(command + " -v " + liferayVersion);
						}
						catch (Exception e) {
							return ProjectCore.createErrorStatus(e);
						}

						return Status.OK_STATUS;
					}

				};

				job.setProperty(LIFERAY_PROJECT_DOWNLOAD_JOB, new Object());

				job.schedule();
			}
		}
		catch (Exception e) {
			return null;
		}

		return lines;
	}

	public static Path getSamplesCachePath() throws IOException {
		Path userHomePath = _USER_HOME_DIR.toPath();

		Path samplesCachePath = userHomePath.resolve(".blade/cache/samples");

		if (!Files.exists(samplesCachePath)) {
			Files.createDirectories(samplesCachePath);
		}

		return samplesCachePath;
	}

	public static boolean isBladeRepoArchiveDownloading(String liferayVersion) {
		IJobManager jobManager = Job.getJobManager();

		Job[] jobs = jobManager.find(null);

		for (Job job : jobs) {
			if (job.getProperty(LIFERAY_PROJECT_DOWNLOAD_JOB) != null) {
				String jobName = job.getName();

				if (jobName.contains(liferayVersion)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isBladeRepoArchiveExist(String liferayVersion) throws IOException {
		Path cachePath = getSamplesCachePath();

		final String bladeRepoName = "liferay-blade-samples-" + liferayVersion;

		final String bladeRepoArchiveName = bladeRepoName + ".zip";

		File bladeRepoArchive = new File(cachePath.toFile(), bladeRepoArchiveName);

		if (bladeRepoArchive.exists() && isZipValid(bladeRepoArchive)) {
			return true;
		}

		return false;
	}

	public static boolean isZipValid(File file) {
		try (ZipFile zipFile = new ZipFile(file)) {
			return true;
		}
		catch (IOException ioe) {
			return false;
		}
	}

	private static final File _USER_HOME_DIR = new File(System.getProperty("user.home"));

}