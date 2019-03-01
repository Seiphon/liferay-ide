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

package com.liferay.ide.upgrade.problems.core.internal.tasks;

import com.liferay.blade.api.MigrationConstants;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.upgrade.plan.core.BaseUpgradeTaskStepAction;
import com.liferay.ide.upgrade.plan.core.UpgradePlan;
import com.liferay.ide.upgrade.plan.core.UpgradePlanner;
import com.liferay.ide.upgrade.plan.core.UpgradeProblem;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepAction;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepActionDoneEvent;
import com.liferay.ide.upgrade.problems.core.FileMigration;
import com.liferay.ide.upgrade.tasks.core.ResourceSelection;
import com.liferay.ide.upgrade.tasks.core.SelectableJavaProjectFilter;

import java.io.File;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Terry Jia
 */
@Component(
	property = {"id=find_upgrade_problems", "order=1", "stepId=find_upgrade_problems", "title=Find Upgrade Problems"},
	scope = ServiceScope.PROTOTYPE, service = UpgradeTaskStepAction.class
)
public class FindUpgradeProblemsTaskStepAction extends BaseUpgradeTaskStepAction {

	@Override
	public IStatus perform(IProgressMonitor progressMonitor) {
		List<IProject> projects = _resourceSelection.selectProjects(
			"select projects", true, new SelectableJavaProjectFilter());

		if (projects.isEmpty()) {
			return Status.CANCEL_STATUS;
		}

		UpgradePlan upgradePlan = _upgradePlanner.getCurrentUpgradePlan();

		List<String> upgradeVersions = upgradePlan.getUpgradeVersions();

		Collection<UpgradeProblem> upgradeProblems = upgradePlan.getUpgradeProblems();

		upgradeProblems.clear();

		Stream<IProject> stream = projects.stream();

		stream.forEach(
			project -> {
				File searchFile = FileUtil.getFile(project);

				List<UpgradeProblem> foundUpgradeProblems = _fileMigration.findUpgradeProblems(
					searchFile, upgradeVersions, progressMonitor);

				upgradePlan.addUpgradeProblems(foundUpgradeProblems);

				_addMarkers(foundUpgradeProblems);
			});

		_upgradePlanner.dispatch(new UpgradeTaskStepActionDoneEvent(FindUpgradeProblemsTaskStepAction.this));

		return Status.OK_STATUS;
	}

	private static void _problemToMarker(UpgradeProblem problem, IMarker marker) throws CoreException {
		marker.setAttribute(IMarker.MESSAGE, problem.getTitle());
		marker.setAttribute("migrationProblem.summary", problem.getSummary());
		marker.setAttribute("migrationProblem.ticket", problem.getTicket());
		marker.setAttribute("migrationProblem.type", problem.getType());
		marker.setAttribute(IMarker.LINE_NUMBER, problem.getLineNumber());
		marker.setAttribute(IMarker.CHAR_START, problem.getStartOffset());
		marker.setAttribute(IMarker.CHAR_END, problem.getEndOffset());
		marker.setAttribute("migrationProblem.autoCorrectContext", problem.getAutoCorrectContext());
		marker.setAttribute("migrationProblem.html", problem.getHtml());
		marker.setAttribute("migrationProblem.status", problem.getStatus());

		IResource resource = problem.getResource();

		marker.setAttribute(IMarker.LOCATION, resource.getName());

		marker.setAttribute(IMarker.SEVERITY, problem.getMarkerType());
	}

	private void _addMarkers(List<UpgradeProblem> upgradeProblems) {
		Stream<UpgradeProblem> stream = upgradeProblems.stream();

		stream.forEach(
			upgradeProblem -> {
				IResource workspaceResource = upgradeProblem.getResource();

				if (FileUtil.exists(workspaceResource)) {
					try {
						IMarker marker = workspaceResource.createMarker(MigrationConstants.MARKER_TYPE);

						upgradeProblem.setMarkerId(marker.getId());

						_problemToMarker(upgradeProblem, marker);
					}
					catch (CoreException ce) {
					}
				}
			});
	}

	@Reference
	private FileMigration _fileMigration;

	@Reference
	private ResourceSelection _resourceSelection;

	@Reference
	private UpgradePlanner _upgradePlanner;

}