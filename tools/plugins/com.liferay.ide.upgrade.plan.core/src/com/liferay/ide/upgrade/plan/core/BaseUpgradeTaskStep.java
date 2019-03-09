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

package com.liferay.ide.upgrade.plan.core;

import com.liferay.ide.upgrade.plan.core.util.ServicesLookup;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 */
public abstract class BaseUpgradeTaskStep extends BaseUpgradePlanElement implements UpgradeTaskStep {

	@Activate
	public void activate(ComponentContext componentContext) {
		super.activate(componentContext);

		Dictionary<String, Object> properties = componentContext.getProperties();

		_requirement = getStringProperty(properties, "requirement");
		_taskId = getStringProperty(properties, "taskId");
		_order = getDoubleProperty(properties, "order");
		_url = getStringProperty(properties, "url");

		_lookupActions(componentContext);
	}

	@Override
	public boolean completed() {
		List<UpgradeTaskStepAction> actions = getActions();

		if (actions.isEmpty()) {
			return getStatus().equals(UpgradePlanElementStatus.COMPLETED);
		}
		else {
			Stream<UpgradeTaskStepAction> stream = actions.stream();

			long count = stream.filter(
				action -> UpgradePlanElementStatus.INCOMPLETE.equals(action.getStatus())
			).count();

			if (count == 0) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean enabled() {
		UpgradePlanner upgradePlanner = ServicesLookup.getSingleService(UpgradePlanner.class, null);

		UpgradePlan upgradePlan = upgradePlanner.getCurrentUpgradePlan();

		List<UpgradeTask> upgradeTasks = upgradePlan.getTasks();

		Stream<UpgradeTask> upgradeTasksStream = upgradeTasks.stream();

		long count = upgradeTasksStream.filter(
			upgradeTask -> getTaskId().equals(upgradeTask.getId())
		).map(
			upgradeTask -> upgradeTask.getSteps()
		).flatMap(
			upgradeTaskStep -> upgradeTaskStep.stream()
		).filter(
			upgradeTaskStep -> (upgradeTaskStep.getOrder() < _order) &&
			UpgradePlanElementRequirement.REQUIRED.equals(upgradeTaskStep.getRequirement())
		).map(
			upgradeTaskStep -> upgradeTaskStep.getActions()
		).flatMap(
			actions -> actions.stream()
		).filter(
			action -> UpgradePlanElementRequirement.REQUIRED.equals(action.getRequirement())
		).filter(
			action -> UpgradePlanElementStatus.INCOMPLETE.equals(action.getStatus())
		).count();

		if (count > 0) {
			return false;
		}
		else {
			upgradeTasksStream = upgradeTasks.stream();

			count = upgradeTasksStream.filter(
				upgradeTask -> getTaskId().equals(upgradeTask.getId())
			).map(
				upgradeTask -> upgradeTask.getSteps()
			).flatMap(
				upgradeTaskStep -> upgradeTaskStep.stream()
			).filter(
				upgradeTaskStep -> (upgradeTaskStep.getOrder() < _order) &&
				UpgradePlanElementRequirement.REQUIRED.equals(upgradeTaskStep.getRequirement())
			).filter(
				upgradeTaskStep -> UpgradePlanElementStatus.INCOMPLETE.equals(upgradeTaskStep.getStatus())
			).map(
				upgradeTaskStep -> upgradeTaskStep.getActions()
			).filter(
				actions -> actions.isEmpty()
			).count();

			if (count > 0) {
				return false;
			}
		}

		return true;
	}

	@Override
	public List<UpgradeTaskStepAction> getActions() {
		return Collections.unmodifiableList(_upgradeTaskStepActions);
	}

	@Override
	public double getOrder() {
		return _order;
	}

	@Override
	public UpgradePlanElementRequirement getRequirement() {
		return UpgradePlanElementRequirement.valueOf(UpgradePlanElementRequirement.class, _requirement.toUpperCase());
	}

	@Override
	public String getTaskId() {
		return _taskId;
	}

	@Override
	public String getUrl() {
		return _url;
	}

	@Override
	public IStatus perform(IProgressMonitor progressMonitor) {
		return Status.CANCEL_STATUS;
	}

	private void _lookupActions(ComponentContext componentContext) {
		BundleContext bundleContext = componentContext.getBundleContext();

		List<UpgradeTaskStepAction> upgradeTaskStepActions = ServicesLookup.getOrderedServices(
			bundleContext, UpgradeTaskStepAction.class, "(stepId=" + getId() + ")");

		Stream<UpgradeTaskStepAction> stream = upgradeTaskStepActions.stream();

		UpgradePlanner upgradePlanner = ServicesLookup.getSingleService(UpgradePlanner.class, null);

		_upgradeTaskStepActions = stream.filter(
			upgradeTaskStepAction -> upgradeTaskStepAction.appliesTo(upgradePlanner.getCurrentUpgradePlan())
		).collect(
			Collectors.toList()
		);
	}

	private double _order;
	private String _requirement;
	private String _taskId;
	private List<UpgradeTaskStepAction> _upgradeTaskStepActions;
	private String _url;

}