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

package com.liferay.ide.project.core.samples.internal;

import com.liferay.ide.core.IWorkspaceProject;
import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.core.workspace.LiferayWorkspaceUtil;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.modules.BladeCLI;
import com.liferay.ide.project.core.samples.NewSampleOp;
import com.liferay.ide.project.core.util.SampleProjectUtil;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.PropertyContentEvent;

/**
 * @author Terry Jia
 * @author Seiphon Wang
 */
public class CategoryPossibleValuesService extends PossibleValuesService implements SapphireContentAccessor {

	@Override
	public void dispose() {
		if (_op() != null) {
			SapphireUtil.detachListener(_op().property(NewSampleOp.PROP_PROJECT_PROVIDER), _listener);
		}

		super.dispose();
	}

	@Override
	public boolean ordered() {
		return true;
	}

	@Override
	protected void compute(Set<String> values) {
		values.addAll(_categoryList);
	}

	@Override
	protected void initPossibleValuesService() {
		super.initPossibleValuesService();

		IWorkspaceProject workspace = LiferayWorkspaceUtil.getLiferayWorkspaceProject();

		IProject project = workspace.getProject();

		IPath location = project.getLocation();

		String liferayVersion = get(_op().getLiferayVersion());

		try {
			if (SampleProjectUtil.isBladeRepoArchiveExist(liferayVersion)) {
				String[] lines = SampleProjectUtil.executeSampleCommand(
					"samples", get(_op().getLiferayVersion()), location.toString(), true);

				_categoryList.clear();

				_categoryList.addAll(_getCategoryFromLines(lines));
			}
			else {
				if (!SampleProjectUtil.isBladeRepoArchiveDownloading(liferayVersion)) {
					Job job = new Job("Downloading liferay blade samples archive for " + liferayVersion) {

						@Override
						protected IStatus run(IProgressMonitor progressMonitor) {
							try {
								String[] lines = BladeCLI.execute("samples -v " + liferayVersion);

								_categoryList.clear();

								_categoryList.addAll(_getCategoryFromLines(lines));

								refresh();
							}
							catch (Exception e) {
								return ProjectCore.createErrorStatus(e);
							}

							return Status.OK_STATUS;
						}

					};

					job.setProperty(SampleProjectUtil.LIFERAY_PROJECT_DOWNLOAD_JOB, new Object());

					job.schedule();
				}
			}
		}
		catch (IOException ioe) {
		}

		_listener = new FilteredListener<PropertyContentEvent>() {

			@Override
			protected void handleTypedEvent(PropertyContentEvent event) {
				refresh();
			}

		};

		SapphireUtil.attachListener(_op().property(NewSampleOp.PROP_LIFERAY_VERSION), _listener);
	}

	private List<String> _getCategoryFromLines(String[] lines) {
		List<String> categoryList = new ArrayList<>();

		if (Objects.nonNull(lines)) {
			for (int i = 2; i < lines.length; i++) {
				if (lines[i].contains(":")) {
					lines[i] = lines[i].trim();

					lines[i] = lines[i].replace(":", "");

					categoryList.add(lines[i]);
				}
			}

			return categoryList.subList(1, categoryList.size());
		}

		return categoryList;
	}

	private NewSampleOp _op() {
		return context(NewSampleOp.class);
	}

	private List<String> _categoryList = new ArrayList<>();
	private FilteredListener<PropertyContentEvent> _listener;

}